package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.constant.DefaultAnimations;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.core.animation.AnimationController;
import mod.azure.azurelib.core.animation.AnimationState;
import mod.azure.azurelib.core.animation.RawAnimation;
import mod.azure.azurelib.core.object.PlayState;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

public class ZoltraakAttackEntity extends SpecialEffectEntity implements GeoEntity, GeoAnimatable {
    public static int ATTACK_DELAY = TickUnits.convertSecondsToTicks(1);
    protected static int ATTACK_ANIOMATION_DELAY = TickUnits.convertSecondsToTicks(1);
    public int attackDelayRemaining = ATTACK_DELAY;

    protected long timeOfDespawnStart = 0;
    protected final int DESPAWN_DELAY = TickUnits.convertSecondsToTicks(1);

    protected float DAMAGE = 5F;

    protected Optional<LivingEntity> target = Optional.empty();
    protected Optional<Vec3> targetPos = Optional.empty();

    protected boolean completedAttack = false;

    public ZoltraakAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ZoltraakAttackEntity(Level level) {
        this(ModEntities.ZOLTRAAK_ATTACK_ENTITY.get(), level);
    }


    public void syncPitchAndYaw()
    {
        setYRot(getYaw());
        setXRot(getPitch());
    }

    @Override
    public void setYRot(float value) {
        super.setYRot(value);

        if(level.isClientSide()) { return; }

        setYaw(value);
    }

    @Override
    public void setXRot(float value) {
        super.setXRot(value);

        if(level.isClientSide()) { return; }

        setPitch(value);
    }

    public float getYaw()
    {
        return entityData.get(DATA_YAW);
    }

    public void setYaw(float value)
    {
        entityData.set(DATA_YAW, value);
    }

    public float getPitch()
    {
        return entityData.get(DATA_PITCH);
    }

    public void setPitch(float value)
    {
        entityData.set(DATA_PITCH, value);
    }


    public void setTarget(LivingEntity entity)
    {
        target = Optional.of(entity);
    }

    public void setTargetPos(Vec3 position)
    {
        targetPos = Optional.of(position);
    }

    public static ZoltraakAttackEntity castZoltraakOnEntity(LivingEntity owner, LivingEntity target, Vec3 spawnPos)
    {
        ZoltraakAttackEntity zoltraak = new ZoltraakAttackEntity(target.level);
        zoltraak.setPos(spawnPos);
        zoltraak.setOwner(owner);
        zoltraak.setTarget(target);
        EntityAlgorithms.lookAt(zoltraak, target);
        target.level.addFreshEntity(zoltraak);
        return zoltraak;
    }

    @Override
    public void tick() {
        super.tick();

        if(level.isClientSide())
        {
            syncPitchAndYaw();
            float yrot = getYRot();
            return;
        }
        float yrot = getYRot();
        if(target.isPresent() && !completedAttack)
        {
            EntityAlgorithms.lookAt(this, target.get());
        }

        attackDelayRemaining--;

        if (attackDelayRemaining - ATTACK_ANIOMATION_DELAY <= 0)
        {
            triggerAnim(ATTACK_ANIMATION_CONTROLLER_ID, ATTACK_ID);
        }

        if(attackDelayRemaining <= 0 && !completedAttack)
        {
            if(target.isPresent())
            {

                performTargetedZoltraakAttack(target.get());
                timeOfDespawnStart = level.getGameTime();
                completedAttack = true;
                triggerAnim(DESPAWN_ANIMATION_CONTROLLER_ID, DESPAWN_ANIMATION_ID);
            }
            else if(targetPos.isPresent())
            {
                // TODO Implement zoltraak without living entity target
            }
        }

        if(completedAttack && Math.abs(timeOfDespawnStart - level.getGameTime()) >= DESPAWN_DELAY)
        {
            discard();
        }

    }

    public void performTargetedZoltraakAttack(Entity target)
    {
        // Perform ray trace
        HitResult hitResult = EntityAlgorithms.getHitScanAtTarget(this, this.position(), target, 128);

        performZoltraakAttack(hitResult, this.position(), DAMAGE);
    }

    public void performZoltraakAttack(HitResult hitResult, Vec3 origin, float damage)
    {

        Vec3 hitVector = hitResult.getLocation();

        Vec3 targetVector = hitVector.subtract(origin);
        Vec3 direction = targetVector.normalize();

        Vec3 beamPath = hitVector.subtract(origin);

        float radius = 0.3F;
        float thickness = 10F;

        // Create a hitbox along the beam path
        AABB hitbox = new AABB(origin, hitVector).inflate(radius);

        // Damage entities in hit box
        doMagicDamageToTargetsInHitBox(getOwner(), hitbox, damage);

        // Spawn magic particles
        ParticleUtil.spawnParticleBeam((ServerLevel) this.level, ParticleTypes.SOUL_FIRE_FLAME, origin, direction, (float) beamPath.length(), radius, thickness);

        // Make Sound
        level.playSound((Player)null,this.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

    }

    public static void doMagicDamageToTargetsInHitBox(LivingEntity sourceEntity, AABB hitbox, float damage)
    {
        // Check for entities within the hitbox
        List<LivingEntity> entitiesHit = EntityAlgorithms.getNonSculkUnitsInBoundingBox(sourceEntity.level, hitbox);

        for (LivingEntity entity : entitiesHit) {
            // Handle entity hit logic here
            if(entity.getUUID() != sourceEntity.getUUID() && !entity.isBlocking())
            {
                entity.hurt(DamageSource.MAGIC, damage);
            }
        }
    }
    protected static final EntityDataAccessor<Float> DATA_YAW = SynchedEntityData.defineId(ZoltraakAttackEntity.class, EntityDataSerializers.FLOAT);
    protected static final EntityDataAccessor<Float> DATA_PITCH = SynchedEntityData.defineId(ZoltraakAttackEntity.class, EntityDataSerializers.FLOAT);

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.getEntityData().define(DATA_YAW, 0.0F);
        this.getEntityData().define(DATA_PITCH, 0.0F);
    }

    // ### GECKOLIB Animation Code ###

    public static final String ATTACK_ID = "attack";
    protected static final RawAnimation ATTACK = RawAnimation.begin().thenPlay(ATTACK_ID);

    public static final String ATTACK_ANIMATION_CONTROLLER_ID = "attack_controller";
    protected final AnimationController ATTACK_ANIMATION_CONTROLLER = new AnimationController<>(this, ATTACK_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .triggerableAnim(ATTACK_ID, ATTACK);

    public static final String DESPAWN_ANIMATION_CONTROLLER_ID = "depsawn_controller";
    public static final RawAnimation DESPAWN = RawAnimation.begin().thenPlayAndHold("misc.die");
    public static final String DESPAWN_ANIMATION_ID = "die";
    protected final AnimationController DESPAWN_ANIMATION_CONTROLLER  = new AnimationController<>(this, DESPAWN_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .triggerableAnim(DESPAWN_ANIMATION_ID, DESPAWN);

    protected final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(
                DefaultAnimations.genericLivingController(this),
                DefaultAnimations.getSpawnController(this, AnimationState::getAnimatable, 0),
                ATTACK_ANIMATION_CONTROLLER,
                DESPAWN_ANIMATION_CONTROLLER
        );
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
