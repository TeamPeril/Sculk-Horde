package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.ImprovedFlyingNavigator;
import com.github.sculkhorde.common.entity.goal.DespawnAfterTime;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.SculkPhantomGoToAnchor;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
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

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;

public class SculkPhantomEntity extends FlyingMob implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added client/model/entity/ SculkPhantomModel.java<br>
     * Added client/renderer/entity/ SculkPhantomRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 15F;
    //The armor of the mob
    public static final float ARMOR = 1F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 5F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.35F;

    // Controls what types of entities this mob can target
    protected final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles().ignoreTargetBelow50PercentHealth().disableTargetingEntitiesInWater();

    public AttackPhase attackPhase = AttackPhase.CIRCLE;
    protected BlockPos anchorPoint = BlockPos.ZERO;
    public static final int TICKS_PER_FLAP = Mth.ceil(24.166098F);
    Vec3 moveTargetPoint = new Vec3(anchorPoint.getX(), anchorPoint.getY(), anchorPoint.getZ());

    Vec3 spawnPoint = null;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkPhantomEntity(EntityType<? extends SculkPhantomEntity> type, Level worldIn)
    {
        super(type, worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
        //this.moveControl = new PhantomMoveControl(this);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        //this.lookControl = new PhantomLookControl(this);
    }

    public static void spawnPhantom(Level worldIn, BlockPos spawnPos)
    {
        SculkPhantomEntity phantom = ModEntities.SCULK_PHANTOM.get().create(worldIn);
        assert phantom != null;
        phantom.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        phantom.spawnPoint = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        worldIn.addFreshEntity(phantom);
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
                .add(Attributes.FLYING_SPEED, 3F)
                .add(net.minecraftforge.common.ForgeMod.ENTITY_GRAVITY.get(), 0.0);
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
                //new DespawnAfterTime(this, TickUnits.convertMinutesToTicks(15)),
                new FallToGroundAfterTime(this, TickUnits.convertMinutesToTicks(15)),
                new FallToTheGroundIfMobsUnder(),
                //new AttackStrategyGoal(),
                new SweepAttackGoal(),
                new selectRandomLocationToVisit(),
                //new CircleAroundAnchorGoal(),

                new SculkPhantomGoToAnchor(this),


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
                new InvalidateTargetGoal(this),
                new NearestLivingEntityTargetGoal<>(this, true, true)
        };
    }

    protected PathNavigation createNavigation(Level level) {
        ImprovedFlyingNavigator flyingpathnavigation = new ImprovedFlyingNavigator(this, level);
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(true);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    public enum AttackPhase {
        CIRCLE,
        SWOOP,
        INFECT
    }
    /** Getters and Setters **/

    public Vec3 getAnchorPoint() {
        return this.anchorPoint.getCenter();
    }

    @Override
    public SquadHandler getSquad() {
        return null;
    }

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
    public void checkDespawn() {}

    protected void checkFallDamage(double p_29370_, boolean p_29371_, BlockState p_29372_, BlockPos p_29373_) {
    }

    @Override
    public boolean isPersistenceRequired() {
        return true;
    }

    public boolean dampensVibrations() {
        return true;
    }

    public boolean isFlapping() {
        return (this.getUniqueFlapTickOffset() + this.tickCount) % TICKS_PER_FLAP == 0;
    }

    protected float getStandingEyeHeight(@NotNull Pose p_33136_, EntityDimensions p_33137_) {
        return p_33137_.height * 0.35F;
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
    }

    public static Vec3 clamp(Vec3 vec, double min, double max) {
        double length = vec.length();
        vec = vec.normalize();
        if (length < min) {
            return vec.scale(min);
        } else if (length > max) {
            return vec.scale(max);
        }
        return vec;
    }

    // This method allows the entity to travel in a given direction
    @Override
    public void travel(Vec3 direction) {
        // If the entity is controlled by the local player
        if (this.isControlledByLocalInstance()) {
            // Move the entity relative to its orientation and the direction vector
            this.moveRelative(getTarget() == null ? 0.04F : 0.05F, direction);

            // Move the entity according to its current velocity
            this.move(MoverType.SELF, this.getDeltaMovement());

            // If the entity is in water, reduce its velocity by 10%
            if (this.isInWater()) {
                this.setDeltaMovement(this.getDeltaMovement().scale((double)0.9F));
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

    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            float f = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount) * 7.448451F * ((float)Math.PI / 180F) + (float)Math.PI);
            float f1 = Mth.cos((float)(this.getUniqueFlapTickOffset() + this.tickCount + 1) * 7.448451F * ((float)Math.PI / 180F) + (float)Math.PI);
            if (f > 0.0F && f1 <= 0.0F) {
                this.level().playLocalSound(this.getX(), this.getY(), this.getZ(), SoundEvents.PHANTOM_FLAP, this.getSoundSource(), 0.95F + this.random.nextFloat() * 0.05F, 0.95F + this.random.nextFloat() * 0.05F, false);
            }

            float f2 = Mth.cos(this.getYRot() * ((float)Math.PI / 180F)) * (1.3F + 0.21F);
            float f3 = Mth.sin(this.getYRot() * ((float)Math.PI / 180F)) * (1.3F + 0.21F);
            float f4 = (0.3F + f * 0.45F) * (0.2F + 1.0F);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() + (double)f2, this.getY() + (double)f4, this.getZ() + (double)f3, 0.0D, 0.0D, 0.0D);
            this.level().addParticle(ParticleTypes.MYCELIUM, this.getX() - (double)f2, this.getY() + (double)f4, this.getZ() - (double)f3, 0.0D, 0.0D, 0.0D);
            return;
        }

        if(spawnPoint == null)
        {
            spawnPoint = new Vec3(getX(), getY(), getZ());
        }
        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(this,2, 3, TickUnits.convertMinutesToTicks(1));
    }

    public SpawnGroupData finalizeSpawn(@NotNull ServerLevelAccessor p_33126_, @NotNull DifficultyInstance p_33127_, @NotNull MobSpawnType p_33128_, @Nullable SpawnGroupData p_33129_, @Nullable CompoundTag p_33130_) {
        this.anchorPoint = this.blockPosition().above(5);
        return super.finalizeSpawn(p_33126_, p_33127_, p_33128_, p_33129_, p_33130_);
    }

    protected @NotNull BodyRotationControl createBodyControl() {
        return new PhantomBodyRotationControl(this);
    }

    /** Save Data **/

    public void readAdditionalSaveData(@NotNull CompoundTag p_33132_) {
        super.readAdditionalSaveData(p_33132_);
        if (p_33132_.contains("AX")) {
            this.anchorPoint = new BlockPos(p_33132_.getInt("AX"), p_33132_.getInt("AY"), p_33132_.getInt("AZ"));
        }
    }

    public void addAdditionalSaveData(@NotNull CompoundTag p_33141_) {
        super.addAdditionalSaveData(p_33141_);
        p_33141_.putInt("AX", this.anchorPoint.getX());
        p_33141_.putInt("AY", this.anchorPoint.getY());
        p_33141_.putInt("AZ", this.anchorPoint.getZ());
    }

    /** Animation **/
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private static final RawAnimation TUMOR_IDLE_ANIMATION = RawAnimation.begin().thenLoop("tumor");

    protected PlayState poseTumorCycle(AnimationState<SculkPhantomEntity> state)
    {
        state.setAnimation(TUMOR_IDLE_ANIMATION);
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericWalkIdleController(this).transitionLength(5));
        controllers.add(new AnimationController<>(this, "blob_idle", 5, this::poseTumorCycle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Sounds **/

    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.PHANTOM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.PHANTOM_DEATH;
    }

    protected void dieAndSpawnCorpse()
    {
        SculkPhantomEntity.this.discard();
        SculkPhantomCorpseEntity corpse = new SculkPhantomCorpseEntity(ModEntities.SCULK_PHANTOM_CORPSE.get(), level());
        corpse.setPos(SculkPhantomEntity.this.getX(), SculkPhantomEntity.this.getY(), SculkPhantomEntity.this.getZ());
        level().addFreshEntity(corpse);

        // Give spore spewer slow falling
        corpse.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(20), 1));
    }

    protected class FallToTheGroundIfMobsUnder extends Goal
    {
        protected long lastTimeOfCheck = 0;
        protected long checkCooldown = TickUnits.convertSecondsToTicks(15);

        protected final int mobCheckRadius = 32;

        public boolean canUse()
        {
            if(level().getGameTime() - lastTimeOfCheck < checkCooldown)
            {
                return false;
            }

            if(spawnPoint == null)
            {
                return false;
            }

            // If less than 300 blocks from spawn point, do not explode.
            if(BlockAlgorithms.getBlockDistanceXZ(blockPosition(), BlockPos.containing(spawnPoint)) < 300)
            {
                return false;
            }

            lastTimeOfCheck = level().getGameTime();

            //Spawn Bounding Box on floor and check for mobs
            BlockPos groundBlockPos = BlockAlgorithms.getGroundBlockPos(level(), blockPosition(), level().getMaxBuildHeight());

            // Find any non-sculk mobs in the area
            List<LivingEntity> nearbyMobs = EntityAlgorithms.getNonSculkEntitiesAtBlockPos((ServerLevel) level(), groundBlockPos, mobCheckRadius);

            for(LivingEntity mob : nearbyMobs)
            {
                boolean isValidTarget = ((ISculkSmartEntity) SculkPhantomEntity.this).getTargetParameters().isEntityValidTarget(mob, false);
                if(isValidTarget)
                {
                    return true;
                }
            }

            return false;
        }

        public boolean canContinueToUse()
        {
            return false;
        }

        public void start()
        {
            dieAndSpawnCorpse();
        }
    }

    protected class selectRandomLocationToVisit extends Goal
    {
        protected long lastTimeOfExecution = 0;
        protected long executionCooldown = TickUnits.convertSecondsToTicks(60);
        protected int circleRadiusVariance = 50;
        protected final int BASE_CIRCLE_RADIUS = 300;

        protected final int CIRCLE_RADIUS_INCREASE = 100;
        protected int currentCircleRadius = BASE_CIRCLE_RADIUS + circleRadiusVariance;


        public boolean canUse()
        {
            if(level().getGameTime() - lastTimeOfExecution < executionCooldown)
            {
                return false;
            }

            if(getTarget() !=  null)
            {
                return false;
            }

            return level().canSeeSky(blockPosition().above());
        }

        public boolean canContinueToUse()
        {
            return false;
        }

        private boolean isGroundPosValid(BlockPos pos)
        {
            // As long as its not a fluid, its valid
            return level().getFluidState(pos).isEmpty() && level().getFluidState(pos.below()).isEmpty();
        }

        public Vec3 getRandomTravelLocationVec3()
        {
            int MAX_ATTEMPTS = 10;

            for(int attempts = 0; attempts < MAX_ATTEMPTS; attempts++) {

                int radius = currentCircleRadius + random.nextInt(circleRadiusVariance) * (random.nextBoolean() ? 1 : -1);
                int x = random.nextInt(radius) * (random.nextBoolean() ? 1 : -1);
                int z = random.nextInt(radius) * (random.nextBoolean() ? 1 : -1);
                BlockPos travelLocation = blockPosition().offset(x, 0, z);
                BlockPos groundBlockPos = BlockAlgorithms.getGroundBlockPos(level(), blockPosition(), level().getMaxBuildHeight());
                if(isGroundPosValid(groundBlockPos))
                {
                    int groundYLevel = groundBlockPos.getY();
                    return new Vec3(travelLocation.getX(), groundYLevel + 50, travelLocation.getZ());
                }
            }
            // If we find no valid location, just return the current location and increase range
            currentCircleRadius += CIRCLE_RADIUS_INCREASE;
            return moveTargetPoint;
        }

        public void start()
        {
            lastTimeOfExecution = level().getGameTime();
            moveTargetPoint = getRandomTravelLocationVec3();
            anchorPoint = BlockPos.containing(moveTargetPoint);
        }
    }

    abstract class MoveTargetGoal extends Goal {
        public MoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }
    }

    class SweepAttackGoal extends MoveTargetGoal {

        private long lastTimeOfAttack = 0;
        private int COOLDOWN = TickUnits.convertSecondsToTicks(5);

        public boolean canUse() {

            if(level().getGameTime() - lastTimeOfAttack < COOLDOWN)
            {
                return false;
            }

            return SculkPhantomEntity.this.getTarget() != null;
        }

        public boolean canContinueToUse()
        {
            LivingEntity target = SculkPhantomEntity.this.getTarget();
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
            SculkPhantomEntity.this.setTarget(null);
        }

        public void tick() {
            LivingEntity target = SculkPhantomEntity.this.getTarget();
            boolean isPhantomNull = target == null;

            if(isPhantomNull)
            {
                return;
            }

            SculkPhantomEntity.this.getNavigation().moveTo(target, 2.0D);
            AABB boundingBox = SculkPhantomEntity.this.getBoundingBox().inflate(0.2F);
            boolean doesPhantomIntersectTarget = boundingBox.intersects(target.getBoundingBox());

            if (doesPhantomIntersectTarget)
            {
                SculkPhantomEntity.this.doHurtTarget(target);
                EntityAlgorithms.reducePurityEffectDuration(target, TickUnits.convertMinutesToTicks(10));
                EntityAlgorithms.applyDebuffEffect(target, ModMobEffects.DISEASED_CYSTS.get(), TickUnits.convertSecondsToTicks(30), 0);
                lastTimeOfAttack = level().getGameTime();
                return;
            }

            if (SculkPhantomEntity.this.horizontalCollision || SculkPhantomEntity.this.hurtTime > 0) {
                lastTimeOfAttack = level().getGameTime();
            }
        }
    }

    class FallToGroundAfterTime extends DespawnAfterTime
    {
        public FallToGroundAfterTime(ISculkSmartEntity mob, int ticksThreshold) {
            super(mob, ticksThreshold);
        }

        @Override
        public void start()
        {
            dieAndSpawnCorpse();
        }
    }

    class PhantomBodyRotationControl extends BodyRotationControl {
        public PhantomBodyRotationControl(Mob p_33216_) {
            super(p_33216_);
        }

        public void clientTick() {
            SculkPhantomEntity.this.yHeadRot = SculkPhantomEntity.this.yBodyRot;
            SculkPhantomEntity.this.yBodyRot = SculkPhantomEntity.this.getYRot();
        }
    }

    protected static class PhantomLookControl extends LookControl {
        public PhantomLookControl(Mob p_33235_) {
            super(p_33235_);
        }

        public void tick() {
        }
    }

    protected class PhantomMoveControl extends MoveControl {
        private static final float MAX_SPEED = 1.8F;
        private static final float MIN_SPEED = 0.2F;
        private static final float TURN_RATE = 4.0F;
        private static final float COLLISION_SPEED_RESET = 0.1F;

        private float speed = COLLISION_SPEED_RESET;

        public PhantomMoveControl(Mob mob) {
            super(mob);
        }

        public void tick() {
            if (hasCollidedHorizontally()) {
                turnAround();
                resetSpeed();
            }

            if (!hasTargetPoint()) {
                return;
            }

            updateRotation();
            adjustSpeedBasedOnRotation();

            // Calculate desired movement vector
            Vec3 targetVector = getTargetDirectionVector();
            targetVector.normalize().scale(speed);

            // Update entity's movement
            Vec3 currentMovement = mob.getDeltaMovement();
            mob.setDeltaMovement(currentMovement.add(targetVector.subtract(currentMovement).scale(0.2D)));
        }

        private boolean hasCollidedHorizontally() {
            return mob.horizontalCollision;
        }

        private void turnAround() {
            mob.setYRot(mob.getYRot() + 180.0F);
        }

        private void resetSpeed() {
            speed = COLLISION_SPEED_RESET;
        }

        private boolean hasTargetPoint() {
            return SculkPhantomEntity.this.moveTargetPoint != null;
        }

        private Vec3 getTargetDirectionVector() {
            Vec3 targetPosition = SculkPhantomEntity.this.moveTargetPoint;
            Vec3 currentPosition = SculkPhantomEntity.this.position();
            return targetPosition.subtract(currentPosition);
        }

        private void updateRotation() {
            float targetRotation = getAngleTowardsTarget();
            float currentRotation = mob.getYRot();
            float smoothTargetRotation = Mth.approachDegrees(currentRotation + 90.0F, targetRotation, TURN_RATE);
            mob.setYRot(smoothTargetRotation - 90.0F);
            mob.yBodyRot = mob.getYRot();
        }

        private void adjustSpeedBasedOnRotation() {
            float angleDifference = Math.abs(Mth.degreesDifferenceAbs(mob.getYRot(), getAngleTowardsTarget()));
            if (angleDifference < 3.0F) {
                speed = Mth.approach(speed, MAX_SPEED, 0.005F * (MAX_SPEED / speed));
            } else {
                speed = Mth.approach(speed, MIN_SPEED, 0.025F);
            }
        }

        private float getAngleTowardsTarget() {
            Vec3 targetDirection = getTargetDirectionVector();
            return (float) Math.toDegrees(Math.atan2(targetDirection.z, targetDirection.x));
        }
    }
}
