package com.github.sculkhoard.common.item;

import com.github.sculkhoard.common.entity.EntityAlgorithms;
import com.github.sculkhoard.common.entity.SculkMiteEntity;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemUseContext;
import net.minecraft.item.Rarity;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.ActionResultType;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class DevWand extends Item implements IForgeItem {
	/* NOTE:
	 * Learned from https://www.youtube.com/watch?v=0vLbG-KrQy4 "Advanced Items - Minecraft Forge 1.16.4 Modding Tutorial"
	 * and learned from https://www.youtube.com/watch?v=itVLuEcJRPQ "Add CUSTOM TOOLS to Minecraft 1.16.5 with Forge"
	 * Also this is just an example item, I don't intend for this to be used
	*/
	 
	public DevWand(Properties properties) {
		super(properties);
		
	}

	//This changes the text you see when hovering over an item
	@Override
	@OnlyIn(Dist.CLIENT)
	public void appendHoverText(ItemStack stack, World worldIn, List<ITextComponent> tooltip, ITooltipFlag flagIn) {
		
		super.appendHoverText(stack, worldIn, tooltip, flagIn); //Not sure why we need this
		
		//If User presses left shift, else
		if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))	{
			tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.dev_wand.shift")); //Text that displays if holding shift
		} else {
			tooltip.add(new TranslationTextComponent("tooltip.sculkhoard.dev_wand")); //Text that displays if not holding shift
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

		//If Player is holding shift, just output sculk mass of world
		if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
		{
			System.out.println("Sculk Accumulated Mass: " + SculkHoard.entityFactory.getSculkAccumulatedMass());
		}
		//If item is not on cool down
		else if(!playerIn.getCooldowns().isOnCooldown(this))
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
				SculkMiteEntity entity = new SculkMiteEntity(worldIn);
				//Set Mob's Position
				entity.setPos(targetX, targetY, targetZ);
				//Spawn instance in world
				worldIn.addFreshEntity(entity);
				//Set Wand on cool down
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
			}
			return ActionResult.pass(itemstack);
		}
		return ActionResult.fail(itemstack);
	}

	/**
	 * This is called when the item is used, before the block is activated.
	 * When Player Shift Right Clicks with it, spawns sculk zombie
	 * @return Return PASS to cause item cooldown. Anything else for no cool down.
	 */
	public ActionResultType onItemUseFirst(ItemStack stack, ItemUseContext context)
	{
		/*
		PlayerEntity playerIn = context.getPlayer();
		World worldIn = context.getLevel();

		//If Player is holding shift, just output sculk mass of world
		if(InputMappings.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
		{
		}
		//If item is not on cool down
		else if(!playerIn.getCooldowns().isOnCooldown(this))
		{
			//Ray trace to see what block the player is looking at
			BlockPos targetPos = EntityAlgorithms.playerTargetBlockPos(playerIn, false);

			double targetX;
			double targetY;
			double targetZ;

			if(targetPos == null) //If Player NOT Looking at Block
			{

			}
			else //If player Looking at Block
			{
				System.out.println("Sculk Accumulated Mass: " + SculkHoard.entityFactory.getSculkAccumulatedMass());
				targetX = (int) targetPos.getX() + 0.5; //We add 0.5 so that the mob can be in the middle of a block
				targetY = (int) targetPos.getY() + 1;
				targetZ = (int) targetPos.getZ() + 0.5; //We add 0.5 so that the mob can be in the middle of a block

				//Give Player Effect
				playerIn.addEffect(new EffectInstance(Effects.ABSORPTION, 200, 5));
				//Create Mob Instance
				SculkMiteEntity entity = new SculkMiteEntity(worldIn);
				//Set Mob's Position
				entity.setPos(targetX, targetY, targetZ);
				//Spawn instance in world
				worldIn.addFreshEntity(entity);
				//Set Wand on cool down
				playerIn.getCooldowns().addCooldown(this, 10); //Cool down for second (20 ticks per second)
			}


			return ActionResultType.PASS;
		}
		*/
		return ActionResultType.FAIL;

	}


}
