package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.goal.*;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
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
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.MoveTowardsTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Random;

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

    @Override
    public SquadHandler getSquad() {
        return null;
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
        this.goalSelector.addGoal(1, new ShootSoulsAttackGoal(this, TickUnits.convertSecondsToTicks(10)));
        this.goalSelector.addGoal(1, new FangsAttackGoal(this));
        this.goalSelector.addGoal(2, new ShortRangeFloorSoulsAttackGoal(this));
        this.goalSelector.addGoal(3, new AttackGoal());
        this.goalSelector.addGoal(5, new MoveTowardsTargetGoal(this, 1.0F, 20F));
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

    private static final RawAnimation IDLE_BODY_ANIMATION = RawAnimation.begin().thenPlay("idle");
    private static final RawAnimation IDLE_TWITCH_ANIMATION = RawAnimation.begin().thenPlay("idle.twitch");
    private static final RawAnimation IDLE_TENDRILS_ANIMATION = RawAnimation.begin().thenPlay("idle.tendrils");
    private static final RawAnimation WALK_ANIMATION = RawAnimation.begin().thenPlay("move.walk");
    private static final RawAnimation RUN_ANIMATION = RawAnimation.begin().thenPlay("move.run");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_1 = RawAnimation.begin().thenPlay("combat.attack1");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_2 = RawAnimation.begin().thenPlay("combat.attack2");
    private static final RawAnimation COMBAT_ATTACK_ANIMATION_3 = RawAnimation.begin().thenPlay("combat.attack3");
    private static final RawAnimation COMBAT_FIREBALL_SHOOT_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.face");
    private static final RawAnimation COMBAT_FIREBALL_SKY_SUMMON_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.sky.summon");
    private static final RawAnimation COMBAT_FIREBALL_SKY_TWITCH_ANIMATION = RawAnimation.begin().thenPlay("combat.fireball.sky.twitch");
    private static final RawAnimation COMBAT_SUMMON_ANIMATION = RawAnimation.begin().thenLoop("combat.summon");
    private static final RawAnimation COMBAT_SUMMON_TWITCH_ANIMATION = RawAnimation.begin().thenLoop("combat.summon.twitch");
    private static final RawAnimation COMBAT_RIFTS_SUMMON_ANIMATION = RawAnimation.begin().thenPlay("combat.rifts.summon");
    private static final RawAnimation COMBAT_SPIKE_LINE = RawAnimation.begin().thenPlay("combat.spike.line");
    private static final RawAnimation COMBAT_SPIKE_TWITCH = RawAnimation.begin().thenPlay("combat.spike.line.twitch");
    private static final RawAnimation COMBAT_SPIKE_RADIAL = RawAnimation.begin().thenPlay("combat.spike.around");
    private static final RawAnimation COMBAT_SPIKE_RADIAL_TWITCH = RawAnimation.begin().thenPlay("combat.spike.around.twitch");
    private static final RawAnimation COMBAT_BUBBLE = RawAnimation.begin().thenPlay("combat.forcefieldbubble.activate");
    private static final RawAnimation COMBAT_BUBBLE_TWITCH = RawAnimation.begin().thenPlay("combat.forcefieldbubble.twitch");

    private final AnimationController COMBAT_ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, "attack_controller", state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim("melee_attack_animation_1", COMBAT_ATTACK_ANIMATION_1)
            .triggerableAnim("melee_attack_animation_2", COMBAT_ATTACK_ANIMATION_2)
            .triggerableAnim("melee_attack_animation_3", COMBAT_ATTACK_ANIMATION_3)
            .triggerableAnim("fireball_shoot_animation", COMBAT_FIREBALL_SHOOT_ANIMATION)
            .triggerableAnim("fireball_sky_summon_animation", COMBAT_FIREBALL_SKY_SUMMON_ANIMATION)
            .triggerableAnim("fireball_sky_twitch_animation", COMBAT_FIREBALL_SKY_TWITCH_ANIMATION)
            .triggerableAnim("summon_animation", COMBAT_SUMMON_ANIMATION)
            .triggerableAnim("rifts_summon_animation", COMBAT_RIFTS_SUMMON_ANIMATION)
            .triggerableAnim("spike_line_animation", COMBAT_SPIKE_LINE)
            .triggerableAnim("spike_radial_animation", COMBAT_SPIKE_RADIAL)
            .triggerableAnim("bubble_animation", COMBAT_BUBBLE);

    private final AnimationController COMBAT_TWITCH_ANIMATION_CONTROLLER = new AnimationController<>(this, "twitch_controller", state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim("fireball_sky_twitch_animation", COMBAT_FIREBALL_SKY_TWITCH_ANIMATION)
            .triggerableAnim("summon_twitch_animation", COMBAT_SUMMON_TWITCH_ANIMATION)
            .triggerableAnim("spike_line_twitch_animation", COMBAT_SPIKE_TWITCH)
            .triggerableAnim("spike_radial_twitch_animation", COMBAT_SPIKE_RADIAL_TWITCH)
            .triggerableAnim("bubble_twitch_animation", COMBAT_BUBBLE_TWITCH);

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

    // Create the animation handler for the leg segment
    protected PlayState poseWalk(AnimationState<SculkSoulReaperEntity> state)
    {
        if(state.isMoving())
        {
            state.setAnimation(WALK_ANIMATION);
        }
        else
        {
            state.setAnimation(IDLE_BODY_ANIMATION);
        }
        return PlayState.CONTINUE;
    }

    protected PlayState poseTwitch(AnimationState<SculkSoulReaperEntity> state)
    {
        state.setAnimation(IDLE_TWITCH_ANIMATION);
        return PlayState.CONTINUE;
    }

    protected PlayState poseTendrils(AnimationState<SculkSoulReaperEntity> state)
    {
        state.setAnimation(IDLE_TENDRILS_ANIMATION);
        return PlayState.CONTINUE;
    }

    protected float getStandingEyeHeight(Pose p_32517_, EntityDimensions p_32518_) {
        return 2.55F;
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

    // ####### Sound Code ###########

    protected SoundEvent getAmbientSound() {
        return ModSounds.SCULK_ENDERMAN_IDLE.get();
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return ModSounds.SCULK_ENDERMAN_HIT.get();
    }

    protected SoundEvent getDeathSound() {
        return ModSounds.SCULK_ENDERMAN_DEATH.get();
    }

    protected void playStepSound(BlockPos pPos, BlockState pBlock) {
        this.playSound(SoundEvents.ZOMBIE_STEP, 0.15F, 1.0F);
    }

    public boolean dampensVibrations() {
        return true;
    }

    
    class AttackGoal extends CustomMeleeAttackGoal
    {

        public AttackGoal()
        {
            super(SculkSoulReaperEntity.this, 1.0D, true, 17);
        }

        protected double getAttackReachSqr(LivingEntity pAttackTarget)
        {
            return 6;
        }

        @Override
        protected float getMinimumDistanceToTarget()
        {
            return 0.5F;
        }

        @Override
        protected void triggerAnimation()
        {
            // Choose between 3 animations randomly
            int random = new Random().nextInt(3);
            if(random == 0)
            {
                ((SculkSoulReaperEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_1");
            }
            else if(random == 1)
            {
                ((SculkSoulReaperEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_2");
            }
            else
            {
                ((SculkSoulReaperEntity)mob).triggerAnim("attack_controller", "melee_attack_animation_3");
            }
        }
    }
}
