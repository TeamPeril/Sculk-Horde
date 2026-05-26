package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.client.model.enitity.SculkBroodSpitterModel;
import com.github.sculkhorde.client.renderer.entity.SculkBroodSpitterRenderer;
import com.github.sculkhorde.common.entity.components.DefaultTargetParameters;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.components.TargetRetention;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.common.entity.projectile.SmallBroodAcidProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkBroodSpitterEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link SculkBroodSpitterModel}<br>
     * Added {@link SculkBroodSpitterRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 15F;
    //The armor of the mob
    public static final float ARMOR = 0F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 4F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 40F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    // Controls what types of entities this mob can target
    private final TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultGroundRangedCombat.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected boolean isLeaping = false;
    protected long leapStartTime = 0;
    protected long MIN_LEAP_TIME = TickUnits.convertSecondsToTicks(1);
    protected boolean isFlying = false;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBroodSpitterEntity(EntityType<? extends SculkBroodSpitterEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkBroodSpitterEntity(Level level, BlockPos pos)
    {
        this(ModEntities.SCULK_BROOD_SPITTER.get(), level);
        moveTo(pos.getCenter());
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

    public boolean isIdle() {
        return getTarget() == null;
    }

    @Override
    public void checkDespawn() {}

    private boolean isParticipatingInRaid = false;

    @Override
    protected int calculateFallDamage(float p_21237_, float p_21238_) {
        return 0;
    }

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
                        new DespawnWhenIdle(this, TickUnits.convertMinutesToTicks(10)),
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new SquadLogicGoal(this),
                        new FollowSquadLeader(this),
                        new PathFindToRaidLocation<>(this),
                        //new LeapAtTargetGoal(this, 0.5F),
                        //new AttackGoal(),
                        new AttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1),
                                new GetInRangeAttackStep(this),
                                new ShootWebAttackStep(this),
                                new LeapAwayAttackStep(this)
                        ),
                        new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true),
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
    protected @NotNull PathNavigation createNavigation(@NotNull Level p_33802_) {
        return new WallClimberNavigation(this, p_33802_);
    }

    // Inside your Mob class
    @Override
    public void travel(Vec3 m) {
        /*
        if (this.isLeaping)
        { // Set this flag in your Goal
            this.move(MoverType.SELF, this.getDeltaMovement());
            //this.setDeltaMovement(this.getDeltaMovement().add(0, -0.15, 0)); // Apply gravity manually
        } else {
            super.travel(m);
        }

         */
        super.travel(m);
    }

    @Override
    public void tick() {
        super.tick();

        isFlying = EntityAlgorithms.getEntityDistanceFromGround(this) > 0.5;
        Vec3 movementVector = this.getDeltaMovement();

        if(level().isClientSide)
        {
            SoundUtil.requestBroodFlightSound(this, isFlying);
        }
        else
        {
            if(isFlying || isLeaping)
            {
                this.setDeltaMovement(movementVector.multiply(1.09D, 1D, 1.09D));
            }

        }
        /*
        if (!this.onGround() && movementVector.y < 0.0D)
        {
            this.setDeltaMovement(movementVector.multiply(1.0D, 0.6D, 1.0D));
        }
        */

        if(isLeaping && level().getGameTime() - leapStartTime > MIN_LEAP_TIME && !isFlying)
        {
            isLeaping = false;
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        TARGET_PARAMETERS.updateTargets();
    }

    @Override
    public void makeStuckInBlock(BlockState blockState, @NotNull Vec3 p_33797_) {
        return;
    }

    @Override
    public float getStepHeight() {
        return 1.1F;
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack", ATTACK_ANIMATION).transitionLength(5);

    protected static final String FLIGHT_ANIMATION_ID = "move.fly";
    private static final RawAnimation FLIGHT_ANIMATION = RawAnimation.begin().thenLoop(FLIGHT_ANIMATION_ID);

    protected PlayState poseWings(AnimationState<SculkBroodSpitterEntity> state) {
        if (isFlying)
        {
            state.setAnimation(FLIGHT_ANIMATION);
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
    }

    private final AnimationController FLIGHT_ANIMATION_CONTROLLER = new AnimationController<>(this, "flight_animation_controller", 0, this::poseWings);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkRunIdleController(this),
                FLIGHT_ANIMATION_CONTROLLER,
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    protected SoundEvent getAmbientSound() {
        return ModSounds.SCULK_BROOD_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.SCULK_BROOD_HURT.get();
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SPIDER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SPIDER_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    @Override
    public float getEyeHeight(Pose p_20237_) {
        return getBbHeight();
    }


    public class LeapAwayAttackStep extends AttackStepGoal
    {
        /**
         * Whether the mob has already performed the leap during the current attack step.
         */
        protected boolean hasLeaped = false;

        /**
         * Ticks elapsed since the leap was performed. Used to time out the leap if it takes too long.
         */
        protected int ticksSinceLeap = 0;

        /**
         * Constructs a new LeapAwayAttackStep for the provided mob.
         *
         * @param mob the mob that will perform the leap-away behaviour
         */
        public LeapAwayAttackStep(Mob mob) {
            super(mob);
        }

        @Override
        public boolean canUse() {
            if(!super.canUse()) return false;
            LivingEntity target = getTarget();
            if(target == null) return false;

            // Only leap away if we are actually close to the target
            return mob.distanceTo(target) < 10.0;
        }

        /**
         * The amount of delay (in ticks) before the attack step begins.
         * This is a short pre-attack windup.
         *
         * @return pre-attack delay in ticks
         */
        @Override
        protected int getPreAttackDelay() {
            return TickUnits.convertSecondsToTicks(0);
        }

        /**
         * The amount of delay (in ticks) after the attack completes before the AI can proceed.
         *
         * @return post-attack delay in ticks
         */
        @Override
        protected int getPostAttackDelay() {
            return TickUnits.convertSecondsToTicks(0F);
        }

        /**
         * Called each AI tick before the attack begins.
         */
        @Override
        public void doPreAttackTick()
        {
            super.doPreAttackTick();
        }

        /**
         * Executes the leap behaviour based on VoltLeapGoal.
         */
        @Override
        protected void doAttackTick() {
            if(!hasLeaped)
            {
                LivingEntity target = getTarget();
                if (target != null) {
                    isLeaping = true;
                    leapStartTime = level().getGameTime();

                    // Logic adapted from VoltLeapGoal
                    // We calculate the angle to the target to leap away from it
                    double dx = mob.getX() - target.getX();
                    double dz = mob.getZ() - target.getZ();
                    float targetAngle = (float) (Math.atan2(dz, dx) * (180 / Math.PI));

                    float leapYaw = (float) Math.toRadians(targetAngle + 90 + mob.getRandom().nextFloat() * 150 - 75);
                    float speed = 1F;

                    SoundUtil.playHostileSoundInLevel(level(), blockPosition(), ModSounds.SCULK_BROOD_FLY_START.get());
                    Vec3 movement = mob.getDeltaMovement().add(speed * Math.cos(leapYaw), 0, speed * Math.sin(leapYaw));
                    //mob.setPose(Pose.LONG_JUMPING);
                    mob.setDeltaMovement(movement.x, 0.9, movement.z);
                    navigation.stop();

                    hasLeaped = true;
                    ticksSinceLeap = 0;
                }
                else
                {
                    setAttackTickComplete();
                }
            }
            else
            {
                ticksSinceLeap++;
                // Finish if we've landed or safety timeout
                if(mob.onGround() || ticksSinceLeap > TickUnits.convertSecondsToTicks(3))
                {
                    setPostAttack(true);
                }
            }
        }

        /**
         * Resets internal state when the attack step stops so the next execution starts fresh.
         */
        @Override
        public void stop() {
            super.stop();
            hasLeaped = false;
            ticksSinceLeap = 0;
        }
    }

    public class ShootWebAttackStep extends AttackStepGoal
    {
        protected int ATTACK_ANIMATION_DELAY = TickUnits.convertSecondsToTicks(0.5F);
        protected int projectilesFired = 0;

        public ShootWebAttackStep(Mob mob) {
            super(mob);
        }

        public int getProjectileAmount()
        {
            return 3;
        }

        @Override
        protected int getPreAttackDelay() {
            return ATTACK_ANIMATION_DELAY;
        }

        @Override
        protected int getPostAttackDelay() {
            return 0;
        }

        @Override
        protected void playPreAttackAnimation()
        {
            //getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.FLOOR_SPEARS_SPELL_USE_ID);
        }

        @Override
        public void stop() {
            super.stop();
            projectilesFired = 0;
        }

        @Override
        protected void doPreAttackTick() {
            super.doPreAttackTick();
            navigation.stop();
            mob.getNavigation().moveTo((double)0, 0, 0, 0);
            navigation.stop();
            mob.setDeltaMovement(new Vec3(0, mob.getDeltaMovement().y, 0));
        }

        @Override
        protected void doAttackTick() {
            super.doAttackTick();

            if(getTarget() == null)
            {
                setAttackTickComplete();
                return;
            }

            while(projectilesFired < getProjectileAmount())
            {
                SmallBroodAcidProjectileEntity projectile = new SmallBroodAcidProjectileEntity(level(), SculkBroodSpitterEntity.this, 4);

                projectile.setPos(mob.position().add(0, mob.getEyeHeight() - projectile.getBoundingBox().getYsize() * .5f, 0));

                double spawnPosX = mob.getX() + mob.getRandom().nextFloat();
                double spawnPosY = mob.getY() + mob.getEyeHeight() + mob.getRandom().nextFloat();
                double spawnPosZ = mob.getZ() + mob.getRandom().nextFloat();

                double targetPosX = mob.getTarget().getX() - spawnPosX  + + mob.getRandom().nextFloat();
                double targetPosY = mob.getTarget().getY(1) - spawnPosY + + mob.getRandom().nextFloat();
                double targetPosZ = mob.getTarget().getZ() - spawnPosZ + + mob.getRandom().nextFloat();

                // Create a vector for the direction
                Vec3 direction = new Vec3(targetPosX, targetPosY, targetPosZ).normalize();

                // Shoot the projectile in the direction vector
                projectile.shoot(direction);

                mob.playSound(SoundEvents.LLAMA_SPIT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
                mob.level().addFreshEntity(projectile);
                projectilesFired++;
            }

            setPostAttack(true);
        }
    }

    public class GetInRangeAttackStep extends AttackStepGoal
    {

        public GetInRangeAttackStep(Mob mob) {
            super(mob);
        }

        @Override
        protected int getPreAttackDelay() {
            return 0;
        }

        @Override
        protected int getPostAttackDelay() {
            return 0;
        }

        @Override
        protected void doAttackTick() {
            super.doAttackTick();

            float MIN_DISTANCE = 10;

            if(getTarget() == null || (EntityAlgorithms.getDistanceBetweenEntities(mob, getTarget()) < MIN_DISTANCE && getSensing().hasLineOfSight(getTarget())))
            {
                setPostAttack(true);
                navigation.stop();
                return;
            }

            navigation.moveTo(getTarget(), 1.0F);
        }

        @Override
        public void stop() {
            super.stop();
            navigation.stop();
        }
    }
}
