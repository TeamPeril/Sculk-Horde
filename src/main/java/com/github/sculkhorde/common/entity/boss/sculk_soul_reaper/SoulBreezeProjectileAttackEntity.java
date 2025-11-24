package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SoulBreezeProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity {
    public SoulBreezeProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulBreezeProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_BREEZE_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {

        double knockbackResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        Vec3 direction = entity.position().subtract(position()).normalize();
        float knockbackStrength = 0.4F;


        double d1 = Math.max(0.0D, 1.0D - knockbackResistance);
        Vec3 knockback = direction.scale(knockbackStrength * d1);
        entity.setDeltaMovement(entity.getDeltaMovement().add(knockback).add(0, 0.4F, 0));
        this.doEnchantDamageEffects((LivingEntity) getOwner(), entity);
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level().getRandom().nextFloat());
        float spawnY = (float) (getY() + level().getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level().getRandom().nextFloat());
        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        Vector3f deltaMovement = new Vector3f(0, 0, 0);
        String breezeColor = "958DD3";
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(), breezeColor, 0.8F, spawn, deltaMovement);
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 1.75F;
    }

    @Override
    public float getDamage() {

        if(getOwner() instanceof LivingEntity livingEntity)
        {
            return super.getDamage() + (EntityAlgorithms.getStrengthOfLivingEntity(livingEntity) * 2);
        }

        return super.getDamage();
    }

    @Override
    public Optional<SoundEvent> getImpactSound() {
        return Optional.of(SoundEvents.FIREWORK_ROCKET_BLAST);
    }


    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {

    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
}
