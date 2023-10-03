package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SculkSweeperSword extends SwordItem {

    public SculkSweeperSword() {
        this(Tiers.DIAMOND, 4, -3F, new Item.Properties().rarity(Rarity.EPIC).setNoRepair().durability(40));
    }
    public SculkSweeperSword(Tier tier, int baseDamage, float baseAttackSpeed, Properties prop) {
        super(tier, baseDamage, baseAttackSpeed, prop);
    }


    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity targetEntity, LivingEntity ownerEntity) {
        // Create a 2 block square bounding box around target and damage enemies
        AABB aabb = new AABB(targetEntity.blockPosition());
        aabb = aabb.inflate(10.0D);

        for(LivingEntity livingEntity : targetEntity.level().getEntitiesOfClass(LivingEntity.class, aabb))
        {
            if(livingEntity != ownerEntity)
            {
                boolean isSculkLivingEntity = EntityAlgorithms.isSculkLivingEntity.test(livingEntity);


                if(isSculkLivingEntity && itemStack.getDamageValue() > 0)
                {
                    targetEntity.hurt(ownerEntity.damageSources().indirectMagic(targetEntity, ownerEntity), isSculkLivingEntity ? getDamage() : getDamage() / 2);
                    itemStack.setDamageValue(itemStack.getDamageValue() - 1);
                }
                else
                {
                    AABB spikeHitbox = new AABB(targetEntity.blockPosition());
                    spikeHitbox = spikeHitbox.inflate(20.0D);
                    for(LivingEntity possibleSpikeTargets : targetEntity.level().getEntitiesOfClass(LivingEntity.class, spikeHitbox))
                    {
                        if(possibleSpikeTargets != ownerEntity)
                        {
                            boolean isSculkLivingEntity2 = EntityAlgorithms.isSculkLivingEntity.test(possibleSpikeTargets);
                            if(isSculkLivingEntity2 )
                            {
                                SculkSpineSpikeAttackEntity sculkSpineSpikeAttackEntity = new SculkSpineSpikeAttackEntity(ownerEntity, possibleSpikeTargets.getX(), possibleSpikeTargets.getY(), possibleSpikeTargets.getZ());
                                targetEntity.level().addFreshEntity(sculkSpineSpikeAttackEntity);
                                // Give effect
                                possibleSpikeTargets.addEffect(new MobEffectInstance(MobEffects.LEVITATION, TickUnits.convertSecondsToTicks(5), 1));
                            }
                        }
                    }

                    itemStack.setDamageValue(itemStack.getMaxDamage());
                    break;
                }
            }
        }
        return true;
    }

    @Override
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_sweeper_sword"));
    }
}
