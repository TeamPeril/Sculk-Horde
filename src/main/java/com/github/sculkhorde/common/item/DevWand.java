package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.mixin.structures.ForgeChunkManagerAccessor;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.google.common.base.Function;
import net.minecraft.client.Minecraft;
import net.minecraft.server.level.ServerChunkCache;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.TicketType;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.TooltipFlag;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.InteractionHand;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ForcedChunksSavedData;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import net.minecraftforge.common.world.ForgeChunkManager;
import net.minecraftforge.server.ServerLifecycleHooks;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DevWand extends Item implements IForgeItem {
	/* NOTE:
	 * Learned from https://www.youtube.com/watch?v=0vLbG-KrQy4 "Advanced Items - Minecraft Forge 1.16.4 Modding Tutorial"
	 * and learned from https://www.youtube.com/watch?v=itVLuEcJRPQ "Add CUSTOM TOOLS to Minecraft 1.16.5 with Forge"
	 * Also this is just an example item, I don't intend for this to be used
	*/


	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public DevWand(Properties properties) {
		super(properties);
		
	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public DevWand() {this(getProperties());}

	/**
	 * Determines the properties of an item.<br>
	 * I made this in order to be able to establish a item's properties from within the item class and not in the ItemRegistry.java
	 * @return The Properties of the item
	 */
	public static Properties getProperties()
	{
		return new Item.Properties()
				.rarity(Rarity.EPIC)
				.fireResistant();

	}

	@Override
	public Rarity getRarity(ItemStack itemStack) {
		return Rarity.EPIC;
	}

	public void announceToAllPlayers(Component message)
	{
		ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers().forEach((player) -> player.displayClientMessage(message, false));
	}

	@Override
	public InteractionResultHolder<ItemStack> use(Level worldIn, Player playerIn, InteractionHand handIn)
	{
		ItemStack itemstack = playerIn.getItemInHand(handIn);

		//If item is not on cool down
		if(worldIn.isClientSide())
		{
			return InteractionResultHolder.fail(itemstack);
		}

		ForcedChunksSavedData data = ((ServerLevel) worldIn).getDataStorage().get(ForcedChunksSavedData::load, "chunks");
		announceToAllPlayers(Component.literal("Entity Forced Chunks Before Wipe: " + data.getEntityForcedChunks().getTickingChunks().size()));
		data.getEntityForcedChunks().getTickingChunks().clear();
		announceToAllPlayers(Component.literal("Entity Forced Chunks After Wipe: " + data.getEntityForcedChunks().getTickingChunks().size()));

		announceToAllPlayers(Component.literal("Block Entity Forced Chunks Before Wipe: " + data.getBlockForcedChunks().getTickingChunks().size()));
		data.getBlockForcedChunks().getTickingChunks().clear();
		announceToAllPlayers(Component.literal("Block Entity Forced Chunks After Wipe: " + data.getBlockForcedChunks().getTickingChunks().size()));

		return InteractionResultHolder.pass(itemstack);
	}
}
