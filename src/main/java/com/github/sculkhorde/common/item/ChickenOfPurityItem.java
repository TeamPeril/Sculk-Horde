package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.Nullable;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class ChickenOfPurityItem extends Item implements IForgeItem {
    public ChickenOfPurityItem(Properties p_41383_) {
        super(p_41383_);
    }

    public ChickenOfPurityItem() {
        super(getProperties());
    }

    public static Properties getProperties()
    {
        return new Properties()
                .rarity(Rarity.UNCOMMON);

    }

    @Override
    public boolean isEdible() {
        return true;
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .saturationMod(7.2f)
                .nutrition(6)
                .effect(new MobEffectInstance(ModMobEffects.PURITY.get(), TickUnits.convertMinutesToTicks(15), 0), 1.0F)
                .build();
        return foodProperties;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
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
