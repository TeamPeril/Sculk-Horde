package com.github.sculkhorde.common.effect;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.living.MobEffectEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

public class RootedEffect extends MobEffect implements IPotionExpireEffect{

    public static int liquidColor = ColorUtil.hexToInt(ColorUtil.sculkBoneColor1);
    public static MobEffectCategory effectType = MobEffectCategory.HARMFUL;
    public long COOLDOWN = TickUnits.convertSecondsToTicks(1);
    public long cooldownTicksRemaining = COOLDOWN;
    protected Random random = new Random();


    /**
     * Old Dumb Constructor
     * @param effectType Determines if harmful or not
     * @param liquidColor The color in some number format
     */
    protected RootedEffect(MobEffectCategory effectType, int liquidColor) {
        super(effectType, liquidColor);
        addAttributeModifier(Attributes.ATTACK_DAMAGE, -0.8F, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.MAX_HEALTH, -0.5F, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_KNOCKBACK, -1F, AttributeModifier.Operation.MULTIPLY_BASE);
        addAttributeModifier(Attributes.ATTACK_SPEED, -0.5F, AttributeModifier.Operation.MULTIPLY_BASE);
    }

    /**
     * Simpler Constructor
     */
    public RootedEffect() {
        this(effectType, liquidColor);
    }


    @Override
    public void applyEffectTick(LivingEntity sourceEntity, int amp) {

        if(sourceEntity.level().isClientSide())
        {
            return;
        }

        if(sourceEntity instanceof ServerPlayer player)
        {
            player.causeFoodExhaustion(10F);
        }

    }

    @Override
    public void onPotionExpire(MobEffectEvent.Expired event)
    {
        if(event.getEntity().level().isClientSide()) { return;}

        LivingEntity entity = event.getEntity();
        // OR mob outside of world border
        if(entity == null || EntityAlgorithms.isSculkLivingEntity.test(entity))
        {
            return;
        }

        BlockAlgorithms.setBlockStructure(entity.level(), entity.blockPosition(), ModBlocks.BROOD_NEST_CORE_BLOCK.get().defaultBlockState());
        if(!EntityAlgorithms.isLivingEntityExplicitDenyTarget(entity))
        {
            entity.hurt(entity.damageSources().magic(), Integer.MAX_VALUE);
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

    public MobEffect addAttributeModifier(Attribute attribute, double value, AttributeModifier.Operation operation) {
        return addAttributeModifier(attribute, UUID.randomUUID().toString(), value, operation);
    }

}
