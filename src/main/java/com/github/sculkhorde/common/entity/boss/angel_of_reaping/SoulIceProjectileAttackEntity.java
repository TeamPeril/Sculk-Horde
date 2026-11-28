package com.github.sculkhorde.common.entity.boss.angel_of_reaping;

import com.github.sculkhorde.common.entity.projectile.AbstractProjectileEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import org.joml.Vector3f;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

import java.util.Optional;

public class SoulIceProjectileAttackEntity extends AbstractProjectileEntity implements GeoEntity {
    public SoulIceProjectileAttackEntity(EntityType<? extends Projectile> pEntityType, Level pLevel) {
        super(pEntityType, pLevel);
        setNoGravity(true);
    }

    public SoulIceProjectileAttackEntity(Level level, LivingEntity shooter, float damage)
    {
        this(ModEntities.SOUL_ICE_PROJECTILE.get(), level);
        setOwner(shooter);
        setDamage(damage);
    }

    @Override
    protected void applyEffectToEntity(LivingEntity entity) {
         entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, TickUnits.convertSecondsToTicks(10), 0));
         entity.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, TickUnits.convertSecondsToTicks(10), 0));
    }

    @Override
    public void trailParticles() {
        float spawnX = (float) (getX() + level().getRandom().nextFloat());
        float spawnY = (float) (getY() + level().getRandom().nextFloat());
        float spawnZ = (float) (getZ() + level().getRandom().nextFloat());
        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        ParticleUtil.spawnSnowflakeParticleOnClient((ClientLevel) level(), spawn, new Vector3f(0,0,0));
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
