package com.github.sculkhorde.common.entity.boss.angel_of_reaping;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SoulSpearProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity {
    public SoulSpearProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulSpearProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_SPEAR_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
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
    protected void onHitBlock(BlockHitResult hitResult) {
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {

    }



    @Override
    public void trailParticles() {
        if(level().isClientSide())
        {
            level().addParticle(ParticleTypes.SOUL, this.position().x, this.position().y, this.position().z, 0, 0,0);
        }
    }

    @Override
    public void impactParticles(double x, double y, double z) {

    }

    @Override
    public float getSpeed() {
        return 3F;
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
