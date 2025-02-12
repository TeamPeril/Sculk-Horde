package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.ParticleUtil;
import com.mojang.math.Vector3f;
import mod.azure.azurelib.animatable.GeoEntity;
import mod.azure.azurelib.core.animatable.GeoAnimatable;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SoulFireProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity, GeoAnimatable {
    public SoulFireProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulFireProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_FIRE_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {
        entity.setSecondsOnFire(10);
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level.getRandom().nextFloat());
        float spawnY = (float) (getY() + level.getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level.getRandom().nextFloat());
        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        ParticleUtil.spawnFlameParticleOnClient((ClientLevel) level, spawn, new Vector3f(0,0,0));
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1.75F;
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.FIREWORK_ROCKET_BLAST);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);
    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
