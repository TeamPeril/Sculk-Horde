package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.ImprovedFlyingNavigator;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.systems.path_builder_system.BuiltPath;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.BodyRotationControl;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.RangedAttackMob;
import net.minecraft.world.entity.projectile.LargeFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

public class SculkGhastEntity extends FlyingMob implements GeoEntity, ISculkSmartEntity, RangedAttackMob {

    /**
     * In order to create a mob, the following files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added client/model/entity/ SculkPhantomModel.java<br>
     * Added client/renderer/entity/ SculkPhantomRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 100F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 6F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.20F;

    // Controls what types of entities this mob can target
    protected final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().disableTargetingEntitiesInWater();
    protected final double MAX_MOB_MASS_STORED = 1000D;
    protected final ArrayList<Mob> storedMobs = new ArrayList<>();
    protected Position goalPosition; // Used for sending the ghast to a location.

    // Built path assigned by path builder system (nullable)
    private BuiltPath builtPath;

    // New: track whether an external system assigned a built path that we should report completion for.
    // The event should call setBuiltPath(...) to assign, poll hasCompletedAssignedBuiltPath(), then call clearCompletedAssignedBuiltPath().
    private boolean builtPathAssignedFlag = false;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkGhastEntity(EntityType<? extends SculkGhastEntity> type, Level worldIn)
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
                .add(Attributes.FLYING_SPEED, 0.5F)
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
                new Despawn(this, TickUnits.convertMinutesToTicks(15)),
                //new selectRandomLocationToVisit(),
                //new SculkGhastGoToAnchor(this),
                new ShootGhastProjectile(this,  48, 0),
                // Follow a built path when provided by PathBuilderSystem
                new FollowBuiltPathGoal(this, 2.0D),
                new DropOffMobsNearHostiles(),
                new FindAndStoreIdleMobs(),
                new SculkGhastWanderGoal(this, 1.0F, TickUnits.convertSecondsToTicks(3), 20)
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
                new TargetAttacker(this),
                new NearestLivingEntityTargetGoal<>(this, true, false)
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

    public double getStoredMobMass()
    {
        double result = 0;
        for(Mob e : storedMobs)
        {
            if(!e.getAttributes().hasAttribute(Attributes.MAX_HEALTH))
            {
                continue;
            }

            result += e.getAttributes().getValue(Attributes.MAX_HEALTH);
        }

        return result;
    }

    public boolean canStoreMob(Mob entity)
    {
        if(entity == null || entity.isDeadOrDying())
        {
            return false;
        }

        if(entity.getUUID().equals(getUUID()))
        {
            return false;
        }

        if(!EntityAlgorithms.isSculkLivingEntity.test((entity)) && !EntityAlgorithms.isLivingEntityAllyToSculkHorde(entity))
        {
            return false;
        }

        if(entity.getTarget() != null)
        {
            return false;
        }

        if(getStoredMobMass() >= MAX_MOB_MASS_STORED)
        {
            return false;
        }

        if(!getSensing().hasLineOfSight(entity))
        {
            return false;
        }

        if(entity.getBbHeight() > SculkGhastEntity.this.getBbHeight() || entity.getBbWidth() > SculkGhastEntity.this.getBbWidth())
        {
            return false;
        }

        if(entity instanceof SculkGhastEntity)
        {
            return false;
        }

        if(entity.getMaxHealth() >= 50)
        {
            return false;
        }

        if(entity instanceof ISculkSmartEntity smartEntity)
        {
            return !smartEntity.isParticipatingInRaid();
        }

        return true;
    }

    public void storeMob(Mob entity)
    {
        storedMobs.add(entity);
        entity.discard();
    }

    // New getter & setter for built path (updated to set the assigned flag)
    public BuiltPath getBuiltPath() {
        return builtPath;
    }

    public void setBuiltPath(BuiltPath builtPathIn) {
        if (builtPathIn == null) {
            this.builtPath = null;
            // do not clear the assigned flag here — the event tracks completion by observing hasCompletedAssignedBuiltPath()
            // when the FollowBuiltPathGoal finishes it clears the path (set to null) and the event will observe that.
            return;
        } else {
            this.builtPath = builtPathIn.createCopy();
            this.builtPathAssignedFlag = true;
        }
    }

    // Signal whether an external system has assigned a non-null built path that the ghast should follow.
    public boolean hasBuiltPathAssigned() {
        return builtPathAssignedFlag && builtPath != null && builtPath.hasPath();
    }

    // Returns true when a previously assigned built path is completed.
    // Completion is detected either when the BuiltPath reports complete or when the FollowBuiltPathGoal clears the path (builtPath == null).
    public boolean hasCompletedAssignedBuiltPath() {
        if (!builtPathAssignedFlag) return false;
        if (builtPath == null) return true; // cleared by goal -> completed
        return builtPath.isPathComplete();
    }

    // Call this from the event after observing completion to reset internal tracking.
    public void clearCompletedAssignedBuiltPath() {
        builtPathAssignedFlag = false;
    }

    // Helpful progress accessor for monitoring
    public double getBuiltPathProgressFraction() {
        if (builtPath == null) return 0.0;
        return builtPath.getProgressFraction();
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

    // #### Functions ####

    public void releaseMob()
    {
        if(level() == null) { return; }

        if(!storedMobs.isEmpty())
        {
            Mob storedEntity = storedMobs.get(0);
            int spawnX = (int) (getX() + getRandom().nextIntBetweenInclusive((int) (getBbWidth() * -1), (int) getBbWidth()));
            int spawnZ = (int) (getZ() + getRandom().nextIntBetweenInclusive((int) (getBbWidth() * -1), (int) getBbWidth()));
            Mob spawnedEntity = (Mob) storedEntity.getType().spawn((ServerLevel) level(), new BlockPos(spawnX, (int) getY(), spawnZ), MobSpawnType.MOB_SUMMONED);
            if(spawnedEntity == null) { return; }

            spawnedEntity.setTarget(getTarget());
            spawnedEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(10), 0));
            spawnedEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, TickUnits.convertSecondsToTicks(10), 1));
            spawnedEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TickUnits.convertSecondsToTicks(10), 0));
            storedMobs.remove(0);
            return;
        }

        Mob storedEntity = new SculkMetamorphosisPodEntity(level(), TickUnits.convertSecondsToTicks(10));
        storedEntity.setPos(position());
        level().addFreshEntity(storedEntity);
        storedEntity.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(10), 0));
        storedEntity.addEffect(new MobEffectInstance(MobEffects.REGENERATION, TickUnits.convertSecondsToTicks(10), 1));
        storedEntity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TickUnits.convertSecondsToTicks(10), 0));

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

    @Override
    public void performRangedAttack(@NotNull LivingEntity target, float power) {
        if (!isSilent()) {
            level().levelEvent(null, 1016, blockPosition(), 0);
        }

        Vec3 vec3 = getViewVector(1.0F);
        double d2 = target.getX() - (getX() + vec3.x * power);
        double d3 = target.getY(0.5D) - (0.5D + getY(0.5D));
        double d4 = target.getZ() - (getZ() + vec3.z * power);

        LargeFireball largefireball = new LargeFireball(level(), this, d2, d3, d4, (int) power);
        largefireball.setPos(getX() + vec3.x * 4.0D, getY(0.5D) + 0.5D, largefireball.getZ() + vec3.z * 4.0D);
        level().addFreshEntity(largefireball);

    }

    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            return;
        }

        /*
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

         */
    }

    protected @NotNull BodyRotationControl createBodyControl() {
        return new GhastBodyRotationControl(this);
    }

    /** Save Data **/


    /** Animation **/
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(DefaultAnimations.genericWalkIdleController(this).transitionLength(5));
        //controllers.add(new AnimationController<>(this, "blob_idle", 5, this::poseTumorCycle));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Sounds **/

    protected SoundEvent getHurtSound(@NotNull DamageSource pDamageSource) {
        return SoundEvents.GHAST_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.GHAST_DEATH;
    }


    public boolean isAnchorPosValid(BlockPos pos)
    {
        boolean isThereIsNoFluid = level().getFluidState(pos).isEmpty() && level().getFluidState(pos.below()).isEmpty();
        boolean isItFarEnoughAway = distanceToSqr(Vec3.atCenterOf(pos)) > 30;
        // As long as its not a fluid, its valid
        return isThereIsNoFluid && isItFarEnoughAway;
    }

    protected class ShootGhastProjectile extends CustomAttackGoal
    {

        public ShootGhastProjectile(Mob mob, float maxDistanceForAttackIn, int attackDelay) {
            super(mob, maxDistanceForAttackIn, attackDelay);
            //setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public void start() {
            super.start();
        }

        @Override
        protected long getExecutionCooldown() {
            return TickUnits.convertSecondsToTicks(5);
        }

        @Override
        protected void checkAndAttack(LivingEntity targetMob)
        {
            if (isTargetInvalid()) {
                return;
            }
            performRangedAttack(targetMob, 2.0F);
        }
    }

    protected class DropOffMobsNearHostiles extends Goal
    {
        protected float requiredDistance = 5;
        protected long timeOfLastPathRecalculation = 0;
        protected long timeOfLastMobRelease = 0;

        protected BlockPos stagingArea;

        public DropOffMobsNearHostiles()
        {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        @Override
        public boolean canUse() {

            if(getTarget() == null)
            {
                return false;
            }

            return true;
        }


        @Override
        public boolean canContinueToUse() {

            return canUse();
        }

        public long getMobReleaseCooldown()
        {
            if(storedMobs.isEmpty())
            {
                return TickUnits.convertSecondsToTicks(30);
            }

            return TickUnits.convertSecondsToTicks(1);
        }

        @Override
        public void start() {
            super.start();

            if(getTarget() != null) { navigation.moveTo(getTarget(), 1.0F);}
            stagingArea = BlockAlgorithms.getGroundBlockPosUnderEntity(level(), SculkGhastEntity.this).above(5);
        }



        @Override
        public void tick() {
            super.tick();

            if(getTarget() == null)
            {
                return;
            }

            float pathRecalculationCooldown;
            float distanceFromStagingArea= BlockAlgorithms.getBlockDistance(stagingArea, blockPosition());

            if(distanceFromStagingArea > requiredDistance)
            {
                if(distanceFromStagingArea <= 32)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(1F);
                }
                else if(distanceFromStagingArea <= 48)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(3F);
                }
                else
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(6F);
                }

                if(level().getGameTime() - timeOfLastPathRecalculation >= pathRecalculationCooldown)
                {
                    navigation.moveTo(stagingArea.getX(), stagingArea.getY(), stagingArea.getZ(), 1.0F);
                    timeOfLastPathRecalculation = level().getGameTime();
                }
                return;
            }

            if(level().getGameTime() - timeOfLastMobRelease < getMobReleaseCooldown())
            {
                return;
            }

            timeOfLastMobRelease = level().getGameTime();
            navigation.stop();
            releaseMob();
            releaseMob();
            releaseMob();
            releaseMob();
            releaseMob();
            releaseMob();
        }
    }

    protected class FindAndStoreIdleMobs extends Goal implements IDebuggableGoal
    {
        protected List<Entity> targets;

        protected long timeOfLastPathRecalculation = 0;
        protected long timeOfLastSearch = 0;
        protected final long MOB_SEARCH_COOLDOWN = TickUnits.convertSecondsToTicks(5);

        protected float pathRecalculationCooldown;
        protected boolean isTimeToResetPathCooldown = true;

        public FindAndStoreIdleMobs()
        {
            setFlags(EnumSet.of(Goal.Flag.MOVE));
            setFlags(EnumSet.of(Flag.TARGET));
        }

        public Predicate<Entity> canStoreMobPredicate = (entity) ->
        {
            if(entity instanceof Mob mobEntity)
            {
                return canStoreMob(mobEntity);
            }

            return false;
        };

        @Override
        public boolean canUse() {

            if(getTarget() != null)
            {
                lastReasonForGoalNoStart = "Has Target";
                return false;
            }

            if(getStoredMobMass() >= MAX_MOB_MASS_STORED)
            {
                lastReasonForGoalNoStart = "Storage Full";
                return false;
            }

            if(level().getGameTime() - timeOfLastSearch >= MOB_SEARCH_COOLDOWN)
            {
                AABB searchBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(position(), 128);
                targets = EntityAlgorithms.getEntitiesInBoundingBox((ServerLevel) level(), searchBox, canStoreMobPredicate);
                timeOfLastSearch = level().getGameTime();
            }

            if(targets.isEmpty())
            {
                lastReasonForGoalNoStart = "No Targets";
                return false;
            }

            return true;
        }

        public void cleanTargets()
        {
            ArrayList<Entity> targetsToRemove = new ArrayList<>();

            for(Entity target : targets)
            {
                if(canStoreMob((Mob) target))
                {
                    continue;
                }

                targetsToRemove.add(target);
            }

            for(Entity target : targetsToRemove)
            {
                targets.remove(target);
            }
        }

        @Override
        public boolean canContinueToUse() {

            cleanTargets();

            if(targets.isEmpty())
            {
                return false;
            }

            return getStoredMobMass() < MAX_MOB_MASS_STORED;
        }

        @Override
        public void start() {
            super.start();
            isTimeToResetPathCooldown = true;
            navigation.moveTo(targets.get(0), 1.0F);
            lastTimeOfGoalExecution = level().getGameTime();
        }

        @Override
        public void stop() {
            super.stop();
        }

        @Override
        public void tick() {
            super.tick();

            if(targets.isEmpty())
            {
                return;
            }

            Mob target = (Mob) targets.get(0);

            float distanceFromTarget = EntityAlgorithms.getDistanceBetweenEntities(SculkGhastEntity.this, target);

            if(isTimeToResetPathCooldown)
            {
                if(distanceFromTarget <= 5)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(0.5F);
                }
                else if(distanceFromTarget <= 10)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(1F);
                }
                else if(distanceFromTarget <= 32)
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(3F);
                }
                else
                {
                    pathRecalculationCooldown = TickUnits.convertSecondsToTicks(6F);
                }
                isTimeToResetPathCooldown = false;
            }


            if(level().getGameTime() - timeOfLastPathRecalculation >= pathRecalculationCooldown)
            {
                navigation.moveTo(targets.get(0), 1.0F);
                timeOfLastPathRecalculation = level().getGameTime();
                isTimeToResetPathCooldown = true;
            }

            if(distanceFromTarget < getBbWidth() + 3)
            {
                storeMob(target);
                targets.remove(0);
            }
        }

        public String lastReasonForGoalNoStart;
        @Override
        public Optional<String> getLastReasonForGoalNoStart() {
            return Optional.empty();
        }
        @Override
        public Optional<String> getGoalName() {
            return Optional.of("FindAndStoreIdleMobs");
        }

        public long lastTimeOfGoalExecution;
        @Override
        public long getLastTimeOfGoalExecution() {
            return lastTimeOfGoalExecution;
        }

        @Override
        public long getTimeRemainingBeforeCooldownOver() {
            return 0;
        }
    }

    protected class Despawn extends DespawnAfterTime
    {
        public Despawn(ISculkSmartEntity mob, int ticksThreshold) {
            super(mob, ticksThreshold);
        }

        public long calculateTicksThreshold()
        {
            return ticksThreshold/3;
        }

        @Override
        public boolean canUse()
        {
            boolean mobHasBeenNameTagged = ((Mob) mob).hasCustomName();
            boolean hasNoStoredMobs = getStoredMobMass() <= 0;
            if(hasNoStoredMobs && level.getGameTime() - creationTime > calculateTicksThreshold() && !mob.isParticipatingInRaid() && !mobHasBeenNameTagged)
            {
                return true;
            }
            return false;
        }
    }

    protected class GhastBodyRotationControl extends BodyRotationControl {
        public GhastBodyRotationControl(Mob p_33216_) {
            super(p_33216_);
        }

        public void clientTick() {
            SculkGhastEntity.this.yHeadRot = SculkGhastEntity.this.yBodyRot;
            SculkGhastEntity.this.yBodyRot = SculkGhastEntity.this.getYRot();
        }
    }

    /**
     * Goal: follow a BuiltPath assigned to the ghast.
     * Iterative behaviour: each tick move toward the current next step; when within threshold advance to the next step.
     * Stops when the path is complete or becomes invalid.
     */
    protected class FollowBuiltPathGoal extends Goal {
        private final SculkGhastEntity ghast;
        private final double speed;
        private long lastMoveTick = 0L;
        private final long moveCooldownTicks = TickUnits.convertSecondsToTicks(1F);
        private final double proximity = 5D; // squared distance threshold

        public FollowBuiltPathGoal(SculkGhastEntity ghastIn, double speedIn) {
            this.ghast = ghastIn;
            this.speed = speedIn;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            BuiltPath p = ghast.getBuiltPath();
            return p != null && p.hasPath() && !p.isPathComplete();
        }

        @Override
        public boolean canContinueToUse() {
            BuiltPath p = ghast.getBuiltPath();
            return p != null && p.hasPath() && !p.isPathComplete();
        }

        @Override
        public void start() {
            // ensure navigation is ready
            ghast.getNavigation().stop();
            lastMoveTick = 0L;
        }

        @Override
        public void stop() {
            // clear navigation on stop; do not delete the path automatically (external systems may reuse it)
            ghast.getNavigation().stop();
            BuiltPath path = ghast.getBuiltPath();
            if (path != null) {
                path.setComplete();
                ghast.setBuiltPath(null);
            }
        }

        @Override
        public void tick() {
            BuiltPath p = ghast.getBuiltPath();
            if (p == null || !p.hasPath() || p.isPathComplete()) {
                return;
            }

            Optional<BlockPos> nextOpt = p.getNextStep();
            if (nextOpt.isEmpty()) {
                return;
            }
            BlockPos next = nextOpt.get();
            Vec3 target = Vec3.atCenterOf(next);

            // if we're close enough to the next step, advance
            if (BlockAlgorithms.getBlockDistance(ghast.blockPosition(), BlockPos.containing(target)) <= proximity) {
                boolean advanced = p.advanceToNextStep();
                if (!advanced && p.isPathComplete()) {
                    // finished
                    ghast.getNavigation().stop();
                    return;
                }
                // immediately try to get the new next step on the same tick
                return;
            }

            // throttle navigation commands
            long gameTime = ghast.level().getGameTime();
            if (gameTime - lastMoveTick >= moveCooldownTicks) {
                ghast.getNavigation().moveTo(target.x, target.y, target.z, speed);
                lastMoveTick = gameTime;
            }
        }
    }
}
