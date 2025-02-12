package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.Optional;

public class CorrodingEffect extends MobEffect {

    public static int liquidColor = ColorUtil.hexToRGB(ColorUtil.sculkAcidColor1);
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(1);
    public long cooldownTicksRemaining = COOLDOWN;
    private Optional<LivingEntity> attacker = Optional.empty();


    /**
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected CorrodingEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    public CorrodingEffect() {
        this(effectType, liquidColor);
    }

    public void setAttacker(LivingEntity attacker)
    {
        if(attacker != null) { this.attacker = Optional.of(attacker); }
    }


    public static void applyToEntity(LivingEntity source, LivingEntity victim, int duration)
    {
        if(victim == null) { return; }

        CorrodingEffect effect = ModMobEffects.CORRODED.get();

        if(source != null) { effect.setAttacker(source); }

        if(victim.hasEffect(effect))
        {
            victim.addEffect(new MobEffectInstance(effect, duration + victim.getEffect(ModMobEffects.CORRODED.get()).getDuration(), 0));
            return;
        }

        victim.addEffect(new MobEffectInstance(effect, duration, 0));
    }

    public static boolean isEntityAffectableByCorroded(LivingEntity victim)
    {
        CorrodingEffect effect = ModMobEffects.CORRODED.get();
        if(victim.hasEffect(effect))
        {
            return true;
        }

        return victim.canBeAffected(new MobEffectInstance(effect, 0, 0));
    }

    
    public float getNextFloatBetweenInclusive(RandomSource rng, float min, float max)
    {
        return (rng.nextFloat() * (max-min)) + min;
    }


    @Override
    public void applyEffectTick(LivingEntity victimEntity, int amp) {
        if(victimEntity.level.isClientSide())
        {
            float spawnWidth = victimEntity.getBbWidth() / 2;
            float spawnHeight = victimEntity.getBbHeight() / 2;
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
        }

        if(attacker.isPresent())
        {
            EntityAlgorithms.doSculkTypeDamageToEntity(attacker.get(), victimEntity, 2, 1);
        }
        else
        {
            EntityAlgorithms.doSculkTypeDamageToEntity(victimEntity, victimEntity, 2, 1);
        }
    }

    private void spawnRandomParticle(LivingEntity victimEntity, float maxWidthOffset, float maxHeightOffset)
    {
        float randomX = (float) (victimEntity.getX() + getNextFloatBetweenInclusive(victimEntity.getRandom(), -maxWidthOffset, maxWidthOffset));
        float randomY = (float) (victimEntity.getY() + getNextFloatBetweenInclusive(victimEntity.getRandom(),-maxHeightOffset, maxHeightOffset) + maxHeightOffset);
        float randomZ = (float) (victimEntity.getZ() + getNextFloatBetweenInclusive(victimEntity.getRandom(),-maxWidthOffset, maxWidthOffset));
        //victimEntity.level().addParticle(new DustParticleOptions(Vec3.fromRGB24(2726783).toVector3f(), 2.0F), randomX, randomY, randomZ, 0.0D, victimEntity.getRandom().nextDouble() * - 1, 0.0D);


    /**
     * A function that is called every tick an entity has this effect. <br>
     * I do not use because it does not provide any useful inputs like
     * the entity it is affecting. <br>
     * I instead use ForgeEventSubscriber.java to handle the logic.
     * @param ticksLeft The amount of ticks remaining
     * @param amplifier The level of the effect
     * @return Determines if the effect should apply.
     */
    }
}
