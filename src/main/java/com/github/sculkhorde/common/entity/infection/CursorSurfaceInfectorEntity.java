package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.BlockInfestationHelper;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;

import static com.github.sculkhorde.util.BlockAlgorithms.isExposedToInfestationWardBlock;

public class CursorSurfaceInfectorEntity extends CursorInfectorEntity{
    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     *
     * @param worldIn The world to initialize this mob in
     */
    public CursorSurfaceInfectorEntity(Level worldIn) {
        super(worldIn);
    }

    public CursorSurfaceInfectorEntity(EntityType<?> pType, Level pLevel) {
        super(pType, pLevel);
    }


    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return true;
        }
        else if(state.isAir())
        {
            return true;
        }
        else if(isExposedToInfestationWardBlock((ServerLevel) this.level(), pos))
        {
            return true;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // Check if block is not beyond world border
        if(!level().isInWorldBounds(pos))
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositons.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) this.level(), pos);
        boolean isBlockNotSculkArachnoid = !state.is(ModBlocks.SCULK_ARACHNOID.get());
        boolean isBlockNotSculkDuraMatter = !state.is(ModBlocks.SCULK_DURA_MATTER.get());

        if(isBlockNotExposedToAir && isBlockNotSculkArachnoid && isBlockNotSculkDuraMatter)
        {
            return true;
        }

        return false;
    }
}
