package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import net.minecraft.core.BlockPos;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

public class DenseEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;

    protected static int distanceAboveGroundRequired = 2;

    /**
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected DenseEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    public DenseEffect() {
        this(effectType, liquidColor);
    }

    public static void applyToEntity(LivingEntity source, LivingEntity victim, int duration)
    {
        if(victim == null) { return; }

        DenseEffect effect = ModMobEffects.DENSE.get();

        if(victim.hasEffect(ModMobEffects.DENSE))
        {
            victim.addEffect(new MobEffectInstance(ModMobEffects.DENSE, duration + victim.getEffect(ModMobEffects.DENSE).getDuration(), 0));
            return;
        }

        victim.addEffect(new MobEffectInstance(ModMobEffects.DENSE, duration, 0));
    }

    @Override
    public boolean applyEffectTick(LivingEntity victimEntity, int amp) {

        if(!victimEntity.onGround() && isBeyondRequiredDistanceFromGround(victimEntity))
        {
            // Note: This needs to happen on the client AND server
            victimEntity.push(0, -0.07 - (0.1 * victimEntity.getSpeed()), 0);
            victimEntity.hurtMarked = true;
        }
        return true;
    }

    public static boolean isBeyondRequiredDistanceFromGround(LivingEntity e) {
        Level world = e.level();
        BlockPos pos = e.blockPosition();
        int y = pos.getY();

        while (y >= distanceAboveGroundRequired) {

            if(!world.getBlockState(new BlockPos(pos.getX(), y - 1, pos.getZ())).isAir())
            {
                return false;
            }

            y--;
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
    @Override
    public boolean shouldApplyEffectTickThisTick(int ticksLeft, int amplifier) {
        return true;
    }
}
