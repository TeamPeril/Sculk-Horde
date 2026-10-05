package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.common.entity.components.TargetFilter;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualSurfaceInfestorCursor;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.EntityFactoryEntry;
import com.github.sculkhorde.systems.gravemind_system.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.SoundUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.constant.DefaultAnimations;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.animation.AnimationController;
import software.bernie.geckolib.animation.RawAnimation;
import software.bernie.geckolib.animation.PlayState;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.ArrayList;
import java.util.Optional;

public class SculkMetamorphosisPodEntity extends Monster implements GeoEntity, ISculkSmartEntity {

    /**
     * In order to create a mob, the following java files were created/edited.<br>
     * Edited {@link com.github.sculkhorde.util.ModEventSubscriber}<br>
     * Edited {@link com.github.sculkhorde.client.ClientModEventSubscriber}<br>
     * Added {@link com.github.sculkhorde.client.model.enitity.SculkMetamorphosisPodModel}<br>
     * Added {@link com.github.sculkhorde.client.renderer.entity.SculkMetamorphosisPodRenderer}<br>
     */

    public static final float MAX_HEALTH = 15F;
    public static final float ARMOR = 20F;
    protected final TargetParameters TARGET_PARAMETERS = new TargetParameters(this, true)
            .filterBy(TargetFilter.PASSIVE_TO_SCULK, TargetFilter.HOSTILE_TO_SCULK);
    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    protected long timeUntilSpawn = TickUnits.convertSecondsToTicks(5);
    protected ArrayList<Entity> entitiesToSpawn = new ArrayList<>();

    /**
     * The Constructor
     * @param type The Mob Type
     * @param worldIn The world to initialize this mob in
     */
    public SculkMetamorphosisPodEntity(EntityType<? extends SculkMetamorphosisPodEntity> type, Level worldIn) {
        super(type, worldIn);
    }

    /**
     * An Easier Constructor where you do not have to specify the Mob Type
     * @param worldIn  The world to initialize this mob in
     */
    public SculkMetamorphosisPodEntity(Level worldIn, long timeUntilSpawn) {
        super(ModEntities.SCULK_METAMORPHOSIS_POD.get(), worldIn);
        this.timeUntilSpawn = timeUntilSpawn;
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
                .add(Attributes.FOLLOW_RANGE, 0)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public void checkDespawn() {}

    public boolean isPushable() {
        return false;
    }

    public boolean isIdle() {
        return false;
    }

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

    public void addEntityToSpawn(Entity e)
    {
        entitiesToSpawn.add(e);
    }

    //Every tick, spawn a short range cursor
    @Override
    public void aiStep() {
        super.aiStep();

        if (level().isClientSide)
        {
            return;
        }

        if(timeUntilSpawn == TickUnits.convertSecondsToTicks(1.30F))
        {
            triggerAnim(ATTACK_ANIMATION_CONTROLLER_ID, ATTACK_ID);
        }

        if(timeUntilSpawn > 0)
        {
            timeUntilSpawn--;
            return;
        }

        ReinforcementRequest request = new ReinforcementRequest((ServerLevel) level(), blockPosition());
        request.sender = ReinforcementRequest.senderType.Summoner;
        request.is_aggressor_nearby = true;
        request.budget = 30;
        request.approvedStrategicValues.add(EntityFactoryEntry.StrategicValues.Combat);
        request.approvedStrategicValues.add(EntityFactoryEntry.StrategicValues.EffectiveOnGround);

        SculkHorde.entityFactory.createReinforcementRequestFromSummoner(level(), blockPosition(), false, request);
        spawnCursor();
        ParticleUtil.spawnSculkExplosion((ServerLevel) level(), position().toVector3f(), 100, 1.0F);
        SoundUtil.playHostileSoundInLevel(level(), blockPosition(), ModSounds.BURROWED_BURST.get());
        discard();
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

    //Animation Related Functions

    public static final String ATTACK_ID = "attack";
    private static final RawAnimation ATTACK_ANIMATION = RawAnimation.begin().thenPlay(ATTACK_ID);

    public static final String ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    private final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .transitionLength(0)
            .triggerableAnim(ATTACK_ID, ATTACK_ANIMATION);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericLivingController(this),
                ATTACK_ANIMATION_CONTROLLER
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }

}
