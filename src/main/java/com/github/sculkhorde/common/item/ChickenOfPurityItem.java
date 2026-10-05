package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ChickenOfPurityItem extends Item {
    public ChickenOfPurityItem(Properties p_41383_) {
        super(p_41383_);
    }

    public ChickenOfPurityItem() {
        super(getProperties());
    }

    public static Properties getProperties()
    {
        return new Properties()
                .rarity(Rarity.UNCOMMON)
                .food(new FoodProperties.Builder()
                        .saturationModifier(7.2f)
                        .nutrition(6)
                        .effect(new MobEffectInstance(ModMobEffects.PURITY, TickUnits.convertMinutesToTicks(15), 0), 1.0F)
                        .build());

    }


    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.food_of_purity.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.food_of_purity.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

}
