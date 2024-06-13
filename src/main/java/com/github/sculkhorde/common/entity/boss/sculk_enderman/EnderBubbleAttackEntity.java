package com.github.sculkhorde.common.entity.boss.sculk_enderman;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.List;
import java.util.function.Predicate;

/**
 * The following java files were created/edited for this entity.<br>
 * Edited {@link ModEntities}<br>
 * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
 * Added {@link EnderBubbleAttackEntity}<br>
 * Added {@link com.github.sculkhorde.client.model.enitity.EnderBubbleAttackModel}<br>
 * Added {@link com.github.sculkhorde.client.renderer.entity.EnderBubbleAttackRenderer}
 */
public class EnderBubbleAttackEntity extends SpecialEffectEntity implements GeoEntity {

    public static int LIFE_TIME = TickUnits.convertSecondsToTicks(10);
    public int currentLifeTicks = 0;
    private double pushUpStrength = 0.1; // Strength of the push effect
    private double orbitStrength = 0.5; // Strength of the orbit effect

    private boolean startedPlayingSFX = false;

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public EnderBubbleAttackEntity( Level level) {
        super(ModEntities.ENDER_BUBBLE_ATTACK.get(), level);
    }

    public EnderBubbleAttackEntity(EntityType<?> entityType, Level level, LivingEntity sourceEntity) {
        super(entityType, level);
    }

    public EnderBubbleAttackEntity enableDeleteAfterTime(int ticks)
    {
        LIFE_TIME = ticks;
        return this;
    }

    private void pullInEntities(double range)
    {
        if(level().isClientSide()) return;

        Predicate<Entity> predicate = (entity) -> {
            if(entity == null) {return false;}

            else if(entity instanceof Player p)
            {
                //p.hurtMarked = true;
                return false;
            }

            else if(entity instanceof SculkEndermanEntity)
            {
                return false;
            }

            boolean entityIsPushable = !entity.noPhysics;

            return entityIsPushable;
        };

        List<Entity> pushAwayList = EntityAlgorithms.getEntitiesInBoundingBox((ServerLevel) level(), this.getBoundingBox().inflate(range, range, range), predicate);

        for(Entity entity : pushAwayList)
        {
            // Push entities away
            float pushAwayStrength = 0.01f;
            Vec3 vector = this.position().subtract(entity.position()).normalize();
            entity.push(vector.x * pushAwayStrength, vector.y * pushAwayStrength, vector.z * pushAwayStrength);

            // Calculate the vector from the black hole to the entity
            double dx = entity.getX() - getX();
            double dy = entity.getY() - getY();
            double dz = entity.getZ() - getZ();

            // Calculate the distance to the black hole
            double distance = Math.sqrt(dx * dx + dy * dy + dz * dz);

            // Normalize the vector
            dx /= distance;
            dy /= distance;
            dz /= distance;

            // Apply the push effect
            //entity.push(dx * pushStrength, dy * pushStrength, dz * pushStrength);
            entity.push(0, pushUpStrength, 0);


            // Calculate the orbit vector (rotate the original vector 90 degrees)
            double ox = -dz;
            double oy = dy;
            double oz = dx;

            // Apply the orbit effect
            entity.push(ox * orbitStrength, oy * orbitStrength, oz * orbitStrength);

        }
    }

    @Override
    public void tick() {
        super.tick();

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        if(!startedPlayingSFX)
        {
            level().playSound((Player)null, this.getX(), this.getY(), this.getZ(), ModSounds.ENDER_BUBBLE_LOOP.get(), this.getSoundSource(), 1.0F, 1F);
            startedPlayingSFX = true;
        }

        pullInEntities(20);

        List<LivingEntity> damageHitList = getEntitiesNearbyCube(LivingEntity.class, 3);

        for (LivingEntity entity : damageHitList)
        {
            if (getOwner() != null && getOwner().equals(entity))
            {
                continue;
            }

            if(getOwner() != null)
            {
                entity.hurt(damageSources().indirectMagic(entity, getOwner()), 5);
            }
            else
            {
                entity.hurt(damageSources().indirectMagic(entity, this), 5);
            }
        }


    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
