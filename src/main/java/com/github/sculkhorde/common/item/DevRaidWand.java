package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.events.RaidEvent.RaidEvent;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
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
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;
import java.util.Optional;

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
			//RaidHandler.raidData.startRaidArtificially((ServerLevel) worldIn, targetPos);
            createRaidEvent((ServerLevel) worldIn, targetPos);
			playerIn.getCooldowns().addCooldown(this, 5); //Cool down for second (20 ticks per second)
			return InteractionResultHolder.pass(itemstack);
		}
		return InteractionResultHolder.fail(itemstack);
	}

    public void createRaidEvent(ServerLevel level, BlockPos raidLocationIn)
    {

        if(ModSavedData.getSaveData().getSculkAccumulatedMass() < ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get() + 1000)
        {
            ModSavedData.getSaveData().setSculkAccumulatedMass(ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.get() + 1000);
            SculkHorde.gravemind.calulateCurrentState();
            SculkHorde.LOGGER.info("Artificially Starting Raid. Mass is now: " + ModSavedData.getSaveData().getSculkAccumulatedMass());
            SculkHorde.LOGGER.info("Artificially Starting Raid. Gravemind is now in state: " + SculkHorde.gravemind.getEvolutionState());
        }

        RaidEvent raidEvent = new RaidEvent(level.dimension());

        removeNoRaidZoneAtBlockPos(level, raidLocationIn);
        ModSavedData.getSaveData().getAreasOfInterestEntries().clear();
        Optional<ModSavedData.AreaOfInterestEntry> possibleAreaOfInterestEntry = ModSavedData.getSaveData().addAreaOfInterestToMemory(level, raidLocationIn);
        if(possibleAreaOfInterestEntry.isPresent())
        {

            raidEvent.setAreaOfInterestEntry(possibleAreaOfInterestEntry.get());
            raidEvent.setState(RaidEvent.State.EVENT_INITIALIZATION);
            ModSavedData.getSaveData().setTicksSinceLastRaid(TickUnits.convertMinutesToTicks(ModConfig.SERVER.sculk_raid_global_cooldown_between_raids_minutes.get()));
            SculkHorde.eventSystem.addEvent(raidEvent);
        }
        else
        {
            raidEvent.setState(RaidEvent.State.FINISHED);
        }
    }

    public void removeNoRaidZoneAtBlockPos(ServerLevel level, BlockPos pos)
    {
        ModSavedData.getSaveData().getNoRaidZoneEntries().removeIf(entry -> entry.isBlockPosInRadius(level, pos));
    }
}
