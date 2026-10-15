package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.common.EffectCure;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PurityEffect extends MobEffect {

    public static int liquidColor = 15518533;
    public static MobEffectCategory effectType = MobEffectCategory.BENEFICIAL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(2);
    public long cooldownTicksRemaining = COOLDOWN;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected PurityEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public PurityEffect() {
        this(effectType, liquidColor);
    }


    @Override
    public boolean applyEffectTick(LivingEntity entity, int amp) {

        if(entity.level().isClientSide()) { return true;}
        // IF entity has a sculk infection, remove it
        if(entity.hasEffect(ModMobEffects.SCULK_INFECTION))
        {
            entity.removeEffect(ModMobEffects.SCULK_INFECTION);
        }

        if(entity.hasEffect(ModMobEffects.SCULK_LURE))
        {
            entity.removeEffect(ModMobEffects.SCULK_LURE);
        }

        if(entity.hasEffect(ModMobEffects.DISEASED_CYSTS))
        {
            entity.removeEffect(ModMobEffects.DISEASED_CYSTS);
        }

        if(entity.hasEffect(ModMobEffects.ROOTED_EFFECT))
        {
            entity.removeEffect(ModMobEffects.ROOTED_EFFECT);
        }

        // If Sculk Living Entity, do damage
        if(EntityAlgorithms.isSculkLivingEntity.test(entity) || EntityAlgorithms.isLivingEntityAllyToSculkHorde(entity))
        {
            entity.hurt(entity.damageSources().magic(), 1);
        }
        return true;

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
    // Stateless check: triggers every 40 ticks (2 seconds) for each entity independently
    @Override
    public boolean shouldApplyEffectTickThisTick(int ticksLeft, int amplifier) {
        return ticksLeft % TickUnits.convertSecondsToTicks(2) == 0;
    }

    @Override
    public void fillEffectCures(Set<EffectCure> cures, MobEffectInstance effectInstance) {
        // Leave cures empty so that milk (or any other cure) cannot remove this effect!
    }

}
