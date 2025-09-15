package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.boss.SpecialEffectEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSounds;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.BeamHitbox;
import com.google.common.base.Predicates;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
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

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class ZoltraakAttackEntity extends SpecialEffectEntity implements GeoEntity {
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
        setYRot(getSyncedEntityYaw());
        setXRot(getSyncedEntityPitch());
    }

    @Override
    public void setYRot(float value) {
        super.setYRot(value);

        if(level().isClientSide()) { return; }

        setSyncedEntityYaw(value);
    }

    @Override
    public void setXRot(float value) {
        super.setXRot(value);

        if(level().isClientSide()) { return; }

        setSyncedEntityPitch(value);
    }

    public float getSyncedEntityYaw()
    {
        return entityData.get(DATA_YAW);
    }

    public void setSyncedEntityYaw(float value)
    {
        entityData.set(DATA_YAW, value);
    }

    public float getSyncedEntityPitch()
    {
        return entityData.get(DATA_PITCH);
    }

    public void setSyncedEntityPitch(float value)
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
        ZoltraakAttackEntity zoltraak = new ZoltraakAttackEntity(target.level());
        zoltraak.setPos(spawnPos);
        zoltraak.setOwner(owner);
        zoltraak.setTarget(target);
        EntityAlgorithms.lookAt(zoltraak, target);
        target.level().addFreshEntity(zoltraak);
        return zoltraak;
    }

    public static ZoltraakAttackEntity castZoltraakFromPlayer(Player owner)
    {
        Vec3 playerLookPos = EntityAlgorithms.playerTargetBlockPos(owner, false).getCenter();

        ZoltraakAttackEntity zoltraak = new ZoltraakAttackEntity(owner.level());
        zoltraak.setPos(owner.getEyePosition());
        zoltraak.setOwner(owner);
        EntityAlgorithms.lookAt(zoltraak, playerLookPos);
        owner.level().addFreshEntity(zoltraak);
        return zoltraak;
    }

    @Override
    public void tick() {
        super.tick();

        if(level().isClientSide())
        {
            syncPitchAndYaw();
            return;
        }
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
            }
            else if(targetPos.isEmpty())
            {
                performForwardZoltraakAttack();
            }

            timeOfDespawnStart = level().getGameTime();
            completedAttack = true;
            triggerAnim(DESPAWN_ANIMATION_CONTROLLER_ID, DESPAWN_ANIMATION_ID);
        }

        if(completedAttack && Math.abs(timeOfDespawnStart - level().getGameTime()) >= DESPAWN_DELAY)
        {
            discard();
        }

    }

    public void performForwardZoltraakAttack() {
        // Eye position of the entity (so it doesn't shoot from feet)
        Vec3 origin = this.position();

        // Direction the entity is looking
        Vec3 lookVec = this.getLookAngle().normalize();

        // Extend direction up to 50 blocks
        Vec3 targetPos = origin.add(lookVec.scale(50.0D));

        // Do a ray trace to see what we hit (block/entity)
        HitResult hitResult = this.level().clip(new ClipContext(
                origin,
                targetPos,
                ClipContext.Block.COLLIDER,   // stop on solid blocks
                ClipContext.Fluid.NONE,       // ignore fluids (change if you want fluids to count)
                this
        ));

        // If we didn't hit anything, just use the end of the 50-block vector
        if (hitResult.getType() == HitResult.Type.MISS) {
            hitResult = BlockHitResult.miss(targetPos, Direction.getNearest(lookVec.x, lookVec.y, lookVec.z), BlockPos.containing(targetPos));
        }

        // Reuse your existing attack logic
        performZoltraakAttack(hitResult, origin, DAMAGE);
    }

    public void performTargetedZoltraakAttack(Entity target)
    {
        // Perform ray trace
        HitResult hitResult = EntityAlgorithms.getHitScanAtTarget(this, this.position(), target, 128);

        performZoltraakAttack(hitResult, this.position(), DAMAGE);
    }

    public void performZoltraakAttack(HitResult hitResult, Vec3 origin, float damage)
    {

        Vec3 hitLocation = hitResult.getLocation();

        Vec3 targetVector = hitLocation.subtract(origin);
        Vec3 direction = targetVector.normalize();

        Vec3 beamPath = hitLocation.subtract(origin);

        float radius = 0.3F;
        float thickness = 10F;

        // Make Sound
        level().playSound(this,this.blockPosition(), ModSounds.ZOLTRAAK_ATTACK.get(), SoundSource.HOSTILE, 1.0F, 1.0F);

        // We now need to check if any of these entities to be hit are playes
        // holding a shield up at the right time, to deflect zoltraak.
        //AABB beamAttackHitBox = new AABB(origin, hitLocation).inflate(radius); // Create a hitbox along the beam path
        BeamHitbox beamAttackHitBox = new BeamHitbox(origin, hitLocation, radius);
        List<LivingEntity> entitiesToBeHitSorted = getEntitiesInBeamHitbox(getOwner(), beamAttackHitBox); // Damage entities in hit box
        Optional<Player> closestPlayerDeflecting = getClosestPlayerDeflecting(entitiesToBeHitSorted);

        if(closestPlayerDeflecting.isPresent())
        {
            // Stop Particle at Player
            double distanceToDeflector = origin.distanceTo(closestPlayerDeflecting.get().position());
            ParticleUtil.spawnParticleBeam((ServerLevel) this.level(), ParticleTypes.SOUL_FIRE_FLAME, origin, direction, (float) distanceToDeflector, radius, thickness);

            // New particle beam in the direction the player is looking
            Vec3 deflectedBeamOrigin = closestPlayerDeflecting.get().getEyePosition();
            Vec3 deflectedBeamDirection = closestPlayerDeflecting.get().getLookAngle();

            // Do Clip Ray cast from player's eyes to block location
            ClipContext rayTrace = new ClipContext(
                    closestPlayerDeflecting.get().getEyePosition(1.0F),
                    closestPlayerDeflecting.get().getEyePosition(1.0F).add(deflectedBeamDirection.scale(32)),
                    ClipContext.Block.OUTLINE,
                    ClipContext.Fluid.NONE,
                    closestPlayerDeflecting.get());

            Vec3 PlayerEyesHitResult = rayTrace.getTo();
            Float deflectedBeamPathLength = (float) PlayerEyesHitResult.subtract(deflectedBeamOrigin).length();
            ParticleUtil.spawnParticleBeam((ServerLevel) this.level(), ParticleTypes.SOUL_FIRE_FLAME, deflectedBeamOrigin, deflectedBeamDirection, deflectedBeamPathLength, radius, thickness);
            BeamHitbox deflectedBeamAttackHitBox = new BeamHitbox(deflectedBeamOrigin, PlayerEyesHitResult, radius);
            doMagicDamageToTargetsInHitBox(closestPlayerDeflecting.get(), deflectedBeamAttackHitBox, damage);
        }
        else
        {
            doMagicDamageToTargetsInHitBox(getOwner(), beamAttackHitBox, damage);
            ParticleUtil.spawnParticleBeam((ServerLevel) this.level(), ParticleTypes.SOUL_FIRE_FLAME, origin, direction, (float) beamPath.length(), radius, thickness);
        }
    }

    public static boolean isPlayerUsingShieldAndForValidDeflectDuration(Player player)
    {
        if(player.getTicksUsingItem() <= 0) { return false; }

        if(!(player.getUseItem().getItem() instanceof ShieldItem)) { return false; }

        int ticksUsingShield = player.getTicksUsingItem();

        return ticksUsingShield <= TickUnits.convertSecondsToTicks(0.5F);
    }

    public static Optional<Player> getClosestPlayerDeflecting(List<LivingEntity> entityListSorted)
    {
        Optional<Player> result = Optional.empty();

        for(LivingEntity entity : entityListSorted)
        {
            if(entity instanceof Player player && isPlayerUsingShieldAndForValidDeflectDuration(player))
            {
                result = Optional.of(player);
                break;
            }
        }

        return result;
    }

    public static List<LivingEntity> getEntitiesInBeamHitbox(LivingEntity sourceEntity, BeamHitbox hitbox)
    {
        // Check for entities within the hitbox
        List<LivingEntity> entitiesHit;

        if(sourceEntity instanceof Player)
        {
            entitiesHit = hitbox.getLivingEntitiesInHitbox(sourceEntity.level(), sourceEntity, Predicates.alwaysTrue());
        }
        else
        {
            entitiesHit = hitbox.getLivingEntitiesInHitbox(sourceEntity.level(), sourceEntity, EntityAlgorithms.isNotSculkHordeLivingEntity);
        }

        entitiesHit.sort(Comparator.comparingDouble(sourceEntity::distanceTo));

        return entitiesHit;
    }

    public static void doMagicDamageToTargetsInHitBox(LivingEntity sourceEntity, BeamHitbox hitbox, float damage)
    {
        // Check for entities within the hitbox
        List<LivingEntity> entitiesHit;

        if(sourceEntity instanceof Player)
        {
            entitiesHit = hitbox.getLivingEntitiesInHitbox(sourceEntity.level(), sourceEntity, Predicates.alwaysTrue());
        }
        else
        {
            entitiesHit = hitbox.getLivingEntitiesInHitbox(sourceEntity.level(), sourceEntity, EntityAlgorithms.isNotSculkHordeLivingEntity);
        }

        for (LivingEntity entity : entitiesHit) {
            // Handle entity hit logic here
            if(entity.getUUID() == sourceEntity.getUUID())
            {
                continue;
            }

            entity.hurt(sourceEntity.damageSources().magic(), damage);

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
            .transitionLength(5)
            .triggerableAnim(ATTACK_ID, ATTACK);

    public static final String DESPAWN_ANIMATION_CONTROLLER_ID = "depsawn_controller";
    public static final RawAnimation DESPAWN = RawAnimation.begin().thenPlayAndHold("misc.die");
    public static final String DESPAWN_ANIMATION_ID = "die";
    protected final AnimationController DESPAWN_ANIMATION_CONTROLLER  = new AnimationController<>(this, DESPAWN_ANIMATION_CONTROLLER_ID, state -> PlayState.STOP)
            .transitionLength(5)
            .triggerableAnim(DESPAWN_ANIMATION_ID, DESPAWN);

    protected final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

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
