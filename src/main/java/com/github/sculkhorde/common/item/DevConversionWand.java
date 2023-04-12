package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

import net.minecraft.world.item.Item.Properties;

public class DevConversionWand extends Item implements IForgeItem {
	/* NOTE:
	 * Learned from https://www.youtube.com/watch?v=0vLbG-KrQy4 "Advanced Items - Minecraft Forge 1.16.4 Modding Tutorial"
	 * and learned from https://www.youtube.com/watch?v=itVLuEcJRPQ "Add CUSTOM TOOLS to Minecraft 1.16.5 with Forge"
	 * Also this is just an example item, I don't intend for this to be used
	*/


	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public DevConversionWand(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public DevConversionWand() {this(getProperties());}


	/**
	 * Determines the properties of an item.<br>
	 * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
	 * @return The Properties of the item
	 */
	public static Properties getProperties()
	{
		return new Item.Properties()
				.tab(SculkHorde.SCULK_GROUP)
				.durability(5)
				.rarity(Rarity.EPIC)
				.fireResistant();

	}

	//This changes the text you see when hovering over an item
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
		tooltip.add(new TranslatableComponent("tooltip.sculkhorde.dev_conversion_wand")); //Text that displays if not holding shift

	}

	@Override
	public Rarity getRarity(ItemStack itemStack) {
		return Rarity.EPIC;
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);

		//If item is not on cool down
		if(!playerIn.getCooldowns().isOnCooldown(this))
		{

			//Ray trace to see what block the player is looking at
			BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

			double targetX;
			double targetY;
			double targetZ;

			if(targetPos != null) //If player Looking at Block
			{
				if(!worldIn.isClientSide())
				{
					// Spawn a Block Traverser
					CursorInfectorEntity cursor = new CursorInfectorEntity((ServerLevel) worldIn);
					cursor.setPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
					worldIn.addFreshEntity(cursor);

				}

				//Set Wand on cool down
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
			}
			return InteractionResultHolder.pass(itemstack);
		}

		return InteractionResultHolder.fail(itemstack);
	}
}
