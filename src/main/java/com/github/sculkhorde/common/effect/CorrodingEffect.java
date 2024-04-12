package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.*;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

public class CorrodingEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(1);
    public long cooldownTicksRemaining = COOLDOWN;
    private Optional<LivingEntity> attacker = Optional.empty();


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected CorrodingEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public CorrodingEffect() {
        this(effectType, liquidColor);
    }
    public CorrodingEffect(LivingEntity attackerIn) {
        this(effectType, liquidColor);
        if(attackerIn != null)
        {
            this.attacker = Optional.of(attackerIn);
        }

    }

    public static void applyToEntity(LivingEntity source, LivingEntity victim, int duration)
    {
        victim.addEffect(new MobEffectInstance(ModMobEffects.CORRODED.get(), duration, 0));
    }

    public double getNextDoubleBetweenInclusive(RandomSource rng, double min, double max)
    {
        return (rng.nextDouble() * (max-min)) + min;
    }


    @Override
    public void applyEffectTick(LivingEntity victimEntity, int amp) {
        if(victimEntity.level().isClientSide())
        {
            double randomX = victimEntity.getX() + getNextDoubleBetweenInclusive(victimEntity.getRandom(), -0.5, 0.5);
            double randomY = victimEntity.getY() + getNextDoubleBetweenInclusive(victimEntity.getRandom(),-1, 1) + 1;
            double randomZ = victimEntity.getZ() + getNextDoubleBetweenInclusive(victimEntity.getRandom(),-0.5, 0.5);
            victimEntity.level().addParticle(new DustParticleOptions(Vec3.fromRGB24(1302700).toVector3f(), 2.0F), randomX, randomY, randomZ, 0.0D, victimEntity.getRandom().nextDouble() * - 1, 0.0D);
            return;
        }

        float damage = 0.1F;
        if(!victimEntity.isInvulnerable())
        {
            victimEntity.hurt(victimEntity.damageSources().generic(), damage);
            victimEntity.setHealth(Math.max(0, victimEntity.getHealth() - 1));
        }


    }

    @Override
    public MobEffect addAttributeModifier(Attribute attribute, String id, double value, AttributeModifier.Operation operation) {
        return super.addAttributeModifier(attribute, id, value, operation);
    }

    /**
     * A function that is called every tick an entity has this effect. <br>
     * I do not use because it does not provide any useful inputs like
     * the entity it is affecting. <br>
     * I instead use ForgeEventSubscriber.java to handle the logic.
     * @param ticksLeft The amount of ticks remaining
     * @param amplifier The level of the effect
     * @return Determines if the effect should apply.
     */
    @Override
    public boolean isDurationEffectTick(int ticksLeft, int amplifier) {
        if(cooldownTicksRemaining > 0)
        {
            cooldownTicksRemaining--;
            return false;
        }
        cooldownTicksRemaining = COOLDOWN;
        return true;

    }
}
