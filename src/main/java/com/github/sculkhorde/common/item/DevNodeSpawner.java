package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class DevNodeSpawner extends Item implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public DevNodeSpawner(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public DevNodeSpawner() {this(getProperties());}

	/**
	 * Determines the properties of an item.<br>
	 * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
	 * @return The Properties of the item
	 */
	public static Properties getProperties()
	{
		return new Properties()
				.rarity(Rarity.EPIC)
				.fireResistant();

	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);

		//If item is not on cool down
		if(playerIn.getCooldowns().isOnCooldown(this) || worldIn.isClientSide())
		{
			return InteractionResultHolder.fail(itemstack);
		}


		SculkHorde.gravemind.placeSculkNode((ServerLevel) worldIn, playerIn.blockPosition(), false);
		return InteractionResultHolder.pass(itemstack);
	}

	//This changes the text you see when hovering over an item
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
		tooltip.add(Component.literal("tooltip.sculkhorde.dev_node_spawner")); //Text that displays if not holding shift

	}
}
