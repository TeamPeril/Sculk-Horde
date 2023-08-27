package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.util.ForgeEventSubscriber;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class InfestationPurifierItem extends Item implements IForgeItem {

    private InfestationPurifierEntity purifier; // The cursor entity

    /**
     * The Constructor that takes in properties
     * @param properties The Properties
     */
    public InfestationPurifierItem(Properties properties) {
        super(properties);
    }

    /**
     * A simpler constructor that does not take in properties.<br>
     * I made this so that registering items in ItemRegistry.java can look cleaner
     */
    public InfestationPurifierItem() {
        this(getProperties());
    }

    /**
     * Determines the properties of an item.<br>
     * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
     * @return The Properties of the item
     */
    public static Properties getProperties()
    {
        return new Item.Properties()
                .rarity(Rarity.EPIC)
                .stacksTo(8);
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
        tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_purifier")); //Text that displays if not holding shift

    }

    @Override
    public Rarity getRarity(ItemStack itemStack) {
        return Rarity.EPIC;
    }

    /**
     * This function occurs when the item is right-clicked on a block.
     * This will then add every block within a sphere of a specified radius if it isnt air
     * and then add it to the convversion queue to be processed in {@link ForgeEventSubscriber#WorldTickEvent}
     * @param worldIn The world
     * @param playerIn The player entity who used it
     * @param handIn The hand they used it in
     * @return
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
    {
        //Get the item the player is holding
        ItemStack itemstack = playerIn.getItemInHand(handIn);

        //If Item not on cool down
        if(playerIn.getCooldowns().isOnCooldown(this) || worldIn.isClientSide())
        {
            return InteractionResultHolder.fail(itemstack);
        }

        //Spawn the Purifier Cursor
        purifier = new InfestationPurifierEntity(worldIn);
        purifier.setPos(playerIn.position().x(), playerIn.position().y(), playerIn.position().z());
        worldIn.addFreshEntity(purifier);

        // Consume Item
        itemstack.shrink(1);
        return InteractionResultHolder.sidedSuccess(itemstack, worldIn.isClientSide());

    }
}
