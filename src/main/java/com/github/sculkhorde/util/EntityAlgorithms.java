package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.EffectRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

import static com.github.sculkhorde.core.SculkHorde.gravemind;

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
            return ((BlockRayTraceResult)block).getBlockPos();
        }
        return null;
    }

    /**
     * Creates a 3D cube around a given origin. The origin is the centroid.
     * @param originX The X coordinate of the origin
     * @param originY The Y coordinate of the origin
     * @param originZ The Z coordinate of the origin
     * @return Returns the Bounding Box
     */
    public static AxisAlignedBB getSearchAreaRectangle(double originX, double originY, double originZ, double w, double h, double l)
    {
        double x1 = originX - w;
        double y1 = originY - h;
        double z1 = originZ - l;
        double x2 = originX + w;
        double y2 = originY + h;
        double z2 = originZ + l;
        return new AxisAlignedBB(x1, y1, z1, x2, y2, z2);
    }


    /**
     * Determines if an Entity belongs to the sculk based on rules
     * @return True if Valid, False otherwise
     */
    public static Predicate<LivingEntity> isSculkLivingEntity = (e) ->
    {
        return e instanceof SculkMiteEntity
                || e instanceof SculkMiteAggressorEntity
                || e instanceof SculkZombieEntity
                || e instanceof SculkSpitterEntity
                || e instanceof SculkSporeSpewerEntity
                || e instanceof SculkBeeHarvesterEntity
                || e instanceof SculkBeeInfectorEntity
                || e instanceof SculkHatcherEntity
                || e instanceof SculkRavagerEntity;
    };

    /**
     * Determines if an Entity is Infected based on if it has a potion effect
     * @param e The Given Entity
     * @return True if Infected, False otherwise
     */
    public static boolean isLivingEntityInfected(LivingEntity e)
    {
        return e.hasEffect(EffectRegistry.SCULK_INFECTION.get());
    }


    /**
     * Determines if an Entity is an aggressor.
     * @param entity The Given Entity
     * @return True if enemy, False otherwise
     */
    public static boolean isLivingEntityHostile(LivingEntity entity)
    {

        return gravemind.getGravemindMemory().getHostileEntries().get(entity.getType().toString()) != null;

    }

    public static boolean isLivingEntitySwimmer(LivingEntity entity)
    {
        // The gramemind does not store swimmers, we need to figure if a mob is swimming
        // by using the entity's ability to swim
        return entity.canBreatheUnderwater();
    }

    /**
     * Gets all living entities in the given bounding box.
     * @param serverWorld The given world
     * @param boundingBox The given bounding box to search for a target
     * @return A list of valid targets
     */
    public static List<LivingEntity> getLivingEntitiesInBoundingBox(ServerWorld serverWorld, AxisAlignedBB boundingBox)
    {
        List<LivingEntity> livingEntitiesInRange = serverWorld.getLoadedEntitiesOfClass(LivingEntity.class, boundingBox, (Predicate<? super LivingEntity>) null);
        return livingEntitiesInRange;
    }


    public static List<BlockPos> createPathToBlockPos(ServerWorld world, BlockPos start, BlockPos end, int maxRange, Predicate<BlockState> obstaclePredicate)
    {
        // Initialize ArrayList
        List<BlockPos> path = new ArrayList<>();

        /**
         * Use A* path finding algorithm to find path between start and end.
         * Should use obstaclePredicate to determine if a block is an obstacle.
         * Should use maxRange to determine if a block is too far away.
         */
        return null;


    }
}
