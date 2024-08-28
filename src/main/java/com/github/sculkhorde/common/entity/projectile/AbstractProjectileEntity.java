package com.github.sculkhorde.common.entity.projectile;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ProjectileUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;

import java.util.Objects;
import java.util.Optional;

public abstract class AbstractProjectileEntity extends Projectile {

    protected static final int EXPIRE_TIME = TickUnits.convertSecondsToTicks(15);

    protected float damage;
    protected float explosionRadius;

    /**
     * Client Side, called every tick
     */
    public abstract void trailParticles();

    /**
     * Server Side, called alongside onHit()
     */
    public abstract void impactParticles(double x, double y, double z);

    public abstract float getSpeed();

    public abstract Optional<SoundEvent> getImpactSound();

    public AbstractProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public void shoot(Vec3 rotation) {
        setDeltaMovement(rotation.scale(getSpeed()));
    }

    public void setDamage(float damage) {
        this.damage = damage;
    }

    public float getDamage() {
        return damage;
    }

    public float getExplosionRadius() {
        return explosionRadius;
    }

    public void setExplosionRadius(float explosionRadius) {
        this.explosionRadius = explosionRadius;
    }

    @Override
    public void checkDespawn() {
        if (level() instanceof ServerLevel serverLevel && !serverLevel.getChunkSource().chunkMap.getDistanceManager().inEntityTickingRange(this.chunkPosition().toLong())) {
            this.discard();
        }
    }

    @Override
    protected boolean canHitEntity(Entity entityIn) {
        if (!entityIn.canBeHitByProjectile()) {
            return false;
        } else {
            Entity entity = this.getOwner();
            return entity == null || !entity.isPassengerOfSameVehicle(entityIn);
        }
    }

    abstract protected void applyEffectToEntity(LivingEntity entity);


    @Override
    protected void onHitEntity(EntityHitResult entityHitResult) {
        if (!this.level().isClientSide()) {
            Entity entity = entityHitResult.getEntity();
            if (entity instanceof LivingEntity livingEntity){
                if(!EntityAlgorithms.isSculkLivingEntity.test(livingEntity))
                {
                    entity.hurt(damageSources().generic(),this.getDamage());
                    applyEffectToEntity(livingEntity);
                }
            }
        }else{
            super.onHitEntity(entityHitResult);
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult hitResult) {
        super.onHitBlock(hitResult);
        if (level().getBlockState(hitResult.getBlockPos()).isSolidRender(level(),hitResult.getBlockPos()))
            discard();
    }

    @Override
    public void tick() {
        super.tick();
        HitResult hitresult = ProjectileUtil.getHitResultOnMoveVector(this, this::canHitEntity);

        if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
            this.onHit(hitresult);
        }

        if (tickCount > EXPIRE_TIME) {
            discard();
            return;
        }
        if (level().isClientSide) {
            trailParticles();
        }
        travel();
    }

    public boolean isBeingShieldBlocked(LivingEntity target)
    {
        if (target.isBlocking()) {
            Vec3 vec32 = this.position();
            if (vec32 != null) {
                Vec3 targetViewVector = target.getViewVector(1.0F);
                Vec3 vec31 = vec32.vectorTo(target.position()).normalize();
                vec31 = new Vec3(vec31.x, 0.0D, vec31.z);
                if (vec31.dot(targetViewVector) < 0.0D) {
                    return true;
                }
            }
        }

        return false;
    }

    public void travel() {
        setPos(position().add(getDeltaMovement()));
        ProjectileUtil.rotateTowardsMovement(this, 1);
        if (!this.isNoGravity()) {
            Vec3 vec34 = this.getDeltaMovement();
            this.setDeltaMovement(vec34.x, vec34.y - (double) 0.05F, vec34.z);
        }
    }



    @Override
    public boolean shouldBeSaved() {
        return super.shouldBeSaved() && !Objects.equals(getRemovalReason(), RemovalReason.UNLOADED_TO_CHUNK);
    }

    protected void doImpactSound(SoundEvent sound) {
        level().playSound(null, getX(), getY(), getZ(), sound, SoundSource.NEUTRAL, 2, .9f + level().random.nextFloat() * .2f);
    }

    @Override
    protected void defineSynchedData() {

    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("Damage", this.getDamage());
        if (explosionRadius != 0) {
            tag.putFloat("ExplosionRadius", explosionRadius);
        }
        tag.putInt("Age", tickCount);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.damage = tag.getFloat("Damage");
        if (tag.contains("ExplosionRadius")) {
            this.explosionRadius = tag.getFloat("ExplosionRadius");
        }
        this.tickCount = tag.getInt("Age");
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    @Override
    public boolean isOnFire() {
        return false;
    }

    /**
     * Whether or not the projectile should treat magic shields as a block impact
     */
    protected boolean shouldPierceShields() {
        return false;
    }
}
