package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals.LookAtTargetOrRandom;
import com.github.sculkhorde.common.entity.components.DefaultTargetParameters;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.components.TargetRetention;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.common.entity.projectile.SculkAcidicProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class SculkSpitterEntity extends Monster implements GeoEntity,ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkSpitter.java<br>
     * Added client/model/entity/ SculkSpitterModel.java<br>
     * Added client/renderer/entity/ SculkSpitterRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //The armor of the mob
    public static final float ARMOR = 4F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 1F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 2F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 40F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private final TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultGroundRangedCombat.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));

    private static final EntityDataAccessor<Boolean> IS_STRAFING = SynchedEntityData.defineId(SculkSpitterEntity.class, EntityDataSerializers.BOOLEAN);

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSpitterEntity(EntityType<? extends SculkSpitterEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkSpitterEntity(Level worldIn) {super(ModEntities.SCULK_SPITTER.get(), worldIn);}

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
                        new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(15)),
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(2)),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new StayInRangeOfTarget(this, 20, 10),
                        new SquadLogicGoal(this),
                        new MountNearestRavager(this),
                        //new RangedAcidAttackGoal(this, 1.0D, TickUnits.convertSecondsToTicks(3), 40),
                        new SpitAttackGoal(this,  40, 10),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),
                        new LookAtTargetOrRandom(this, 10, 0.5F, false),
                        //LookRandomlyGoal(mob)
                        //new RandomLookAroundGoal(this),
                        new OpenDoorGoal(this, true)
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
                        new TargetAttacker(this),
                        new FocusSquadTarget(this),
                        new SculkHordeTargetGoal<>(this)
                };
        return goals;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        TARGET_PARAMETERS.updateTargets();
    }

    public void performRangedAttack(LivingEntity attackTarget) {
        // 1. Initialize the projectile
        SculkAcidicProjectileEntity projectile = new SculkAcidicProjectileEntity(attackTarget.level(), this, 1);

        float inaccuracyFactor = 0.5F;

        if(DifficultyUtil.isCurrentDifficultyEasy())
        {
            inaccuracyFactor = 2.0F;
        }
        else if(DifficultyUtil.isCurrentDifficultyNormal())
        {
            inaccuracyFactor = 1.0F;
        }

        // Constants (adjust these to match your projectile's characteristics)
        final float PROJECTILE_SPEED = 1.6F;
        final double GRAVITY = 0.03;

        // 2. Calculate Distances
        double deltaX = attackTarget.getX() - this.getX();
        double deltaY = attackTarget.getEyeY() - projectile.getY();
        double deltaZ = attackTarget.getZ() - this.getZ();
        double horizontalDistance = Math.sqrt(deltaX * deltaX + deltaZ * deltaZ);

        // 3. Solve for Flight Time (T)

        // Calculate the time needed to cover the horizontal distance
        // using a fixed fraction of the total speed (e.g., assuming 90% is horizontal)
        double speedFraction = 0.9D;
        double timeToTarget = horizontalDistance / (PROJECTILE_SPEED * speedFraction);

        // 4. Calculate Required Vertical Velocity (V_Y)

        // Rearranging the vertical motion equation: V_Y = (deltaY + 0.5 * G * T^2) / T
        double requiredVerticalVelocity = (deltaY + 0.5 * GRAVITY * timeToTarget * timeToTarget) / timeToTarget;

        // 5. Create the Unit Vector

        // V_H = horizontalDistance / timeToTarget
        double horizontalVelocityMagnitude = horizontalDistance / timeToTarget;

        // Normalize the horizontal components (deltaX, deltaZ) to get a direction vector
        double xUnitVector = deltaX / horizontalDistance;
        double zUnitVector = deltaZ / horizontalDistance;

        // Calculate the final X, Y, Z shot components
        double finalXVelocity = xUnitVector * horizontalVelocityMagnitude;
        double finalYVelocity = requiredVerticalVelocity;
        double finalZVelocity = zUnitVector * horizontalVelocityMagnitude;

        // 6. Shoot the projectile (with zero inaccuracy for a perfect shot)
        projectile.shoot(finalXVelocity,
                finalYVelocity,
                finalZVelocity,
                PROJECTILE_SPEED, // This is just the "scale" for the initial velocity
                inaccuracyFactor);

        // 6. Finalize (sound and spawn)
        this.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(projectile);
    }

    // Synced Data
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_STRAFING, false);
    }

    public boolean isStrafing()
    {
        return this.entityData.get(IS_STRAFING);
    }

    public void setStrafing(boolean value)
    {
        this.entityData.set(IS_STRAFING, value);
    }



    // ANIMATIONS
    private static final RawAnimation SIT_ANIMATION = RawAnimation.begin().thenLoop("misc.sit");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenLoop("move.walk");
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenPlay("misc.idle");
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");
    private static final String ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    private static final String ATTACK_ANIMATION_ID = "attack_animation";
    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .triggerableAnim(ATTACK_ANIMATION_ID, ATTACK_ANIMATION).transitionLength(5);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "walk_cycle", 5, this::poseWalkCycle),
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
        );
    }

    protected PlayState poseWalkCycle(AnimationState<SculkSpitterEntity> state)
    {

        if(state.getAnimatable().isPassenger())
        {
            state.setAnimation(SIT_ANIMATION);
        }
        else if(state.isMoving())
        {
            state.setAnimation(WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }


    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SKELETON_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SKELETON_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SKELETON_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SKELETON_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    protected class SpitAttackGoal extends CustomAttackGoal
    {

        public SpitAttackGoal(Mob mob, float maxDistanceForAttackIn, int attackDelay) {
            super(mob, maxDistanceForAttackIn, attackDelay);
        }

        @Override
        protected long getExecutionCooldown() {
            return TickUnits.convertSecondsToTicks(3);
        }

        protected void checkAndAttack(LivingEntity targetMob) {

            if (isTargetInvalid()) {
                return;
            }

            if (!isExecutionCooldownOver()) {
                return;
            }

            performRangedAttack(targetMob);
        }

        @Override
        protected void triggerAnimation() {
            triggerAnim(ATTACK_ANIMATION_CONTROLLER_ID, ATTACK_ANIMATION_ID);
        }
    }

    public class StayInRangeOfTarget extends Goal {
        private final Mob mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final float maxDistance;
        private final float minDistance;

        public StayInRangeOfTarget(Mob reaper, float maxDistance, float minDistance) {
            this.mob = reaper;
            this.speedModifier = 1;
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse()
        {

            if (mob.getTarget() == null)
            {
                return false;
            }
            else if (mob.getTarget().distanceTo(this.mob) < this.minDistance)
            {
                Vec3 vec3 = DefaultRandomPos.getPosAway((PathfinderMob) this.mob, 16, 7, mob.getTarget().position());
                if (vec3 == null)
                {
                    return false;
                }
                else
                {
                    this.wantedX = vec3.x;
                    this.wantedY = vec3.y;
                    this.wantedZ = vec3.z;
                    return true;
                }
            }
            else if (mob.getTarget().distanceTo(this.mob) > this.maxDistance || !mob.getSensing().hasLineOfSight(mob.getTarget()))
            {
                Vec3 vec3 = DefaultRandomPos.getPosTowards((PathfinderMob) this.mob, 16, 7, mob.getTarget().position(), (double)((float)Math.PI / 2F));
                if (vec3 == null)
                {
                    return false;
                }
                else
                {
                    this.wantedX = vec3.x;
                    this.wantedY = vec3.y;
                    this.wantedZ = vec3.z;
                    return true;
                }
            }



            return false;
        }

        public boolean canContinueToUse() {

            if(mob.getTarget() == null)
            {
                return false;
            }

            return !this.mob.getNavigation().isDone() && mob.getTarget().isAlive() && mob.getTarget().distanceTo(this.mob) <= this.maxDistance && mob.getTarget().distanceTo(this.mob) >= this.minDistance;
        }

        public void stop() {
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }
    }
}
