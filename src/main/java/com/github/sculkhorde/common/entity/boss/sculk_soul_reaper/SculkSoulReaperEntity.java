package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals.*;
import com.github.sculkhorde.common.entity.goal.ImprovedRandomStrollGoal;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.SquadHandler;
import com.github.sculkhorde.util.TargetParameters;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class SculkSoulReaperEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link ModEntities}<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link SculkSoulReaperEntity}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkSoulReaperRenderer}
     */

    //The Health
    public static final float MAX_HEALTH = 200F;
    //The armor of the mob
    public static final float ARMOR = 5F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 20F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 5F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 64F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0.4F;

    // Controls what types of entities this mob can target
    private final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();

    // Timing Variables
    protected ServerBossEvent bossEvent;

    // Animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSoulReaperEntity(EntityType<? extends SculkSoulReaperEntity> type, Level worldIn) {
        super(type, worldIn);
        this.setMaxUpStep(1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, -1.0F);
        this.bossEvent = this.createBossEvent();
        this.setPathfindingMalus(BlockPathTypes.UNPASSABLE_RAIL, 0.0F);
    }

    public SculkSoulReaperEntity(Level level, BlockPos pos)
    {
        this(ModEntities.SCULK_SOUL_REAPER.get(), level);
        this.setPos(pos.getX(), pos.getY(), pos.getZ());
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
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0D);
    }

    // Accessors and Modifiers

    public boolean isIdle() {
        return getTarget() == null;
    }

    private boolean isParticipatingInRaid = false;

    protected SquadHandler squad = new SquadHandler(this);
    @Override
    public SquadHandler getSquad() {
        return squad;
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

    @Override
    public void checkDespawn() {}

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new SummonVexAttackGoal(this));
        this.goalSelector.addGoal(1, new ShootSoulSpearAttackGoal(this));
        this.goalSelector.addGoal(1, new ShootSoulFlySwatterAttackGoal(this));
        this.goalSelector.addGoal(2, new ShootSoulsAttackGoal(this, TickUnits.convertSecondsToTicks(10)));
        this.goalSelector.addGoal(2, new FangsAttackGoal(this));
        this.goalSelector.addGoal(2, new ShortRangeFloorSoulsAttackGoal(this));
        this.goalSelector.addGoal(1, new ShootZoltraakAttackGoal(this));
        this.goalSelector.addGoal(5, new SoulReapterNavigator(this, 20F, 10F));
        this.goalSelector.addGoal(6, new ImprovedRandomStrollGoal(this, 1.0D).setToAvoidWater(true));
        this.targetSelector.addGoal(0, new InvalidateTargetGoal(this));
        this.targetSelector.addGoal(1, new TargetAttacker(this));
        this.targetSelector.addGoal(2, new NearestLivingEntityTargetGoal<>(this, false, false));
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount)
    {
        boolean isIndirectMagicDamageType = damageSource.is(DamageTypes.INDIRECT_MAGIC);
        if(isIndirectMagicDamageType)
        {
            return false;
        }

        return super.hurt(damageSource, amount);
    }

    /**
     * Called to update the entity's position/logic.
     */
    public void aiStep()
    {
        if (this.level().isClientSide) {
            for(int i = 0; i < 2; ++i) {
                this.level().addParticle(ParticleTypes.SCULK_SOUL, this.getRandomX(0.5D), this.getRandomY() - 0.25D, this.getRandomZ(0.5D), (this.random.nextDouble() - 0.5D) * 0.8D, -this.random.nextDouble(), (this.random.nextDouble() - 0.5D) * 0.8D);
            }
        }

        this.jumping = false;
        super.aiStep();
    }


    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

    }

    protected ServerBossEvent createBossEvent() {
        ServerBossEvent event = new ServerBossEvent(Component.translatable("entity.sculkhorde.sculk_soul_reaper"), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS);
        return event;
    }

    @Override
    public void startSeenByPlayer(ServerPlayer player) {
        super.startSeenByPlayer(player);
        this.bossEvent.addPlayer(player);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        this.bossEvent.removePlayer(player);
    }

    // ###### Data Code ########
    protected void defineSynchedData()
    {
        super.defineSynchedData();
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.WITHER_IMMUNE_TO);
    }

    // ####### Animation Code ###########

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                //new AnimationController<>(this, "walk_cycle", 5, this::poseWalk),
                //new AnimationController<>(this, "twitch", 5, this::poseTwitch),
                //new AnimationController<>(this, "tendrils", 5, this::poseTendrils),
                //COMBAT_ATTACK_ANIMATION_CONTROLLER
                //COMBAT_TWITCH_ANIMATION_CONTROLLER
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ####### Sound Code ###########

    protected SoundEvent getAmbientSound() {
        return SoundEvents.EVOKER_AMBIENT;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.EVOKER_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.EVOKER_DEATH;
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }
    
}
