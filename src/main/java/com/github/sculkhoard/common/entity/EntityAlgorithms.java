package com.github.sculkhoard.common.entity;

import com.github.sculkhoard.core.EffectRegistry;
import com.github.sculkhoard.core.SculkHoard;
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
     * Determines if an Entity is Infected based on if it has a potion effect
     * @param e The Given Entity
     * @return True if Infected, False otherwise
     */
    public static boolean isLivingEntityInfected(LivingEntity e)
    {
        return e.hasEffect(EffectRegistry.SCULK_INFECTION.get());
    }


    /**
     * Determines if an Entity is friendy.
     * @param e The Given Entity
     * @return True if friendly, False otherwise
     */
    public static boolean isLivingEntityFriendly(LivingEntity e)
    {
        boolean isFriendly = false;
        if(isLivingEntityInfected(e) || e instanceof SculkLivingEntity)
            isFriendly = true;
        //If Player is in creative or in spectator, consider them friendly
        else if(e instanceof PlayerEntity)
        {
            ServerPlayerEntity player = (ServerPlayerEntity) e;
            if(player.isCreative() || player.isSpectator())
            isFriendly = true;
        }

        return isFriendly;
    }


    /**
     * Determines if an Entity is an aggressor.
     * @param e The Given Entity
     * @return True if enemy, False otherwise
     */
    public static boolean isLivingEntityHostile(LivingEntity e)
    {
        String entity = e.getClass().toString();
        return SculkHoard.gravemind.confirmedThreats.contains(entity);
    }

    /**
     * Adds a hostile if it hasnt been added
     */
    public static void addHostile(LivingEntity e)
    {
        if(e == null || e instanceof SculkLivingEntity)
            return;

        String entityString = e.getClass().toString();

        if(!SculkHoard.gravemind.confirmedThreats.contains(entityString) && entityString != null && !entityString.isEmpty() && !(e instanceof CreeperEntity))
        {
            SculkHoard.gravemind.confirmedThreats.add(entityString);
            if(DEBUG_MODE) System.out.println("Sculk Hoard now recognises " + entityString + " as a hostile");
        }

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

    /**
     * Filters out any friendlies from a list.
     * @param list
     * @return A list of valid infection targets
     */
    public static void filterOutFriendlies(List<LivingEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            //If Friendly
            if (isLivingEntityFriendly(list.get(i))) {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
        }
    }


    /**
     * Filters out any friendlies from a list.
     * @param list
     * @return A list of valid infection targets
     */
    public static void filterOutDoNotInteractMobs(List<LivingEntity> list) {
        for (int i = 0; i < list.size(); i++) {
            //If Friendly
            if (list.get(i) instanceof CreeperEntity) {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
        }
    }

    /**
     * Filters out any aggressors and friendlies from a list.
     * @param list
     * @return A list of valid infection targets
     */
    public static void filterOutHostiles(List<LivingEntity> list)
    {
        for(int i = 0; i < list.size(); i++)
        {
            //If Friendly
            if(isLivingEntityFriendly(list.get(i)))
            {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
            //If Player
            else if(list.get(i) instanceof PlayerEntity)
            {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
            //If Iron Golem
            else if(list.get(i) instanceof IronGolemEntity)
            {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
        }
    }

    /**
     * Filters out any non-aggressors and friendlies from a list.
     * @param list
     * @return A list of valid infection targets
     */
    public static void filterOutNonHostiles(List<LivingEntity> list)
    {
        for(int i = 0; i < list.size(); i++)
        {
            //If friendly, filter
            if(isLivingEntityFriendly(list.get(i)) || !isLivingEntityHostile(list.get(i)))
            {
                list.remove(i); //Remove from list
                i--; //Go back one index since the new length of the list is one less.
            }
        }
    }
}
