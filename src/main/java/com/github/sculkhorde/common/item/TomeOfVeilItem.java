package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class TomeOfVeilItem extends TomeItem implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeOfVeilItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeOfVeilItem() {this(getProperties());}



	//This changes the text you see when hovering over an item
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
		/*
		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this

		//If User presses left shift, else
		if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))	{
			tooltip.add(Component.translatable("tooltip.sculkhorde.dev_raid_wand.shift")); //Text that displays if holding shift
		} else {
			tooltip.add(Component.translatable("tooltip.sculkhorde.dev_raid_wand")); //Text that displays if not holding shift
		}

		 */
	}

	@Override
	public int getCooldownTicks()
	{
		return TickUnits.convertSecondsToTicks(1);
	}


	@Override
	public void executePower(Player player)
	{
		toggleVeil(player);
	}

	protected static void toggleVeil(Player player)
	{
		ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);
		if(profile.isVessel())
		{
			if(profile.isActiveVessel())
			{
				profile.setActiveVessel(false);
				player.removeEffect(ModMobEffects.SCULK_VESSEL.get());
				player.sendSystemMessage(Component.literal("Your powers are hidden. You now appear normal."));
			}
			else
			{
				profile.setActiveVessel(true);
				MobEffectInstance effectInstance = new MobEffectInstance(ModMobEffects.SCULK_VESSEL.get(), Integer.MAX_VALUE);
				player.addEffect(effectInstance);
				player.sendSystemMessage(Component.literal("Your powers are now active. You now appear as a vessel."));
			}
		}
		else
		{
			player.sendSystemMessage(Component.literal("You are not a vessel of the Gravemind. Cannot Toggle Veil."));
		}
	}
}
