package buildcraft.api.schematics;

import net.minecraft.block.state.IBlockState;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

/** An {@link IBlockAccess} where instances of {@link ISchematicBlock} can set the blockstate inside of
 * {@link ISchematicBlock#buildWithoutChecks(IBuildableRegion, BlockPos)}. */
public interface IBuildableRegion extends IBlockAccess {
    void setBlockState(BlockPos pos, IBlockState state, NBTTagCompound tileNbt);
}
