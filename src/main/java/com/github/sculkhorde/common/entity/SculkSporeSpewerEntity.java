package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.goal.TargetAttacker;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.core.object.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;

public class SculkSporeSpewerEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited core/ EntityRegistry.java<br>
     * Edited util/ ModEventSubscriber.java<br>
     * Edited client/ ClientModEventSubscriber.java<br>
     * Edited common/world/ModWorldEvents.java (this might not be necessary)<br>
     * Edited common/world/gen/ModEntityGen.java<br>
     * Added common/entity/ SculkSporeSpewerEntity.java<br>
     * Added client/model/entity/ SculkSporeSpewerModel.java<br>
     * Added client/renderer/entity/ SculkSporeSpewerRenderer.java
     */

    //The Health
    public static final float MAX_HEALTH = 40F;
    //The armor of the mob
    public static final float ARMOR = 10F;
    //ATTACK_DAMAGE determines How much damage it's melee attacks do
    public static final float ATTACK_DAMAGE = 0F;
    //ATTACK_KNOCKBACK determines the knockback a mob will take
    public static final float ATTACK_KNOCKBACK = 0F;
    //FOLLOW_RANGE determines how far away this mob can see and chase enemies
    public static final float FOLLOW_RANGE = 0F;
    //MOVEMENT_SPEED determines how far away this mob can see other mobs
    public static final float MOVEMENT_SPEED = 0F;

    // Controls what types of entities this mob can target
    protected TargetParameters TARGET_PARAMETERS = new TargetParameters(this).enableTargetPassives().enableTargetHostiles();

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    protected VirtualSurfaceInfestorCursor cursor;

    protected long INFECTION_INTERVAL_TICKS = TickUnits.convertSecondsToTicks(5);
    protected long lastInfectionTime = 0;
    protected boolean isParticipatingInRaid = false;
    protected final int MAX_FAILED_CURSORS = 4;
    protected int failedCursors = 0;

    public static final EntityDataAccessor<Integer> DATA_TICKS_ALIVE = SynchedEntityData.defineId(SculkEndermanEntity.class, EntityDataSerializers.INT);
    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkSporeSpewerEntity(EntityType<? extends SculkSporeSpewerEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkSporeSpewerEntity(Level worldIn) {super(ModEntities.SCULK_SPORE_SPEWER.get(), worldIn);}

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

    @Override
    public void checkDespawn() {}

    public boolean isIdle() {
        return false;
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

    /**
     * Registers Goals with the entity. The goals determine how an AI behaves ingame.
     * Each goal has a priority with 0 being the highest and as the value increases, the priority is lower.
     * You can manually add in goals in this function, however, I made an automatic system for this.
     */
    @Override
    public void registerGoals() {

            this.goalSelector.addGoal(0, new dieAfterTimeGoal(this));
            this.targetSelector.addGoal(0, new TargetAttacker(this).setAlertAllies());
    }

    //Animation Related Functions
    private static final RawAnimation SPREAD_ANIMATION = RawAnimation.begin().thenPlay("spread");
    private final AnimationController SPREAD_ANIMATION_CONTROLLER = new AnimationController<>(this, "spread_controller", state -> PlayState.STOP)
            .triggerableAnim("spread_animation", SPREAD_ANIMATION);

    // Add our animations
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericLivingController(this),
                SPREAD_ANIMATION_CONTROLLER);
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }


    //Every tick, spawn a short range cursor
    @Override
    public void aiStep() {
        super.aiStep();

        // Only on the client side, spawn dust particles with a specific color
        // Have the partciles fly in random directions
        if (level().isClientSide)
        {
            Random random = new Random();
            for (int i = 0; i < 1; i++) {
                level().addParticle(ModParticles.SCULK_CRUST_PARTICLE.get(), this.position().x, this.position().y + 1.7, this.position().z, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3, (random.nextDouble() - 0.5) * 3);
            }
            return;
        }

        // Track how many cursors fail.
        if(cursor != null && cursor.isFinished() && !cursor.isSuccessful())
        {
            cursor = null;
            failedCursors += 1;
        }

        // If too many failed cursors, just despawn and return health back to horde
        if(failedCursors >= MAX_FAILED_CURSORS)
        {
            ModSavedData.getSaveData().addSculkAccumulatedMass((int) getHealth());
            discard();
        }


        Random random = new Random();
        boolean passRandomChance = random.nextInt(100) == 0;
        boolean isCursorNullOrDead = cursor == null || cursor.isSetToBeDeleted();
        boolean isBlockInfestationEnabled = ModConfig.SERVER.block_infestation_enabled.get();
        // The reason we do this instead of just checking if the horde is active is because sometimes people will spawn these
        // without activating the horde.
        boolean isTheHordeNotDefeated = !ModSavedData.getSaveData().isHordeDefeated();
        boolean canSpawnCursor = passRandomChance && isCursorNullOrDead && isBlockInfestationEnabled && isTheHordeNotDefeated;

        if (canSpawnCursor && !SculkHorde.cursorSystem.isCursorPopulationAtMax()) {
            // Spawn Block Traverser
            spawnCursor();
            triggerAnim("spread_controller", "spread_animation");

        }

        if (level().getGameTime() - lastInfectionTime > INFECTION_INTERVAL_TICKS)
        {
            lastInfectionTime = level().getGameTime();
            // Any entity within 10 blocks of the spewer will be infected
            ArrayList<LivingEntity> entities = (ArrayList<LivingEntity>) EntityAlgorithms.getNonSculkEntitiesAtBlockPos((ServerLevel) level(), this.blockPosition(), 10);
            for (LivingEntity victim : entities)
            {
                if(!((ISculkSmartEntity) this).getTargetParameters().isEntityValidTarget(victim, false))
                {
                    return;
                }

                if(DifficultyUtil.isCurrentDifficultyGreaterThanEasy())
                {
                    EntityAlgorithms.reducePurityEffectDuration(victim, TickUnits.convertMinutesToTicks(1));
                    EntityAlgorithms.applyEffectToTarget(victim, ModMobEffects.SCULK_LURE.get(), TickUnits.convertMinutesToTicks(10), 0);
                }

                if(DifficultyUtil.isCurrentDifficultyEasy())
                {
                    EntityAlgorithms.applyEffectToTarget(victim, SculkMiteEntity.INFECT_EFFECT, TickUnits.convertSecondsToTicks(60), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                }
                else if(DifficultyUtil.isCurrentDifficultyNormal())
                {
                    EntityAlgorithms.applyEffectToTarget(victim, SculkMiteEntity.INFECT_EFFECT, TickUnits.convertSecondsToTicks(40), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                }
                else if(DifficultyUtil.isCurrentDifficultyHard())
                {
                    EntityAlgorithms.applyEffectToTarget(victim, ModMobEffects.DISEASED_CYSTS.get(), TickUnits.convertSecondsToTicks(30), SculkHorde.gravemind.getPotionAmplificationBasedOnGravemindState());
                }
            }
        }
    }

    @Override
    public void die(DamageSource p_21014_) {
        super.die(p_21014_);
        AreaEffectSphericalCloudEntity areaeffectcloud = new AreaEffectSphericalCloudEntity(level(), getX(), getY() - 1, getZ());
        areaeffectcloud.setOwner((LivingEntity) this);
        areaeffectcloud.setRadius(2F);
        areaeffectcloud.setDuration(TickUnits.convertSecondsToTicks(5));
        areaeffectcloud.addEffect(new MobEffectInstance(ModMobEffects.SCULK_INFECTION.get(), TickUnits.convertSecondsToTicks(10), 0));
        level().addFreshEntity(areaeffectcloud);
    }

    protected void spawnCursor()
    {
        if(level().isClientSide() || level().getServer() == null)
        {
            return;
        }

        Optional<VirtualSurfaceInfestorCursor> possibleCursor = CursorSystem.createSurfaceInfestorVirtualCursor(level(), blockPosition());

        if(possibleCursor.isPresent())
        {
            possibleCursor.get().setMaxTransformations(100);
            possibleCursor.get().setMaxRange(100);
            possibleCursor.get().setTickIntervalTicks(10);
            possibleCursor.get().setSearchIterationsPerTick(10);
            cursor = possibleCursor.get();
        }
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.SCULK_CATALYST_BLOOM;
    }

    protected SoundEvent getHurtSound(DamageSource pDamageSource) {
        return SoundEvents.GENERIC_HURT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.SCULK_CATALYST_BREAK;
    }

    /**
     * This is a custom goal that I made to make the mob die after a certain amount of time.
     * This is useful for mobs that are meant to be temporary, such as the Sculk Spore Spewer.
     */
    protected class dieAfterTimeGoal extends Goal
    {
        protected final SculkSporeSpewerEntity entity;

        public dieAfterTimeGoal(SculkSporeSpewerEntity entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void start() {
        }

        @Override
        public void tick()
        {
            if(level().isClientSide())
            {
                return;
            }

            entityData.set(DATA_TICKS_ALIVE, entityData.get(DATA_TICKS_ALIVE) + 1);
            int ticksAlive = entityData.get(DATA_TICKS_ALIVE);
            if (ticksAlive > TickUnits.convertMinutesToTicks(15)) {
                entity.remove(Entity.RemovalReason.DISCARDED);
            }
        }
    }

    public boolean dampensVibrations() {
        return true;
    }

    String DATA_TICKS_ALIVE_IDENTIFIER = "ticks_alive";

    // ###### Data Code ########
    protected void defineSynchedData()
    {
        super.defineSynchedData();
        this.entityData.define(DATA_TICKS_ALIVE, 0);
    }

    public void addAdditionalSaveData(CompoundTag nbt)
    {
        super.addAdditionalSaveData(nbt);
        nbt.putInt(DATA_TICKS_ALIVE_IDENTIFIER, this.entityData.get(DATA_TICKS_ALIVE));
    }

    public void readAdditionalSaveData(CompoundTag nbt)
    {
        super.readAdditionalSaveData(nbt);
        this.entityData.set(DATA_TICKS_ALIVE, nbt.getInt(DATA_TICKS_ALIVE_IDENTIFIER));
    }

}
