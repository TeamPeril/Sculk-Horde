package com.github.sculkhoard.common.entity;

import com.github.sculkhoard.core.EffectRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.World;

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

    public static boolean isLivingEntityInfected(LivingEntity e)
    {
        return e.hasEffect(EffectRegistry.SCULK_INFECTION.get());
    }

    public static <T extends LivingEntity>void spawnSculkLivingEntity(T entity, World worldIn, double x, double y, double z)
    {
        entity.setPos(x, y, z); //Set its position
        worldIn.addFreshEntity(entity);//I think this spawns the actual instance into the world
    }
}
