package com.github.sculkhorde.common.entity.boss.angel_of_reaping;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

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

    protected void applyEffect(LivingEntity entity)
    {
        float finalDamage = DAMAGE + (EntityAlgorithms.getStrengthOfLivingEntity(getOwner()) * 2);
        boolean didHurt = entity.hurt(damageSources().magic(), finalDamage);

        if(!didHurt)
        {
            return;
        }

        if(getOwner() != null)
        {
            entity.hurt(getOwner().damageSources().magic(), finalDamage);
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0), getOwner());
        }
        else
        {
            entity.hurt(damageSources().magic(), finalDamage);
            entity.addEffect(new MobEffectInstance(MobEffects.POISON, TickUnits.convertSecondsToTicks(10), 0));
        }

    }

    public void spawnPartilcesRandomlyInHitboxClientSide() {
        AABB boundingBox = getBoundingBox();
        float spawnX = (float) (boundingBox.minX + (boundingBox.maxX - boundingBox.minX) * level().getRandom().nextFloat());
        float spawnY = (float) (boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * level().getRandom().nextFloat());
        float spawnZ = (float) (boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) * level().getRandom().nextFloat());

        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        Vector3f deltaMovement = new Vector3f(0, 3, 0);

        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(), ColorUtil.getRandomHexAcidColor(level().getRandom()), 0.8F, spawn, deltaMovement);
    }

}
