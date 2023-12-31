package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.github.sculkhorde.util.BlockAlgorithms.isExposedToInfestationWardBlock;

public class CursorInfector extends Cursor{



    public CursorInfector(Level worldIn) {
        super(worldIn);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isNotObstructed(BlockState state, BlockPos pos)
    {
        if(!ModConfig.SERVER.block_infestation_enabled.get())
        {
            return false;
        }
        else if(isExposedToInfestationWardBlock((ServerLevel) this.level(), pos))
        {
            return false;
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return false;
        }
        else if(state.isAir())
        {
            return false;
        }
        // Check if block is not beyond world border
        else if(!level().isInWorldBounds(pos))
        {
            return false;
        }
        // This is to prevent the entity from getting stuck in a loop
        else if(visitedPositons.containsKey(pos.asLong()))
        {
            return false;
        }
        return false;
    }
}
