package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SculkLureEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertMinutesToTicks(5);
    public long cooldownTicksRemaining = COOLDOWN;


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkLureEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public SculkLureEffect() {
        this(effectType, liquidColor);
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int p_19468_) {

        if(EntityAlgorithms.isSculkLivingEntity.test(entity))
        {
            // Remove effect
            entity.removeEffect(ModMobEffects.SCULK_LURE.get());
            return;
        }
        SculkHorde.savedData.reportDeath(entity.blockPosition());

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

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }

}
