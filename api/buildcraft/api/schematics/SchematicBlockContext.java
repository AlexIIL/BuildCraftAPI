package buildcraft.api.schematics;

import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;

public class SchematicBlockContext {
    public final IBlockAccess world;
    public final BlockPos basePos;
    public final BlockPos pos;
    public final IBlockState blockState;
    public final Block block;

    public SchematicBlockContext(IBlockAccess world,
                                 BlockPos basePos,
                                 BlockPos pos) {
        this.world = world;
        this.basePos = basePos;
        this.pos = pos;
        this.blockState = world.getBlockState(pos);
        this.block = blockState.getBlock();
    }
}
