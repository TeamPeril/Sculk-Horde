package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.core.EffectRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

public class EntityAlgorithms {
    /**
     * Returns the block position a player is staring at
     * @param player The player to check
     * @param isFluid Should we consider fluids
     * @return the position the player is staring at
     */
    @Nullable
    public static BlockPos playerTargetBlockPos(Player player, boolean isFluid)
    {
        HitResult block =  player.pick(200.0D, 0.0F, isFluid);

        if(block.getType() == HitResult.Type.BLOCK)
        {
            return ((BlockHitResult)block).getBlockPos();
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
    public static AABB getSearchAreaRectangle(double originX, double originY, double originZ, double w, double h, double l)
    {
        double x1 = originX - w;
        double y1 = originY - h;
        double z1 = originZ - l;
        double x2 = originX + w;
        double y2 = originY + h;
        double z2 = originZ + l;
        return new AABB(x1, y1, z1, x2, y2, z2);
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
                || e instanceof SculkRavagerEntity
                || e instanceof SculkVindicatorEntity;
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
        return SculkHorde.savedData.getHostileEntries().get(entity.getType().toString()) != null;
    }

    public static boolean isLivingEntitySwimmer(LivingEntity entity)
    {
        // The gramemind does not store swimmers, we need to figure if a mob is swimming
        // by using the entity's ability to swim
        return entity instanceof WaterAnimal;
    }

    /**
     * Gets all living entities in the given bounding box.
     * @param serverLevel The given world
     * @param boundingBox The given bounding box to search for a target
     * @return A list of valid targets
     */
    public static List<LivingEntity> getLivingEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox)
    {
        List<LivingEntity> livingEntitiesInRange = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, new Predicate<LivingEntity>() {
            @Override
            public boolean test(LivingEntity livingEntity) {
                return true;
            }
        });
        return livingEntitiesInRange;

    }
}
