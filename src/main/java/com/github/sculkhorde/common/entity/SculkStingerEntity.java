package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.*;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;

public class SculkStingerEntity extends FlyingMob implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added client/model/entity/ SculkPhantomModel.java<br>
     * Added client/renderer/entity/ SculkPhantomRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 5F;
    //The armor of the mob
    public static final float ARMOR = 0F;
    //ATTACK_DAMAGE determines How much damage its melee attacks do
    public static final float ATTACK_DAMAGE = 1F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 0.5F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 16F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.2F;

    // Controls what types of entities this mob can target
    protected final TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultFlyerMeleeInfector.copy();

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkStingerEntity(EntityType<? extends SculkStingerEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        this.moveControl = new FlyingMoveControl(this, 20, true);
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
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 0.2F)
                .add(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get(), 0.0);
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        TARGET_PARAMETERS.updateTargets();
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
        return new Goal[]{
                new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(2)),
                new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(1)),
                new SweepAndBurrowAttackGoal(),
                new FlyingWanderGoal(this, 1.0F, TickUnits.convertSecondsToTicks(3), 10)
        };
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
        return new Goal[]{
                new TargetAttacker(this),
                new SculkHordeTargetGoal<>(this)
        };
    }

    protected @NotNull PathNavigation createNavigation(@NotNull Level level) {
        ImprovedFlyingNavigator flyingpathnavigation = new ImprovedFlyingNavigator(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    /** Getters and Setters **/

    @Override
    public boolean isParticipatingInRaid() {
        return false;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public double getPassengersRidingOffset() {
        return this.getEyeHeight();
    }

    public boolean isIdle() {
        return getTarget() == null;
    }

    /** Attributes **/

    @Override
    public boolean shouldRender(double cameraX, double cameraY, double cameraZ) {
        // Calculate the difference between the entity's position and the camera's position
        double deltaX = this.getX() - cameraX;
        double deltaY = this.getY() - cameraY;
        double deltaZ = this.getZ() - cameraZ;
        // Calculate the squared distance between the entity and the camera
        double squaredDistance = deltaX * deltaX + deltaY * deltaY + deltaZ * deltaZ;
        // Return true if the entity is within the rendering distance, false otherwise
        return this.shouldRenderAtSqrDistance(squaredDistance);
    }
    @Override
    public boolean shouldRenderAtSqrDistance(double squaredDistance) {

        // Get the size of the entity's bounding box
        double size = this.getBoundingBox().getSize();
        // If the size is not a valid number, set it to 1.0
        if (Double.isNaN(size)) {
            size = 1.0D;
        }

        // Multiply the size by a constant factor and the view scale
        size *= 64.0D * getViewScale();
        // Return true if the squared distance is less than the cubed of the size, false otherwise
        return squaredDistance < size * size * size;
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return true;
    }


    @Override
    public void checkDespawn() {}

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    public boolean dampensVibrations() {
        return true;
    }

    protected float getStandingEyeHeight(@NotNull Pose p_33136_, EntityDimensions p_33137_) {
        return p_33137_.height * 0.35F;
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
    }

    // This method allows the entity to travel in a given direction
    @Override
    public void travel(@NotNull Vec3 direction) {
        // If the entity is controlled by the local player
        if (this.isControlledByLocalInstance()) {
            // Move the entity relative to its orientation and the direction vector
            this.moveRelative(getTarget() == null ? 0.04F : 0.05F, direction);

            // Move the entity according to its current velocity
            this.move(MoverType.SELF, this.getDeltaMovement());

            // If the entity is in water, reduce its velocity by 10%
            if (this.isInWater()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.9F));
                // If the entity is in lava, reduce its velocity by 40%
            } else if (this.isInLava()) {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.6F));
            }
            else
            {
                this.setDeltaMovement(this.getDeltaMovement().scale(0.95F));
            }
        }

        // Update the entity's animation based on its movement
        this.calculateEntityAnimation(false);
    }

    /** Events **/


    protected @NotNull net.minecraft.world.entity.ai.control.BodyRotationControl createBodyControl() {
        return new BodyRotationControl(this);
    }

    /** Animation **/
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);



    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this).transitionLength(5));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Sounds **/
    protected SoundEvent getAmbientSound() {
        return ModSounds.SCULK_MITE_IDLE.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.SCULK_MITE_DEATH.get();
    }

    protected SoundEvent getHurtSound(DamageSource p_29795_) {
        return ModSounds.SCULK_MITE_HURT.get();
    }

    abstract static class MoveTargetGoal extends Goal {
        public MoveTargetGoal() {
            this.setFlags(EnumSet.of(Flag.MOVE));
        }
    }

    class SweepAndBurrowAttackGoal extends MoveTargetGoal {

        private long lastTimeOfAttack = 0;
        private final int COOLDOWN = TickUnits.convertSecondsToTicks(0);

        public boolean canUse() {

            if(level().getGameTime() - lastTimeOfAttack < COOLDOWN)
            {
                return false;
            }

            return SculkStingerEntity.this.getTarget() != null;
        }

        public boolean canContinueToUse()
        {
            LivingEntity target = SculkStingerEntity.this.getTarget();
            if (target == null)
            {
                return false;
            }

            if (!target.isAlive())
            {
                return false;
            }

            return this.canUse();
        }

        public void stop() {
            SculkStingerEntity.this.setTarget(null);
        }

        public void tick() {
            LivingEntity target = SculkStingerEntity.this.getTarget();
            boolean isPhantomNull = target == null;

            if(isPhantomNull)
            {
                return;
            }

            SculkStingerEntity.this.getNavigation().moveTo(target, 1.0D);
            float attackReach = 0.5F;
            //boolean doesIntersectTarget = EntityAlgorithms.getDistanceBetweenEntities(target, SculkStingerEntity.this) <= attackReach;
            boolean doesIntersectTarget = getBoundingBox().inflate(attackReach).intersects(target.getBoundingBox());
            boolean isHealthBelow50Percent = target.getHealth() / target.getMaxHealth() <= 0.5F;

            if (doesIntersectTarget)
            {
                SculkStingerEntity.this.doHurtTarget(target);

                if(isHealthBelow50Percent && !target.hasEffect(SculkMiteEntity.INFECT_EFFECT))
                {
                    if(DifficultyUtil.isCurrentDifficultyEasy())
                    {
                        EntityAlgorithms.applyEffectToTarget(target, SculkMiteEntity.INFECT_EFFECT, TickUnits.convertSecondsToTicks(60), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                    }
                    else if(DifficultyUtil.isCurrentDifficultyNormal())
                    {
                        EntityAlgorithms.applyEffectToTarget(target, SculkMiteEntity.INFECT_EFFECT, TickUnits.convertSecondsToTicks(40), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                    }
                    else if(DifficultyUtil.isCurrentDifficultyHard())
                    {
                        EntityAlgorithms.applyEffectToTarget(target, SculkMiteEntity.INFECT_EFFECT, TickUnits.convertSecondsToTicks(30), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                    }
                }
                lastTimeOfAttack = level().getGameTime();
                return;
            }


            if (SculkStingerEntity.this.horizontalCollision || SculkStingerEntity.this.hurtTime > 0) {
                lastTimeOfAttack = level().getGameTime();
            }
        }
    }

    class BodyRotationControl extends net.minecraft.world.entity.ai.control.BodyRotationControl {
        public BodyRotationControl(Mob p_33216_) {
            super(p_33216_);
        }

        public void clientTick() {
            SculkStingerEntity.this.yHeadRot = SculkStingerEntity.this.yBodyRot;
            SculkStingerEntity.this.yBodyRot = SculkStingerEntity.this.getYRot();
        }
    }


}
