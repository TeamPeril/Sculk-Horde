package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomSwimmingGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.animal.Salmon;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkSquidEntity extends WaterAnimal implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkMite.java<br>
     * Added client/model/entity/ SculkMiteModel.java<br>
     * Added client/renderer/entity/ SculkMiteRenderer.java
     */

    public static final float MAX_HEALTH = 5F;
    public static final float ATTACK_DAMAGE = 1F;
    public static final float FOLLOW_RANGE = 30F;
    public static final float MOVEMENT_SPEED = 0.20F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles().enableMustReachTarget().enableTargetSwimmers();
    private SquadHandler squad = new SquadHandler(this);
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public float xBodyRot;
    public float xBodyRotO;
    public float zBodyRot;
    public float zBodyRotO;
    public float rotateSpeed;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSquidEntity(EntityType<? extends SculkSquidEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.ATTACK_DAMAGE, ATTACK_DAMAGE)
                .add(Attributes.ATTACK_KNOCKBACK, 0.1)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED);
    }

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SALMON_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SALMON_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource p_29795_) {
        return SoundEvents.SALMON_HURT;
    }

    /**
     * @return if this entity may not naturally despawn.
     */
    @Override
    public boolean isPersistenceRequired() {
        return true;
    }


    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {
        super.registerGoals();

        this.goalSelector.addGoal(0, new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(2)));
        this.goalSelector.addGoal(0, new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(1)));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0D, 10));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(6, new MeleeAttackGoal(this, (double)1F, true));
        //this.goalSelector.addGoal(1, new SculkSalmonInfectGoal(this, 1.0D, true));

        Goal[] targetSelectorPayload = targetSelectorPayload();
        for(int priority = 0; priority < targetSelectorPayload.length; priority++)
        {
            this.targetSelector.addGoal(priority, targetSelectorPayload[priority]);
        }
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
                        new InvalidateTargetGoal(this),
                        new TargetAttacker(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)
                };
        return goals;
    }

    protected PathNavigation createNavigation(Level p_28362_) {
        return new WaterBoundPathNavigation(this, p_28362_);
    }

    protected boolean closeToNextPos() {
        BlockPos blockpos = this.getNavigation().getTargetPos();
        return blockpos != null ? blockpos.closerToCenterThan(this.position(), 12.0D) : false;
    }

    public void aiStep() {
        super.aiStep(); // Call the base class method to perform basic AI tasks

        // Update body rotation variables
        this.xBodyRotO = this.xBodyRot; // Store the old horizontal body rotation
        this.zBodyRotO = this.zBodyRot; // Store the old vertical body rotation

        // The following tentacle-related code has been removed as per your request

        // Check if the entity is in water or a bubble column
        if (this.isInWaterOrBubble()) {
            // Calculate the new body rotation based on the movement vector
            Vec3 vec3 = this.getDeltaMovement();
            double horizontalDistance = vec3.horizontalDistance(); // Get the horizontal distance of the movement vector

            // Update the horizontal body rotation (y-axis rotation)
            this.yBodyRot += (-((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI) - this.yBodyRot) * 0.1F;
            this.setYRot(this.yBodyRot); // Set the entity's rotation to the new calculated value

            // Update the vertical body rotation (z-axis rotation)
            this.zBodyRot += (float)Math.PI * this.rotateSpeed * 1.5F;

            // Update the pitch of the body (x-axis rotation)
            this.xBodyRot += (-((float)Mth.atan2(horizontalDistance, vec3.y)) * (180F / (float)Math.PI) - this.xBodyRot) * 0.1F;
        } else {
            // If the entity is not in water, adjust the pitch of the body to be more downward
            this.xBodyRot += (-90.0F - this.xBodyRot) * 0.02F;
        }
    }


    public void travel(Vec3 p_28383_) {
        if (this.isEffectiveAi() && this.isInWater())
        {
            this.moveRelative(MOVEMENT_SPEED, p_28383_);
            this.move(MoverType.SELF, this.getDeltaMovement());

            Vec3 normalizedDelta = this.getDeltaMovement().normalize();
            Vec3 cappedDelta = normalizedDelta.scale(Math.min(MOVEMENT_SPEED, this.getDeltaMovement().length()));

            this.setDeltaMovement(cappedDelta);
            if (this.getTarget() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0.0D, -0.005D, 0.0D));
            }
        } else {
            super.travel(p_28383_);
        }

    }


    //Animation Stuff below
    //private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("misc.idle");
    //private static final RawAnimation LAND_ANIMATION = RawAnimation.begin().thenLoop("misc.land");


    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "walk_cycle", 5, this::poseWalkCycle)
        );
    }

    protected PlayState poseWalkCycle(AnimationState<SculkSquidEntity> state)
    {

        if(state.getAnimatable().level().getFluidState(state.getAnimatable().blockPosition()).is(Fluids.WATER))
        {
            //state.setAnimation(SWIM_ANIMATION);
        }
        else
        {
            //state.setAnimation(LAND_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean isParticipatingInRaid = false;

    @Override
    public SquadHandler getSquad() {
        return squad;
    }

    @Override
    public boolean isParticipatingInRaid() {
        return false;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
        this.isParticipatingInRaid = isParticipatingInRaidIn;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public boolean dampensVibrations() {
        return true;
    }

}
