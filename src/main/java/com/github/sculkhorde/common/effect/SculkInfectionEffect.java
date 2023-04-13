package com.github.sculkhorde.common.effect;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;

import java.util.ArrayList;
import java.util.List;

public class SculkInfectionEffect extends MobEffect {

    public static int spawnInterval = 20;
    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkInfectionEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public SculkInfectionEffect() {
        this(effectType, liquidColor);
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int amplifier) {
        super.applyEffectTick(entity, amplifier);
    }

    /**
     * A function that is called every tick an entity has this effect. <br>
     * I do not use because it does not provide any useful inputs like
     * the entity it is affecting. <br>
     * I instead use ForgeEventSubscriber.java to handle the logic.
     * @param ticksLeft The amount of ticks remaining
     * @param amplifier The level of the effect
     * @return ??
     */
    @Override
    public boolean isDurationEffectTick(int ticksLeft, int amplifier) {
        return super.isDurationEffectTick(ticksLeft, amplifier);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }

}
