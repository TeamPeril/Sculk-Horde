package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.advancement.GravemindEvolveImmatureTrigger;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.AdvancementUtil;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.component.DataComponents;
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

public class WardenBeefItem extends Item {

    public WardenBeefItem() {
        super(getPropertiesItem());
    }


    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getPropertiesItem()
    {
        return new Item.Properties()
                .rarity(Rarity.EPIC)
                .food(getPropertiesFood());

    }
    public static FoodProperties getPropertiesFood() {
        return (new FoodProperties.Builder()).nutrition(20).saturationModifier(1.2F).build();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.has(DataComponents.FOOD)) {
            if (true || player.canEat(itemstack.get(DataComponents.FOOD).canAlwaysEat())) {
                player.startUsingItem(hand);
                return InteractionResultHolder.consume(itemstack);
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        } else {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }
    }

    @Override
    public ItemStack finishUsingItem(ItemStack itemStack, Level level, LivingEntity entity) {
        if(!level.isClientSide())
        {
            SculkHorde.setDebugMode(!SculkHorde.isDebugMode());
            EntityAlgorithms.announceToAllPlayers((ServerLevel) level, Component.literal("Debug Mode is now: " + SculkHorde.isDebugMode()));
        }
        return entity.eat(level, itemStack);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.sculkhorde.warden_beef"));

    }
}
