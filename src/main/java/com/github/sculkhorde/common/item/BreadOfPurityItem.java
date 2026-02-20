package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.common.extensions.IForgeItem;
import org.jetbrains.annotations.Nullable;

public class BreadOfPurityItem extends Item implements IForgeItem {
    public BreadOfPurityItem(Properties p_41383_) {
        super(p_41383_);
    }

    public BreadOfPurityItem() {
        super(getProperties());
    }

    public static Properties getProperties()
    {
        return new Item.Properties()
                .rarity(Rarity.UNCOMMON);

    }

    @Override
    public boolean isEdible() {
        return true;
    }

    @Override
    public @Nullable FoodProperties getFoodProperties(ItemStack stack, @Nullable LivingEntity entity) {
        FoodProperties foodProperties = new FoodProperties.Builder()
                .saturationMod(0.6f)
                .effect(new MobEffectInstance(ModMobEffects.PURITY.get(), TickUnits.convertMinutesToTicks(15), 0), 1.0F)
                .build();
        return foodProperties;
    }


}
