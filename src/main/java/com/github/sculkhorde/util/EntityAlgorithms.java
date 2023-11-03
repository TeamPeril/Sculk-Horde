package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
            e.getType().is(ModEntities.EntityTags.SCULK_ENTITY);

    /**
     * Determines if an Entity is Infected based on if it has a potion effect
     * @param e The Given Entity
     * @return True if Infected, False otherwise
     */
    public static boolean isLivingEntityInfected(LivingEntity e)
    {
        return e.hasEffect(ModMobEffects.SCULK_INFECTION.get());
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
     * Determines if we should avoid targeting an entity at all costs.
     * @param entity The Given Entity
     * @return True if we should avoid, False otherwise
     */
    public static boolean isLivingEntityExplicitDenyTarget(LivingEntity entity)
    {
        if(entity == null)
        {
            return true;
        }

        // Is entity not a mob or player?
        if(!(entity instanceof Mob) && !(entity instanceof Player))
        {
            return true;
        }

        //If not attackable or invulnerable or is dead/dying
        if(!entity.isAttackable() || entity.isInvulnerable() || !entity.isAlive())
        {
            return true;
        }

        if(entity instanceof Player player)
        {
            if(player.isCreative() || player.isSpectator())
            {
                return true;
            }

            if(player.getEffect(ModMobEffects.PURITY.get()) != null && player.getEffect(ModMobEffects.PURITY.get()).getAmplifier() > 50)
            {
                return true;
            }
        }

        if(entity instanceof Creeper)
        {
            return true;
        }

        if(isSculkLivingEntity.test(entity))
        {
            return true;
        }

        if(entity.getType().is(ModEntities.EntityTags.SCULK_ENTITY))
        {
            return true;
        }

        if(ModColaborationHelper.isThisAFromAnotherWorldEntity(entity) && ModConfig.SERVER.target_faw_entities.get())
        {
            return true;
        }

        if(ModColaborationHelper.isThisASporeEntity(entity) && ModConfig.SERVER.target_spore_entities.get())
        {
            return true;
        }

        return false;
    }

    public static void spawnEntitiesOnCircumference(ServerLevel level, Vec3 origin, int radius, int amount, EntityType<?> type)
    {
        ArrayList<Entity> entities = new ArrayList<Entity>();
        ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
        for(int i = 0; i < possibleSpawns.size(); i++)
        {
            Vec3 spawnPos = possibleSpawns.get(i);
            Entity entity = type.create(level);
            entity.setPos(spawnPos.x(), spawnPos.y(), spawnPos.z());
            entities.add(entity);
        }

        for (Entity entity : entities) {
            level.addFreshEntity(entity);
        }
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

    /**
     * Gets all living entities in the given bounding box.
     * @param serverLevel The given world
     * @param boundingBox The given bounding box to search for a target
     * @param predicate The given predicate to filter the results
     * @return A list of valid targets
     */
    public static List<LivingEntity> getLivingEntitiesInBoundingBox(ServerLevel serverLevel, AABB boundingBox, Predicate<LivingEntity> predicate)
    {
        List<LivingEntity> livingEntitiesInRange = serverLevel.getEntitiesOfClass(LivingEntity.class, boundingBox, predicate);
        return livingEntitiesInRange;
    }

    public static AABB createBoundingBoxCubeAtBlockPos(Vec3 origin, int squareLength)
    {
        double halfLength = squareLength/2;
        AABB boundingBox = new AABB(origin.x() - halfLength, origin.y() - halfLength, origin.z() - halfLength, origin.x() + halfLength, origin.y() + halfLength, origin.z() + halfLength);
        return boundingBox;
    }

    public static AABB createBoundingBoxRectableAtBlockPos(Vec3 origin, int width, int height, int length)
    {
        double halfWidth = width/2;
        double halfHeight = height/2;
        double halfLength = length/2;

        AABB boundingBox = new AABB(origin.x() - halfWidth, origin.y() - halfHeight, origin.z() - halfLength, origin.x() + halfWidth, origin.y() + halfHeight, origin.z() + halfLength);
        return boundingBox;
    }

    public static List<LivingEntity> getNonSculkEntitiesAtBlockPos(ServerLevel level, BlockPos origin, int squareLength)
    {
        AABB boundingBox = createBoundingBoxCubeAtBlockPos(origin.getCenter(), squareLength);
        List<LivingEntity> livingEntitiesInRange = level.getEntitiesOfClass(LivingEntity.class, boundingBox, new Predicate<LivingEntity>() {
            @Override
            public boolean test(LivingEntity livingEntity) {
                return !EntityAlgorithms.isSculkLivingEntity.test(livingEntity);
            }
        });
        return livingEntitiesInRange;
    }



    public static void announceToAllPlayers(ServerLevel level, Component message)
    {
        level.players().forEach((player) -> player.displayClientMessage(message, false));
    }

    public static class DelayedHurtScheduler
    {
        private int ticksRemaining;
        private int delayInTicks;
        private Mob damageDealer;
        private boolean active = false;

        private double attackReach = 0.0;

        public DelayedHurtScheduler(Mob damageDealer, int delayInTicks)
        {
            this.damageDealer = damageDealer;
            this.delayInTicks = delayInTicks;
            this.ticksRemaining = delayInTicks;
        }

        private ISculkSmartEntity getDamageDealerAsISculkSmartEntity()
        {
            return (ISculkSmartEntity) damageDealer;
        }

        private Mob getDamageDealerAsMob()
        {
            return damageDealer;
        }

        public void tick()
        {
            if(!active)
            {
                return;
            }

            if(ticksRemaining > 0)
            {
                ticksRemaining--;
            }
            else
            {
                tryToDealDamage();
                reset();
            }
        }

        private boolean tryToDealDamage()
        {
            Optional<Entity> target = Optional.ofNullable(getDamageDealerAsMob().getTarget());


            if(damageDealer == null || !getDamageDealerAsMob().isAlive())
            {
                return false;
            }
            else if(target.isEmpty())
            {
                return false;
            }
            else if(!target.get().isAlive())
            {
                return false;
            }
            else if(getDamageDealerAsMob().distanceTo(target.get()) > attackReach)
            {
                return false;
            }

            getDamageDealerAsMob().swing(InteractionHand.MAIN_HAND);
            getDamageDealerAsMob().doHurtTarget(getDamageDealerAsMob().getTarget());
            return true;
        }

        public void trigger(double attackReach)
        {
            this.attackReach = attackReach;
            active = true;
        }

        public void reset()
        {
            ticksRemaining = delayInTicks;
            active = false;
        }
    }
}
