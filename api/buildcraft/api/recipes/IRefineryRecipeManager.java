package buildcraft.api.recipes;

import java.util.Collection;
import java.util.stream.Stream;

import javax.annotation.Nullable;

import com.google.common.base.Predicate;

import net.minecraftforge.fluids.FluidStack;

public interface IRefineryRecipeManager {
    IHeatableRecipe createHeatingRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    IHeatableRecipe addHeatableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting);

    ICoolableRecipe createCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks);

    ICoolableRecipe addCoolableRecipe(FluidStack in, FluidStack out, int heatFrom, int heatTo, int ticks, boolean replaceExisting);

    IDistilationRecipe createDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks);

    IDistilationRecipe addDistilationRecipe(FluidStack in, FluidStack outGas, FluidStack outLiquid, int ticks, boolean replaceExisting);

    IRefineryRegistry<IHeatableRecipe> getHeatableRegistry();

    IRefineryRegistry<ICoolableRecipe> getCoolableRegistry();

    IRefineryRegistry<IDistilationRecipe> getDistilationRegistry();

    public interface IRefineryRegistry<R extends IRefineryRecipe> {
        /** @return an unmodifiable collection containing all of the distillation recipes that satisfy the given
         *         predicate. All of the recipe objects are guaranteed to never be null. */
        Stream<R> getRecipes(Predicate<R> toReturn);

        /** @return an unmodifiable set containing all of the distillation recipes. */
        Collection<R> getAllRecipes();

        @Nullable
        R getRecipeForInput(FluidStack fluid);

        Collection<R> removeRecipes(Predicate<R> toRemove);

        /** Adds the given recipe to the registry.
         * 
         * @param recipe The recipe to add.
         * @param replaceExisting If true then an existing recipe that matches {@link IRefineryRecipe#in()} with
         *            {@link FluidStack#isFluidEqual(FluidStack)} will be replaced. Otherwise the matching recipe will
         *            be returned and the given recipe will not be added.
         * @return The recipe that will be returned by {@link #getRecipeForInput(FluidStack)} when given the
         *         {@link FluidStack} in recipe.in() */
        R addRecipe(R recipe, boolean replaceExisting);
    }

    public interface IRefineryRecipe {
        int ticks();

        FluidStack in();
    }

    public interface IHeatExchangerRecipe extends IRefineryRecipe {
        @Nullable
        FluidStack out();

        int heatFrom();

        int heatTo();
    }

    public interface IHeatableRecipe extends IHeatExchangerRecipe {}

    public interface ICoolableRecipe extends IHeatExchangerRecipe {}

    public interface IDistilationRecipe extends IRefineryRecipe {
        FluidStack outGas();

        FluidStack outLiquid();
    }
}
