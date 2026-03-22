package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import org.joml.Vector3f;

public class DiseasedAtmosphereEffect extends MobEffect {

    public static int liquidColor = ColorUtil.hexToRGB(ColorUtil.purityLightColor4);
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(0.5F);
    public long cooldownTicksRemaining = COOLDOWN;

    public long DAMAGE_COOLDOWN = TickUnits.convertSecondsToTicks(5);
    public long timeOfLastDamageTick = 0;


    /**
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected DiseasedAtmosphereEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    public DiseasedAtmosphereEffect() {
        this(effectType, liquidColor);
    }

    public static void applyToEntity(LivingEntity victim, int duration)
    {
        if(!isEntityAffectableByThisEffect(victim))
        {
           return;
        }

        DiseasedAtmosphereEffect effect = ModMobEffects.DISEASED_ATMOSPHERE.get();
        victim.addEffect(new MobEffectInstance(effect, duration, 0));
    }

    public static boolean isEntityAffectableByThisEffect(LivingEntity victim)
    {
        if(victim == null || victim.isDeadOrDying())
        {
            return false;
        }

        DiseasedAtmosphereEffect effect = ModMobEffects.DISEASED_ATMOSPHERE.get();
        PurityEffect purity = ModMobEffects.PURITY.get();
        if(victim.hasEffect(effect) || victim.hasEffect(purity))
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
        if(victimEntity.level().isClientSide())
        {
            float spawnWidth = victimEntity.getBbWidth() / 2;
            float spawnHeight = victimEntity.getBbHeight() / 2;
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
            spawnRandomParticle(victimEntity, spawnWidth, spawnHeight);
        }

        PurityEffect purity = ModMobEffects.PURITY.get();

        if(victimEntity.hasEffect(purity) || Math.abs(victimEntity.level().getGameTime() - timeOfLastDamageTick) < DAMAGE_COOLDOWN)
        {
            return;
        }
        timeOfLastDamageTick = victimEntity.level().getGameTime();

        EntityAlgorithms.doSculkPiercingDamageToEntity(victimEntity, victimEntity, 1 + amp, 1);
        if(victimEntity instanceof ServerPlayer player)
        {
            player.causeFoodExhaustion(4F);
        }
    }

    private void spawnRandomParticle(LivingEntity victimEntity, float maxWidthOffset, float maxHeightOffset)
    {
        float randomX = (float) (victimEntity.getX() + getNextFloatBetweenInclusive(victimEntity.getRandom(), -maxWidthOffset, maxWidthOffset));
        float randomY = (float) (victimEntity.getY() + getNextFloatBetweenInclusive(victimEntity.getRandom(),-maxHeightOffset, maxHeightOffset) + maxHeightOffset);
        float randomZ = (float) (victimEntity.getZ() + getNextFloatBetweenInclusive(victimEntity.getRandom(),-maxWidthOffset, maxWidthOffset));
        ParticleUtil.spawnColoredDustParticleOnClient((ClientLevel) victimEntity.level(),
                ColorUtil.getRandomSculkLightColor(victimEntity.getRandom()),
                0.8F,
                new Vector3f(randomX, randomY, randomZ),
                new Vector3f(0, victimEntity.getRandom().nextFloat() * - 1, 0));
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
