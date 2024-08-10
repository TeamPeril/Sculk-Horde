package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class TomeOfSporeItem extends TomeItem implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeOfSporeItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeOfSporeItem() {this(getProperties());}



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
		return TickUnits.convertSecondsToTicks(20);
	}


	@Override
	public void executePower(Player player)
	{
		scraficeHealthAndSpawnSporeSpewer(player);
	}

	protected static void scraficeHealthAndSpawnSporeSpewer(Player player)
	{
		ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);
		if(profile.isVessel())
		{
			if(profile.isActiveVessel())
			{
				player.hurt(player.damageSources().genericKill(), player.getMaxHealth() - 1);
				player.addEffect(new MobEffectInstance(MobEffects.HUNGER, TickUnits.convertSecondsToTicks(10), 2));
				SculkSporeSpewerEntity entity = new SculkSporeSpewerEntity(player.level());
				entity.setPos(player.position());
				player.level().addFreshEntity(entity);
			}
		}
	}
}
