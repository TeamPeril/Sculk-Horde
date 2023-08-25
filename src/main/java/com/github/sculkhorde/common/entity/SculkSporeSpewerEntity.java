package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.EffectRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ParticleRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SculkSporeSpewerEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkSporeSpewerEntity.java<br>
     * Added client/model/entity/ SculkSporeSpewerModel.java<br>
     * Added client/renderer/entity/ SculkSporeSpewerRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 40F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 0F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 0F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 0F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives();

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    private CursorSurfaceInfectorEntity cursor;

    private long INFECTION_INTERVAL_MILLIS = TimeUnit.SECONDS.toMillis(20);
    private long lastInfectionTime = 0;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSporeSpewerEntity(EntityType<? extends SculkSporeSpewerEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkSporeSpewerEntity(Level worldIn) {super(EntityRegistry.SCULK_SPORE_SPEWER.get(), worldIn);}

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ARMOR, ARMOR)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, ATTACK_KNOCKBACK)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return false;
    }

    private boolean isParticipatingInRaid = false;

    @Override
    public boolean isParticipatingInRaid() {
        return isParticipatingInRaid;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
        isParticipatingInRaid = isParticipatingInRaidIn;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        Goal[] goalSelectorPayload = goalSelectorPayload();
        for(int priority = 0; priority < goalSelectorPayload.length; priority++)
        {
            this.goalSelector.addGoal(priority, goalSelectorPayload[priority]);
        }

        Goal[] targetSelectorPayload = targetSelectorPayload();
        for(int priority = 0; priority < targetSelectorPayload.length; priority++)
        {
            this.targetSelector.addGoal(priority, targetSelectorPayload[priority]);
        }

    }

    /**
     * Prepares an array of goals to give to registerGoals() for the goalSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] goalSelectorPayload()
    {
        Goal[] goals =
                {
                        // MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new dieAfterTimeGoal(this),
                };
        return goals;
    }

    /**
     * Prepares an array of goals to give to registerGoals() for the targetSelector.<br>
     * The purpose was to make registering goals simpler by automatically determining priority
     * based on the order of the items in the array. First element is of priority 0, which
     * represents highest priority. Priority value then increases by 1, making each element
     * less of a priority than the last.
     * @return Returns an array of goals ordered from highest to lowest piority
     */
    public Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        //HurtByTargetGoal(mob)
                        new TargetAttacker(this).setAlertAllies(),
                };
        return goals;
    }
    //Animation Related Functions

    private static final RawAnimation SPREAD_ANIMATION = RawAnimation.begin().thenPlay("spread");
    private final AnimationController SPREAD_ANIMATION_CONTROLLER = new AnimationController<>(this, "spread_controller", state -> PlayState.STOP)
            .triggerableAnim("spread_animation", SPREAD_ANIMATION);

    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericLivingController(this),
                SPREAD_ANIMATION_CONTROLLER);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    //Every tick, spawn a short range cursor
    @Override
    public void aiStep() {
        super.aiStep();

        // Only on the client side, spawn dust particles with a specific color
        // Have the partciles fly in random directions
        if (level().isClientSide)
        {
            Random random = new Random();
            for (int i = 0; i < 1; i++) {
                level().addParticle(ParticleRegistry.SCULK_CRUST_PARTICLE.get(), this.position().x, this.position().y + 1.7, this.position().z, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3);
            }
            return;
        }

        Random random = new Random();
        if (random.nextInt(100) == 0 && (cursor == null || !cursor.isAlive())) {
            // Spawn Block Traverser
            cursor = new CursorSurfaceInfectorEntity(level());
            cursor.setPos(this.blockPosition().getX(), this.blockPosition().getY() - 1, this.blockPosition().getZ());
            cursor.setMaxTransformations(100);
            cursor.setMaxRange(100);
            cursor.setTickIntervalMilliseconds(10);
            cursor.setSearchIterationsPerTick(1);
            level().addFreshEntity(cursor);
            triggerAnim("spread_controller", "spread_animation");
        }

        if (System.currentTimeMillis() - lastInfectionTime > INFECTION_INTERVAL_MILLIS)
        {
            lastInfectionTime = System.currentTimeMillis();
            // Any entity within 10 blocks of the spewer will be infected
            ArrayList<LivingEntity> entities = (ArrayList<LivingEntity>) EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerLevel) level(), this.getBoundingBox().inflate(10));
            for (LivingEntity entity : entities)
            {
                if (entity instanceof LivingEntity && ((ISculkSmartEntity) this).getTargetParameters().isEntityValidTarget(entity, false))
                {
                    entity.addEffect(new MobEffectInstance(EffectRegistry.SCULK_INFECTION.get(), 500, 3));
                }
            }
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SCULK_CATALYST_BLOOM;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SCULK_CATALYST_BREAK;
    }

    /**
     * This is a custom goal that I made to make the mob die after a certain amount of time.
     * This is useful for mobs that are meant to be temporary, such as the Sculk Spore Spewer.
     */
    private class dieAfterTimeGoal extends Goal
    {
        private final SculkSporeSpewerEntity entity;
        private int timeUntilDeath = 0;

        public dieAfterTimeGoal(SculkSporeSpewerEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void start() {
            timeUntilDeath = 20 * 60 * 60; //Die after 60 Minutes
        }

        @Override
        public void tick()
        {
            if(level().isClientSide())
            {
                return;
            }

            timeUntilDeath--;
            if (timeUntilDeath <= 0) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public boolean dampensVibrations() {
        return true;
    }


    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */
}
