package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import mod.azure.azurelib.core.animatable.instance.AnimatableInstanceCache;
import mod.azure.azurelib.core.animation.AnimatableManager;
import mod.azure.azurelib.util.AzureLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class ElementalPoisonMagicCircleAttackEntity extends ElementalFireMagicCircleAttackEntity {

    public ElementalPoisonMagicCircleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ElementalPoisonMagicCircleAttackEntity(Level level) {
        this(ModEntities.ELEMENTAL_POISON_MAGIC_CIRCLE.get(), level);
    }

    public ElementalPoisonMagicCircleAttackEntity(Level level, double x, double y, double z, float angle, LivingEntity owner) {
        this(level);
        setPos(x,y,z);
        this.setYRot(angle * (180F / (float)Math.PI));
        setOwner(owner);
    }

    @Override
    public void tick() {

        if(level.isClientSide()) { return; }

        currentLifeTicks++;

        // If the entity is alive for more than LIFE_TIME, discard it
        if(currentLifeTicks >= LIFE_TIME && LIFE_TIME != -1) this.discard();

        AABB hitbox = getBoundingBox().inflate(0,5,0);

        List<LivingEntity> damageHitList = EntityAlgorithms.getEntitiesExceptOwnerInBoundingBox(getOwner(), (ServerLevel) level, hitbox);

        for (LivingEntity entity : damageHitList)
        {
            if (getOwner() != null && getOwner().equals(entity))
            {
                continue;
            }

            if(getOwner() != null)
            {
                boolean didHurt = entity.hurt(DamageSource.MAGIC, DAMAGE);
                if(didHurt)
                {
                    double damageResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    double d1 = Math.max(0.0D, 1.0D - damageResistance);
                    entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.6D * d1, 0.0D));
                    this.doEnchantDamageEffects(getOwner(), entity);
                }

            }
            else
            {
                entity.hurt(DamageSource.MAGIC, DAMAGE);
            }
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0));
        }


    }

    // ### GECKOLIB Animation Code ###
    private final AnimatableInstanceCache cache = AzureLibUtil.createInstanceCache(this);

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        //controllers.add(DefaultAnimations.genericIdleController(this));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }

}
