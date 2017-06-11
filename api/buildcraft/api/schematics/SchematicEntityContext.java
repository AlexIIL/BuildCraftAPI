package buildcraft.api.schematics;

import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class SchematicEntityContext {
    public final IBlockAccess world;
    public final BlockPos basePos;
    public final Entity entity;

    public SchematicEntityContext(IBlockAccess world,
                                  BlockPos basePos,
                                  Entity entity) {
        this.world = world;
        this.basePos = basePos;
        this.entity = entity;
    }
}
