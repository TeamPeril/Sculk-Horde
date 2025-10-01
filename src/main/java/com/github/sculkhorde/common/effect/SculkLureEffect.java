package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.DifficultyUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;

public class SculkLureEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long cooldownTicksRemaining = 0;


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

    public long getCooldownBasedOnDifficulty()
    {
        if(DifficultyUtil.isCurrentDifficultyEasy())
        {
            return TickUnits.convertMinutesToTicks(5);
        }
        else if(DifficultyUtil.isCurrentDifficultyNormal())
        {
            return TickUnits.convertMinutesToTicks(2);
        }
        else
        {
            return TickUnits.convertMinutesToTicks(1);
        }
    }

    @Override
    public void applyEffectTick(LivingEntity entity, int p_19468_) {

        if(entity.level().isClientSide()) { return;}
        if(EntityAlgorithms.isSculkLivingEntity.test(entity))
        {
            // Remove effect
            entity.removeEffect(ModMobEffects.SCULK_LURE.get());
            return;
        }
        if(ModSavedData.getSaveData() != null) { ModSavedData.getSaveData().reportDeath((ServerLevel) entity.level(), entity.blockPosition()); }

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
        cooldownTicksRemaining = getCooldownBasedOnDifficulty();
        return true;

    }

    @Override
    public List<ItemStack> getCurativeItems() {
        ArrayList<ItemStack> ret = new ArrayList<ItemStack>();
        return ret;
    }

}
