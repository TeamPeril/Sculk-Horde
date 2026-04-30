package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.*;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.common.entity.projectile.AcidBlobProjectileEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.animal.WaterAnimal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
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

import javax.annotation.Nullable;
import java.util.EnumSet;

public class SculkGuardianEntity extends WaterAnimal implements GeoEntity, ISculkSmartEntity {

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

    public static final float MAX_HEALTH = 8F;
    public static final float ATTACK_DAMAGE = 5F;
    public static final float FOLLOW_RANGE = 16F;
    public static final float MOVEMENT_SPEED = 0.20F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultSwimmerRangedCombat.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkGuardianEntity(EntityType<? extends SculkGuardianEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.moveControl = new FishMoveControl(this);
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
        return SoundEvents.GUARDIAN_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.GUARDIAN_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource p_29795_) {
        return SoundEvents.GUARDIAN_HURT;
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

        this.goalSelector.addGoal(0, new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(10)));
        this.goalSelector.addGoal(0, new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(5)));
        //this.goalSelector.addGoal(1, new ChargeAttackGoal(this));
        this.goalSelector.addGoal(2, new SpitAcidBlobAttackGoal(this, FOLLOW_RANGE, 10, 10));
        this.goalSelector.addGoal(3, new SculkGuardianCombatNavigator(this, 32, 0));
        this.goalSelector.addGoal(4, new RandomSwimmingGoal(this, 1.0D, 10));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));


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
                        new TargetAttacker(this),
                        new SculkHordeTargetGoal<>(this)
                };
        return goals;
    }

    public void performRangedAttack(LivingEntity target) {
        AcidBlobProjectileEntity acid = new AcidBlobProjectileEntity(target.level(), this, 4);
        acid.setPos(this.position());
        double d0 = target.getX() - this.getX();
        double d1 = target.getY(0.3333333333333333D) - acid.getY();
        double d2 = target.getZ() - this.getZ();
        double d3 = Math.sqrt(d0 * d0 + d2 * d2);
        acid.shoot(d0, d1 + d3 * (double)0.2F, d2, 1.6F, (float)(14 - this.level().getDifficulty().getId() * 4));
        this.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 1.0F / (this.getRandom().nextFloat() * 0.4F + 0.8F));
        this.level().addFreshEntity(acid);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if(SculkHorde.isDebugMode())
        {
            player.displayClientMessage(Component.literal("Running Goals: " + goalSelector.getRunningGoals().toString()), false);
            player.displayClientMessage(Component.literal("\nTarget: " + getTarget()), false);

            if(getTarget() != null)
            {
                EntityAlgorithms.applyEffectToTarget(getTarget(), MobEffects.GLOWING, TickUnits.convertSecondsToTicks(10), 0);
            }

            return InteractionResult.PASS;
        }

        return InteractionResult.PASS;
    }

    protected PathNavigation createNavigation(Level p_28362_) {
        return new WaterBoundPathNavigation(this, p_28362_);
    }

    protected boolean closeToNextPos() {
        BlockPos blockpos = this.getNavigation().getTargetPos();
        return blockpos != null ? blockpos.closerToCenterThan(this.position(), 12.0D) : false;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        TARGET_PARAMETERS.updateTargets();
    }

    public void travel(Vec3 movementVector) {
        if(!this.isEffectiveAi()) { return; }

        if (this.isInWater())
        {
            this.moveRelative(MOVEMENT_SPEED, movementVector);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        }
        else
        {
            super.travel(movementVector);
        }
    }


    //Animation Stuff below
    private static final RawAnimation IDLE_ANIMATION = RawAnimation.begin().thenLoop("misc.idle");
    private static final RawAnimation SWIM_ANIMATION = RawAnimation.begin().thenLoop("move.swim");
    private static final RawAnimation STUCK_ANIMATION = RawAnimation.begin().thenLoop("stuck");

    private static  final String ATTACK_ANIMATION_ID = "shoot";
    private static  final String ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay(ATTACK_ANIMATION_ID);

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .triggerableAnim(ATTACK_ANIMATION_ID, ATTACK_ANIMATION);



    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                new AnimationController<>(this, "walk_cycle", 5, this::poseSwimCycle),
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
        );
    }

    protected PlayState poseSwimCycle(AnimationState<SculkGuardianEntity> state)
    {

        if(state.getAnimatable().level().getFluidState(state.getAnimatable().blockPosition()).isEmpty())
        {
            state.setAnimation(SWIM_ANIMATION);
        }
        else if(state.getAnimatable().getX() != state.getAnimatable().xOld || state.getAnimatable().getZ() != state.getAnimatable().zOld)
        {
            state.setAnimation(SWIM_ANIMATION);
        }
        else {
            state.setAnimation(IDLE_ANIMATION);
        }

        return PlayState.CONTINUE;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    private boolean isParticipatingInRaid = false;

    

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

    static class FishMoveControl extends MoveControl {
        private final SculkGuardianEntity fish;

        FishMoveControl(SculkGuardianEntity p_27501_) {
            super(p_27501_);
            this.fish = p_27501_;
        }

        public void tick() {
            if(fish.level().isClientSide()) { return; }

            if(!mob.isEyeInFluid(FluidTags.WATER))
            {
                fish.setDeltaMovement(fish.getDeltaMovement().add(0.0D, -0.001D, 0.0D));
            }

            else if (this.operation == Operation.MOVE_TO && !this.fish.getNavigation().isDone()) {
                float f = (float)(this.speedModifier * this.fish.getAttributeValue(Attributes.MOVEMENT_SPEED));
                this.fish.setSpeed(Mth.lerp(0.125F, this.fish.getSpeed(), f));
                double d0 = this.wantedX - this.fish.getX();
                double d1 = this.wantedY - this.fish.getY();
                double d2 = this.wantedZ - this.fish.getZ();
                if (d1 != 0.0D) {
                    double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                    this.fish.setDeltaMovement(this.fish.getDeltaMovement().add(0.0D, (double)this.fish.getSpeed() * (d1 / d3) * 0.1D, 0.0D));
                }

                if (d0 != 0.0D || d2 != 0.0D) {
                    float f1 = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                    this.fish.setYRot(this.rotlerp(this.fish.getYRot(), f1, 90.0F));
                    this.fish.yBodyRot = this.fish.getYRot();
                }

            } else {
                this.fish.setSpeed(0.0F);
            }
        }
    }


    protected class SpitAcidBlobAttackGoal extends CustomAttackGoal2
    {


        public SpitAcidBlobAttackGoal(Mob mob, float maxDistanceForAttackIn, long preAttackDelay, long postAttackDelay) {
            super(mob, maxDistanceForAttackIn, preAttackDelay, postAttackDelay);
        }

        @Override
        protected long getExecutionCooldown() {
            return TickUnits.convertSecondsToTicks(3);
        }

        @Override
        protected void playPreAttackAnimation() {
            triggerAnim(ATTACK_ANIMATION_CONTROLLER_ID, ATTACK_ANIMATION_ID);
        }

        @Override
        protected void doAttack() {
            performRangedAttack(getTarget());
            moveToNextState();
        }
    }

    public class RandomSwimmingGoal extends RandomStrollGoal {
        public RandomSwimmingGoal(PathfinderMob mob, double speedModifier, int interval) {
            super(mob, speedModifier, interval, false);
        }

        @Nullable
        protected Vec3 getPosition() {
            return BehaviorUtils.getRandomSwimmablePos(this.mob, 10, 7);
        }
    }

    public class SculkGuardianCombatNavigator extends Goal {
        private final SculkGuardianEntity mob;
        private double wantedX;
        private double wantedY;
        private double wantedZ;
        private final double speedModifier;
        private final float maxDistance;
        private final float minDistance;

        public SculkGuardianCombatNavigator(SculkGuardianEntity guardian, float maxDistance, float minDistance) {
            this.mob = guardian;
            this.speedModifier = 1;
            this.maxDistance = maxDistance;
            this.minDistance = minDistance;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse()
        {
            if (getTarget() == null)
            {
                return false;
            }

            float distanceToTarget = getTarget().distanceTo(this.mob);

            if (distanceToTarget < this.minDistance)
            {
                Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, (int) this.minDistance + 5, (int) (this.minDistance - distanceToTarget), getTarget().position());
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
            else if (distanceToTarget > this.maxDistance || !mob.getSensing().hasLineOfSight(getTarget()))
            {
                Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, (int) (distanceToTarget - this.maxDistance), 1, getTarget().position(), (double)((float)Math.PI / 2F));
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

            if(this.mob.getNavigation().isDone())
            {
                return false;
            }

            if(getTarget() == null || !getTarget().isAlive())
            {
                return false;
            }

            if(getTarget().distanceTo(this.mob) > this.maxDistance)
            {
                return false;
            }

            if(getTarget().distanceTo(this.mob) < this.minDistance)
            {
               return false;
            }

            return true;
        }

        public void stop() {
            setTarget(null);
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }
    }

}
