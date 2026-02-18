package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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
        if(entity.level().isClientSide() ) { return;}

        // Give strength and speed to the player if near sculk node
        Optional<ModSavedData.NodeEntry> nearestNode = NodeUtil.getClosestNode((ServerLevel) entity.level(), entity.blockPosition());

        if(nearestNode.isEmpty() || ModSavedData.getSaveData().isHordeDefeated())
        {
            return;
        }

        boolean isInSameDimension = BlockAlgorithms.areTheseDimensionsEqual((ServerLevel) entity.level(), nearestNode.get().getDimension());
        boolean inRangeOfNode = BlockAlgorithms.getBlockDistance(entity.blockPosition(), nearestNode.get().getPosition()) <= 200;
        if(isInSameDimension && inRangeOfNode)
        {
            EntityAlgorithms.applyEffectToTarget(entity, MobEffects.MOVEMENT_SPEED, TickUnits.convertMinutesToTicks(2), 0);
            EntityAlgorithms.applyEffectToTarget(entity, MobEffects.DAMAGE_BOOST, TickUnits.convertMinutesToTicks(2), 0);
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
