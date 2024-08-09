package com.github.sculkhorde.common.item;

import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.PlayerProfileHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.extensions.IForgeItem;

public abstract class TomeItem extends Item implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeItem() {this(getProperties());}

	/**
	 * Determines the properties of an item.<br>
	 * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
	 * @return The Properties of the item
	 */
	public static Properties getProperties()
	{
		return new Properties()
				.rarity(Rarity.EPIC);

	}


	@Override
	public Rarity getRarity(ItemStack itemStack) {
		return Rarity.EPIC;
	}

	public int getCooldownTicks()
	{
		return 0;
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

		if(worldIn.isClientSide())
		{
			// Do something in the future
			return InteractionResultHolder.fail(itemstack);
		}
		if(!PlayerProfileHandler.isPlayerVessel(playerIn))
		{
			// Do something in the future
			return InteractionResultHolder.fail(itemstack);
		}

		//If item is not on cool down
		if(!playerIn.getCooldowns().isOnCooldown(this) && targetPos != null)
		{

			playerIn.getCooldowns().addCooldown(this, getCooldownTicks()); //

			executePower(playerIn);

			return InteractionResultHolder.pass(itemstack);
		}
		return InteractionResultHolder.fail(itemstack);
	}

	public void executePower(Player player)
	{

	}
}
