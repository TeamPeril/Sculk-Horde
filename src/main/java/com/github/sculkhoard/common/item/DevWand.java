package com.github.sculkhoard.common.item;

import java.util.List;

import com.github.sculkhoard.common.entity.SculkZombieEntity;
import org.lwjgl.glfw.GLFW;

import net.minecraft.client.Minecraft;
import net.minecraft.client.util.ITooltipFlag;
import net.minecraft.client.util.InputMappings;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.EffectInstance;
import net.minecraft.potion.Effects;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.text.ITextComponent;
import net.minecraft.util.text.TranslationTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class DevWand extends Item{
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
	
	//This is what occurs when you right click an item with it in your hand
	@Override
	public ActionResult<ItemStack> use(World worldIn, PlayerEntity playerIn, Hand handIn) {		
		playerIn.addEffect(new EffectInstance(Effects.ABSORPTION, 200, 5)); //Give Player Effect

		SculkZombieEntity entity = new SculkZombieEntity(worldIn); //Create Zombie Instance
		
		entity.setPos(playerIn.getX(), playerIn.getY(), playerIn.getZ()); //Set its position to player
		
		worldIn.addFreshEntity(entity);//I think this spawns the actual instance into the world
		
		playerIn.getCooldowns().addCooldown(this, 20); //Cool down for second (20 ticks per second)
		
	    return ActionResult.success(playerIn.getItemInHand(handIn)); //Then we have to return this for some reason
		
	}
}
