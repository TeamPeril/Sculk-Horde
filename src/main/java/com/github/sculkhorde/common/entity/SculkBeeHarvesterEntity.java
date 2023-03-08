package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.common.block.SculkFloraBlock;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.common.tileentity.SculkBeeNestTile;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.entity.*;
import net.minecraft.entity.ai.RandomPositionGenerator;
import net.minecraft.entity.ai.attributes.AttributeModifierMap;
import net.minecraft.entity.ai.attributes.Attributes;
import net.minecraft.entity.ai.controller.FlyingMovementController;
import net.minecraft.entity.ai.controller.LookController;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.LookRandomlyGoal;
import net.minecraft.entity.ai.goal.SwimGoal;
import net.minecraft.entity.passive.IFlyingAnimal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.fluid.Fluid;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.particles.IParticleData;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.pathfinding.FlyingPathNavigator;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathNavigator;
import net.minecraft.pathfinding.PathNodeType;
import net.minecraft.tags.ITag;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundEvent;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.Difficulty;
import net.minecraft.world.IWorld;
import net.minecraft.world.IWorldReader;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import software.bernie.geckolib3.core.IAnimatable;
import software.bernie.geckolib3.core.PlayState;
import software.bernie.geckolib3.core.builder.AnimationBuilder;
import software.bernie.geckolib3.core.controller.AnimationController;
import software.bernie.geckolib3.core.event.predicate.AnimationEvent;
import software.bernie.geckolib3.core.manager.AnimationData;
import software.bernie.geckolib3.core.manager.AnimationFactory;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class SculkBeeHarvesterEntity extends SculkLivingEntity implements IAnimatable, IFlyingAnimal {

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

    public static final int SPAWN_Y_MAX =15;

    protected AnimationFactory factory = new AnimationFactory(this);

    protected static final DataParameter<Byte> DATA_FLAGS_ID = EntityDataManager.defineId(SculkBeeHarvesterEntity.class, DataSerializers.BYTE);
    protected static final DataParameter<Integer> DATA_REMAINING_ANGER_TIME = EntityDataManager.defineId(SculkBeeHarvesterEntity.class, DataSerializers.INT);
    protected float rollAmount;
    protected float rollAmountO;
    protected int timeSinceSting;
    protected int ticksWithoutNectarSinceExitingHive;
    protected int stayOutOfHiveCountdown;
    protected int numCropsGrownSincePollination;
    protected int remainingCooldownBeforeLocatingNewHive = 0;
    protected int remainingCooldownBeforeLocatingNewFlower = 0;
    @Nullable
    protected BlockPos savedFlowerPos = null;
    @Nullable
    protected BlockPos hivePos = null;
    protected PollinateGoal beePollinateGoal;
    protected SculkBeeHarvesterEntity.FindBeehiveGoal goToHiveGoal;
    protected SculkBeeHarvesterEntity.FindFlowerGoal goToKnownFlowerGoal;
    protected int underWaterTicks;
    protected final int COOLDOWN_FIND_NEW_HIVE = 1200;

    /** Constructors **/

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(EntityType<? extends SculkBeeHarvesterEntity> type, World worldIn) {
        super(type, worldIn);
        this.moveControl = new FlyingMovementController(this, 20, true);
        this.lookControl = new SculkBeeHarvesterEntity.BeeLookController(this);
        this.setPathfindingMalus(PathNodeType.DANGER_FIRE, -1.0F);
        this.setPathfindingMalus(PathNodeType.WATER, -1.0F);
        this.setPathfindingMalus(PathNodeType.WATER_BORDER, 16.0F);
        this.setPathfindingMalus(PathNodeType.COCOA, -1.0F);
        this.setPathfindingMalus(PathNodeType.FENCE, -1.0F);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkBeeHarvesterEntity(World worldIn)
    {
        this(EntityRegistry.SCULK_BEE_HARVESTER, worldIn);
    }

    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
        this.entityData.define(DATA_REMAINING_ANGER_TIME, 0);
    }

    @Override
    protected boolean shouldDespawnInPeaceful() {
        return false;
    }

    /**----------Accessor Methods----------**/


    /**
     * Determines & registers the attributes of the mob.
     * @return The Attributes
     */
    public static AttributeModifierMap.MutableAttribute createAttributes()
    {
        return LivingEntity.createLivingAttributes()
                .add(Attributes.MAX_HEALTH, MAX_HEALTH)
                .add(Attributes.FOLLOW_RANGE,FOLLOW_RANGE)
                .add(Attributes.MOVEMENT_SPEED, MOVEMENT_SPEED)
                .add(Attributes.FLYING_SPEED, (double)0.6F);
    }

    /**
     * This function tells minecraft how much xp should drop when
     * a player kills this mob.
     * @param player The player that killed this mob
     * @return The XP Amount
     */
    @Override
    protected int getExperienceReward(PlayerEntity player)
    {
        return 3;
    }

    @Nullable
    public BlockPos getHivePos() {
        return this.hivePos;
    }

    protected int getCropsGrownSincePollination() {
        return this.numCropsGrownSincePollination;
    }

    protected boolean getFlag(int pFlagId) {
        return (this.entityData.get(DATA_FLAGS_ID) & pFlagId) != 0;
    }

    protected SoundEvent getAmbientSound() {
        return null;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.BEE_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.BEE_DEATH;
    }


    /**
     * Gets the animation factory
     * @return ???
     */
    @Override
    public AnimationFactory getFactory()
    {
        return this.factory;
    }

    public float getWalkTargetValue(BlockPos pPos, IWorldReader pLevel)
    {
        return pLevel.getBlockState(pPos).isAir() ? 10.0F : 0.0F;
    }

    protected float getStandingEyeHeight(Pose pPose, EntitySize pSize) {
        return this.isBaby() ? pSize.height * 0.5F : pSize.height * 0.5F;
    }

    @OnlyIn(Dist.CLIENT)
    public Vector3d getLeashOffset() {
        return new Vector3d(0.0D, (double)(0.5F * this.getEyeHeight()), (double)(this.getBbWidth() * 0.2F));
    }

    public SculkBeeHarvesterEntity getBreedOffspring(ServerWorld pServerLevel, AgeableEntity pMate) {
        return EntityRegistry.SCULK_BEE_HARVESTER.create(pServerLevel);
    }

    /**
     * Returns the volume for the sounds this mob makes.
     */
    protected float getSoundVolume() {
        return 0.4F;
    }

    @Nullable
    public BlockPos getSavedFlowerPos() {
        return this.savedFlowerPos;
    }

    @OnlyIn(Dist.CLIENT)
    public float getRollAmount(float pPartialTick) {
        return MathHelper.lerp(pPartialTick, this.rollAmountO, this.rollAmount);
    }

    /**----------Modifier Methods----------**/

    protected void resetNumCropsGrownSincePollination() {
        this.numCropsGrownSincePollination = 0;
    }

    protected void incrementNumCropsGrownSincePollination() {
        ++this.numCropsGrownSincePollination;
    }

    protected void setHasNectar(boolean pHasNectar) {
        if (pHasNectar) {
            this.resetTicksWithoutNectarSinceExitingHive();
        }

        this.setFlag(8, pHasNectar);
    }

    protected void setHasStung(boolean pHasStung) {
        this.setFlag(4, pHasStung);
    }

    protected void setRolling(boolean pIsRolling) {
        this.setFlag(2, pIsRolling);
    }

    public void setSavedFlowerPos(BlockPos pSavedFlowerPos) {
        this.savedFlowerPos = pSavedFlowerPos;
    }


    protected void setFlag(int pFlagId, boolean pValue)
    {
        if (pValue)
        {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) | pFlagId));
        }
        else
        {
            this.entityData.set(DATA_FLAGS_ID, (byte)(this.entityData.get(DATA_FLAGS_ID) & ~pFlagId));
        }

    }

    public void resetTicksWithoutNectarSinceExitingHive() {
        this.ticksWithoutNectarSinceExitingHive = 0;
    }

    public void setStayOutOfHiveCountdown(int pStayOutOfHiveCountdown) {
        this.stayOutOfHiveCountdown = pStayOutOfHiveCountdown;
    }

    protected void updateRollAmount() {
        this.rollAmountO = this.rollAmount;
        if (this.isRolling()) {
            this.rollAmount = Math.min(1.0F, this.rollAmount + 0.2F);
        } else {
            this.rollAmount = Math.max(0.0F, this.rollAmount - 0.24F);
        }

    }

    /**----------Boolean Methods----------**/

    public boolean hasHive() {
        return this.hivePos != null;
    }

    protected boolean isHiveValid() {
        if (!this.hasHive()) {
            return false;
        } else {
            TileEntity tileentity = this.level.getBlockEntity(this.hivePos);
            return tileentity instanceof SculkBeeNestTile;
        }
    }

    protected boolean doesHiveHaveSpace(BlockPos pHivePos)
    {
        TileEntity tileentity = this.level.getBlockEntity(pHivePos);
        //TileEntity tileentity = .getTileEntity(this.level, pHivePos);
        if (tileentity instanceof SculkBeeNestTile)
        {
            return !((SculkBeeNestTile)tileentity).isFull();
        }
        else if(tileentity == null)
        {
            return false;
        }
        else
        {
            return false;
        }
    }

    public boolean hasNectar()
    {
        return this.getFlag(8);
    }

    public boolean hasStung() {
        return this.getFlag(4);
    }

    protected boolean isRolling() {
        return this.getFlag(2);
    }

    protected boolean isTooFarAway(BlockPos pPos) {
        return !this.closerThan(pPos, 32);
    }

    protected boolean isFlowerValid(BlockPos pPos)
    {
        return this.level.isLoaded(pPos) & getFlowerPredicate().test(this.level.getBlockState(pPos));
    }

    public boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    protected boolean isTiredOfLookingForNectar() {
        return this.ticksWithoutNectarSinceExitingHive > 3600;
    }

    protected boolean isHiveNearFire() {
        if (this.hivePos == null) {
            return false;
        } else {
            TileEntity tileentity = this.level.getBlockEntity(this.hivePos);
            return tileentity instanceof SculkBeeNestTile && ((SculkBeeNestTile)tileentity).isFireNearby();
        }
    }

    protected boolean wantsToEnterHive() {
        if (this.stayOutOfHiveCountdown <= 0 && !this.beePollinateGoal.isPollinating() && !this.hasStung() && this.getTarget() == null) {
            boolean flag = this.isTiredOfLookingForNectar() || this.hasNectar();
            return flag && !this.isHiveNearFire();
        } else {
            return false;
        }
    }

    /**----------Event Methods----------**/


    /**
     * Called to update the entity's position/logic.
     */
    public void tick() {
        super.tick();
        if (this.hasNectar() && this.getCropsGrownSincePollination() < 10 && this.random.nextFloat() < 0.05F) {
            for(int i = 0; i < this.random.nextInt(2) + 1; ++i) {
                this.spawnFluidParticle(this.level, this.getX() - (double)0.3F, this.getX() + (double)0.3F, this.getZ() - (double)0.3F, this.getZ() + (double)0.3F, this.getY(0.5D), ParticleTypes.FALLING_NECTAR);
            }
        }

        this.updateRollAmount();
    }


    /**
     * Called every tick so the entity can update its state as required. For example, zombies and skeletons use this to
     * react to sunlight and start to burn.
     */
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            if (this.stayOutOfHiveCountdown > 0) {
                --this.stayOutOfHiveCountdown;
            }

            if (this.remainingCooldownBeforeLocatingNewHive > 0) {
                --this.remainingCooldownBeforeLocatingNewHive;
            }

            if (this.remainingCooldownBeforeLocatingNewFlower > 0) {
                --this.remainingCooldownBeforeLocatingNewFlower;
            }

            boolean flag = !this.hasStung() && this.getTarget() != null && this.getTarget().distanceToSqr(this) < 4.0D;
            this.setRolling(flag);
            if (this.tickCount % 20 == 0 && !this.isHiveValid()) {
                this.hivePos = null;
            }
        }

    }


    protected void customServerAiStep()
    {
        boolean flag = this.hasStung();
        if (this.isInWaterOrBubble()) {
            ++this.underWaterTicks;
        } else {
            this.underWaterTicks = 0;
        }

        if (this.underWaterTicks > 20) {
            this.hurt(DamageSource.DROWN, 1.0F);
        }

        if (flag) {
            ++this.timeSinceSting;
            if (this.timeSinceSting % 5 == 0 && this.random.nextInt(MathHelper.clamp(1200 - this.timeSinceSting, 1, 1200)) == 0) {
                this.hurt(DamageSource.GENERIC, this.getHealth());
            }
        }

        if (!this.hasNectar()) {
            ++this.ticksWithoutNectarSinceExitingHive;
        }

        if (!this.level.isClientSide) {
            //this.updatePersistentAnger((ServerWorld)this.level, false);
        }

    }

    /**
     * The function that determines if a position is a good spawn location<br>
     * @param config ???
     * @param world The world that the mob is trying to spawn in
     * @param reason An object that indicates why a mob is being spawned
     * @param pos The Block Position of the potential spawn location
     * @param random ???
     * @return Returns a boolean determining if it is a suitable spawn location
     */
    public static boolean passSpawnCondition(EntityType<? extends CreatureEntity> config, IWorld world, SpawnReason reason, BlockPos pos, Random random)
    {
        // If peaceful, return false
        if (world.getDifficulty() == Difficulty.PEACEFUL) return false;
            // If not because of chunk generation or natural, return false
        else if (reason != SpawnReason.CHUNK_GENERATION && reason != SpawnReason.NATURAL) return false;
            //If above SPAWN_Y_MAX and the block below is not sculk crust, return false
        else if (pos.getY() > SPAWN_Y_MAX && world.getBlockState(pos.below()).getBlock() != BlockRegistry.CRUST.get()) return false;
        return true;
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
            this.goalSelector.addGoal(priority, targetSelectorPayload[priority]);
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
    private Goal[] goalSelectorPayload()
    {
        this.beePollinateGoal = new SculkBeeHarvesterEntity.PollinateGoal();
        this.goToHiveGoal = new SculkBeeHarvesterEntity.FindBeehiveGoal();
        this.goToKnownFlowerGoal = new SculkBeeHarvesterEntity.FindFlowerGoal();

        Goal[] goals =
                {
                        new SculkBeeHarvesterEntity.UpdateBeehiveGoal(),
                        new SculkBeeHarvesterEntity.EnterBeehiveGoal(),
                        this.beePollinateGoal,
                        this.goToHiveGoal,
                        this.goToKnownFlowerGoal,


                        //LookRandomlyGoal(mob)
                        new LookRandomlyGoal(this),
                        new SculkBeeHarvesterEntity.WanderGoal(),
                        new SwimGoal(this),
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
    private Goal[] targetSelectorPayload()
    {
        Goal[] goals =
                {
                        new TargetAttacker(this).setAlertSculkLivingEntities(),
                };
        return goals;
    }

    /** ~~~~~~~~ ANIMATION ~~~~~~~~ **/
    private <E extends IAnimatable> PlayState predicate(AnimationEvent<E> event)
    {
        event.getController().setAnimation(new AnimationBuilder().addAnimation("animation.sculk_bee.flying", true));
        return PlayState.CONTINUE;
    }

    @Override
    public void registerControllers(AnimationData data) {
        data.addAnimationController(new AnimationController(this, "controller", 0, this::predicate));
    }

    /** ~~~~~~~~ Save Data ~~~~~~~~ **/

    public void addAdditionalSaveData(CompoundNBT pCompound) {
        super.addAdditionalSaveData(pCompound);
        if (this.hasHive())
        {
            pCompound.put("HivePos", NBTUtil.writeBlockPos(this.getHivePos()));
        }

        if (this.hasSavedFlowerPos())
        {
            pCompound.put("FlowerPos", NBTUtil.writeBlockPos(this.getSavedFlowerPos()));
        }

        pCompound.putBoolean("HasNectar", this.hasNectar());
        pCompound.putBoolean("HasStung", this.hasStung());
        pCompound.putInt("TicksSincePollination", this.ticksWithoutNectarSinceExitingHive);
        pCompound.putInt("CannotEnterHiveTicks", this.stayOutOfHiveCountdown);
        pCompound.putInt("CropsGrownSincePollination", this.numCropsGrownSincePollination);
        //this.addPersistentAngerSaveData(pCompound);
    }

    /**
     * (abstract) Protected helper method to read subclass entity data from NBT.
     */
    public void readAdditionalSaveData(CompoundNBT pCompound)
    {
        this.hivePos = null;
        if (pCompound.contains("HivePos")) {
            this.hivePos = NBTUtil.readBlockPos(pCompound.getCompound("HivePos"));
        }

        this.savedFlowerPos = null;
        if (pCompound.contains("FlowerPos")) {
            this.savedFlowerPos = NBTUtil.readBlockPos(pCompound.getCompound("FlowerPos"));
        }

        super.readAdditionalSaveData(pCompound);
        this.setHasNectar(pCompound.getBoolean("HasNectar"));
        this.setHasStung(pCompound.getBoolean("HasStung"));
        this.ticksWithoutNectarSinceExitingHive = pCompound.getInt("TicksSincePollination");
        this.stayOutOfHiveCountdown = pCompound.getInt("CannotEnterHiveTicks");
        this.numCropsGrownSincePollination = pCompound.getInt("CropsGrownSincePollination");
        if(!level.isClientSide) //FORGE: allow this entity to be read from nbt on client. (Fixes MC-189565)
        {
            //this.readPersistentAngerSaveData((ServerWorld)this.level, pCompound);
        }
    }


    /**
     * Returns new PathNavigateGround instance
     */
    protected PathNavigator createNavigation(World pLevel) {
        FlyingPathNavigator flyingpathnavigator = new FlyingPathNavigator(this, pLevel) {
            public boolean isStableDestination(BlockPos pPos) {
                return !this.level.getBlockState(pPos.below()).isAir();
            }

            public void tick()
            {
                if (!SculkBeeHarvesterEntity.this.beePollinateGoal.isPollinating()) {
                    super.tick();
                }
            }
        };
        flyingpathnavigator.setCanOpenDoors(false);
        flyingpathnavigator.setCanFloat(false);
        flyingpathnavigator.setCanPassDoors(true);
        return flyingpathnavigator;
    }

    public boolean causeFallDamage(float pFallDistance, float pDamageMultiplier) {
        return false;
    }

    protected void checkFallDamage(double pY, boolean pOnGround, BlockState pState, BlockPos pPos) {
    }

    protected boolean makeFlySound() {
        return true;
    }

    public void dropOffNectar() {
        this.setHasNectar(false);
        this.resetNumCropsGrownSincePollination();
    }

    /**
     * Called when the entity is attacked.
     */
    public boolean hurt(DamageSource pSource, float pAmount) {
        if (this.isInvulnerableTo(pSource)) {
            return false;
        } else {
            Entity entity = pSource.getEntity();
            if (!this.level.isClientSide) {
                this.beePollinateGoal.stopPollinating();
            }

            return super.hurt(pSource, pAmount);
        }
    }

    protected void jumpInLiquid(ITag<Fluid> pFluidTag) {
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.01D, 0.0D));
    }

    protected boolean closerThan(BlockPos pPos, int pDistance)
    {
        return pPos.closerThan(this.blockPosition(), (double)pDistance);
    }

    protected void spawnFluidParticle(World pLevel, double pStartX, double pEndX, double pStartZ, double pEndZ, double pPosY, IParticleData pParticleOption)
    {
        pLevel.addParticle(pParticleOption, MathHelper.lerp(pLevel.random.nextDouble(), pStartX, pEndX), pPosY, MathHelper.lerp(pLevel.random.nextDouble(), pStartZ, pEndZ), 0.0D, 0.0D, 0.0D);
    }

    protected void pathfindRandomlyTowards(BlockPos pPos)
    {
        //Get the bottom center position of the block
        Vector3d targetPos = Vector3d.atBottomCenterOf(pPos);

        int i = 0;
        BlockPos blockpos = this.blockPosition();
        int height = (int)targetPos.y - blockpos.getY();

        if (height > 2)
        {
            i = 4;
        }
        else if (height < -2)
        {
            i = -4;
        }

        int k = 6;
        int l = 8;
        int i1 = blockpos.distManhattan(pPos);

        if (i1 < 15)
        {
            k = i1 / 2;
            l = i1 / 2;
        }

        Vector3d vector3d1 = RandomPositionGenerator.getAirPosTowards(this, k, l, i, targetPos, (double)((float)Math.PI / 10F));
        if (vector3d1 != null) {
            this.navigation.setMaxVisitedNodesMultiplier(0.5F);
            this.navigation.moveTo(vector3d1.x, vector3d1.y, vector3d1.z, 1.0D);
        }
    }

    /**
     * We override this and keep it blank so that this mob doesnt not despawn
     */
    @Override
    public void checkDespawn() {}


    /**----------CLASSES----------**/


    public class BeeLookController extends LookController
    {
        BeeLookController(MobEntity pMob)
        {
            super(pMob);
        }

        /**
         * Updates look
         */
        public void tick()
        {
            super.tick();
        }

        protected boolean resetXRotOnTick()
        {
            return !SculkBeeHarvesterEntity.this.beePollinateGoal.isPollinating();
        }
    }

    public abstract class PassiveGoal extends Goal
    {
        /**
         * Constructor
         */
        public PassiveGoal() {}

        public abstract boolean canBeeUse();

        public abstract boolean canBeeContinueToUse();

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse() {return this.canBeeUse();}

        /**
         * Returns whether an in-progress EntityAIBase should continue executing
         */
        public boolean canContinueToUse() {
            return this.canBeeContinueToUse();
        }
    }

    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Currently determines if a block is a valid flower.
     */
    private final Predicate<BlockState> VALID_POLLINATION_BLOCKS = (validBlocksPredicate) ->
    {
        return validBlocksPredicate.getBlock() instanceof SculkFloraBlock;
    };

    protected Predicate<BlockState> getFlowerPredicate()
    {
        return VALID_POLLINATION_BLOCKS;
    }

    protected class PollinateGoal extends PassiveGoal
    {

        protected int successfulPollinatingTicks = 0;
        protected int lastSoundPlayedTick = 0;
        protected boolean pollinating;
        protected Vector3d hoverPos;
        protected int pollinatingTicks = 0;

        /**
         * Constructor
         */
        PollinateGoal()
        {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Determines if the goal can execute. <br>
         * Will do various checks for false cases and will
         * have a random chance to return false regardless.
         *
         * If none of this is the case, then it will find a flower,
         * save its position, then navigate to it.
         * @return true if goal can execute, false otherwise
         */
        public boolean canBeeUse()
        {
            //If under cool-down, return false
            if (SculkBeeHarvesterEntity.this.remainingCooldownBeforeLocatingNewFlower > 0)
            {
                return false;
            }
            //If entity has nectar, return false
            else if (SculkBeeHarvesterEntity.this.hasNectar())
            {
                return false;
            }
            //There is a random chance to return false
            else if (SculkBeeHarvesterEntity.this.random.nextFloat() < 0.7F)
            {
                return false;
            }
            //Find a flower, go to it
            else
            {
                Optional<BlockPos> optional = this.findNearbyFlower();
                if (optional.isPresent())
                {
                    SculkBeeHarvesterEntity.this.savedFlowerPos = optional.get();
                    SculkBeeHarvesterEntity.this.navigation.moveTo(
                            (double)SculkBeeHarvesterEntity.this.savedFlowerPos.getX() + 0.5D,
                            (double)SculkBeeHarvesterEntity.this.savedFlowerPos.getY() + 0.5D,
                            (double)SculkBeeHarvesterEntity.this.savedFlowerPos.getZ() + 0.5D,
                            (double)1.2F
                    );

                    return true;
                }
                else
                {
                    return false;
                }
            }
        }//END OF canBeeUse()

        /**
         * Will determine if the goal should continue to execute. <br>
         * Does various checks
         * @return True/False
         */
        public boolean canBeeContinueToUse()
        {
            //If we arent pollinating, return false
            if (!this.pollinating)
            {
                return false;
            }
            //If no flower position stored in memory, return false
            else if (!SculkBeeHarvesterEntity.this.hasSavedFlowerPos())
            {
                return false;
            }
            //If pollinating for too long, random chance to return false
            else if (this.hasPollinatedLongEnough())
            {
                return true;
            }
            //If tickcount is divisable by 20 and flower is not valid, wipe memory and return false
            else if (SculkBeeHarvesterEntity.this.tickCount % 20 == 0
                    && !SculkBeeHarvesterEntity.this.isFlowerValid(SculkBeeHarvesterEntity.this.savedFlowerPos))
            {
                SculkBeeHarvesterEntity.this.savedFlowerPos = null;
                return false;
            }
            //Otherwise return true
            else
            {
                return true;
            }
        }

        /**
         * @return true if pollinating for more than x, false otherwise
         */
        protected boolean hasPollinatedLongEnough()
        {
            return this.successfulPollinatingTicks > 400;
        }

        /**
         * @return TRUE if pollinating, FALSE otherwise
         */
        protected boolean isPollinating()
        {
            return this.pollinating;
        }

        /**
         * Sets this.pollinating to FALSE
         */
        protected void stopPollinating()
        {
            this.pollinating = false;
        }

        /**
         * Execute the goal. <br>
         *
         */
        public void start()
        {
            this.successfulPollinatingTicks = 0;
            this.pollinatingTicks = 0;
            this.lastSoundPlayedTick = 0;
            this.pollinating = true;
            SculkBeeHarvesterEntity.this.resetTicksWithoutNectarSinceExitingHive();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop()
        {
            //If entity has pollinated for enough time
            if (this.hasPollinatedLongEnough())
            {
                //Set entity to have nectar
                SculkBeeHarvesterEntity.this.setHasNectar(true);
            }

            //Set pollination to false
            this.pollinating = false;
            //Stop navigation
            SculkBeeHarvesterEntity.this.navigation.stop();
            //reset cooldown
            SculkBeeHarvesterEntity.this.remainingCooldownBeforeLocatingNewFlower = 200;
        }

        /**
         * Gets executed every tick this goal is being executed
         */
        public void tick()
        {
            int MAX_POLLINATION_TICKS = 600;
            ++this.pollinatingTicks;//Keep track how long we have been pollinating

            //if pollinating for x amount of ticks
            if (this.pollinatingTicks > MAX_POLLINATION_TICKS)
            {
                //Erase flower from memory
                SculkBeeHarvesterEntity.this.savedFlowerPos = null;
            }
            //If not have reached x amount of ticks
            else
            {
                //Flower Position
                Vector3d offsetFlowerPos = Vector3d.atBottomCenterOf(SculkBeeHarvesterEntity.this.savedFlowerPos).add(0.0D, (double)0.6F, 0.0D);

                //If not close enough to the flower, go there
                if (offsetFlowerPos.distanceTo(SculkBeeHarvesterEntity.this.position()) > 1.0D)
                {
                    this.hoverPos = offsetFlowerPos; //Set the hover pos
                    this.setWantedPos(); //Let the navigator know we want to be there
                }
                //If close enough
                else
                {
                    //If hoverpos not set yet, set it
                    if (this.hoverPos == null)
                    {
                        this.hoverPos = offsetFlowerPos;
                    }

                    //Track if the entity is in range of the flower
                    boolean isInRangeOfFlower = SculkBeeHarvesterEntity.this.position().distanceTo(this.hoverPos) <= 0.1D;
                    boolean isHoverPosOffseted = true;

                    //If not in range and over max pollination ticks
                    if (!isInRangeOfFlower && this.pollinatingTicks > MAX_POLLINATION_TICKS)
                    {
                        //wipe flower from memory
                        SculkBeeHarvesterEntity.this.savedFlowerPos = null;
                    }
                    //If in range of flower and not over max pollination ticks
                    else
                    {
                        //If in range of flower
                        if (isInRangeOfFlower)
                        {
                            //Calculate chance of success
                            boolean randomChanceOfSuccess = SculkBeeHarvesterEntity.this.random.nextInt(25) == 0;

                            //If rng success
                            if (randomChanceOfSuccess)
                            {
                                //Randomly Offset hover position on x and z axis
                                this.hoverPos = new Vector3d(
                                        offsetFlowerPos.x() + (double)this.getRandomOffset(),
                                        offsetFlowerPos.y(),
                                        offsetFlowerPos.z() + (double)this.getRandomOffset()
                                );

                                //Stop navigating
                                SculkBeeHarvesterEntity.this.navigation.stop();
                            }
                            else
                            {
                                isHoverPosOffseted = false;
                            }

                            SculkBeeHarvesterEntity.this.getLookControl().setLookAt(offsetFlowerPos.x(), offsetFlowerPos.y(), offsetFlowerPos.z());
                        }

                        if (isHoverPosOffseted)
                        {
                            //Set hover pos to actual position of
                            this.setWantedPos();
                        }

                        ++this.successfulPollinatingTicks; //Track successful pollination ticks

                        //Given random chance and x amount of time
                        if (SculkBeeHarvesterEntity.this.random.nextFloat() < 0.05F && this.successfulPollinatingTicks > this.lastSoundPlayedTick + 60)
                        {
                            //Play a sound
                            this.lastSoundPlayedTick = this.successfulPollinatingTicks;
                            SculkBeeHarvesterEntity.this.playSound(SoundEvents.BEE_POLLINATE, 1.0F, 1.0F);
                        }

                    }
                }
            }
        }//END OF tick()

        /**
         * Tell the navigator we want to be at the hoverPos
         */
        protected void setWantedPos()
        {
            SculkBeeHarvesterEntity.this.getMoveControl().setWantedPosition(this.hoverPos.x(), this.hoverPos.y(), this.hoverPos.z(), (double)0.35F);
        }

        /**
         * Choose a random float
         * @return a random float
         */
        protected float getRandomOffset()
        {
            return (SculkBeeHarvesterEntity.this.random.nextFloat() * 2.0F - 1.0F) * 0.33333334F;
        }

        /**
         * Finds the position closest flower
         * @return the BlockPos of the flower
         */
        protected Optional<BlockPos> findNearbyFlower()
        {
            return this.findNearestBlock(getFlowerPredicate(), 32.0D);
        }

        /**
         * Finds the closes block within a distance
         * @param pPredicate The Block
         * @param pDistance The search distance
         * @return The Position of the block
         */
        protected Optional<BlockPos> findNearestBlock(Predicate<BlockState> pPredicate, double pDistance)
        {
            //The origin of our search
            BlockPos blockpos = SculkBeeHarvesterEntity.this.blockPosition();
            //?
            BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

            //Search area for block
            for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
            {
                for(int j = 0; (double)j < pDistance; ++j)
                {
                    for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
                    {
                        for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
                        {
                            blockpos$mutable.setWithOffset(blockpos, k, i - 1, l);

                            //If the block is close enough and is the right blockstate
                            if (blockpos.closerThan(blockpos$mutable, pDistance)
                                    && pPredicate.test(SculkBeeHarvesterEntity.this.level.getBlockState(blockpos$mutable)))
                            {
                                return Optional.of(blockpos$mutable); //Return position
                            }
                        }
                    }
                }
            }
            //else return empty
            return Optional.empty();
        }
    } //END POLLINATE GOAL

    /**
     * Tells an entity to enter a hive
     */
    protected class EnterBeehiveGoal extends SculkBeeHarvesterEntity.PassiveGoal
    {
        /**
         * Constructor
         */
        protected EnterBeehiveGoal(){}

        /**
         * Determines if this goal can execute
         * @return true/false
         */
        public boolean canBeeUse()
        {
            //If we have a hive, want to enter it, and its within 2 blocks
            if (SculkBeeHarvesterEntity.this.hasHive() &&
                    SculkBeeHarvesterEntity.this.wantsToEnterHive() &&
                    SculkBeeHarvesterEntity.this.hivePos.closerThan(SculkBeeHarvesterEntity.this.position(), 2.0D))
            {
                //Get the hive tile entity
                TileEntity tileentity = SculkBeeHarvesterEntity.this.level.getBlockEntity(SculkBeeHarvesterEntity.this.hivePos);

                //If its the right instance
                if (tileentity instanceof SculkBeeNestTile)
                {
                    //convert to hive tile entity
                    SculkBeeNestTile beehivetileentity = (SculkBeeNestTile)tileentity;
                    //If it isnt full, the entity can execute the goal
                    if (!beehivetileentity.isFull())
                    {
                        return true;
                    }
                    //Else, erase the hive from our memory
                    SculkBeeHarvesterEntity.this.hivePos = null;
                }
            }

            //Return false otherwise
            return false;
        }

        /**
         * This is a one shot task, there is no continue to use so return false
         * @return false
         */
        public boolean canBeeContinueToUse()
        {
            return false;
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start()
        {
            //Get hive tile entity
            TileEntity tileentity = SculkBeeHarvesterEntity.this.level.getBlockEntity(SculkBeeHarvesterEntity.this.hivePos);
            //If right instance
            if (tileentity instanceof SculkBeeNestTile)
            {
                //Add entity to hive
                SculkBeeNestTile beehivetileentity = (SculkBeeNestTile)tileentity;
                beehivetileentity.addOccupant(SculkBeeHarvesterEntity.this);
            }

        }
    }

    /**
     * Things that this goal does <br>
     * -Blacklist hives that take too long to get to. <br>
     * -Deletes hives from memory that are too far. <br>
     * -Tells entity to go to hive if not navigating. <br>
     * -Checks to see if entity gets stuck.
     */
    protected class FindBeehiveGoal extends SculkBeeHarvesterEntity.PassiveGoal
    {
        private int travellingTicks = SculkBeeHarvesterEntity.this.level.random.nextInt(10);
        private List<BlockPos> blacklistedTargets = Lists.newArrayList();
        @Nullable
        private Path lastPath = null;
        private int ticksStuck;

        /**
         * Constructor
         */
        FindBeehiveGoal()
        {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Deterrmines if we can start this goal
         * @return true/false
         */
        public boolean canBeeUse()
        {
            return SculkBeeHarvesterEntity.this.hivePos != null
                    && !SculkBeeHarvesterEntity.this.hasRestriction()
                    && SculkBeeHarvesterEntity.this.wantsToEnterHive()
                    && !this.hasReachedTarget(SculkBeeHarvesterEntity.this.hivePos)
                    && SculkBeeHarvesterEntity.this.level.getBlockState(SculkBeeHarvesterEntity.this.hivePos).is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get());
        }

        /**
         * Determines if this goal can continue to execute
         * @return true/false
         */
        public boolean canBeeContinueToUse()
        {
            return this.canBeeUse();
        }

        /**
         * Execute a one shot task or start executing a continuous task
         */
        public void start()
        {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            super.start();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop()
        {
            this.travellingTicks = 0;
            this.ticksStuck = 0;
            SculkBeeHarvesterEntity.this.navigation.stop();
            SculkBeeHarvesterEntity.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        /**
         * Keep ticking a continuous task that has already been started <br>
         * -Tells entity to go to hive if not navigating. <br>
         * -Blacklist hives that take too long to get to. <br>
         * -Deletes hives from memory that are too far. <br>
         * -Checks to see if entity gets stuck.
         */
        public void tick()
        {
            //if we have a saved hive
            if (SculkBeeHarvesterEntity.this.hivePos != null)
            {
                ++this.travellingTicks; //Track how long the bee is traveling


                //If we have been traveling too long
                //TODO REMOVE TEMPORARY DISABLED FEATURE
                if (false && this.travellingTicks > 600)
                {
                    //Blacklist hive
                    this.dropAndBlacklistHive();
                }

                //If we are not navigating somewhere
                else if (!SculkBeeHarvesterEntity.this.navigation.isInProgress())
                {
                    //If the hive is not closer than x
                    if (!SculkBeeHarvesterEntity.this.closerThan(SculkBeeHarvesterEntity.this.hivePos, 16))
                    {
                        //If the hive is  too far
                        //TODO REMOVE TEMPORARY DISABLED FEATURE
                        if (false && SculkBeeHarvesterEntity.this.isTooFarAway(SculkBeeHarvesterEntity.this.hivePos))
                        {
                            //Delete from memory
                            this.dropHive();
                        }
                        //If hive isnt too far
                        else
                        {
                            //Go to it
                            SculkBeeHarvesterEntity.this.pathfindRandomlyTowards(SculkBeeHarvesterEntity.this.hivePos);
                        }
                    }
                    //If we havent been traveling for too long, and we are currently navigating
                    else
                    {
                        //Store If a path exist and the hive is reachable
                        boolean flag = this.pathfindDirectlyTowards(SculkBeeHarvesterEntity.this.hivePos);

                        //If a path does not exist or the hive is not reachable
                        if (!flag)
                        {
                            this.dropAndBlacklistHive(); //Blacklist hive
                        }

                        //If we are on the same path
                        //TODO REMOVE TEMPORARY DISABLED FEATURE
                        else if (false && this.lastPath != null && SculkBeeHarvesterEntity.this.navigation.getPath().sameAs(this.lastPath))
                        {
                            ++this.ticksStuck; //Keep track to see if we are stuck

                            //If we are stuck
                            if (this.ticksStuck > 60)
                            {
                                //Remove hive from memory and reset stuck ticks
                                this.dropHive();
                                this.ticksStuck = 0;
                            }
                        }
                        //Else set last path to our current path
                        else
                        {
                            this.lastPath = SculkBeeHarvesterEntity.this.navigation.getPath();
                        }

                    }
                }
            }
        }//END OF TICK()

        /**
         * Tells the entity to move towards a position
         * @param pPos The target position
         * @return If it is possible to reach
         */
        private boolean pathfindDirectlyTowards(BlockPos pPos)
        {
            SculkBeeHarvesterEntity.this.navigation.setMaxVisitedNodesMultiplier(10.0F);
            SculkBeeHarvesterEntity.this.navigation.moveTo((double)pPos.getX(), (double)pPos.getY(), (double)pPos.getZ(), 1.0D);
            return SculkBeeHarvesterEntity.this.navigation.getPath() != null && SculkBeeHarvesterEntity.this.navigation.getPath().canReach();
        }

        /**
         * Just checks if a position is blacklisted
         * @param pPos The target position
         * @return True if blacklisted, false if not
         */
        private boolean isTargetBlacklisted(BlockPos pPos) {
            return this.blacklistedTargets.contains(pPos);
        }

        /**
         * Blacklists up to 3 positions
         * @param pPos The position
         */
        private void blacklistTarget(BlockPos pPos)
        {
            this.blacklistedTargets.add(pPos);

            //Remove blacklisted positions until = 3
            while(this.blacklistedTargets.size() > 3)
            {
                this.blacklistedTargets.remove(0);
            }

        }

        /**
         * Just clears the blacklist
         */
        private void clearBlacklist() {
            this.blacklistedTargets.clear();
        }

        /**
         * Removes a hive from memory and black lists it
         */
        private void dropAndBlacklistHive()
        {
            if (SculkBeeHarvesterEntity.this.hivePos != null)
            {
                this.blacklistTarget(SculkBeeHarvesterEntity.this.hivePos);
            }

            this.dropHive();
        }

        /**
         * Removes hive from memory
         */
        private void dropHive()
        {
            SculkBeeHarvesterEntity.this.hivePos = null;
            SculkBeeHarvesterEntity.this.remainingCooldownBeforeLocatingNewHive = COOLDOWN_FIND_NEW_HIVE;
        }

        /**
         * If the entity is closer than 2 blocks, then it has reached target
         * @param pPos The target Position
         * @return
         */
        private boolean hasReachedTarget(BlockPos pPos)
        {
            if (SculkBeeHarvesterEntity.this.closerThan(pPos, 2))
            {
                return true;
            }
            else
            {
                Path path = SculkBeeHarvesterEntity.this.navigation.getPath();
                return path != null && path.getTarget().equals(pPos) && path.canReach() && path.isDone();
            }
        }
    }

    /**
     * This goal makes the bee, if possible, to go to a known flower.
     */
    protected class FindFlowerGoal extends SculkBeeHarvesterEntity.PassiveGoal {
        private int travellingTicks = SculkBeeHarvesterEntity.this.level.random.nextInt(10);

        /**
         * Constructor
         */
        FindFlowerGoal()
        {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Determines if the bee should start the FindFlowerGoal goal.
         * @return
         */
        public boolean canBeeUse()
        {
            return SculkBeeHarvesterEntity.this.savedFlowerPos != null
                    && !SculkBeeHarvesterEntity.this.hasRestriction()
                    && this.wantsToGoToKnownFlower()
                    && SculkBeeHarvesterEntity.this.isFlowerValid(SculkBeeHarvesterEntity.this.savedFlowerPos)
                    && !SculkBeeHarvesterEntity.this.closerThan(SculkBeeHarvesterEntity.this.savedFlowerPos, 2);
        }

        /**
         * Determines if the goal should continue to exectute
         * @return true/false
         */
        public boolean canBeeContinueToUse() {
            return this.canBeeUse();
        }

        /**
         * start executing the task
         */
        public void start() {
            this.travellingTicks = 0;
            super.start();
        }

        /**
         * Reset the task's internal state. Called when this task is interrupted by another one
         */
        public void stop() {
            this.travellingTicks = 0;
            SculkBeeHarvesterEntity.this.navigation.stop();
            SculkBeeHarvesterEntity.this.navigation.resetMaxVisitedNodesMultiplier();
        }

        /**
         * Keep ticking a continuous task that has already been started. <br>
         * This routine controls the bee going to a known flower
         */
        public void tick() {

            //If we know the position of a flower
            if (SculkBeeHarvesterEntity.this.savedFlowerPos != null)
            {
                //Keep track of how long we have been traveling
                ++this.travellingTicks;

                //If we have been traveling for more than 600 ticks
                if (this.travellingTicks > 600)
                {
                    //It is too far away and reset our saved flower position
                    SculkBeeHarvesterEntity.this.savedFlowerPos = null;
                }
                //If we are done navigating
                else if (!SculkBeeHarvesterEntity.this.navigation.isInProgress())
                {
                    //If the flower is too far away, reset flower position
                    if (SculkBeeHarvesterEntity.this.isTooFarAway(SculkBeeHarvesterEntity.this.savedFlowerPos))
                    {
                        SculkBeeHarvesterEntity.this.savedFlowerPos = null;
                    }
                    //If it is not too far away, go to flower
                    else
                    {
                        SculkBeeHarvesterEntity.this.pathfindRandomlyTowards(SculkBeeHarvesterEntity.this.savedFlowerPos);
                    }
                }
            }
        }

        /**
         * Determines if a bee should seek out the known flower.
         * @return true/false
         */
        private boolean wantsToGoToKnownFlower() {
            return SculkBeeHarvesterEntity.this.ticksWithoutNectarSinceExitingHive > 2400;
        }
    }


    /**
     * Update what this entity considers its home
     */
    protected class UpdateBeehiveGoal extends SculkBeeHarvesterEntity.PassiveGoal {

        /**
         * Constructor
         */
        protected UpdateBeehiveGoal() {}

        /**
         * Determines if this goal can start. <br>
         * If the bee has no cool down, doesnt have a hive, and wants to enter one.
         * Then we can locate a new hive
         * @return true/false
         */
        public boolean canBeeUse()
        {
            //TODO: UNDO CHANGES IF NECESSARY
            return SculkBeeHarvesterEntity.this.remainingCooldownBeforeLocatingNewHive == 0
                    && !SculkBeeHarvesterEntity.this.hasHive()
                    /*&& SculkBeeHarvesterEntity.this.wantsToEnterHive()*/;
        }

        /**
         * Determines if the goal can continue to execute
         * @return false
         */
        public boolean canBeeContinueToUse() {
            return false;
        }

        /**
         * Execute the goal
         */
        public void start()
        {
            //Set up cooldown
            SculkBeeHarvesterEntity.this.remainingCooldownBeforeLocatingNewHive = COOLDOWN_FIND_NEW_HIVE;
            //Initialize list of possible hives
            List<BlockPos> list = this.findNearbyHivesWithSpace();

            //If the list is not empty
            if (!list.isEmpty())
            {
                //Loop through list
                for(BlockPos blockpos : list)
                {
                    //If this hive is not blacklisted
                    if (!SculkBeeHarvesterEntity.this.goToHiveGoal.isTargetBlacklisted(blockpos))
                    {
                        //Make our new hive
                        SculkBeeHarvesterEntity.this.hivePos = blockpos;
                        return;
                    }
                }

                //If we were not able to find a good hive, just set our hive to
                //the first one in the list
                SculkBeeHarvesterEntity.this.goToHiveGoal.clearBlacklist();
                SculkBeeHarvesterEntity.this.hivePos = list.get(0);
            }
        }

        /**
         * Determines what is a valid hive block for a bee
         */
        private final Predicate<BlockState> VALID_HIVE_BLOCKS = (validBlocksPredicate) ->
        {
            if (validBlocksPredicate.is(BlockRegistry.SCULK_BEE_NEST_BLOCK.get()))
            {
                return true;
            }
            return false;

        };

        /**
         * Returns a list of the positions of blocks that fit the predicate VALID_HIVE_BLOCKS
         * @return The Array List of block positions
         */
        private List<BlockPos> findNearbyHivesWithSpace()
        {
            List<BlockPos> list = BlockAlgorithms.getBlocksInArea(
                    (ServerWorld) SculkBeeHarvesterEntity.this.level,
                    SculkBeeHarvesterEntity.this.blockPosition(),
                    VALID_HIVE_BLOCKS,
                    20
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

    /**
     * Allow the entity to fly randomly around
     */
    protected class WanderGoal extends Goal
    {
        /**
         * Constructor
         */
        WanderGoal()
        {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        /**
         * Returns whether execution should begin. You can also read and cache any state necessary for execution in this
         * method as well.
         */
        public boolean canUse()
        {
            //If not currently navigating and given a random chance
            return SculkBeeHarvesterEntity.this.navigation.isDone()
                    && SculkBeeHarvesterEntity.this.savedFlowerPos == null
                    //&& SculkBeeHarvesterEntity.this.hasNectar() == true
                    && SculkBeeHarvesterEntity.this.random.nextInt(10) == 0;
        }

        /**
         * Returns whether this goal should continue executing
         */
        public boolean canContinueToUse() {
            return SculkBeeHarvesterEntity.this.navigation.isInProgress();
        }

        /**
         * Execute the goal. <br>
         *
         */
        public void start()
        {

            Vector3d vector3d = this.findPos();
            if (vector3d != null)
            {
                SculkBeeHarvesterEntity.this.navigation.moveTo(SculkBeeHarvesterEntity.this.navigation.createPath(new BlockPos(vector3d), 1), 1.0D);
            }

        }

        /**
         * Get random position
         * @return
         */
        @Nullable
        private Vector3d findPos()
        {
            Vector3d vector3d;

            //If there is a valid hive and within 22 blocks of it
            if (SculkBeeHarvesterEntity.this.isHiveValid() && !SculkBeeHarvesterEntity.this.closerThan(SculkBeeHarvesterEntity.this.hivePos, 22))
            {
                Vector3d vector3d1 = Vector3d.atCenterOf(SculkBeeHarvesterEntity.this.hivePos);
                vector3d = vector3d1.subtract(SculkBeeHarvesterEntity.this.position()).normalize();
            }
            else
            {
                vector3d = SculkBeeHarvesterEntity.this.getViewVector(0.0F);
            }

            int i = 8;
            Vector3d vector3d2 = RandomPositionGenerator.getAboveLandPos(SculkBeeHarvesterEntity.this, 8, 7, vector3d, ((float)Math.PI / 2F), 2, 1);
            return vector3d2 != null ? vector3d2 : RandomPositionGenerator.getAirPos(SculkBeeHarvesterEntity.this, 8, 4, -2, vector3d, (double)((float)Math.PI / 2F));
        }
    }
}
