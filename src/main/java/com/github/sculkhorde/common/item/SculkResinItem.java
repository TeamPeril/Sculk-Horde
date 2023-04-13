package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class SculkResinItem extends Item implements IForgeItem {

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public SculkResinItem(Properties properties) {
        super(properties);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public SculkResinItem() {
        this(getProperties());
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Properties();
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.literal("tooltip.sculkhorde.sculk_resin")); //Text that displays if not holding shift

    }
}
