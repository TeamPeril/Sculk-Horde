package com.github.sculkhorde.common.effect;

import java.util.ArrayList;
import java.util.List;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

public class SculkVesselEffect extends MobEffect {

    public static int liquidColor = 338997;
    public static MobEffectCategory effectType = MobEffectCategory.BENEFICIAL;

    public long COOLDOWN = TickUnits.convertSecondsToTicks(10);
    public long cooldownTicksRemaining = COOLDOWN;

    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected SculkVesselEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
    }

    /**
     * Simpler Constructor
     */
    public SculkVesselEffect() {
        this(effectType, liquidColor);
    }


    @Override
    public void applyEffectTick(LivingEntity entity, int amp) {
        if(entity.level.isClientSide())
        {
            return;
        }

        // Give strength and speed to the player if near sculk node
        ModSavedData.NodeEntry nearestNode = SculkHorde.savedData.getClosestNodeEntry((ServerLevel) entity.level, entity.blockPosition());

        if(nearestNode == null)
        {
            return;
        }

        boolean isInSameDimension = BlockAlgorithms.areTheseDimensionsEqual((ServerLevel) entity.level, nearestNode.getDimension());
        boolean inRangeOfNode = BlockAlgorithms.getBlockDistance(entity.blockPosition(), nearestNode.getPosition()) <= 200;
        if(isInSameDimension && inRangeOfNode)
        {
            entity.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, TickUnits.convertMinutesToTicks(2), 0));
            entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, TickUnits.convertMinutesToTicks(2), 0));
        }

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
