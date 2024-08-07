package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkSpineSpikeAttackEntity;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.ArrayList;
import java.util.List;

public class TomeOfSpinesItem extends TomeItem implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeOfSpinesItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeOfSpinesItem() {this(getProperties());}



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
		return TickUnits.convertSecondsToTicks(15);
	}


	@Override
	public void executePower(Player player)
	{

		for(int i = 4; i < 20; i++)
		{
			spawnSpikesOnCircumference(player, i, i * 4, (((i - 4) * 5)));
		}
	}

	protected static void spawnSpikesOnCircumference(Player player, int radius, int amount, int delayTicks)
	{
		Vec3 origin = new Vec3(player.getX(), player.getY() + player.getEyeHeight(), player.getZ());
		ArrayList<SculkSpineSpikeAttackEntity> entities = new ArrayList<SculkSpineSpikeAttackEntity>();
		ArrayList<Vec3> possibleSpawns = BlockAlgorithms.getPointsOnCircumferenceVec3(origin, radius, amount);
		for(int i = 0; i < possibleSpawns.size(); i++)
		{
			Vec3 spawnPos = possibleSpawns.get(i);
			SculkSpineSpikeAttackEntity entity = new SculkSpineSpikeAttackEntity(player, player.getX(), player.getY(), player.getZ(), delayTicks);

			double spawnHeight = getSpawnHeight(player, BlockPos.containing(spawnPos));
			Vec3 possibleSpawnPosition = new Vec3(spawnPos.x(), spawnHeight, spawnPos.z());
			// If the block below our spawn is solid, spawn the entity
			if(!player.level().getBlockState(BlockPos.containing(possibleSpawnPosition).below()).canBeReplaced())
			{
				entity.setPos(possibleSpawnPosition.x(), possibleSpawnPosition.y(), possibleSpawnPosition.z());
				entities.add(entity);
				entity.setOwner(player);
			}
		}

		for (SculkSpineSpikeAttackEntity entity : entities) {
			player.level().addFreshEntity(entity);
		}
	}

	protected static int getSpawnHeight(Player player, BlockPos startPos)
	{
		BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos(startPos.getX(), startPos.getY(), startPos.getZ());
		int iterationsElapsed = 0;
		int iterationsMax = 7;
		while(iterationsElapsed < iterationsMax)
		{

			iterationsElapsed++;

			if(!player.level().getBlockState(mutablePos).canBeReplaced())
			{
				continue;
			}
			mutablePos.move(0, -1, 0);

		}
		return mutablePos.getY() + 1;
	}
}
