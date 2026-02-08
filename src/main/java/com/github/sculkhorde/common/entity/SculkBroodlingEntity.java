package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulPoisonProjectileAttackEntity;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.OpenDoorGoal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkBroodlingEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkBroodlingModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkBroodlingRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 10F;
    //The armor of the mob
    public static final float ARMOR = 0F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 4F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableMustReachTarget();
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected boolean isLeaping = false;
    protected long leapStartTime = 0;
    protected long MIN_LEAP_TIME = TickUnits.convertSecondsToTicks(1);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBroodlingEntity(EntityType<? extends SculkBroodlingEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkBroodlingEntity(Level level, BlockPos pos)
    {
        this(ModEntities.SCULK_BROODLING.get(), level);
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
                        new InvalidateTargetGoal(this),
                        //HurtByTargetGoal(mob)
                        new TargetAttacker(this),
                        new FocusSquadTarget(this),
                        new NearestLivingEntityTargetGoal<>(this, true, true)

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
        if (this.isLeaping)
        { // Set this flag in your Goal
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().add(0, -0.08, 0)); // Apply gravity manually
        } else {
            super.travel(m);
        }
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        Vec3 movementVector = this.getDeltaMovement();
        if (!this.onGround() && movementVector.y < 0.0D) {
            this.setDeltaMovement(movementVector.multiply(1.0D, 0.6D, 1.0D));
        }


        if(isLeaping && level().getGameTime() - leapStartTime > MIN_LEAP_TIME && onGround())
        {
            isLeaping = false;
        }


        if(SculkHorde.isDebugMode())
        {
            String customDebugName = "";
            for(WrappedGoal wrappedGoal : goalSelector.getRunningGoals().toList())
            {
                Goal goal = wrappedGoal.getGoal();
                if(goal instanceof IDebuggableGoal debugGoal)
                {
                    customDebugName += debugGoal.getGoalName().get();

                }
                else
                {
                    customDebugName += goal.getClass().getSimpleName();
                }
                customDebugName += " | ";
            }

            setCustomName(Component.literal(customDebugName));
        }

    }

    @Override
    public void makeStuckInBlock(BlockState blockState, @NotNull Vec3 p_33797_) {
        return;
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack", ATTACK_ANIMATION).transitionLength(5);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                //DefaultAnimations.genericWalkIdleController(this),
                //ATTACK_ANIMATION_CONTROLLER,
                //DefaultAnimations.genericLivingController(this)
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }



    protected SoundEvent getAmbientSound() {
        return SoundEvents.SPIDER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SPIDER_HURT;
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

    public class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(SculkBroodlingEntity.this, 1.0D, true, 10);
        }

        @Override
        public boolean canUse()
        {
            boolean canWeUse = ((ISculkSmartEntity)this.mob).getTargetParameters().isEntityValidTarget(this.mob.getTarget(), true);
            // If the mob is already targeting something valid, don't bother
            return canWeUse;
        }

        @Override
        public boolean canContinueToUse()
        {
            return canUse();
        }

        @Override
        protected int getAttackInterval() {
            return TickUnits.convertSecondsToTicks(0.5F);
        }

        @Override
        protected void triggerAnimation() {
            //((SculkBroodHatcherEntity)mob).triggerAnim("attack_controller", "attack");
        }

        @Override
        public void onTargetHurt(LivingEntity target) {
            super.onTargetHurt(target);

            if(DifficultyUtil.isCurrentDifficultyEasy())
            {
                EntityAlgorithms.applyEffectToTarget(target, ModMobEffects.ROOTED_EFFECT.get(), TickUnits.convertMinutesToTicks(3), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                EntityAlgorithms.applyEffectToTarget(target, MobEffects.POISON, TickUnits.convertSecondsToTicks(5), 0);
            }
            else if(DifficultyUtil.isCurrentDifficultyNormal())
            {
                EntityAlgorithms.applyEffectToTarget(target, ModMobEffects.ROOTED_EFFECT.get(), TickUnits.convertMinutesToTicks(2), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                EntityAlgorithms.applyEffectToTarget(target, MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0);
            }
            else if(DifficultyUtil.isCurrentDifficultyHard())
            {
                EntityAlgorithms.applyEffectToTarget(target, ModMobEffects.ROOTED_EFFECT.get(), TickUnits.convertMinutesToTicks(1), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                EntityAlgorithms.applyEffectToTarget(target, MobEffects.POISON, TickUnits.convertSecondsToTicks(15), 0);
            }
        }
    }

    public class LeapAwayAttackStep extends AttackStepGoal
    {
        /**
         * The destination the mob will attempt to leap to. Null when not yet calculated or when reset.
         */
        protected Vec3 leapDestination = null;

        /**
         * Whether the mob has already performed the leap during the current attack step.
         */
        protected boolean hasLeaped = false;

        /**
         * Ticks elapsed since the leap was performed. Used to time out the leap if it takes too long.
         */
        protected int ticksSinceLeap = 0;

        /**
         * The initial velocity computed to land exactly at the destination and time budget.
         */
        protected Vec3 initialLeapVelocity = null;

        /**
         * Planned number of ticks the leap should take.
         */
        protected int plannedFlightTicks = 0;

        // Distance constraints for candidate leap destinations (horizontal distance in blocks)
        protected static final double MIN_LEAP_DISTANCE = 3.0;
        protected static final double MAX_LEAP_DISTANCE = 8.0;

        // Emergency fallback flag: if we cannot find a valid precise leap, just jump far backwards
        protected boolean emergencyBackJump = false;

        /**
         * Constructs a new LeapAwayAttackStep for the provided mob.
         *
         * @param mob the mob that will perform the leap-away behaviour
         */
        public LeapAwayAttackStep(Mob mob) {
            super(mob);
        }

        /**
         * The amount of delay (in ticks) before the attack step begins.
         * This is a short pre-attack windup to allow the leap destination to be computed.
         *
         * @return pre-attack delay in ticks
         */
        @Override
        protected int getPreAttackDelay() {
            return TickUnits.convertSecondsToTicks(0.25F);
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
         * Helper to determine whether a candidate direction points away from the current target.
         *
         * This computes the dot product between the vector to the candidate position and the
         * vector to the target; if the dot product is <= 0 the candidate is at least 90° away
         * (i.e. not toward the target). A null target is treated as 'not toward'.
         */
        protected boolean isDirectionNotTowardTarget(Vec3 from, Vec3 to, LivingEntity target)
        {
            if(target == null)
            {
                return true;
            }
            Vec3 mobToTarget = target.position().subtract(from).normalize();
            Vec3 mobToCandidate = to.subtract(from).normalize();
            double dot = mobToCandidate.dot(mobToTarget);
            return dot <= 0.0;
        }

        /**
         * Generic line-of-sight check between two positions using block collision.
         */
        protected boolean canSeeFromTo(Vec3 from, Vec3 to)
        {
            net.minecraft.world.phys.HitResult hit = mob.level().clip(new net.minecraft.world.level.ClipContext(from, to, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mob));
            return hit.getType() == net.minecraft.world.phys.HitResult.Type.MISS;
        }

        /**
         * Ensures that from a hypothetical landing position, the broodling can still see its current target.
         * Uses eye heights for realism.
         */
        protected boolean canSeeTargetFromPosition(Vec3 fromPosition)
        {
            LivingEntity target = getTarget();
            if(target == null)
            {
                return false;
            }
            Vec3 eyeFrom = new Vec3(fromPosition.x, fromPosition.y + mob.getEyeHeight(), fromPosition.z);
            Vec3 eyeTo = target.getEyePosition();
            return canSeeFromTo(eyeFrom, eyeTo);
        }

        /**
         * Compute initial per-tick velocity to land at end after exactly T ticks, under constant gravity.
         * Ignores drag for simplicity.
         */
        protected Vec3 computeBallisticVelocity(Vec3 start, Vec3 end, int ticks)
        {
            if(ticks <= 0)
            {
                return null;
            }
            double g = 0.08; // Minecraft gravity per tick
            Vec3 d = end.subtract(start);
            double vx = d.x / ticks;
            double vz = d.z / ticks;
            double vy = (d.y + 0.5 * g * ticks * ticks) / ticks;
            return new Vec3(vx, vy, vz);
        }

        /**
         * Simulates the trajectory and checks for collisions with blocks along the path.
         * Uses simple kinematics without drag for prediction and raycasts between successive points.
         */
        protected boolean isTrajectoryClear(Vec3 start, Vec3 initialVelocity, int ticks)
        {
            if(initialVelocity == null || ticks <= 0)
            {
                return false;
            }
            double g = 0.08;
            Vec3 prev = start;
            for(int t = 1; t <= ticks; t++)
            {
                // position after t ticks: p = start + v0 * t + 0.5 * a * t^2, with a = (0, -g, 0)
                Vec3 pos = new Vec3(
                        start.x + initialVelocity.x * t,
                        start.y + initialVelocity.y * t - 0.5 * g * t * t,
                        start.z + initialVelocity.z * t
                );
                net.minecraft.world.phys.HitResult hit = mob.level().clip(new net.minecraft.world.level.ClipContext(prev, pos, net.minecraft.world.level.ClipContext.Block.COLLIDER, net.minecraft.world.level.ClipContext.Fluid.NONE, mob));
                if(hit.getType() != net.minecraft.world.phys.HitResult.Type.MISS)
                {
                    return false;
                }
                prev = pos;
            }
            return true;
        }

        /**
         * Try various flight durations and choose one that yields a clear arc. Stores results if successful.
         */
        protected boolean tryComputeClearBallisticTo(Vec3 destination)
        {
            Vec3 start = mob.position();
            // Slightly raise start to the eye height to avoid clipping into ground immediately
            start = new Vec3(start.x, start.y + 0.01, start.z);
            int[] candidateDurations = new int[]{10, 12, 14, 16, 18, 20};
            double maxHorizontalSpeed = 1.6; // reasonable cap for broodling leap
            double maxVerticalSpeed = 1.2;
            for(int T : candidateDurations)
            {
                Vec3 v0 = computeBallisticVelocity(start, destination, T);
                if(v0 == null)
                {
                    continue;
                }
                double horizSpeed = Math.sqrt(v0.x * v0.x + v0.z * v0.z);
                if(horizSpeed > maxHorizontalSpeed || Math.abs(v0.y) > maxVerticalSpeed)
                {
                    continue; // too fast for our mob
                }
                if(isTrajectoryClear(start, v0, T))
                {
                    this.initialLeapVelocity = v0;
                    this.plannedFlightTicks = T;
                    return true;
                }
            }
            return false;
        }

        /**
         * Attempts to find a reachable destination that is away from the current target.
         *
         * The method picks several radii and angular offsets to sample candidate positions
         * around the mob in the horizontal plane. It prefers positions that are not toward the
         * target and that are reachable by the mob's navigation system. If none of the sampled
         * candidates are reachable, it falls back to a lateral perpendicular direction.
         *
         * @return a reachable Vec3 destination (same Y as the mob) or null if none found
         */
        protected Vec3 findReachableDestinationAwayFromTarget()
        {
            LivingEntity target = getTarget();
            if(target == null)
            {
                return null;
            }

            Vec3 mobPos = mob.position();
            Vec3 awayDir = mobPos.subtract(target.position());
            if(awayDir.lengthSqr() < 1.0E-6)
            {
                awayDir = new Vec3(1,0,0);
            }
            awayDir = new Vec3(awayDir.x, 0, awayDir.z).normalize();

            double[] radii = new double[]{MIN_LEAP_DISTANCE, (MIN_LEAP_DISTANCE + MAX_LEAP_DISTANCE) * 0.5, MAX_LEAP_DISTANCE};
            double[] angleOffsets = new double[]{0, Math.toRadians(30), Math.toRadians(-30), Math.toRadians(60), Math.toRadians(-60), Math.toRadians(90), Math.toRadians(-90)};

            for(double r : radii)
            {
                for(double off : angleOffsets)
                {
                    double cos = Math.cos(off);
                    double sin = Math.sin(off);
                    Vec3 dir = new Vec3(
                            awayDir.x * cos - awayDir.z * sin,
                            0,
                            awayDir.x * sin + awayDir.z * cos
                    ).normalize();

                    Vec3 candidate = mobPos.add(dir.scale(r));

                    // Keep Y roughly the same as mob; navigation will handle small differences
                    candidate = new Vec3(candidate.x, mobPos.y, candidate.z);

                    // Enforce horizontal distance within [MIN, MAX]
                    double horizDist = candidate.subtract(new Vec3(mobPos.x, candidate.y, mobPos.z)).horizontalDistance();
                    if(horizDist < MIN_LEAP_DISTANCE - 1e-3 || horizDist > MAX_LEAP_DISTANCE + 1e-3)
                    {
                        continue;
                    }

                    if(!isDirectionNotTowardTarget(mobPos, candidate, target))
                    {
                        continue;
                    }

                    // Check reachability on foot (path exists)
                    if(mob.getNavigation().createPath(BlockPos.containing(candidate), 1) == null)
                    {
                        continue;
                    }

                    // Ensure from candidate the target remains visible (line of sight)
                    if(!canSeeTargetFromPosition(candidate))
                    {
                        continue;
                    }

                    // Try to compute a ballistic arc and ensure it is unobstructed
                    if(tryComputeClearBallisticTo(candidate))
                    {
                        return candidate;
                    }
                }
            }

            // Fallback: pick a lateral perpendicular direction if available
            Vec3 perp = new Vec3(-awayDir.z, 0, awayDir.x).normalize();
            double fallbackR = Math.max(MIN_LEAP_DISTANCE, Math.min(MAX_LEAP_DISTANCE, 4.0));
            Vec3 fallback = mobPos.add(perp.scale(fallbackR));
            Vec3 fallbackFlat = new Vec3(fallback.x, mobPos.y, fallback.z);
            if(mob.getNavigation().createPath(BlockPos.containing(fallbackFlat), 1) != null
                    && canSeeTargetFromPosition(fallbackFlat)
                    && tryComputeClearBallisticTo(fallbackFlat))
            {
                return fallbackFlat;
            }

            return null;
        }

        /**
         * Called each AI tick before the attack begins. Ensures a leap destination has been computed.
         */
        @Override
        public void doPreAttackTick()
        {
            super.doPreAttackTick();
            isLeaping = true;
            leapStartTime = level().getGameTime();
            if(leapDestination == null)
            {
                leapDestination = findReachableDestinationAwayFromTarget();
                if(leapDestination == null)
                {
                    // Could not find a valid precise leap position, enable emergency back jump
                    emergencyBackJump = true;
                }
            }
        }

        /**
         * Executes the leap behaviour. If the mob hasn't leaped yet it will set the mob's motion
         * towards the precomputed destination. After leaping it waits for the mob to land or a
         * timeout to elapse before marking the attack tick as complete.
         */
        @Override
        protected void doAttackTick() {
            // Initiate leap if we have a destination and computed velocity
            if(!hasLeaped)
            {
                if(leapDestination == null || initialLeapVelocity == null || plannedFlightTicks <= 0)
                {
                    // Could not satisfy precise leap checks; perform an emergency backward jump
                    emergencyBackJump = true;

                    Vec3 mobPos = mob.position();
                    LivingEntity target = getTarget();
                    Vec3 backDir;
                    if(target != null)
                    {
                        Vec3 away = mobPos.subtract(target.position());
                        if(away.lengthSqr() < 1.0E-6)
                        {
                            away = new Vec3(1, 0, 0);
                        }
                        backDir = new Vec3(away.x, 0, away.z).normalize();
                    }
                    else
                    {
                        // Fallback to opposite of look direction if no target
                        Vec3 look = mob.getLookAngle();
                        backDir = new Vec3(-look.x, 0, -look.z);
                        if(backDir.lengthSqr() < 1.0E-6)
                        {
                            backDir = new Vec3(1, 0, 0);
                        }
                        backDir = backDir.normalize();
                    }

                    double horizontalSpeed = Math.min(1.8, Math.max(1.1, (MAX_LEAP_DISTANCE - MIN_LEAP_DISTANCE) * 0.25 + 1.0));
                    double upward = 0.6;
                    Vec3 delta = backDir.scale(horizontalSpeed).add(0, upward, 0);
                    mob.setDeltaMovement(delta);
                    hasLeaped = true;
                    ticksSinceLeap = 0;
                    return;
                }
                // Apply the computed initial velocity to exactly land on the destination (ignoring drag)
                mob.setDeltaMovement(initialLeapVelocity);
                hasLeaped = true;
                ticksSinceLeap = 0;
            }
            else
            {
                ticksSinceLeap++;
                // Finish if we've landed, exceeded planned flight time (if any), or safety timeout
                int planned = plannedFlightTicks > 0 ? plannedFlightTicks : TickUnits.convertSecondsToTicks(3);
                if(mob.onGround() || ticksSinceLeap >= planned || ticksSinceLeap > TickUnits.convertSecondsToTicks(3))
                {
                    setAttackTickComplete();
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
            leapDestination = null;
            initialLeapVelocity = null;
            plannedFlightTicks = 0;
            ticksSinceLeap = 0;
            emergencyBackJump = false;
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
            //getReaper().triggerAnim(SculkSoulReaperEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, SculkSoulReaperEntity.FLOOR_SPEARS_SPELL_USE_ID);
        }

        @Override
        public void stop() {
            super.stop();
            projectilesFired = 0;
        }

        @Override
        protected void doPreAttackTick() {
            super.doPreAttackTick();
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
                SoulPoisonProjectileAttackEntity projectile = new SoulPoisonProjectileAttackEntity(level(), SculkBroodlingEntity.this, 1);

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

            setAttackTickComplete();
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

            float MIN_DISTANCE = 15;

            if(getTarget() == null || (EntityAlgorithms.getDistanceBetweenEntities(mob, getTarget()) < MIN_DISTANCE && getSensing().hasLineOfSight(getTarget())))
            {
                setAttackTickComplete();
                navigation.stop();
                return;
            }

            navigation.moveTo(getTarget(), 1.0F);
        }
    }
}
