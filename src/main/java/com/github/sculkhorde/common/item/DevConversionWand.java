package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.infection.CursorLongRangeEntity;
import com.github.sculkhorde.common.entity.infection.CursorShortRangeEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

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
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
		tooltip.add(new TranslationTextComponent("tooltip.sculkhorde.dev_conversion_wand")); //Text that displays if not holding shift

	}

	@Override
	public Rarity getRarity(ItemStack itemStack) {
		return Rarity.EPIC;
	}

	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn)
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
					CursorShortRangeEntity cursor = new CursorShortRangeEntity((ServerWorld) worldIn);
					cursor.setPos(targetPos.getX(), targetPos.getY(), targetPos.getZ());
					worldIn.addFreshEntity(cursor);

				}

				//Set Wand on cool down
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
			}
			return ActionResult.pass(itemstack);
		}

		return ActionResult.fail(itemstack);
	}
}
