package com.github.sculkhorde.common.entity;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.function.Predicate;

import javax.annotation.Nullable;

import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.google.common.collect.Lists;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.util.Mth;
import net.minecraft.util.VisibleForDebug;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.FlyingMoveControl;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.GoalSelector;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.navigation.FlyingPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.util.AirAndWaterRandomPos;
import net.minecraft.world.entity.ai.util.AirRandomPos;
import net.minecraft.world.entity.ai.util.HoverRandomPos;
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

/**
 * A lot of this is copied from BeeEntity.java.
 * I do not want to learn how to use mixins, so I am just copying the code.
 */
public class SculkBeeHarvesterEntity extends Monster implements GeoEntity, FlyingAnimal {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Added common/entity/ SculkBeeHarvesterEntity.java<br>
     * Added client/model/entity/ SculkBeeHarvesterModel.java<br>
     * Added client/renderer/entity/ SculkBeeHarvesterRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 20F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 25F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.25F;

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    public static final float FLAP_DEGREES_PER_TICK = 120.32113F;
    public static final int TICKS_PER_FLAP = Mth.ceil(1.4959966F);
    public static final String TAG_CROPS_GROWN_SINCE_POLLINATION = "CropsGrownSincePollination";
    public static final String TAG_CANNOT_ENTER_HIVE_TICKS = "CannotEnterHiveTicks";
    public static final String TAG_TICKS_SINCE_POLLINATION = "TicksSincePollination";
    public static final String TAG_HAS_STUNG = "HasStung";
    public static final String TAG_HAS_NECTAR = "HasNectar";
    public static final String TAG_FLOWER_POS = "FlowerPos";
    public static final String TAG_HIVE_POS = "HivePos";
    protected int timeWithoutHive;
    protected boolean hasHiveInRange;
    protected int disruptorInRange;
    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(Bee.class, EntityDataSerializers.BYTE);
    protected static final int FLAG_ROLL = 2;
    protected static final int FLAG_HAS_STUNG = 4;
    protected static final int FLAG_HAS_NECTAR = 8;
    protected static final int STING_DEATH_COUNTDOWN = 1200;
    protected static final int TICKS_BEFORE_GOING_TO_KNOWN_FLOWER = 2400;
    protected static final int TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME = 3600;
    protected static final int MIN_ATTACK_DIST = 4;
    protected static final int MAX_CROPS_GROWABLE = 10;
    protected static final int POISON_SECONDS_NORMAL = 10;
    protected static final int POISON_SECONDS_HARD = 18;
    protected static final int TOO_FAR_DISTANCE = 64;
    protected static final int HIVE_CLOSE_ENOUGH_DISTANCE = 2;
    protected static final int PATHFIND_TO_HIVE_WHEN_CLOSER_THAN = 16;
    protected static final int HIVE_SEARCH_DISTANCE = 20;

    protected float rollAmount;
    protected float rollAmountO;
    protected int timeSinceSting;
    int ticksWithoutNectarSinceExitingHive;
    protected int numCropsGrownSincePollination;
    protected static final int COOLDOWN_BEFORE_LOCATING_NEW_HIVE = 200;
    int remainingCooldownBeforeLocatingNewHive;
    protected static final int COOLDOWN_BEFORE_LOCATING_NEW_FLOWER = 200;
    int remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(this.random, 20, 60);
    @Nullable
    BlockPos savedFlowerPos;
    @Nullable
    BlockPos hivePos;
    BeePollinateGoal beePollinateGoal;
    BeeGoToHiveGoal goToHiveGoal;
    protected BeeGoToKnownFlowerGoal goToKnownFlowerGoal;
    protected int underWaterTicks;
    protected int failedFlowerFindAttempts = 0;

    /** Constructors **/

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(EntityType<? extends SculkBeeHarvesterEntity> type, Level worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMoveControl(this, 20, true);
        this.lookControl = new BeeLookControl(this);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.COCOA, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.FENCE, -1.0F);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(Level worldIn)
    {
        this(ModEntities.SCULK_BEE_HARVESTER.get(), worldIn);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    public float getWalkTargetValue(BlockPos p_27788_, LevelReader p_27789_) {
        return p_27789_.getBlockState(p_27788_).isAir() ? 10.0F : 0.0F;
    }

    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeSupplier.Builder createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, 0.6F);
    }

    public void addAdditionalSaveData(CompoundTag compoundTag) {
        super.addAdditionalSaveData(compoundTag);
        if (this.hasHive()) {
            compoundTag.put(TAG_HIVE_POS, NbtUtils.writeBlockPos(this.getHivePos()));
        }

        if (this.hasSavedFlowerPos()) {
            compoundTag.put(TAG_FLOWER_POS, NbtUtils.writeBlockPos(this.getSavedFlowerPos()));
        }

        compoundTag.putBoolean(TAG_HAS_NECTAR, this.hasNectar());
        compoundTag.putBoolean(TAG_HAS_STUNG, this.hasStung());
        compoundTag.putInt(TAG_TICKS_SINCE_POLLINATION, this.ticksWithoutNectarSinceExitingHive);
        compoundTag.putInt(TAG_CROPS_GROWN_SINCE_POLLINATION, this.numCropsGrownSincePollination);
    }

    public void readAdditionalSaveData(CompoundTag compoundTag) {
        this.hivePos = null;
        if (compoundTag.contains(TAG_HIVE_POS)) {
            this.hivePos = NbtUtils.readBlockPos(compoundTag.getCompound(TAG_HIVE_POS));
        }

        this.savedFlowerPos = null;
        if (compoundTag.contains(TAG_FLOWER_POS)) {
            this.savedFlowerPos = NbtUtils.readBlockPos(compoundTag.getCompound(TAG_FLOWER_POS));
        }

        super.readAdditionalSaveData(compoundTag);
        this.setHasNectar(compoundTag.getBoolean(TAG_HAS_NECTAR));
        this.setHasStung(compoundTag.getBoolean(TAG_HAS_STUNG));
        this.ticksWithoutNectarSinceExitingHive = compoundTag.getInt(TAG_TICKS_SINCE_POLLINATION);
        this.numCropsGrownSincePollination = compoundTag.getInt(TAG_CROPS_GROWN_SINCE_POLLINATION);
    }


    /** ~~~~~~~~ ACCESSORS & Modifiers ~~~~~~~~ **/

    private final Predicate<BlockPos> IS_VALID_FLOWER = (blockPos) -> {
        BlockState blockState = this.level.getBlockState(blockPos);
        if (blockState.hasProperty(BlockStateProperties.WATERLOGGED) && blockState.getValue(BlockStateProperties.WATERLOGGED)) {
            return false;
        }
        else if (blockState.is(ModBlocks.BlockTags.SCULK_BEE_HARVESTABLE))
        {
            return true;
        }

        return false;
    };

    public double getArrivalThreshold() {
        return 0.1D;
    }

    public Predicate<BlockPos> getIsFlowerValidPredicate() {
        return this.IS_VALID_FLOWER;
    }

    @Nullable
    public BlockPos getSavedFlowerPos() {
        return this.savedFlowerPos;
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    public void setSavedFlowerPos(BlockPos blockPos) {
        this.savedFlowerPos = blockPos;
    }

    @VisibleForDebug
    public int getTravellingTicks() {
        return Math.max(this.goToHiveGoal.travellingTicks, this.goToKnownFlowerGoal.travellingTicks);
    }

    @VisibleForDebug
    public List<BlockPos> getBlacklistedHives() {
        return this.goToHiveGoal.blacklistedTargets;
    }


    @VisibleForDebug
    public boolean hasHive() {
        return getHivePos() != null;
    }

    @Nullable
    @VisibleForDebug
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    public boolean hasNectar() {
        return this.getFlag(8);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    public boolean isFood(ItemStack itemStack) {
        return itemStack.is(ItemTags.FLOWERS);
    }

    @VisibleForDebug
    public GoalSelector getGoalSelector() {
        return this.goalSelector;
    }

    public boolean isFlapping() {
        return this.isFlying() && this.tickCount % TICKS_PER_FLAP == 0;
    }


    public void resetTicksWithoutNectarSinceExitingHive() {
        this.ticksWithoutNectarSinceExitingHive = 0;
    }

    protected boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingHive > TICKS_WITHOUT_NECTAR_BEFORE_GOING_HOME || failedFlowerFindAttempts > 5;
    }

    protected int getCropsGrownSincePollination() {
        return this.numCropsGrownSincePollination;
    }

    protected void resetNumCropsGrownSincePollination() {
        this.numCropsGrownSincePollination = 0;
    }

    protected void incrementNumCropsGrownSincePollination() {
        ++this.numCropsGrownSincePollination;
    }

    protected boolean wantsToEnterHive() {
        if (this.isTiredOfLookingForNectar() || this.level.isRaining() || this.hasNectar() || this.hivePos == null || this.hivePos == BlockPos.ZERO)
        {
            return !isHiveNearFire();
        } else {
            return false;
        }
    }

    protected boolean isHiveNearFire() {
        if (this.hivePos == null) {
            return false;
        } else {
            BlockEntity blockentity = this.level.getBlockEntity(this.hivePos);
            return blockentity instanceof SculkBeeNestBlockEntity && ((SculkBeeNestBlockEntity)blockentity).isFireNearby();
        }
    }

    protected boolean doesHiveHaveSpace(BlockPos blockPos) {
        BlockEntity blockentity = this.level.getBlockEntity(blockPos);
        if (blockentity instanceof SculkBeeNestBlockEntity) {
            return !((SculkBeeNestBlockEntity)blockentity).isFull();
        } else {
            return false;
        }
    }

    protected boolean isHiveValid() {
        if (!this.hasHive()) {
            return false;
        } else if (this.isTooFarAway(this.hivePos)) {
            return false;
        } else {
            BlockEntity blockentity = this.level.getBlockEntity(this.hivePos);
            return blockentity instanceof SculkBeeNestBlockEntity;
        }
    }

    protected void setHasNectar(boolean hasNectar) {
        if (hasNectar) {
            this.resetTicksWithoutNectarSinceExitingHive();
        }

        this.setFlag(8, hasNectar);
    }

    protected void setHasStung(boolean hasStung) {
        this.setFlag(4, hasStung);
    }

    protected boolean isTooFarAway(BlockPos blockPos) {
        return !this.closerThan(blockPos, TOO_FAR_DISTANCE);
    }

    protected void setFlag(int p_27833_, boolean p_27834_) {
        if (p_27834_) {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | p_27833_));
        } else {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~p_27833_));
        }

    }

    protected boolean getFlag(int p_27922_) {
        return (this.entityData.get(DATA_FLAGS_ID) & p_27922_) != 0;
    }

    protected boolean isFlowerValid(BlockPos pos)
    {
        return this.level.isLoaded(pos) && getIsFlowerValidPredicate().test(pos);
    }


    /** ~~~~~~~~ Events ~~~~~~~~ **/

    // This method's only purposes is so that I can override it in the child class
    protected void executeCodeOnPollination()
    {

    }

    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < MAX_CROPS_GROWABLE && this.random.nextFloat() < 0.05F) {
            for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.spawnFluidParticle(this.level, this.getX() - (double)0.3F, this.getX() + (double)0.3F, this.getZ() - (double)0.3F, this.getZ() + (double)0.3F, this.getY(0.5D), ParticleTypes.FALLING_NECTAR);
            }
        }

        //this.updateRollAmount();
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide)
        {

            if (this.remainingCooldownBeforeLocatingNewHive > 0) {
                --this.remainingCooldownBeforeLocatingNewHive;
            }

            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                --this.remainingCooldownBeforeLocatingNewFlower;
            }

            boolean flag = !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < MIN_ATTACK_DIST;
            //this.setRolling(flag);
            if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
        }

    }

    @Override
    protected void customServerAiStep() {
        boolean flag = this.hasStung();
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(this.damageSources().drown(), 1.0F);
        }

        if (flag) {
            ++this.timeSinceSting;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(Mth.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurt(this.damageSources().generic(), this.getHealth());
            }
        }

        if (!this.hasNectar()) {
            ++this.ticksWithoutNectarSinceExitingHive;
        }

    }

    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SculkBeeHarvesterEntity.BeeEnterHiveGoal());
        this.beePollinateGoal = new SculkBeeHarvesterEntity.BeePollinateGoal();
        this.goalSelector.addGoal(4, this.beePollinateGoal);
        this.goalSelector.addGoal(5, new SculkBeeHarvesterEntity.BeeLocateHiveGoal());
        this.goToHiveGoal = new SculkBeeHarvesterEntity.BeeGoToHiveGoal();
        this.goalSelector.addGoal(5, this.goToHiveGoal);
        this.goToKnownFlowerGoal = new SculkBeeHarvesterEntity.BeeGoToKnownFlowerGoal();
        this.goalSelector.addGoal(6, this.goToKnownFlowerGoal);
        this.goalSelector.addGoal(7, new SculkBeeHarvesterEntity.BeeWanderGoal());
        this.goalSelector.addGoal(8, new FloatGoal(this));
        //this.targetSelector.addGoal(1, (new SculkBeeHarvesterEntity.BeeHurtByOtherGoal(this)).setAlertOthers(new Class[0]));
    }


    @Override
    protected PathNavigation createNavigation(Level p_27815_) {
        FlyingPathNavigation flyingpathnavigation = new FlyingPathNavigation(this, p_27815_) {
            public boolean isStableDestination(BlockPos p_27947_) {
                return !this.level.getBlockState(p_27947_.below()).isAir();
            }

            public void tick() {
                if (!SculkBeeHarvesterEntity.this.beePollinateGoal.isPollinating()) {
                    super.tick();
                }
            }
        };
        flyingpathnavigation.setCanOpenDoors(false);
        flyingpathnavigation.setCanFloat(false);
        flyingpathnavigation.setCanPassDoors(true);
        return flyingpathnavigation;
    }

    protected void spawnFluidParticle(Level p_27780_, double p_27781_, double p_27782_, double p_27783_, double p_27784_, double p_27785_, ParticleOptions p_27786_) {
        p_27780_.addParticle(p_27786_, Mth.lerp(p_27780_.random.nextDouble(), p_27781_, p_27782_), p_27785_, Mth.lerp(p_27780_.random.nextDouble(), p_27783_, p_27784_), 0.0D, 0.0D, 0.0D);
    }

    public void dropOffNectar() {
        this.setHasNectar(false);
        this.resetNumCropsGrownSincePollination();
    }

    public boolean hurt(DamageSource p_27762_, float p_27763_) {
        if (this.isInvulnerableTo(p_27762_)) {
            return false;
        } else {
            if (!this.level.isClientSide) {
                this.beePollinateGoal.stopPollinating();
            }

            return super.hurt(p_27762_, p_27763_);
        }
    }

    public MobType getMobType() {
        return MobType.ARTHROPOD;
    }

    public Vec3 getLeashOffset() {
        return new Vec3(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
    }

    protected void pathfindRandomlyTowards(BlockPos p_27881_) {
        Vec3 vec3 = Vec3.atBottomCenterOf(p_27881_);
        int i = 0;
        BlockPos blockpos = this.blockPosition();
        int j = (int)vec3.y - blockpos.getY();
        if (j > 2) {
            i = 4;
        } else if (j < -2) {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int i1 = blockpos.distManhattan(p_27881_);
        if (i1 < 15) {
            k = i1 / 2;
            l = i1 / 2;
        }

        Vec3 vec31 = AirRandomPos.getPosTowards(this, k, l, i, vec3, (double)((float)Math.PI / 10F));
        if (vec31 != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(vec31.x, vec31.y, vec31.z, 1.0D);
        }
    }

    protected void jumpInLiquidInternal() {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/
    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(DefaultAnimations.genericFlyController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    boolean closerThan(BlockPos p_27817_, int p_27818_) {
        return p_27817_.closerThan(this.blockPosition(), (double)p_27818_);
    }

    /**
     * We override this and keep it blank so that this mob doesnt not despawn
     */
    @Override
    public void checkDespawn() {}

    public boolean dampensVibrations() {
        return true;
    }

    protected void checkFallDamage(double p_27754_, boolean p_27755_, BlockState p_27756_, BlockPos p_27757_) {
    }

    /* DO NOT USE THIS FOR ANYTHING, CAUSES DESYNC
    @Override
    public void onRemovedFromWorld() {
        SculkHorde.savedData.addSculkAccumulatedMass((int) this.getHealth());
        super.onRemovedFromWorld();
    }
    */

    @Override
    public boolean isFlying() {
        return true;
    }

    /* ~~~~~~~~ Classes ~~~~~~~~ */

    protected abstract class BaseBeeGoal extends Goal {
        public abstract boolean canBeeUse();

        public abstract boolean canBeeContinueToUse();

        public boolean canUse() {
            return this.canBeeUse();
        }

        public boolean canContinueToUse() {
            return this.canBeeContinueToUse();
        }
    }

    protected class BeeAttackGoal extends MeleeAttackGoal {
        BeeAttackGoal(PathfinderMob p_27960_, double p_27961_, boolean p_27962_) {
            super(p_27960_, p_27961_, p_27962_);
        }

        public boolean canUse() {
            return super.canUse() && !SculkBeeHarvesterEntity.this
                    .hasStung();
        }

        public boolean canContinueToUse() {
            return super.canContinueToUse() && !SculkBeeHarvesterEntity.this
                    .hasStung();
        }
    }

    protected class BeeEnterHiveGoal extends SculkBeeHarvesterEntity.BaseBeeGoal {
        public boolean canBeeUse() {
            if (SculkBeeHarvesterEntity.this
                    .hasHive() && SculkBeeHarvesterEntity.this
                    .wantsToEnterHive() && SculkBeeHarvesterEntity.this
                    .hivePos.closerToCenterThan(SculkBeeHarvesterEntity.this
                            .position(), 2.0D)) {
                BlockEntity blockentity = SculkBeeHarvesterEntity.this
                        .level.getBlockEntity(SculkBeeHarvesterEntity.this
                                .hivePos);
                if (blockentity instanceof SculkBeeNestBlockEntity) {
                    SculkBeeNestBlockEntity SculkBeeNestBlockEntity = (SculkBeeNestBlockEntity)blockentity;
                    if (!SculkBeeNestBlockEntity.isFull()) {
                        return true;
                    }

                    SculkBeeHarvesterEntity.this
                            .hivePos = null;
                }
            }

            return false;
        }

        public boolean canBeeContinueToUse() {
            return false;
        }

        public void start() {
            BlockEntity blockentity = SculkBeeHarvesterEntity.this
                    .level.getBlockEntity(SculkBeeHarvesterEntity.this
                            .hivePos);
            if (blockentity instanceof SculkBeeNestBlockEntity SculkBeeNestBlockEntity)
            {
                failedFlowerFindAttempts = 0;
                SculkBeeNestBlockEntity.addOccupant(SculkBeeHarvesterEntity.this
                        , SculkBeeHarvesterEntity.this
                                .hasNectar());
            }

        }
    }

    @VisibleForDebug
    public class BeeGoToHiveGoal extends SculkBeeHarvesterEntity.BaseBeeGoal {
        public static final int MAX_TRAVELLING_TICKS = TickUnits.convertMinutesToTicks(2);
        int travellingTicks = SculkBeeHarvesterEntity.this
                .level.random.nextInt(10);
        protected static final int MAX_BLACKLISTED_TARGETS = 3;
        final List<BlockPos> blacklistedTargets = Lists.newArrayList();
        @Nullable
        protected Path lastPath;
        protected static final int TICKS_BEFORE_HIVE_DROP = 60;
        protected int ticksStuck;

        BeeGoToHiveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canBeeUse() {

            if(SculkBeeHarvesterEntity.this.hivePos == null)
            {
                return false;
            }

            if(SculkBeeHarvesterEntity.this.hasRestriction())
            {
                return false;
            }

            if(this.hasReachedTarget(SculkBeeHarvesterEntity.this.hivePos))
            {
                return false;
            }

            if(!SculkBeeHarvesterEntity.this.level.getBlockState(SculkBeeHarvesterEntity.this.hivePos).is(ModBlocks.SCULK_BEE_NEST_BLOCK.get()))
            {
                return false;
            }

            return true;
        }

        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        public void start() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        public void stop() {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            SculkBeeHarvesterEntity.this
                    .navigation.stop();
            SculkBeeHarvesterEntity.this
                    .navigation.resetMaxVisitedNodesMultiplier();
        }

        public void tick() {
            if (SculkBeeHarvesterEntity.this
                    .hivePos != null) {
                ++this.travellingTicks;
                if (this.travellingTicks > this.adjustedTickDelay(MAX_TRAVELLING_TICKS)) {
                    this.dropAndBlacklistHive();
                } else if (!SculkBeeHarvesterEntity.this
                        .navigation.isInProgress()) {
                    if (!SculkBeeHarvesterEntity.this
                            .closerThan(SculkBeeHarvesterEntity.this
                                    .hivePos, 16)) {
                        if (SculkBeeHarvesterEntity.this
                                .isTooFarAway(SculkBeeHarvesterEntity.this
                                        .hivePos)) {
                            this.dropHive();
                        } else {
                            SculkBeeHarvesterEntity.this
                                    .pathfindRandomlyTowards(SculkBeeHarvesterEntity.this
                                            .hivePos);
                        }
                    } else {
                        boolean flag = this.pathfindDirectlyTowards(SculkBeeHarvesterEntity.this
                                .hivePos);
                        if (!flag) {
                            this.dropAndBlacklistHive();
                        } else if (this.lastPath != null && SculkBeeHarvesterEntity.this
                                .navigation.getPath().sameAs(this.lastPath)) {
                            ++this.ticksStuck;
                            if (this.ticksStuck > TICKS_BEFORE_HIVE_DROP) {
                                this.dropHive();
                                this.ticksStuck = 0;
                            }
                        } else {
                            this.lastPath = SculkBeeHarvesterEntity.this
                                    .navigation.getPath();
                        }

                    }
                }
            }
        }

        protected boolean pathfindDirectlyTowards(BlockPos p_27991_) {
            SculkBeeHarvesterEntity.this
                    .navigation.setMaxVisitedNodesMultiplier(10.0F);
            SculkBeeHarvesterEntity.this
                    .navigation.moveTo((double)p_27991_.getX(), (double)p_27991_.getY(), (double)p_27991_.getZ(), 1.0D);
            return SculkBeeHarvesterEntity.this
                    .navigation.getPath() != null && SculkBeeHarvesterEntity.this
                    .navigation.getPath().canReach();
        }

        protected boolean isTargetBlacklisted(BlockPos p_27994_) {
            return this.blacklistedTargets.contains(p_27994_);
        }

        protected void blacklistTarget(BlockPos p_27999_) {
            this.blacklistedTargets.add(p_27999_);

            while(this.blacklistedTargets.size() > MAX_BLACKLISTED_TARGETS) {
                this.blacklistedTargets.remove(0);
            }

        }

        protected void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        protected void dropAndBlacklistHive() {
            if (SculkBeeHarvesterEntity.this
                    .hivePos != null) {
                this.blacklistTarget(SculkBeeHarvesterEntity.this
                        .hivePos);
            }

            this.dropHive();
        }

        protected void dropHive() {
            SculkBeeHarvesterEntity.this
                    .hivePos = null;
            SculkBeeHarvesterEntity.this
                    .remainingCooldownBeforeLocatingNewHive = 200;
        }

        protected boolean hasReachedTarget(BlockPos p_28002_) {
            if (SculkBeeHarvesterEntity.this
                    .closerThan(p_28002_, 2)) {
                return true;
            } else {
                Path path = SculkBeeHarvesterEntity.this
                        .navigation.getPath();
                return path != null && path.getTarget().equals(p_28002_) && path.canReach() && path.isDone();
            }
        }
    }

    public class BeeGoToKnownFlowerGoal extends BaseBeeGoal {
        protected static final int MAX_TRAVELLING_TICKS = TickUnits.convertMinutesToTicks(1);
        int travellingTicks = level.random.nextInt(10);

        BeeGoToKnownFlowerGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canBeeUse() {
            return savedFlowerPos != null && !hasRestriction() && this.wantsToGoToKnownFlower() && isFlowerValid(savedFlowerPos) && !closerThan(savedFlowerPos, 2);
        }

        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        public void stop() {
            this.travellingTicks = 0;
            navigation.stop();
            navigation.resetMaxVisitedNodesMultiplier();
        }

        public void tick() {
            if (savedFlowerPos != null) {
                ++this.travellingTicks;
                if (this.travellingTicks > this.adjustedTickDelay(MAX_TRAVELLING_TICKS)) {
                    savedFlowerPos = null;
                } else if (!navigation.isInProgress()) {
                    if (isTooFarAway(savedFlowerPos)) {
                        savedFlowerPos = null;
                    } else {
                        pathfindRandomlyTowards(savedFlowerPos);
                    }
                }
            }
        }

        protected boolean wantsToGoToKnownFlower() {
            return ticksWithoutNectarSinceExitingHive > TICKS_BEFORE_GOING_TO_KNOWN_FLOWER;
        }
    }

    protected class BeeLocateHiveGoal extends BaseBeeGoal {
        public boolean canBeeUse() {
            return remainingCooldownBeforeLocatingNewHive == 0 && !hasHive() && wantsToEnterHive();
        }

        public boolean canBeeContinueToUse() {
            return false;
        }

        public void start() {
            remainingCooldownBeforeLocatingNewHive = 200;
            List<BlockPos> list = this.findNearbyHivesWithSpace();
            if (!list.isEmpty()) {
                for(BlockPos blockpos : list) {
                    if (!goToHiveGoal.isTargetBlacklisted(blockpos) && BlockAlgorithms.getBlockDistance(blockpos, SculkBeeHarvesterEntity.this.blockPosition()) <= TOO_FAR_DISTANCE) {
                        hivePos = blockpos;
                        return;
                    }
                }

                goToHiveGoal.clearBlacklist();
                hivePos = list.get(0);
                return;
            }
            return;
        }

        private final Predicate<BlockState> VALID_HIVE_BLOCKS = (validBlocksPredicate) ->
        {
            if (validBlocksPredicate.is(ModBlocks.SCULK_BEE_NEST_BLOCK.get()))
            {
                return true;
            }
            return false;

        };

        protected List<BlockPos> findNearbyHivesWithSpace()
        {
            List<BlockPos> list = BlockAlgorithms.getBlocksInArea(
                    (ServerLevel) SculkBeeHarvesterEntity.this.level,
                    SculkBeeHarvesterEntity.this.blockPosition(),
                    VALID_HIVE_BLOCKS,
                    TOO_FAR_DISTANCE - 1
            );

            //Remove hives from list without space
            for(int index = 0; index < list.size(); index++)
            {
                if(!SculkBeeHarvesterEntity.this.doesHiveHaveSpace(list.get(index)))
                {
                    list.remove(index);
                    index--;
                }
            }
            return list;
        }
    }

    protected class BeeLookControl extends LookControl {
        BeeLookControl(Mob p_28059_) {
            super(p_28059_);
        }

        public void tick()
        {
            super.tick();
        }

        protected boolean resetXRotOnTick() {
            return !beePollinateGoal.isPollinating();
        }
    }

    protected class BeePollinateGoal extends BaseBeeGoal {
        protected static final int MIN_POLLINATION_TICKS = 400;
        protected static final int MIN_FIND_FLOWER_RETRY_COOLDOWN = 20 * 60 * 15; // 15 Seconds
        protected static final int MAX_FIND_FLOWER_RETRY_COOLDOWN = 20 * 60 * 30; // 30 Seconds
        protected static final int POSITION_CHANGE_CHANCE = 25;
        protected static final float SPEED_MODIFIER = 0.35F;
        protected static final float HOVER_HEIGHT_WITHIN_FLOWER = 0.6F;
        protected static final float HOVER_POS_OFFSET = 0.33333334F;
        protected int successfulPollinatingTicks;
        protected int lastSoundPlayedTick;
        protected boolean pollinating;
        @Nullable
        protected Vec3 hoverPos;
        protected int pollinatingTicks;
        protected static final int MAX_POLLINATING_TICKS = 600;

        BeePollinateGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canBeeUse() {
            if (remainingCooldownBeforeLocatingNewFlower > 0 || failedFlowerFindAttempts > 5) {
                return false;
            } else if (hasNectar()) {
                return false;
            } else if (level.isRaining()) {
                return false;
            } else {
                Optional<BlockPos> optional = this.findNearbyFlower();
                if (optional.isPresent())
                {
                    savedFlowerPos = optional.get();
                    navigation.moveTo((double)savedFlowerPos.getX() + 0.5D, (double)savedFlowerPos.getY(), (double)savedFlowerPos.getZ() + 0.5D, (double)1.2F);
                    return true;
                } else {
                    failedFlowerFindAttempts = 0;
                    remainingCooldownBeforeLocatingNewFlower = Mth.nextInt(random, MIN_FIND_FLOWER_RETRY_COOLDOWN, MAX_FIND_FLOWER_RETRY_COOLDOWN);
                    return false;
                }
            }
        }

        public boolean canBeeContinueToUse() {
            if (!this.pollinating) {
                return false;
            } else if (!hasSavedFlowerPos()) {
                return false;
            } else if (level.isRaining()) {
                return false;
            } else if (this.hasPollinatedLongEnough()) {
                return random.nextFloat() < 0.2F;
            } else if (tickCount % 20 == 0 && !isFlowerValid(savedFlowerPos)) {
                savedFlowerPos = null;
                return false;
            } else {
                return true;
            }
        }

        protected boolean hasPollinatedLongEnough() {
            return this.successfulPollinatingTicks > MIN_POLLINATION_TICKS;
        }

        protected boolean isPollinating() {
            return this.pollinating;
        }

        protected void stopPollinating() {
            this.pollinating = false;
        }

        public void start() {
            this.successfulPollinatingTicks = 0;
            this.pollinatingTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.pollinating = true;
            resetTicksWithoutNectarSinceExitingHive();
        }

        public void stop() {
            if (this.hasPollinatedLongEnough()) {
                setHasNectar(true);
                executeCodeOnPollination();
            }

            this.pollinating = false;
            navigation.stop();
            remainingCooldownBeforeLocatingNewFlower = 200;
        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        public void tick() {
            ++this.pollinatingTicks;
            if (this.pollinatingTicks > MAX_POLLINATING_TICKS) {
                savedFlowerPos = null;
            } else {
                Vec3 vec3 = Vec3.atBottomCenterOf(savedFlowerPos).add(0.0D, (double)HOVER_HEIGHT_WITHIN_FLOWER, 0.0D);
                if (vec3.distanceTo(position()) > 1.0D) {
                    this.hoverPos = vec3;
                    this.setWantedPos();
                } else {
                    if (this.hoverPos == null) {
                        this.hoverPos = vec3;
                    }

                    boolean flag = position().distanceTo(this.hoverPos) <= getArrivalThreshold();
                    boolean flag1 = true;
                    if (!flag && this.pollinatingTicks > MAX_POLLINATING_TICKS) {
                        savedFlowerPos = null;
                    } else {
                        if (flag) {
                            boolean flag2 = random.nextInt(POSITION_CHANGE_CHANCE) == 0;
                            if (flag2) {
                                this.hoverPos = new Vec3(vec3.x() + (double)this.getOffset(), vec3.y(), vec3.z() + (double)this.getOffset());
                                navigation.stop();
                            } else {
                                flag1 = false;
                            }

                            getLookControl().setLookAt(vec3.x(), vec3.y(), vec3.z());
                        }

                        if (flag1) {
                            this.setWantedPos();
                        }

                        ++this.successfulPollinatingTicks;
                        if (random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60) {
                            this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                            playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }

        protected void setWantedPos() {
            getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), (double)SPEED_MODIFIER);
        }

        protected float getOffset() {
            return (random.nextFloat() * 2.0F - 1.0F) * HOVER_POS_OFFSET;
        }

        protected Optional<BlockPos> findNearbyFlower() {
            return BlockAlgorithms.findBlockInCubeBlockPosPredicate((ServerLevel) level, blockPosition(), getIsFlowerValidPredicate(), 5);
        }

    }

    protected class BeeWanderGoal extends Goal {
        protected static final int WANDER_THRESHOLD = TOO_FAR_DISTANCE - 1;

        BeeWanderGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return navigation.isDone() && random.nextInt(10) == 0;
        }

        public boolean canContinueToUse() {
            return navigation.isInProgress();
        }

        public void start() {
            Vec3 vec3 = this.findPos();
            if (vec3 != null) {
                navigation.moveTo(navigation.createPath(BlockPos.containing(vec3), 1), 1.0D);
            }

        }

        @Nullable
        protected Vec3 findPos() {
            Vec3 vec3;
            if (isHiveValid() && !closerThan(hivePos, WANDER_THRESHOLD)) {
                Vec3 vec31 = Vec3.atCenterOf(hivePos);
                vec3 = vec31.subtract(position()).normalize();
            } else {
                vec3 = getViewVector(0.0F);
            }

            int i = 8;
            Vec3 vec32 = HoverRandomPos.getPos(SculkBeeHarvesterEntity.this, 8, 7, vec3.x, vec3.z, ((float)Math.PI / 2F), 3, 1);
            return vec32 != null ? vec32 : AirAndWaterRandomPos.getPos(SculkBeeHarvesterEntity.this, 8, 4, -2, vec3.x, vec3.z, (double)((float)Math.PI / 2F));
        }
    }
}
