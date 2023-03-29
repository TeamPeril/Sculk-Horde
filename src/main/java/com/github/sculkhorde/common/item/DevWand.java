package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.SculkHatcherEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.EntityAlgorithms;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

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
		
		//If User presses left shift, else
		if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))	{
			tooltip.add(new TranslationTextComponent("tooltip.sculkhorde.dev_wand.shift")); //Text that displays if holding shift
		} else {
			tooltip.add(new TranslationTextComponent("tooltip.sculkhorde.dev_wand")); //Text that displays if not holding shift
		}
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
		if(!playerIn.getCooldowns().isOnCooldown(this) && !worldIn.isClientSide())
		{
			//If Player is holding shift, just output sculk mass of world
			if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				playerIn.displayClientMessage(
						new StringTextComponent(
								"Sculk Accumulated Mass: " + SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() + "\n" +
								"Known Nodes: " + SculkHorde.gravemind.getGravemindMemory().getNodeEntries().size() + "\n" +
								"Known Nests: " + SculkHorde.gravemind.getGravemindMemory().getBeeNestEntries().size() + "\n" +
								"Known Hostiles Amount: " + SculkHorde.gravemind.getGravemindMemory().getHostileEntries().size() + "\n" +
								"Conversion Queue Size: " + SculkHorde.infestationConversionTable.conversionQueue.size() + "\n"
						), false);
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
				return ActionResult.pass(itemstack);
			}
			//If player clicks left-alt, set sculk mass to 10k
			else if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_ALT))
			{
				playerIn.displayClientMessage(new StringTextComponent("Adding 100 Sculk Mass"), false);
				SculkHorde.gravemind.getGravemindMemory().addSculkAccumulatedMass(100);
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
				return ActionResult.pass(itemstack);
			}
			else
			{


				//Ray trace to see what block the player is looking at
				BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

				double targetX;
				double targetY;
				double targetZ;

				if(targetPos != null) //If player Looking at Block
				{
					targetX = targetPos.getX() + 0.5; //We add 0.5 so that the mob can be in the middle of a block
					targetY = targetPos.getY() + 1;
					targetZ = targetPos.getZ() + 0.5; //We add 0.5 so that the mob can be in the middle of a block

					//Give Player Effect
					playerIn.addEffect(new EffectInstance(Effects.ABSORPTION, 200, 5));
					//Create Mob Instance
					SculkSporeSpewerEntity entity = new SculkSporeSpewerEntity(worldIn);
					//Set Mob's Position
					entity.setPos(targetX, targetY, targetZ);
					//Spawn instance in world
					worldIn.addFreshEntity(entity);
					//Set Wand on cool down
					playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
				}

				return ActionResult.pass(itemstack);
			}
		}
		return ActionResult.fail(itemstack);
	}
}
