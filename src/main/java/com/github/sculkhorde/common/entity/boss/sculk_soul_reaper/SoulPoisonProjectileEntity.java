package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;

import java.util.Optional;

public class SoulPoisonProjectileEntity extends AbstractProjectileEntity {
    public SoulPoisonProjectileEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
    }

    public SoulPoisonProjectileEntity(Level level, LivingEntity shooter)
    {
        this(ModEntities.SOUL_POISON_PROJECTILE.get(), level);
        setOwner(shooter);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {
        entity.addEffect(new MobEffectInstance(MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0));
    }

    @Override
    public void trailParticles() {

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
}
