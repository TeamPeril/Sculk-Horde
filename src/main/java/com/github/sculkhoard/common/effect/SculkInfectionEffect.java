package com.github.sculkhoard.common.effect;

import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.EffectType;
import net.minecraft.potion.Effects;
import net.minecraftforge.common.extensions.IForgeEffect;

import java.util.ArrayList;
import java.util.List;

public class SculkInfectionEffect extends Effect implements IForgeEffect {

    public static int liquidColor = 4738376;
    public static EffectType effectType = EffectType.HARMFUL;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkInfectionEffect(EffectType effectType, int liquidColor) {
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
