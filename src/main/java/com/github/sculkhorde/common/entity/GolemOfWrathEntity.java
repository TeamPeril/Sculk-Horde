package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.block.GolemOfWrathAnimatorBlock;
import com.github.sculkhorde.common.entity.goal.CustomAttackGoal;
import com.github.sculkhorde.common.entity.goal.NearestInfectionModEntityTargetGoal;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GolemOfWrathEntity extends PathfinderMob implements GeoEntity, IPurityGolemEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.core.ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}.java<br>
     * Added {@link GolemOfWrathEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.GolemOfWrathModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.GolemOfWrathRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 200F;
    //The armor of the mob
    public static final float ARMOR = 30F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 10F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 3F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 32F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.45F;
    public static final float KNOCKBACK_RESISTANCE = 100.0F;

    // Controls what types of entities this mob can target
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected BlockPos boundBlockPos = null;
    protected boolean belongsToBoundBlock = false;


    public GolemOfWrathEntity(Level worldIn) {
        super(ModEntities.GOLEM_OF_WRATH.get(), worldIn);
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public GolemOfWrathEntity(EntityType<? extends GolemOfWrathEntity> type, Level worldIn) {
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
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.KNOCKBACK_RESISTANCE, MOVEMENT_SPEED);
    }


    // #### Events ####

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        super.registerGoals();

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
                        //SwimGoal(mob)
                        new FloatGoal(this),
                        new GroundSlamAttackGoal(),
                        new MeleeAttackGoal(),
                        new GolemOfWrathNavigation(),
                        new WaterAvoidingRandomStrollGoal(this, 0.3D),
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
                        new NearestInfectionModEntityTargetGoal<>(this, true, true)
                                .setIgnoreFlyingTargets(true)
                                .setIgnoreSwimmingTargets(true),
                        new HurtByTargetGoal(this)
                };
        return goals;
    }

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();

        if(!hasEffect(ModMobEffects.PURITY.get()))
        {
            MobEffectInstance effect = new MobEffectInstance(ModMobEffects.PURITY.get(), Integer.MAX_VALUE, 1);
            addEffect(effect);
        }

        if(!hasEffect(MobEffects.REGENERATION))
        {
            MobEffectInstance effect = new MobEffectInstance(MobEffects.REGENERATION, Integer.MAX_VALUE, 0);
            addEffect(effect);
        }

        if(hasEffect(ModMobEffects.CORRODED.get()))
        {
            removeEffect(ModMobEffects.CORRODED.get());
        }

        if(getLastAttacker() != null && getTarget() != null && EntityAlgorithms.isInfectionModEntity.test(getLastAttacker()))
        {
            if(distanceTo(getLastAttacker()) < distanceTo(getTarget()))
            {
                setTarget(getLastAttacker());
            }
        }


        // If we do not belong to a block, ignore
        if(!belongsToBoundBlock())
        {
            return;
        }
        // If we are block bound, but it is destroyed, die
        else if(!isBoundBlockPresent())
        {
            this.hurt(damageSources().genericKill(), Integer.MAX_VALUE);
        }
        // If we are block bound, but we travel too far from it, just die.
        else if(BlockAlgorithms.getBlockDistance(blockPosition(), getBoundBlockPos().get()) > getMaxDistanceFromBoundBlockBeforeDeath())
        {
            this.hurt(damageSources().genericKill(), Integer.MAX_VALUE);
        }

    }

    @Override
    protected void tickDeath() {
        super.tickDeath();

        if(level().isClientSide())
        {
            return;
        }

        if(isBoundBlockPresent())
        {
            convertBoundBlockToDepleted();
        }
    }

    @Override
    public BlockState getDepletedBoundBlockState() {
        return ModBlocks.DEPLETED_GOLEM_OF_WRATH_ANIMATOR_BLOCK.get().defaultBlockState();
    }

    @Override
    public void convertBoundBlockToDepleted() {
        if(isBoundBlockPresent())
        {
            BlockAlgorithms.setBlockMisc(level(), getBoundBlockPos().get(), getDepletedBoundBlockState());
        }
    }

    @Override
    public boolean belongsToBoundBlock() {
        return belongsToBoundBlock;
    }

    @Override
    public boolean isBoundBlockPresent() {

        if(getBoundBlockPos().isEmpty())
        {
            return false;
        }

        return level().getBlockState(getBoundBlockPos().get()).getBlock() instanceof GolemOfWrathAnimatorBlock;
    }

    @Override
    public Optional<BlockPos> getBoundBlockPos() {
        return Optional.ofNullable(boundBlockPos);
    }

    @Override
    public void setBoundBlockPos(BlockPos pos)
    {
        boundBlockPos = pos;
        belongsToBoundBlock = true;
    }

    @Override
    public int getMaxDistanceFromBoundBlockBeforeDeath() {
        return 100;
    }

    @Override
    public int getMaxTravelDistanceFromBoundBlock() {
        return getMaxDistanceFromBoundBlockBeforeDeath() - 20;
    }

    // #### Animation Code ####

    public static final String ATTACK_MELEE_ID = "attack.melee";
    private static final RawAnimation ATTACK_MELEE_ANIMATION = RawAnimation.begin().thenPlay(ATTACK_MELEE_ID);

    public static final String SPIN_ATTACK_MELEE_ID = "attack.spin";
    private static final RawAnimation SPIN_ATTACK_MELEE_ANIMATION = RawAnimation.begin().thenPlay(SPIN_ATTACK_MELEE_ID);

    public static final String COMBAT_ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    private final AnimationController COMBAT_ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim(ATTACK_MELEE_ID, ATTACK_MELEE_ANIMATION)
            .triggerableAnim(SPIN_ATTACK_MELEE_ID, SPIN_ATTACK_MELEE_ANIMATION);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericWalkRunIdleController(this).transitionLength(5),
                COMBAT_ATTACK_ANIMATION_CONTROLLER
        );
    }
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // #### Sound Code ####

    protected SoundEvent getAmbientSound() {
        return SoundEvents.IRON_GOLEM_REPAIR;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.IRON_GOLEM_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.IRON_GOLEM_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.IRON_GOLEM_STEP, 0.15F, 1.0F);
    }

    class MeleeAttackGoal extends CustomAttackGoal
    {

        public MeleeAttackGoal()
        {
            super(GolemOfWrathEntity.this, GolemOfWrathEntity.this.getBbWidth() * 2, TickUnits.convertSecondsToTicks(0.5F));
        }

        @Override
        protected long getExecutionCooldown() {
            return TickUnits.convertSecondsToTicks(2);
        }

        @Override
        protected void triggerAnimation() {
            triggerAnim(COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, ATTACK_MELEE_ID);
        }

        @Override
        public void onTargetHurt(LivingEntity target)
        {
            AABB hitbox = HitboxUtil.createBoundingBoxCubeAtBlockPos(target.position(), 10);
            List<LivingEntity> enemies = EntityAlgorithms.getAllInfectionModEntitiesInBoundingBox((ServerLevel) mob.level(), hitbox);
            for(LivingEntity entity : enemies)
            {
                if(entity.getUUID().equals(GolemOfWrathEntity.this.getUUID()))
                {
                    continue;
                }
                entity.hurt(mob.damageSources().mobAttack(mob), GolemOfWrathEntity.ATTACK_DAMAGE);

                CursorSurfacePurifierEntity cursor = new CursorSurfacePurifierEntity(mob.level());
                cursor.setPos(target.position());
                cursor.setTickIntervalMilliseconds(10);
                cursor.setMaxLifeTimeMillis(TimeUnit.SECONDS.toMillis(60));
                cursor.setMaxTransformations(20);
                mob.level().addFreshEntity(cursor);
            }
        }
    }

    class GroundSlamAttackGoal extends CustomAttackGoal {

        public GroundSlamAttackGoal() {
            super(GolemOfWrathEntity.this, 1.0F,  10);
        }

        @Override
        public long getCanUseCheckInterval() {
            return TickUnits.convertSecondsToTicks(2);
        }

        @Override
        public boolean canUse() {

            if (!super.canUse()) {
                return false;
            }

            List<LivingEntity> hostiles = EntityAlgorithms.getAllInfectionModEntitiesInBoundingBox((ServerLevel) level(), getBoundingBox().inflate(7));
            return hostiles.size() > 4;
        }

        @Override
        public boolean canContinueToUse() {
            return !canUse();
        }


        @Override
        public void onTargetHurt(LivingEntity target) {
            super.onTargetHurt(target);
            List<LivingEntity> entities = EntityAlgorithms.getAllInfectionModEntitiesInBoundingBox((ServerLevel) level(), getBoundingBox().inflate(7));
            float pushAwayStrength = 5f; // Increased push strength for better outwards effect
            float pushUpStrength = 3f;   // Separate push up strength for vertical component.

            for (LivingEntity entity : entities) {
                if (entity.getUUID().equals(GolemOfWrathEntity.this.getUUID())) {
                    continue;
                }

                EntityAlgorithms.pushAwayEntitiesFromPosition(position(), entity, pushAwayStrength, pushUpStrength);
                CursorSurfacePurifierEntity cursor = new CursorSurfacePurifierEntity(entity.level());
                cursor.setPos(entity.position());
                cursor.setTickIntervalMilliseconds(10);
                cursor.setMaxLifeTimeMillis(TimeUnit.SECONDS.toMillis(60));
                cursor.setMaxTransformations(20);
                entity.level().addFreshEntity(cursor);
            }
            SoundUtil.playHostileSoundInLevel(level(), blockPosition(), SoundEvents.RAVAGER_ATTACK);
        }

        @Override
        protected void triggerAnimation() {
            triggerAnim(COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, SPIN_ATTACK_MELEE_ID);
        }
    }
    protected class GolemOfWrathNavigation extends Goal {
        protected final PathfinderMob mob;
        protected double wantedX;
        protected double wantedY;
        protected double wantedZ;
        protected final double speedModifier;

        // Add cooldown-related fields
        protected int pathRecalculationCooldown = 0;
        protected static final int PATH_RECALCULATION_INTERVAL_TICKS = TickUnits.convertSecondsToTicks(1); // Ticks (1 second at 20 ticks/sec)

        public GolemOfWrathNavigation() {
            this.mob = GolemOfWrathEntity.this;
            this.speedModifier = 1;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        public IPurityGolemEntity getGolem() {
            return (IPurityGolemEntity) mob;
        }

        public boolean canUse() {
            // Decrement cooldown
            if (pathRecalculationCooldown > 0) {
                pathRecalculationCooldown--;
                return false;
            }

            boolean doesGolemBelongToABoundBlockThatIsPresent = getGolem().belongsToBoundBlock() && getGolem().isBoundBlockPresent();

            if(!doesGolemBelongToABoundBlockThatIsPresent) {
                return false;
            }

            boolean isGolemTooFarFromBoundBlock = BlockAlgorithms.getBlockDistanceXZ(mob.blockPosition(), getGolem().getBoundBlockPos().get()) >= getGolem().getMaxTravelDistanceFromBoundBlock();
            boolean isGolemIdle = mob.getTarget() == null;
            boolean isGolemTooCloseToBoundBlock = BlockAlgorithms.getBlockDistanceXZ(mob.blockPosition(), getGolem().getBoundBlockPos().get()) < 10;
            boolean shouldGoToBoundBlock = (isGolemTooFarFromBoundBlock || isGolemIdle) && !isGolemTooCloseToBoundBlock;
            boolean shouldChaseTarget = !isGolemIdle;

            Vec3 potentialPosition = null;
            if(shouldGoToBoundBlock) {
                potentialPosition = DefaultRandomPos.getPosTowards(this.mob, 16, 7, getBoundBlockPos().get().getCenter(), Math.PI / 2F);
            }
            else if(shouldChaseTarget) {
                potentialPosition = DefaultRandomPos.getPosTowards(this.mob, 16, 7, getTarget().position(), Math.PI / 2F);
            }

            if(potentialPosition != null) {
                this.wantedX = potentialPosition.x;
                this.wantedY = potentialPosition.y;
                this.wantedZ = potentialPosition.z;
                // Reset cooldown when new path is calculated
                pathRecalculationCooldown = PATH_RECALCULATION_INTERVAL_TICKS;
                return true;
            }

            return false;
        }

        public boolean canContinueToUse() {
            // Allow continued use even during cooldown
            return !mob.getNavigation().isDone();
        }

        public void stop() {
            // Reset cooldown when stopping
            pathRecalculationCooldown = 0;
        }

        public void start() {
            this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
        }
    }
}
