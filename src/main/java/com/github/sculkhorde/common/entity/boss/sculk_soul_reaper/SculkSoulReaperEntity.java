package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import com.github.sculkhorde.common.entity.SculkVindicatorEntity;
import com.github.sculkhorde.common.entity.SculkWitchEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals.*;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.entity_debugging.GoalDebuggerUtility;
import com.github.sculkhorde.common.entity.entity_debugging.IDebuggableGoal;
import com.github.sculkhorde.common.entity.goal.ImprovedRandomStrollGoal;
import com.github.sculkhorde.common.entity.goal.InvalidateTargetGoal;
import com.github.sculkhorde.common.entity.goal.NearestLivingEntityTargetGoal;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModParticles;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadEvent;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.BossEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.WrappedGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;
import java.util.UUID;

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
    protected int mobDifficultyLevel = 1;
    private final TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetHostiles().enableTargetInfected().disableBlackListMobs();
    protected ServerBossEvent bossEvent;

    // Animation
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected Optional<LivingEntity> hitTarget = Optional.empty();

    protected boolean isUsingSpell = false;

    protected ReaperAttackSequenceGoal currentAttack;

    public static final String parentEventUUIDIdentifier = "parent_event_uuid";
    protected UUID parentEventUUID;

    protected static final EntityDataAccessor<Byte> DATA_FLAGS_ID = SynchedEntityData.defineId(SculkSoulReaperEntity.class, EntityDataSerializers.BYTE);
    private static final int FLAG_IS_SHOOTING_ELEMENTALS = 1;

    protected boolean updatedEventTitle = false;

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

    public SculkSoulReaperEntity(Level level, BlockPos pos, UUID eventUUID)
    {
        this(level, pos);
        parentEventUUID = eventUUID;
    }

    public static SculkSoulReaperEntity spawnWithDifficulty(Level level, Vec3 pos, int mobDifficultyLevel, boolean withSquad)
    {
        SculkSoulReaperEntity reaper = new SculkSoulReaperEntity(ModEntities.SCULK_SOUL_REAPER.get(), level);
        reaper.setPos(pos);
        reaper.setMobDifficultyLevel(mobDifficultyLevel);
        Squad squad = new Squad(reaper);

        if(mobDifficultyLevel >= 4)
        {
            reaper.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, Integer.MAX_VALUE, mobDifficultyLevel - 4));
            reaper.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, Integer.MAX_VALUE, mobDifficultyLevel - 4));
        }

        level.addFreshEntity(reaper);
        if(!withSquad)
        {
            return reaper;
        }

        SculkRavagerEntity ravager = new SculkRavagerEntity(level);
        ravager.setPos(pos);
        reaper.startRiding(ravager);
        level.addFreshEntity(ravager);

        SculkWitchEntity witch1 = new SculkWitchEntity(level);
        witch1.setPos(pos);
        level.addFreshEntity(witch1);
        SculkWitchEntity witch2 = new SculkWitchEntity(level);
        witch2.setPos(pos);
        level.addFreshEntity(witch2);

        SculkVindicatorEntity vindicator1 = new SculkVindicatorEntity(level);
        vindicator1.setPos(pos);
        level.addFreshEntity(vindicator1);
        SculkVindicatorEntity vindicator2 = new SculkVindicatorEntity(level);
        vindicator2.setPos(pos);
        level.addFreshEntity(vindicator2);
        SculkVindicatorEntity vindicator3 = new SculkVindicatorEntity(level);
        vindicator3.setPos(pos);
        level.addFreshEntity(vindicator3);

        squad.forceAcceptMemberIntoSquad(ravager);
        squad.forceAcceptMemberIntoSquad(witch1);
        squad.forceAcceptMemberIntoSquad(witch2);
        squad.forceAcceptMemberIntoSquad(vindicator1);
        squad.forceAcceptMemberIntoSquad(vindicator2);
        squad.forceAcceptMemberIntoSquad(vindicator3);
        return reaper;
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
    public void setParentEventUUID(UUID eventUUID)
    {
        parentEventUUID = eventUUID;
    }
    public boolean isIdle() {
        return getTarget() == null;
    }

    private boolean isParticipatingInRaid = false;

    protected Squad squad = new Squad(this);
    

    @Override
    public boolean isParticipatingInRaid() {
        return isParticipatingInRaid;
    }

    @Override
    public void setParticipatingInRaid(boolean isParticipatingInRaidIn) {
        isParticipatingInRaid = isParticipatingInRaidIn;
    }

    public boolean isShootingElementals()
    {
        return getFlag(FLAG_IS_SHOOTING_ELEMENTALS);
    }

    public void setFlagIsShootingElementals(boolean isShootingElementals)
    {
        setFlag(FLAG_IS_SHOOTING_ELEMENTALS, isShootingElementals);
    }


    @Override
    public TargetParameters getTargetParameters() {
        return TARGET_PARAMETERS;
    }

    public int getMobDifficultyLevel()
    {
        if(hasEffect(ModMobEffects.SOUL_DISRUPTION.get()))
        {
            return Math.max(1, mobDifficultyLevel - 1);
        }

        return mobDifficultyLevel;
    }

    public void setMobDifficultyLevel(int value)
    {
        mobDifficultyLevel = value;
    }

    public Optional<LivingEntity> getHitTarget()
    {
        return hitTarget;
    }

    public void setHitTarget(LivingEntity e)
    {
        hitTarget = Optional.of(e);
    }

    public boolean getIsUsingSpell()
    {
        return isUsingSpell;
    }

    public void startUsingSpell()
    {
        isUsingSpell = true;
    }

    public void stopUsingSpell()
    {
        isUsingSpell = false;
    }

    public ReaperAttackSequenceGoal getCurrentAttack()
    {
        if(currentAttack != null && currentAttack.isAttackSequenceFinished())
        {
            clearCurrentAttack();
        }

        return currentAttack;
    }

    public void setCurrentAttack(ReaperAttackSequenceGoal goal)
    {
        currentAttack = goal;
    }

    public void clearCurrentAttack()
    {
        currentAttack = null;
    }

    public boolean isThereAnotherAttackActive(ReaperAttackSequenceGoal goal)
    {
        if(getCurrentAttack() == null)
        {
            return false;
        }
        else if(getCurrentAttack().equals(goal))
        {
            return false;
        }

        return true;
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

        this.goalSelector.addGoal(1, new SummonVexAttackGoal(this,1 , -1));


        // #### LEVEL 1 ####

        this.goalSelector.addGoal(2, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 1,1,
                new FloorSoulSpearLineAttackGoal(this),
                new FangsAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new FangsAttackGoal(this),
                new ZoltraakAttackGoal(this)
        ));

        // #### LEVEL 2 ####

        this.goalSelector.addGoal(1, new ReaperCloseRangeAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 2,2,
                new SoulBlastAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(2, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(15), 2,2,
                new ShootElementalSoulProjectilesGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new ElementalMagicCircleAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ElementalMagicCircleAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(3, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(5), 2,2,
                new ZoltraakAttackGoal(this),
                new ShootSoulSpearAttackGoal(this),
                new SummonSoulSpearSummonerGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootSoulSpearAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(4, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 2,2,
                new ZoltraakAttackGoal(this),
                new FangsAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new FangsAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new ZoltraakAttackGoal(this)
        ));

        // #### LEVEL 3+ ####

        this.goalSelector.addGoal(1, new ReaperCloseRangeAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 3,-1,
                new SoulBlastAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new FangsAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(2, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(20), 3,-1,
                new ZoltraakAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this),
                new ZoltraakAttackGoal(this),
                new FloorSoulSpearLineAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(3, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(15), 3,-1,
                new ShootElementalSoulProjectilesGoal(this),
                new ElementalMagicCircleAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ElementalMagicCircleAttackGoal(this)
        ));

        this.goalSelector.addGoal(4, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(2), 3,-1,
                new ShootSoulSpearAttackGoal(this),
                new SummonSoulSpearSummonerGoal(this),
                new ShootSoulSpearAttackGoal(this),
                new SummonSoulSpearSummonerGoal(this),
                new ShootSoulSpearAttackGoal(this),
                new FloorSoulSpearsFollowingAttackGoal(this)
        ));

        this.goalSelector.addGoal(4, new ReaperAttackSequenceGoal(this, TickUnits.convertSecondsToTicks(1), 3,-1,
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ZoltraakAttackGoal(this),
                new ShootElementalSoulProjectilesGoal(this),
                new ZoltraakBarrageAttackGoal(this)
        ));

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
        if (this.level().isClientSide)
        {
            if(random.nextBoolean())
            {
                this.level().addParticle(ModParticles.ANCIENT_DIALECT_PARTICLE.get(),
                        this.getRandomX(3D),
                        this.getRandomY() - 0.25D,
                        this.getRandomZ(3D),
                        0,
                        0,
                        0);
            }

        }

        this.jumping = false;
        super.aiStep();
    }

    public void tick()
    {
        super.tick();
        if (this.level().isClientSide)
        {
            return;
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

    // ####### Boss Bar Event Stuff #######

    /**
     * Called every tick to update the entity's position/logic.
     */
    protected void customServerAiStep()
    {
        super.customServerAiStep();
        this.bossEvent.setProgress(this.getHealth() / this.getMaxHealth());

        if(!SculkHorde.eventSystem.doesEventExist(parentEventUUID) && parentEventUUID != null)
        {
            SculkHorde.LOGGER.info("SculkSoulReaperEntity | Despawned myself because parent event does not exist.");
            despawn();
        }


        // Update Boss Title
        if (!updatedEventTitle) {
            Component title = Component.translatable("entity.sculkhorde.sculk_soul_reaper")
                    .append(Component.literal(" 💀" + getMobDifficultyLevel()));

            if (getHitTarget().isPresent() && !getHitTarget().get().getScoreboardName().isEmpty())
            {
                title = title.copy()
                        .append(Component.literal(" ("))
                        .append(getHitTarget().get().getDisplayName()) // append the Component directly
                        .append(Component.literal(")"));
            }
            bossEvent.setName(title);
            updatedEventTitle = true;
        }

        // This is to make sure there arent any duplicate soul reapers in the world.
        // I know this is nested if statement hell, but I was tired and it works.
        if(SculkHorde.eventSystem.doesEventExist(parentEventUUID))
        {
            Event parentEvent = SculkHorde.eventSystem.getEvent(parentEventUUID);

            if(parentEvent instanceof HitSquadEvent hitSquadEvent)
            {
                if(hitSquadEvent.getReaper().isPresent())
                {
                    if(!hitSquadEvent.getReaper().get().getUUID().equals(getUUID()))
                    {
                        SculkHorde.LOGGER.info("SculkSoulReaperEntity | Despawned myself because parent event already has a reaper. I am not supposed to exist.");
                        despawn();
                    }
                }
                else
                {
                    SculkHorde.LOGGER.info("SculkSoulReaperEntity | Despawned myself because parent event has not yet spawned a reaper. I am not supposed to exist.");
                    despawn();
                }
            }
            else
            {
                SculkHorde.LOGGER.info("SculkSoulReaperEntity | Despawned because parent event was not even a hitsquad event. How did we get here?");
                despawn();
            }
        }

    }

    protected void despawn()
    {
        level().getServer().tell(new TickTask(level().getServer().getTickCount() + 1, () -> {
            discard();
        }));
    }

    protected ServerBossEvent createBossEvent() {
        Component title = Component.translatable("entity.sculkhorde.sculk_soul_reaper")
                .append(Component.literal(" 💀" + getMobDifficultyLevel()));

        if(getHitTarget().isPresent() && !getHitTarget().get().getScoreboardName().isEmpty())
        {
            title = title.copy().append(" (" + getHitTarget().get().getDisplayName() + ")");
        }


        ServerBossEvent event = new ServerBossEvent(title, BossEvent.BossBarColor.BLUE, BossEvent.BossBarOverlay.PROGRESS);
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
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_FLAGS_ID, (byte)0);
    }

    private boolean getFlag(int id) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        return (i & id) != 0;
    }

    private void setFlag(int id, boolean value) {
        int i = this.entityData.get(DATA_FLAGS_ID);
        if (value) {
            i |= id;
        } else {
            i &= ~id;
        }

        this.entityData.set(DATA_FLAGS_ID, (byte)(i & 255));
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt("difficulty", getMobDifficultyLevel());
        if(parentEventUUID != null)
        {
            nbt.putUUID(parentEventUUIDIdentifier, parentEventUUID);
        }
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        setMobDifficultyLevel(nbt.getInt("difficulty"));
        if(nbt.contains(parentEventUUIDIdentifier))
        {
            parentEventUUID = nbt.getUUID(parentEventUUIDIdentifier);
        }
    }

    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source.is(DamageTypeTags.WITHER_IMMUNE_TO);
    }

    // #### Animation Code ####

    public static final String SHOOTING_ELEMENTALS_ID = "attack.shooting_elementals";
    private static final RawAnimation SHOOTING_ELEMENTALS_ANIMATION = RawAnimation.begin().thenPlayAndHold(SHOOTING_ELEMENTALS_ID);

    public static final String ATTACK_SPELL_USE_ID = "attack.spell_use";
    public static final RawAnimation FANGS_SPELL_USE = RawAnimation.begin().thenPlay(ATTACK_SPELL_USE_ID);
    public static final String ZOLTRAAK_SPELL_USE_ID = "attack.zoltraak";
    public static final RawAnimation ZOLTRAAK_SPELL_USE = RawAnimation.begin().thenPlay(ZOLTRAAK_SPELL_USE_ID);

    public static final String MAGIC_CIRCLE_SPELL_USE_ID = "attack.magic_circle";
    private static final RawAnimation MAGIC_CIRCLE_SPELL_USE = RawAnimation.begin().thenPlay(MAGIC_CIRCLE_SPELL_USE_ID);

    public static final String FLOOR_SPEARS_SPELL_USE_ID = "attack.floor_spears";
    private static final RawAnimation FLOOR_SPEARS_SPELL_USE = RawAnimation.begin().thenPlay(FLOOR_SPEARS_SPELL_USE_ID);

    public static final String SOUL_SPEAR_SPELL_USE_ID = "attack.soul_spear";
    private static final RawAnimation SOUL_SPEAR_SPELL_USE = RawAnimation.begin().thenPlay(SOUL_SPEAR_SPELL_USE_ID);

    public static final String ELEMENTAL_PROJECTILE_SPELL_CHARGE_ID = "attack.elemental_projectiles.charge";
    private static final RawAnimation ELEMENTAL_PROJECTILE_SPELL_CHARGE = RawAnimation.begin().thenPlay(ELEMENTAL_PROJECTILE_SPELL_CHARGE_ID);

    public static final String ELEMENTAL_PROJECTILE_SPELL_SHOOT_ID = "attack.elemental_projectiles.shoot";
    private static final RawAnimation ELEMENTAL_PROJECTILE_SPELL_SHOOT = RawAnimation.begin().thenPlay(ELEMENTAL_PROJECTILE_SPELL_SHOOT_ID);

    public static final String COMBAT_ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    private final AnimationController COMBAT_ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .triggerableAnim(ZOLTRAAK_SPELL_USE_ID, ZOLTRAAK_SPELL_USE)
            .triggerableAnim(MAGIC_CIRCLE_SPELL_USE_ID, MAGIC_CIRCLE_SPELL_USE)
            .triggerableAnim(FLOOR_SPEARS_SPELL_USE_ID, FLOOR_SPEARS_SPELL_USE)
            .triggerableAnim(ELEMENTAL_PROJECTILE_SPELL_CHARGE_ID, ELEMENTAL_PROJECTILE_SPELL_CHARGE)
            .triggerableAnim(ELEMENTAL_PROJECTILE_SPELL_SHOOT_ID, ELEMENTAL_PROJECTILE_SPELL_SHOOT)
            .triggerableAnim(SOUL_SPEAR_SPELL_USE_ID, SOUL_SPEAR_SPELL_USE)
            .triggerableAnim(ATTACK_SPELL_USE_ID, FANGS_SPELL_USE)
            .triggerableAnim(ELEMENTAL_PROJECTILE_SPELL_CHARGE_ID, ELEMENTAL_PROJECTILE_SPELL_CHARGE)
            .triggerableAnim(ELEMENTAL_PROJECTILE_SPELL_SHOOT_ID, ELEMENTAL_PROJECTILE_SPELL_SHOOT);;


    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers)
    {
        controllers.add(
                DefaultAnimations.genericWalkIdleController(this).transitionLength(5),
                DefaultAnimations.genericLivingController(this).transitionLength(5),
                COMBAT_ATTACK_ANIMATION_CONTROLLER.transitionLength(5),
                new AnimationController<>(this, "shooting", 5, this::poseElementalShooting)
        );
    }

    protected PlayState poseElementalShooting(AnimationState<SculkSoulReaperEntity> state)
    {
        if(state.getAnimatable().isShootingElementals())
        {
            state.setAnimation(SHOOTING_ELEMENTALS_ANIMATION);
            return PlayState.CONTINUE;
        }

        return PlayState.STOP;
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


    //#### Debug Function ####
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {

        if(player.level().isClientSide())
        {
            return super.mobInteract(player, hand);
        }

        if(SculkHorde.isDebugMode())
        {
            SculkHorde.LOGGER.info("\nACTIVE GOALS\n");
            for(WrappedGoal wrapGoal : goalSelector.getRunningGoals().toList())
            {
                Goal goal = wrapGoal.getGoal();
                if(!(goal instanceof ReaperCloseRangeAttackSequenceGoal))
                {
                    continue;
                }
                GoalDebuggerUtility.printGoalToConsole(player.level(), goal);
            }

            SculkHorde.LOGGER.info("\nINACTIVE GOALS\n");
            for(WrappedGoal wrapGoal : goalSelector.getAvailableGoals())
            {
                Goal goal = wrapGoal.getGoal();
                GoalDebuggerUtility.printGoalToConsole(player.level(), goal);
            }
        }

        return super.mobInteract(player, hand);
    }
}
