package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

public class DevRaidWand extends Item implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public DevRaidWand(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public DevRaidWand() {this(getProperties());}

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

	//This changes the text you see when hovering over an item
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this

		//If User presses left shift, else
		if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))	{
			tooltip.add(Component.translatable("tooltip.sculkhorde.dev_raid_wand.shift")); //Text that displays if holding shift
		} else {
			tooltip.add(Component.translatable("tooltip.sculkhorde.dev_raid_wand")); //Text that displays if not holding shift
		}
	}

	@Override
	public Rarity getRarity(ItemStack itemStack) {
		return Rarity.EPIC;
	}


	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);
		BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

		//If item is not on cool down
		if(!playerIn.getCooldowns().isOnCooldown(this) && !worldIn.isClientSide() && targetPos != null)
		{
			RaidHandler.raidData.startRaidArtificially((ServerLevel) worldIn, targetPos);
			playerIn.getCooldowns().addCooldown(this, 5); //Cool down for second (20 ticks per second)
			return InteractionResultHolder.pass(itemstack);
		}
		return InteractionResultHolder.fail(itemstack);
	}
}
