package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.List;

public class TomeOfSacrificeItem extends TomeItem implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeOfSacrificeItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeOfSacrificeItem() {this(getProperties());}



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
		if(PlayerProfileHandler.isPlayerVessel(player))
		{
			ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);
			profile.setActiveVessel(false);
			profile.setVessel(false);
			explode(player);
			spawnInfectors(player);
			SculkNodeBlock.tryPlaceSculkNode((ServerLevel) player.level(), player.blockPosition(), true);
			spawnSculkEnderman(player);
		}
	}

	protected static void explode(Entity entity)
	{
		entity.level().explode(entity, entity.getX(), entity.getY(), entity.getZ(), 4.0F, Level.ExplosionInteraction.NONE);
		entity.kill();
	}

	protected void spawnInfectors(Entity entity)
	{
		entity.level().getServer().tell(new net.minecraft.server.TickTask(entity.level().getServer().getTickCount() + 1, () -> {
			int numToSpawn = 30;
			int spawnRange = 5;
			for (int i = 0; i < numToSpawn; i++) {

				double x = entity.getX() + (entity.level().getRandom().nextDouble() * spawnRange) - spawnRange / 2;
				double z = entity.getZ() + (entity.level().getRandom().nextDouble() * spawnRange) - spawnRange / 2;
				double y = entity.getY() + (entity.level().getRandom().nextDouble() * spawnRange / 2) - spawnRange / 4;
				CursorSurfaceInfectorEntity infector = new CursorSurfaceInfectorEntity(ModEntities.CURSOR_SURFACE_INFECTOR.get(), entity.level());
				infector.setPos(x, y, z);
				infector.setTickIntervalMilliseconds(3);
				infector.setMaxTransformations(100);
				infector.setMaxRange(100);
				infector.setCanBeManuallyTicked(false);
				entity.level().addFreshEntity(infector);
			}
		}));
	}

	protected void spawnSculkEnderman(Entity e)
	{
		SculkEndermanEntity entity = new SculkEndermanEntity(e.level(), e.blockPosition());
		entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, TickUnits.convertHoursToTicks(1), 1));
		entity.addEffect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, TickUnits.convertHoursToTicks(1), 1));
		entity.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, TickUnits.convertHoursToTicks(1), 0));
		e.level().addFreshEntity(entity);
	}
}
