package com.github.sculkhoard.util;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.gravemind.Gravemind;
import net.minecraft.entity.IAngerable;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.boss.WitherEntity;
import net.minecraft.entity.monster.AbstractRaiderEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.passive.IronGolemEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Predicate;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;
import static com.github.sculkhoard.core.SculkHoard.gravemind;

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

    /**
     * Creates a 3D cube around a given origin. The origin is the centroid.
     * @param originX
     * @param originY
     * @param originZ
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
     * Filters out any mobs that do not fit the filter
     * @param list The list of possible targets
     */
    public static void filterOutNonTargets(List<LivingEntity> list, boolean targetHostiles, boolean targetPassives, boolean targetInfected)
    {
        for(int i = 0; i < list.size(); i++)
        {
            boolean isNonTarget = false;

            //If sculk living entity, do not attack
            if(list.get(i) instanceof SculkLivingEntity)
            {
                isNonTarget = true;
            }

            //Do not attack creepers
            if(list.get(i) instanceof CreeperEntity)
            {
                isNonTarget = true;
            }

            //If not attackable or invulnerable or is dead/dying
            if(!list.get(i).isAttackable() || list.get(i).isInvulnerable() || list.get(i).isDeadOrDying() || list.get(i).isSpectator())
            {
                isNonTarget = true;
            }

            //If not attackable or invulnerable or is dead/dying
            if(list.get(i) instanceof PlayerEntity && ((PlayerEntity) list.get(i)).isCreative())
            {
                isNonTarget = true;
            }

            //If we do not attack infected and entity is infected
            if(!targetInfected && isLivingEntityInfected(list.get(i)))
            {
                isNonTarget = true;
            }

            //If we do not attack passives and entity is non-hostile
            if(!targetPassives && !isLivingEntityHostile(list.get(i))) //NOTE: horde assumes everything is passive until provoked
            {
                isNonTarget = true;
            }

            //If we do not attack hostiles and target is hostile
            if(!targetHostiles && isLivingEntityHostile(list.get(i)))
            {
                isNonTarget = true;
            }

            //If friendly, filter
            if(isNonTarget)
            {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
        }
    }

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
}
