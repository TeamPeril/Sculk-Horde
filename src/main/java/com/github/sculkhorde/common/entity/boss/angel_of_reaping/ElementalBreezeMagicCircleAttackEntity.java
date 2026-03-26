package com.github.sculkhorde.common.entity.boss.angel_of_reaping;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3f;

public class ElementalBreezeMagicCircleAttackEntity extends ElementalFireMagicCircleAttackEntity {


    public ElementalBreezeMagicCircleAttackEntity(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    public ElementalBreezeMagicCircleAttackEntity(Level level) {
        this(ModEntities.ELEMENTAL_BREEZE_MAGIC_CIRCLE.get(), level);
    }

    public ElementalBreezeMagicCircleAttackEntity(Level level, double x, double y, double z, float angle, LivingEntity owner) {
        this(level);
        setPos(x,y,z);
        this.setYRot(angle * (180F / (float)Math.PI));
        setOwner(owner);
    }

    public void spawnPartilcesRandomlyInHitboxClientSide() {
        AABB boundingBox = getBoundingBox();
        float spawnX = (float) (boundingBox.minX + (boundingBox.maxX - boundingBox.minX) * level().getRandom().nextFloat());
        float spawnY = (float) (boundingBox.minY + (boundingBox.maxY - boundingBox.minY) * level().getRandom().nextFloat());
        float spawnZ = (float) (boundingBox.minZ + (boundingBox.maxZ - boundingBox.minZ) * level().getRandom().nextFloat());

        Vector3f spawn = new Vector3f(spawnX, spawnY, spawnZ);
        Vector3f deltaMovement = new Vector3f(0, 3, 0);

        String breezeColor = "958DD3";
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) level(), breezeColor, 0.8F, spawn, deltaMovement);
    }

    protected void applyEffect(LivingEntity entity)
    {

        float finalDamage = DAMAGE + (EntityAlgorithms.getStrengthOfLivingEntity(getOwner()) * 2);
        double damageResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        double d1 = Math.max(0.0D, 1.0D - damageResistance);
        entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.3D * d1, 0.0D));

        boolean didHurt = entity.hurt(damageSources().magic(), finalDamage);
        if(!didHurt)
        {
            return;
        }


        //double damageResistance = entity.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
        //double d1 = Math.max(0.0D, 1.0D - damageResistance);
        //entity.setDeltaMovement(entity.getDeltaMovement().add(0.0D, 0.6D * d1, 0.0D));


        if(getOwner() != null)
        {
            entity.hurt(getOwner().damageSources().magic(), finalDamage);
        }
        else
        {
            entity.hurt(damageSources().magic(), finalDamage);
        }

    }

}
