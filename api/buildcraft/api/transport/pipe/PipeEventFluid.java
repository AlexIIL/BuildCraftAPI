package buildcraft.api.transport.pipe;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.Nonnull;

import net.minecraft.util.EnumFacing;

import net.minecraftforge.fluids.FluidStack;

public abstract class PipeEventFluid extends PipeEvent {

    public final IFlowFluid flow;

    protected PipeEventFluid(IPipeHolder holder, IFlowFluid flow) {
        super(holder);
        this.flow = flow;
    }

    /** @deprecated Because cancellation is going to be removed (at some point in the future) */
    @Deprecated
    protected PipeEventFluid(boolean canBeCancelled, IPipeHolder holder, IFlowFluid flow) {
        super(canBeCancelled, holder);
        this.flow = flow;
    }

    public static class TryInsert extends PipeEventFluid {
        public final EnumFacing from;
        /** The incoming fluidstack. Currently changing this does nothing. */
        @Nonnull
        public final FluidStack fluid;

        public TryInsert(IPipeHolder holder, IFlowFluid flow, EnumFacing from, @Nonnull FluidStack fluid) {
            super(true, holder, flow);
            this.from = from;
            this.fluid = fluid;
        }
    }

    /** Fired after collecting the amounts of fluid that can be moved from each pipe part into the centre. */
    public static class PreMoveToCentre extends PipeEventFluid {
        /** The fluid that is being moved. Future versions of BC *might* allow more than one fluid type per pipe, but
         * for the moment the API doesn't allow pipes to do this. */
        public final FluidStack fluid;

        /** The maximum amount of fluid that the centre pipe could accept. */
        public final int totalAcceptable;

        /** Array of {@link EnumFacing#getIndex()} to the maximum amount of fluid that a given side can offer. DO NOT
         * CHANGE THIS! */
        public final int[] totalOffered;

        // Used for checking the state
        private final int[] totalOfferedCheck;

        /** Array of {@link EnumFacing#getIndex()} to the amount of fluid that the given side will actually offer to the
         * centre. This should *never* be larger than */
        public final int[] actuallyOffered;

        public PreMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int totalAcceptable,
            int[] totalOffered, int[] actuallyOffered) {
            super(holder, flow);
            this.fluid = fluid;
            this.totalAcceptable = totalAcceptable;
            this.totalOffered = totalOffered;
            totalOfferedCheck = Arrays.copyOf(totalOffered, totalOffered.length);
            this.actuallyOffered = actuallyOffered;
        }

        @Override
        public String checkStateForErrors() {
            for (int i = 0; i < totalOffered.length; i++) {
                if (totalOffered[i] != totalOfferedCheck[i]) {
                    return "Changed totalOffered";
                }
                if (actuallyOffered[i] > totalOffered[i]) {
                    return "actuallyOffered[" + i + "](=" + actuallyOffered[i]
                        + ") shouldn't be greater than totalOffered[" + i + "](=" + totalOffered[i] + ")";
                }
            }
            return super.checkStateForErrors();
        }
    }

    /** Fired after {@link PreMoveToCentre} when all of the amounts have been totalled up. */
    public static class OnMoveToCentre extends PipeEventFluid {
        /** The fluid that is being moved. Future versions of BC *might* allow more than one fluid type per pipe, but
         * for the moment the API doesn't allow pipes to do this. */
        public final FluidStack fluid;

        public final int[] fluidLeavingSide;
        public final int[] fluidEnteringCentre;

        // Used for checking the state maximums
        private final int[] fluidLeaveCheck, fluidEnterCheck;

        public OnMoveToCentre(IPipeHolder holder, IFlowFluid flow, FluidStack fluid, int[] fluidLeavingSide,
            int[] fluidEnteringCentre) {
            super(holder, flow);
            this.fluid = fluid;
            this.fluidLeavingSide = fluidLeavingSide;
            this.fluidEnteringCentre = fluidEnteringCentre;
            fluidLeaveCheck = Arrays.copyOf(fluidLeavingSide, fluidLeavingSide.length);
            fluidEnterCheck = Arrays.copyOf(fluidEnteringCentre, fluidEnteringCentre.length);
        }

        @Override
        public String checkStateForErrors() {
            for (int i = 0; i < fluidLeavingSide.length; i++) {
                if (fluidLeavingSide[i] > fluidLeaveCheck[i]) {
                    return "fluidLeavingSide[" + i + "](=" + fluidLeavingSide[i]
                        + ") shouldn't be bigger than its original value!(=" + fluidLeaveCheck[i] + ")";
                }
                if (fluidEnteringCentre[i] > fluidEnterCheck[i]) {
                    return "fluidEnteringCentre[" + i + "](=" + fluidEnteringCentre[i]
                        + ") shouldn't be bigger than its original value!(=" + fluidEnterCheck[i] + ")";
                }
                if (fluidEnteringCentre[i] > fluidLeavingSide[i]) {
                    return "fluidEnteringCentre[" + i + "](=" + fluidEnteringCentre[i]
                        + ") shouldn't be bigger than fluidLeavingSide[" + i + "](=" + fluidLeavingSide[i] + ")";
                }
            }
            return super.checkStateForErrors();
        }
    }

    public static class SideCheck extends PipeEventFluid {
        public final FluidStack fluid;

        /** The priorities of each side. Stored inversely to the values given, so a higher priority will have a lower
         * value than a lower priority. */
        private final int[] priority = new int[6];
        private int allowed = 0b111_111;

        public SideCheck(IPipeHolder holder, IFlowFluid flow, FluidStack fluid) {
            super(holder, flow);
            this.fluid = fluid;
        }

        public void reset() {
            allowed = 0b111_111;
        }

        /** Checks to see if a side if allowed. Note that this may return true even though a later handler might
         * disallow a side, so you should only use this to skip checking a side (for example a diamond pipe might not
         * check the filters for a specific side if its already been disallowed) */
        public boolean isAllowed(EnumFacing side) {
            return isAllowed(side.ordinal());
        }

        private boolean isAllowed(int index) {
            return ((allowed >> index) & 1) == 1;
        }

        /** Disallows the specific side(s) from being a destination for the item. If no sides are allowed, then the
         * fluid will stay in the current pipe section. */
        public void disallow(EnumFacing... sides) {
            for (EnumFacing side : sides) {
                allowed &= ~(1 << side.ordinal());
            }
        }

        public void disallowAll(Collection<EnumFacing> sides) {
            for (EnumFacing side : sides) {
                allowed &= ~(1 << side.ordinal());
            }
        }

        public void disallowAllExcept(EnumFacing side) {
            if (isAllowed(side)) {
                allowed = 1 << side.ordinal();
            } else {
                disallowAll();
            }
        }

        public void disallowAllExcept(EnumFacing... sides) {
            switch (sides.length) {
                case 0: {
                    disallowAll();
                    return;
                }
                case 1: {
                    disallowAllExcept(sides[0]);
                    return;
                }
                default: {
                    int retained = 0;
                    for (EnumFacing side : sides) {
                        retained |= 1 << side.ordinal();
                    }
                    allowed &= retained;
                    return;
                }
            }
        }

        public void disallowAllExcept(Collection<EnumFacing> sides) {
            disallowAllExcept(sides.toArray(new EnumFacing[0]));
        }

        public void disallowAll() {
            allowed = 0;
        }

        public void increasePriority(EnumFacing side) {
            increasePriority(side, 1);
        }

        public void increasePriority(EnumFacing side, int by) {
            priority[side.ordinal()] -= by;
        }

        public void decreasePriority(EnumFacing side) {
            decreasePriority(side, 1);
        }

        public void decreasePriority(EnumFacing side, int by) {
            increasePriority(side, -by);
        }

        public int getAllowedBitSet() {
            return allowed;
        }

        public boolean areAnyAllowed() {
            return allowed != 0;
        }

        public int getAllowedCount() {
            return Integer.bitCount(allowed);
        }

        public int getHighestPriorityAllowedBitSet() {
            if (allowed == 0) {
                return 0;
            } else if (getAllowedCount() == 1) {
                return allowed;
            }

            // higher = lower, so start at max
            int highestPriority = Integer.MAX_VALUE;
            for (int i = 0; i < 6; i++) {
                if (priority[i] < highestPriority && isAllowed(i)) {
                    highestPriority = priority[i];
                }
            }

            int bitset = 0;
            for (int i = 0; i < 6; i++) {
                if (priority[i] == highestPriority && isAllowed(i)) {
                    bitset |= 1 << i;
                }
            }
            return bitset;
        }

        public List<EnumFacing> getRandomisedOrder() {
            int bitset = getHighestPriorityAllowedBitSet();
            if (bitset == 0) {
                return Collections.emptyList();
            }
            List<EnumFacing> list = new ArrayList<>(6);
            for (EnumFacing side : EnumFacing.values()) {
                if ((bitset & (1 << side.ordinal())) != 0) {
                    list.add(side);
                }
            }
            if (list.size() > 1) {
                Collections.shuffle(list);
            }
            return list;
        }
    }
}
