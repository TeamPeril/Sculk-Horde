package com.github.sculkhoard.common.entity;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;

import javax.annotation.Nullable;

public class EntityAlgorithms {

    /**
     * Returns the block position a player is staring at
     * @param player The player to check
     * @param isFluid Should we consider fluids
     * @return the position the player is staring at
     */
    @Nullable
    public static BlockPos playerTargetBlockPos(PlayerEntity player, boolean isFluid)
    {
        RayTraceResult block =  player.pick(200.0D, 0.0F, isFluid);

        if(block.getType() == RayTraceResult.Type.BLOCK)
        {
            BlockPos blockpos = ((BlockRayTraceResult)block).getBlockPos();
            return blockpos;
        }
        return null;
    }
}
