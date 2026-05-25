package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.*;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.common.entity.goal.ReturnToNestGoal;
import com.github.sculkhorde.common.entity.projectile.SmallBroodAcidProjectileEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkBroodHatcherEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkBroodHatcherModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkBroodHatcherRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 100F;
    //The armor of the mob
    public static final float ARMOR = 6F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 18F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 3F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.25F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = DefaultTargetParameters.DefaultGroundMeleeInfector.copy(this)
            .addRetentionRule(TargetRetention.maxDistance(FOLLOW_RANGE + 10));
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected SculkBroodSpitterEntity child1;
    protected SculkBroodSpitterEntity child2;
    protected SculkBroodSpitterEntity child3;

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBroodHatcherEntity(EntityType<? extends SculkBroodHatcherEntity> type, Level worldIn) {
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
                        new ReturnToNestGoal(this, 1.0D),
                        new AttackSequenceGoal(this, TickUnits.convertSecondsToTicks(5),
                                //new LeapAttackStep(this),
                                new MeleeAttackStep(this),
                                new MeleeAttackStep(this),
                                new MeleeAttackStep(this),
                                new RainProjectilesAttackStep(this)
                        ),
                        new ImprovedRandomStrollGoal(this, 0.5D).setToAvoidWater(true),
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
    protected @NotNull PathNavigation createNavigation(Level p_33802_) {
        return new WallClimberNavigation(this, p_33802_);
    }
    @Override
    public void makeStuckInBlock(BlockState p_33796_, Vec3 p_33797_) {
        if (p_33796_.is(Blocks.COBWEB)) {
            // do nothing
        }

    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        TARGET_PARAMETERS.updateTargets();


        EntityAlgorithms.getNonSculkUnitsInBoundingBox(level(), getBoundingBox().inflate(0.5)).forEach(entity -> {
            //entity.hurt(damageSources().mobAttack(this), (float) getAttributeValue(Attributes.ATTACK_DAMAGE));
        });
        /*
        // I know this code is simple and kinda dumb, but idc. It works just fine
        if(child1 == null || child1.isDeadOrDying())
        {
            child1 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child1);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child1);
            }
        }

        if(child2 == null || child2.isDeadOrDying())
        {
            child2 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child2);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child2);
            }
        }

        if(child3 == null || child3.isDeadOrDying())
        {
            child3 = new SculkBroodlingEntity(level(), blockPosition());
            level().addFreshEntity(child3);

            if(SquadSystem.getSquadOfLivingEntity(this).isPresent())
            {
                SquadSystem.getSquadOfLivingEntity(this).get().forceAcceptMemberIntoSquad(child3);
            }
        }

         */
    }

    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay("attack");

    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .triggerableAnim("attack", ATTACK_ANIMATION).transitionLength(5);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkRunIdleController(this),
                ATTACK_ANIMATION_CONTROLLER,
                DefaultAnimations.genericLivingController(this)
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


    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        ModSavedData.getSaveData().addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */

    protected class LeapAttackStep extends AttackStepGoal
    {
        protected boolean hasLeaped = false;
        protected int ticksSinceLeap = 0;
        protected final float leapStrength = 1.0F;
        protected float shortestDistanceToTarget = 300;

        public LeapAttackStep(Mob mob) {
            super(mob);
        }

        @Override
        public boolean canUse() {
            return super.canUse() && mob.getTarget() != null;
        }

        @Override
        protected int getPreAttackDelay() {
            return TickUnits.convertSecondsToTicks(0.5F);
        }

        @Override
        protected int getPostAttackDelay() {
            return 0;
        }

        @Override
        protected void doAttackTick() {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                setPostAttack(true);
                return;
            }


            if (!hasLeaped) {
                Vec3 directionHorizontal = new Vec3(target.getX() - mob.getX(), 0.0, target.getZ() - mob.getZ());
                double distanceHorizontal = directionHorizontal.length();

                // If the target is too close, just do a small hop
                if (distanceHorizontal < 0.1) {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(0, 0.5, 0));
                } else {
                    // We want to land at the target's position.
                    // Let's assume a fixed time for the leap, say 1 second (20 ticks).
                    // Or better, we can use a formula for projectile motion.
                    // v_y = (d_y + 0.5 * g * t^2) / t
                    // v_x = d_x / t

                    float ticksInAir = 15; // Adjusted for a snappy leap
                    float gravity = 0.08f; // Default Minecraft gravity for most mobs

                    double velocityY = (target.getY() - mob.getY() + 0.5 * gravity * ticksInAir * ticksInAir) / ticksInAir;
                    double velocityX = (target.getX() - mob.getX()) / ticksInAir;
                    double velocityZ = (target.getZ() - mob.getZ()) / ticksInAir;

                    // Cap the velocities to avoid insane leaps
                    double maxVelocity = 5;
                    Vec3 velocity = new Vec3(velocityX, velocityY, velocityZ);
                    if (velocity.length() > maxVelocity) {
                        velocity = velocity.normalize().scale(maxVelocity);
                    }

                    mob.setDeltaMovement(velocity);
                    mob.setNoGravity(true);
                }

                mob.lookAt(target, 30.0F, 30.0F);
                hasLeaped = true;
                ticksSinceLeap = 0;
            } else {

                // Keep track of our distance to the target
                float distanceToTarget = EntityAlgorithms.getDistanceBetweenEntities(mob, target);
                if(distanceToTarget < shortestDistanceToTarget)
                {
                    shortestDistanceToTarget = distanceToTarget;
                }

                if(distanceToTarget >= shortestDistanceToTarget)
                {
                    mob.setNoGravity(false);
                }

                ticksSinceLeap++;

                if (ticksSinceLeap > TickUnits.convertSecondsToTicks(0.5F)
                        && (EntityAlgorithms.isOnGround(mob)
                        || distanceToTarget >= shortestDistanceToTarget)
                        || ticksSinceLeap > TickUnits.convertSecondsToTicks(2)
                        ||  distanceToTarget <= (getBbWidth()/2.0) + 1) {
                    setPostAttack(true);
                    mob.setNoGravity(false);
                }
            }
        }

        @Override
        public void stop() {
            super.stop();
            hasLeaped = false;
            ticksSinceLeap = 0;
        }
    }

    protected class MeleeAttackStep extends AttackStepGoal
    {
        protected int ticksUntilNextPathRecalculation = 0;

        public MeleeAttackStep(Mob mob) {
            super(mob);
        }

        @Override
        protected int getPreAttackDelay() {
            return TickUnits.convertSecondsToTicks(20);
        }

        @Override
        protected int getPostAttackDelay() {
            return 0;
        }

        @Override
        public void start() {
            super.start();

            if(getTarget() != null)
            {
                navigation.moveTo(getTarget(), 1.2D);
            }
        }

        @Override
        protected void doPreAttackTick() {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                setAttackTickComplete();
                return;
            }

            mob.getLookControl().setLookAt(target, 30.0F, 30.0F);
            double distanceFromTarget = mob.distanceTo(target);

            if (distanceFromTarget <= 3.0D) {
                setPreAttack(false);
                return;
            }

            ticksUntilNextPathRecalculation--;
            if (ticksUntilNextPathRecalculation <= 0) {
                navigation.moveTo(target, 1.2D);
                ticksUntilNextPathRecalculation = TickUnits.convertSecondsToTicks(0.5F);
            }
        }

        @Override
        protected void doAttackTick() {
            LivingEntity target = mob.getTarget();
            if (target == null) {
                setPostAttack(true);
                return;
            }

            AABB hitbox = HitboxUtil.createBoundingBoxCubeAtBlockPos(target.position(), 3);
            for(LivingEntity e : EntityAlgorithms.getNonSculkUnitsInBoundingBox(mob.level(), hitbox))
            {
                if(EntityAlgorithms.isInvalidTargetForSculkHorde(e))
                {
                    continue;
                }

                EntityAlgorithms.doCorrodedDamageToEntity(mob, e, 18);
            }

            setPostAttack(true);
            navigation.stop();
        }
    }

    protected class RainProjectilesAttackStep extends AttackStepGoal
    {
        protected final int duration = TickUnits.convertSecondsToTicks(10);
        protected int ticksElapsed = 0;
        protected final int projectilesPerTick = 10;
        protected final float range = 10F;

        public RainProjectilesAttackStep(Mob mob) {
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
            ticksElapsed++;

            if (ticksElapsed > duration) {
                setPostAttack(true);
                return;
            }

            for (int i = 0; i < projectilesPerTick; i++) {
                SmallBroodAcidProjectileEntity projectile = new SmallBroodAcidProjectileEntity(level(), (LivingEntity) mob, 4);
                projectile.setNoGravity(false);

                double offsetX = (mob.getRandom().nextDouble() - 0.5) * 2 * range;
                double offsetZ = (mob.getRandom().nextDouble() - 0.5) * 2 * range;
                double offsetY = (mob.getRandom().nextDouble() - 0.5) * 2 * range;

                Vec3 targetPos = mob.position().add(offsetX, offsetY, offsetZ);
                Vec3 spawnPos = mob.getBoundingBox().getCenter();
                Vec3 direction = targetPos.subtract(spawnPos).normalize();

                projectile.setPos(spawnPos.x, spawnPos.y, spawnPos.z);
                projectile.shoot(direction.x, direction.y, direction.z, 1.5F, 0F);
                level().addFreshEntity(projectile);
            }
        }

        @Override
        public void stop() {
            super.stop();
            ticksElapsed = 0;
        }
    }
}
