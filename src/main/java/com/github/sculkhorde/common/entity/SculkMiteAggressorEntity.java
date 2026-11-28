package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.DefaultTargetParameters;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.components.TargetRetention;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.animal.Pig;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathType;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class SculkMiteAggressorEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkMiteAggressor.java<br>
     * Added client/model/entity/ SculkMiteAggressorModel.java<br>
     * Added client/renderer/entity/ SculkMiteAggressorRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 5F;
    //The armor of the mob
    public static final float ARMOR = 2F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 2F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 16F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultGroundMeleeCombat.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkMiteAggressorEntity(EntityType<? extends SculkMiteAggressorEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(PathType.UNPASSABLE_RAIL, 0.0F);
    }

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
    protected void customServerAiStep() {
        super.customServerAiStep();
        TARGET_PARAMETERS.updateTargets();
    }

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(5)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(2)),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadLogicGoal(this),
                        //MeleeAttackGoal(mob, speedModifier, followingTargetEvenIfNotSeen)
                        new CustomMeleeAttackGoal2(this, 1.5F, 0, 0),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        new MiteLeapAtTargetGoal(this, 0.5F, TickUnits.convertSecondsToTicks(3)), // Only works on hard
                        //WaterAvoidingRandomWalkingGoal(mob, speedModifier)
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),
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
                        new HurtByTargetGoal(this),
                        new FocusSquadTarget(this),
                        new SculkHordeTargetGoal<>(this)
                };
        return goals;
    }


    //Animation Stuff below

    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(DefaultAnimations.genericWalkIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    protected class MiteLeapAtTargetGoal extends Goal
    {

        protected final Mob mob;
        protected final float yd;

        protected long timeOfLastLeap = 0;
        protected long cooldown;

        public MiteLeapAtTargetGoal(Mob mob, float leapDistance, long cooldown) {
            this.mob = mob;
            this.yd = leapDistance;
            this.setFlags(EnumSet.of(Flag.JUMP, Flag.LOOK));
            this.cooldown = cooldown;
        }

        public boolean canUse() {
            if(level().getGameTime() - timeOfLastLeap < cooldown)
            {
                return false;
            }
            else if(DifficultyUtil.isCurrentDifficultyLessThanHard())
            {
                return false;
            }
            else if (this.mob.isVehicle())
            {
                return false;
            }
            else if(mob.getTarget() == null)
            {
                return false;
            }

            double $$distanceFromTarget = EntityAlgorithms.getDistanceBetweenEntities(mob, mob.getTarget());
            if ($$distanceFromTarget > 7)
            {
                return false;
            }
            return true;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void start() {
            timeOfLastLeap = level().getGameTime();

            // 1. Calculate the initial horizontal delta movement toward the target
            // We only care about the X and Z components for the horizontal direction.
            Vec3 directionHorizontal = new Vec3(
                    mob.getTarget().getX() - this.mob.getX(),
                    0.0, // Set Y component to 0 to only get the horizontal direction
                    mob.getTarget().getZ() - this.mob.getZ()
            );

            Vec3 finalDeltaMovement;

            // Check if the horizontal distance is great enough to normalize
            if (directionHorizontal.lengthSqr() > 1.0E-7) {
                // Normalize the vector, scale by the 'power' parameter, and add existing momentum
                finalDeltaMovement = directionHorizontal
                        .normalize()
                        // Scale by the 'power' parameter for horizontal speed
                        .scale(this.yd)
                        // Add a small fraction of the mob's current horizontal momentum to smooth the transition
                        .add(this.mob.getDeltaMovement().scale(0.2));
            } else {
                // If the target is directly above/below, use the mob's existing horizontal momentum
                finalDeltaMovement = this.mob.getDeltaMovement().scale(0.2);
            }

            // 2. Set the mob's new delta movement.
            // Use the calculated X and Z from the finalDeltaMovement vector,
            // and explicitly use the 'yd' parameter for the vertical jump strength.
            this.mob.setDeltaMovement(finalDeltaMovement.x, (double)this.yd, finalDeltaMovement.z);
            this.mob.lookAt(getTarget(), 30F, 30F);
        }
    }
}
