package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
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
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.animal.Cat;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
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
     * Added client/model/entity/ SculkBlightwingModel.java<br>
     * Added client/renderer/entity/ SculkBlightwingRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 5F;
    //The armor of the mob
    public static final float ARMOR = 1F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 1F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 1F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 30F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.3F;

    // Controls what types of entities this mob can target
    private TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles().ignoreTargetBelow50PercentHealth();

    //factory The animation factory used for animations
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private AttackPhase attackPhase = AttackPhase.CIRCLE;
    private BlockPos anchorPoint = BlockPos.ZERO;
    public static final int TICKS_PER_FLAP = Mth.ceil(24.166098F);
    private static final EntityDataAccessor<Integer> ID_SIZE = SynchedEntityData.defineId(Phantom.class, EntityDataSerializers.INT);

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
        this.moveControl = new PhantomMoveControl(this);
        this.lookControl = new PhantomLookControl(this);
    }

    public static LivingEntity spawnPhantom(Level worldIn, BlockPos spawnPos)
    {
        SculkPhantomEntity phantom = ModEntities.SCULK_PHANTOM.get().create(worldIn);
        phantom.setPos(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        phantom.spawnPoint = new Vec3(spawnPos.getX(), spawnPos.getY(), spawnPos.getZ());
        worldIn.addFreshEntity(phantom);
        return phantom;
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
                        new explodeAndDropSporeSpewer(),
                        new selectRandomLocationToVisit(),
                        new AttackStrategyGoal(),
                        new SweepAttackGoal(),
                        new CircleAroundAnchorGoal(),
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
                        new NearestLivingEntityTargetGoal<>(this, true, true)
                };
        return goals;
    }



    static enum AttackPhase {
        CIRCLE,
        SWOOP,
        INFECT;
    }
    /** Getters and Setters **/

    @Override
    public boolean shouldRenderAtSqrDistance(double distance) {
        return true;
    }

    protected Vec3 getRandomTravelLocationVec3()
    {
        int radius = 300;
        int x = random.nextInt(radius) * (random.nextBoolean() ? 1 : -1);
        int z = random.nextInt(radius) * (random.nextBoolean() ? 1 : -1);
        BlockPos travelLocation = blockPosition().offset(x,0,z);

        BlockPos groundBlockPos = BlockAlgorithms.getGroundBlockPos(level(), blockPosition(), level().getMaxBuildHeight());
        int groundYLevel = groundBlockPos.getY();
        return new Vec3(travelLocation.getX(), groundYLevel + 50, travelLocation.getZ());
    }


    private boolean isParticipatingInRaid = false;

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
        this.isParticipatingInRaid = isParticipatingInRaidIn;
    }

    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public double getPassengersRidingOffset() {
        return (double)this.getEyeHeight();
    }

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return getTarget() == null;
    }


    /**
     * @return if this entity may not naturally despawn.
     */
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

    private void updatePhantomSizeInfo() {
        this.refreshDimensions();
        this.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue((6.0));
    }

    protected float getStandingEyeHeight(Pose p_33136_, EntityDimensions p_33137_) {
        return p_33137_.height * 0.35F;
    }

    public int getUniqueFlapTickOffset() {
        return this.getId() * 3;
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
        // Disabling until i redo chunk system
        //EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(this,2, 3, TickUnits.convertMinutesToTicks(1));
    }

    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_33126_, DifficultyInstance p_33127_, MobSpawnType p_33128_, @Nullable SpawnGroupData p_33129_, @Nullable CompoundTag p_33130_) {
        this.anchorPoint = this.blockPosition().above(5);
        return super.finalizeSpawn(p_33126_, p_33127_, p_33128_, p_33129_, p_33130_);
    }

    protected BodyRotationControl createBodyControl() {
        return new PhantomBodyRotationControl(this);
    }


    /** Save Data **/


    public void onSyncedDataUpdated(EntityDataAccessor<?> p_33134_) {
        if (ID_SIZE.equals(p_33134_)) {
            this.updatePhantomSizeInfo();
        }

        super.onSyncedDataUpdated(p_33134_);
    }


    public void readAdditionalSaveData(CompoundTag p_33132_) {
        super.readAdditionalSaveData(p_33132_);
        if (p_33132_.contains("AX")) {
            this.anchorPoint = new BlockPos(p_33132_.getInt("AX"), p_33132_.getInt("AY"), p_33132_.getInt("AZ"));
        }
    }

    public void addAdditionalSaveData(CompoundTag p_33141_) {
        super.addAdditionalSaveData(p_33141_);
        p_33141_.putInt("AX", this.anchorPoint.getX());
        p_33141_.putInt("AY", this.anchorPoint.getY());
        p_33141_.putInt("AZ", this.anchorPoint.getZ());
    }

    /** Animation **/
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    /** Sounds **/

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SILVERFISH_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.SILVERFISH_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SILVERFISH_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.SILVERFISH_STEP, 0.15F, 1.0F);
    }

    protected class explodeAndDropSporeSpewer extends Goal
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

            if(!level().canSeeSky(blockPosition().above()))
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

            // Die and spawn spore spewer
            SculkPhantomEntity.this.hurt(SculkPhantomEntity.this.damageSources().genericKill(), Float.MAX_VALUE);
            SculkSporeSpewerEntity sporeSpewer = new SculkSporeSpewerEntity(ModEntities.SCULK_SPORE_SPEWER.get(), level());
            sporeSpewer.setPos(SculkPhantomEntity.this.getX(), SculkPhantomEntity.this.getY(), SculkPhantomEntity.this.getZ());
            level().addFreshEntity(sporeSpewer);

            // Give spore spewer slow falling
            sporeSpewer.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, TickUnits.convertSecondsToTicks(20), 1));
        }
    }

    protected class selectRandomLocationToVisit extends Goal
    {
        protected long lastTimeOfExecution = 0;
        protected long executionCooldown = TickUnits.convertSecondsToTicks(60);

        public boolean canUse()
        {
            if(level().getGameTime() - lastTimeOfExecution < executionCooldown)
            {
                return false;
            }

            return level().canSeeSky(blockPosition().above());
        }

        public boolean canContinueToUse()
        {
            return false;
        }

        public void start()
        {
            lastTimeOfExecution = level().getGameTime();
            moveTargetPoint = getRandomTravelLocationVec3();
            anchorPoint = BlockPos.containing(moveTargetPoint);
        }
    }

    protected class AttackStrategyGoal extends Goal {
        private int nextSweepTick;

        public boolean canUse() {
            LivingEntity target = getTarget();

            if(target == null)
            {
                return false;
            }
            else if(!canAttack(target, TargetingConditions.DEFAULT))
            {
                return false;
            }


            return true;
        }

        public void start() {

            if(level().canSeeSky(blockPosition().above()))
            {
                SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.INFECT;
                return;
            }

            this.nextSweepTick = this.adjustedTickDelay(10);
            SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.CIRCLE;
            this.setAnchorAboveTarget();
        }

        public void stop() {
            if(SculkPhantomEntity.this.attackPhase == SculkPhantomEntity.AttackPhase.INFECT)
            {
                return;
            }
            SculkPhantomEntity.this.anchorPoint = SculkPhantomEntity.this.level().getHeightmapPos(Heightmap.Types.MOTION_BLOCKING, SculkPhantomEntity.this.anchorPoint).above(10 + SculkPhantomEntity.this.random.nextInt(20));
        }

        public void tick() {
            if(SculkPhantomEntity.this.attackPhase == SculkPhantomEntity.AttackPhase.INFECT)
            {
                return;
            }


            if (SculkPhantomEntity.this.attackPhase == SculkPhantomEntity.AttackPhase.CIRCLE) {
                --this.nextSweepTick;
                if (this.nextSweepTick <= 0) {
                    SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.SWOOP;
                    this.setAnchorAboveTarget();
                    this.nextSweepTick = this.adjustedTickDelay((8 + SculkPhantomEntity.this.random.nextInt(4)) * 20);
                    SculkPhantomEntity.this.playSound(SoundEvents.PHANTOM_SWOOP, 10.0F, 0.95F + SculkPhantomEntity.this.random.nextFloat() * 0.1F);
                }
            }

        }

        private void setAnchorAboveTarget() {
            SculkPhantomEntity.this.anchorPoint = SculkPhantomEntity.this.getTarget().blockPosition().above(20 + SculkPhantomEntity.this.random.nextInt(20));
            if (SculkPhantomEntity.this.anchorPoint.getY() < SculkPhantomEntity.this.level().getSeaLevel()) {
                SculkPhantomEntity.this.anchorPoint = new BlockPos(SculkPhantomEntity.this.anchorPoint.getX(), SculkPhantomEntity.this.level().getSeaLevel() + 1, SculkPhantomEntity.this.anchorPoint.getZ());
            }

        }
    }

    abstract class MoveTargetGoal extends Goal {
        public MoveTargetGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        protected boolean touchingTarget() {
            return SculkPhantomEntity.this.moveTargetPoint.distanceToSqr(SculkPhantomEntity.this.getX(), SculkPhantomEntity.this.getY(), SculkPhantomEntity.this.getZ()) < 4.0D;
        }
    }

    protected class CircleAroundAnchorGoal extends MoveTargetGoal {
        private float angle;
        private float distance;
        private float height;
        private float clockwise;

        public boolean canUse() {
            return (SculkPhantomEntity.this.getTarget() == null || SculkPhantomEntity.this.attackPhase == SculkPhantomEntity.AttackPhase.CIRCLE)
                    && SculkPhantomEntity.this.attackPhase != SculkPhantomEntity.AttackPhase.INFECT;
        }

        public void start() {
            this.distance = 5.0F + SculkPhantomEntity.this.random.nextFloat() * 10.0F;
            this.height = -4.0F + SculkPhantomEntity.this.random.nextFloat() * 9.0F;
            this.clockwise = SculkPhantomEntity.this.random.nextBoolean() ? 1.0F : -1.0F;
            this.selectNext();
        }

        @Override
        public void stop() {
            super.stop();
        }

        public void tick() {
            if (SculkPhantomEntity.this.random.nextInt(this.adjustedTickDelay(350)) == 0) {
                //this.height = -4.0F + SculkPhantomEntity.this.random.nextFloat() * 9.0F;
                this.height = -4.0F + SculkPhantomEntity.this.random.nextFloat() * 9.0F;
            }

            if (SculkPhantomEntity.this.random.nextInt(this.adjustedTickDelay(250)) == 0) {
                ++this.distance;
                if (this.distance > 15.0F) {
                    this.distance = 5.0F;
                    this.clockwise = -this.clockwise;
                }
            }

            if (SculkPhantomEntity.this.random.nextInt(this.adjustedTickDelay(450)) == 0) {
                this.angle = SculkPhantomEntity.this.random.nextFloat() * 2.0F * (float)Math.PI;
                this.selectNext();
            }

            if (this.touchingTarget()) {
                this.selectNext();
            }

            if (SculkPhantomEntity.this.moveTargetPoint.y < SculkPhantomEntity.this.getY() && !SculkPhantomEntity.this.level().isEmptyBlock(SculkPhantomEntity.this.blockPosition().below(1))) {
                this.height = Math.max(1.0F, this.height);
                this.selectNext();
            }

            if (SculkPhantomEntity.this.moveTargetPoint.y > SculkPhantomEntity.this.getY() && !SculkPhantomEntity.this.level().isEmptyBlock(SculkPhantomEntity.this.blockPosition().above(1))) {
                this.height = Math.min(-1.0F, this.height);
                this.selectNext();
            }

        }

        private void selectNext() {
            if (BlockPos.ZERO.equals(SculkPhantomEntity.this.anchorPoint)) {
                SculkPhantomEntity.this.anchorPoint = SculkPhantomEntity.this.blockPosition();
            }

            this.angle += this.clockwise * 15.0F * ((float)Math.PI / 180F);
            SculkPhantomEntity.this.moveTargetPoint = Vec3.atLowerCornerOf(SculkPhantomEntity.this.anchorPoint).add((double)(this.distance * Mth.cos(this.angle)), (double)(-4.0F + this.height), (double)(this.distance * Mth.sin(this.angle)));
        }
    }

    class SweepAttackGoal extends MoveTargetGoal {
        private static final int CAT_SEARCH_TICK_DELAY = 20;
        private boolean isScaredOfCat;
        private int catSearchTick;

        public boolean canUse() {
            return SculkPhantomEntity.this.getTarget() != null && SculkPhantomEntity.this.attackPhase == SculkPhantomEntity.AttackPhase.SWOOP;
        }

        public boolean canContinueToUse() {
            LivingEntity livingentity = SculkPhantomEntity.this.getTarget();
            if (livingentity == null) {
                return false;
            } else if (!livingentity.isAlive()) {
                return false;
            } else {
                if (livingentity instanceof Player) {
                    Player player = (Player)livingentity;
                    if (livingentity.isSpectator() || player.isCreative()) {
                        return false;
                    }
                }

                if (!this.canUse()) {
                    return false;
                } else {
                    if (SculkPhantomEntity.this.tickCount > this.catSearchTick) {
                        this.catSearchTick = SculkPhantomEntity.this.tickCount + 20;
                        List<Cat> list = SculkPhantomEntity.this.level().getEntitiesOfClass(Cat.class, SculkPhantomEntity.this.getBoundingBox().inflate(16.0D), EntitySelector.ENTITY_STILL_ALIVE);

                        for(Cat cat : list) {
                            cat.hiss();
                        }

                        this.isScaredOfCat = !list.isEmpty();
                    }

                    return !this.isScaredOfCat;
                }
            }
        }

        public void start() {
        }

        public void stop() {
            SculkPhantomEntity.this.setTarget((LivingEntity)null);
            SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.CIRCLE;
        }

        public void tick() {
            LivingEntity livingentity = SculkPhantomEntity.this.getTarget();
            if (livingentity != null) {
                SculkPhantomEntity.this.moveTargetPoint = new Vec3(livingentity.getX(), livingentity.getY(0.5D), livingentity.getZ());
                if (SculkPhantomEntity.this.getBoundingBox().inflate((double)0.2F).intersects(livingentity.getBoundingBox())) {
                    SculkPhantomEntity.this.doHurtTarget(livingentity);
                    SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.CIRCLE;
                    if (!SculkPhantomEntity.this.isSilent()) {
                        SculkPhantomEntity.this.level().levelEvent(1039, SculkPhantomEntity.this.blockPosition(), 0);
                    }
                } else if (SculkPhantomEntity.this.horizontalCollision || SculkPhantomEntity.this.hurtTime > 0) {
                    SculkPhantomEntity.this.attackPhase = SculkPhantomEntity.AttackPhase.CIRCLE;
                }

            }
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

    protected class PhantomLookControl extends LookControl {
        public PhantomLookControl(Mob p_33235_) {
            super(p_33235_);
        }

        public void tick() {
        }
    }

    protected class PhantomMoveControl extends MoveControl {
        private float speed = 0.1F;

        public PhantomMoveControl(Mob mob) {
            super(mob);
        }

        public void tick() {
            if (SculkPhantomEntity.this.horizontalCollision) {
                SculkPhantomEntity.this.setYRot(SculkPhantomEntity.this.getYRot() + 180.0F);
                this.speed = 0.1F;
            }

            double d0 = SculkPhantomEntity.this.moveTargetPoint.x - SculkPhantomEntity.this.getX();
            double d1 = SculkPhantomEntity.this.moveTargetPoint.y - SculkPhantomEntity.this.getY();
            double d2 = SculkPhantomEntity.this.moveTargetPoint.z - SculkPhantomEntity.this.getZ();
            double d3 = Math.sqrt(d0 * d0 + d2 * d2);
            if (Math.abs(d3) > (double)1.0E-5F) {
                double d4 = 1.0D - Math.abs(d1 * (double)0.7F) / d3;
                d0 *= d4;
                d2 *= d4;
                d3 = Math.sqrt(d0 * d0 + d2 * d2);
                double d5 = Math.sqrt(d0 * d0 + d2 * d2 + d1 * d1);
                float f = SculkPhantomEntity.this.getYRot();
                float f1 = (float)Mth.atan2(d2, d0);
                float f2 = Mth.wrapDegrees(SculkPhantomEntity.this.getYRot() + 90.0F);
                float f3 = Mth.wrapDegrees(f1 * (180F / (float)Math.PI));
                SculkPhantomEntity.this.setYRot(Mth.approachDegrees(f2, f3, 4.0F) - 90.0F);
                SculkPhantomEntity.this.yBodyRot = SculkPhantomEntity.this.getYRot();
                if (Mth.degreesDifferenceAbs(f, SculkPhantomEntity.this.getYRot()) < 3.0F)
                {
                    this.speed = Mth.approach(this.speed, 1.8F, 0.005F * (1.8F / this.speed));
                }
                else
                {
                    this.speed = Mth.approach(this.speed, 0.2F, 0.025F);
                }

                float f4 = (float)(-(Mth.atan2(-d1, d3) * (double)(180F / (float)Math.PI)));
                SculkPhantomEntity.this.setXRot(f4);
                float f5 = SculkPhantomEntity.this.getYRot() + 90.0F;
                double d6 = (double)(this.speed * Mth.cos(f5 * ((float)Math.PI / 180F))) * Math.abs(d0 / d5);
                double d7 = (double)(this.speed * Mth.sin(f5 * ((float)Math.PI / 180F))) * Math.abs(d2 / d5);
                double d8 = (double)(this.speed * Mth.sin(f4 * ((float)Math.PI / 180F))) * Math.abs(d1 / d5);
                Vec3 vec3 = SculkPhantomEntity.this.getDeltaMovement();
                SculkPhantomEntity.this.setDeltaMovement(vec3.add((new Vec3(d6, d8, d7)).subtract(vec3).scale(0.2D)));
            }

        }
    }
}
