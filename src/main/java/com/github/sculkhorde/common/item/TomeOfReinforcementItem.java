package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class TomeOfReinforcementItem extends TomeItem implements IForgeItem {

	/**
	 * The Constructor that takes in properties
	 * @param properties The Properties
	 */
	public TomeOfReinforcementItem(Properties properties) {
		super(properties);

	}

	/**
	 * A simpler constructor that does not take in properties.<br>
	 * I made this so that registering items in ItemRegistry.java can look cleaner
	 */
	public TomeOfReinforcementItem() {this(getProperties());}


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
	public int getCooldownTicks() {
		return TickUnits.convertSecondsToTicks(30);
	}

	@Override
	public void executePower(Player player)
	{
		summonReinforcement((ServerLevel) player.level(), player.blockPosition());
	}

	protected static void summonReinforcement(ServerLevel level, BlockPos blockPos)
	{
		level.sendParticles(ParticleTypes.SCULK_SOUL, blockPos.getX() + 0.5D, blockPos.getY() + 1.15D, blockPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
		level.playSound((Player)null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
		//Choose spawn positions
		ArrayList<BlockPos> possibleSpawnPositions = getSpawnPositionsInCube(level, blockPos, 5, 10);

		BlockPos[] finalizedSpawnPositions = new BlockPos[10];

		//Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
		for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
		{
			finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
		}

		//If the array is empty, just spawn above block
		if (possibleSpawnPositions.isEmpty()) {
			finalizedSpawnPositions[0] = blockPos.above();
		}

		//Give gravemind context to our request to make more informed situations
		ReinforcementRequest request = new ReinforcementRequest((ServerLevel) level, finalizedSpawnPositions);
		request.sender = ReinforcementRequest.senderType.Summoner;
		request.is_aggressor_nearby = true;

		//Request reinforcement from entity factory (this request gets approved or denied by gravemind)
		SculkHorde.entityFactory.createReinforcementRequestFromSummoner(level, blockPos, false, request);
	}


	/**
	 * Returns true if the block below is a sculk block,
	 * and if the two blocks above it are free.
	 * @param worldIn The World
	 * @param pos The Position to spawn the entity
	 * @return True/False
	 */
	protected static boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
	{
		boolean isBlockBelowSolid = BlockAlgorithms.isSolid(worldIn, pos.below());
		boolean isBaseBlockReplaceable = worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER);
		boolean isBlockAboveReplaceable = worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

		return isBlockBelowSolid && isBaseBlockReplaceable && isBlockAboveReplaceable;

	}

	protected static ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions)
	{
		ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length);
		ArrayList<BlockPos> finalList = new ArrayList<>();
		Random rng = new Random();
		for(int count = 0; count < amountOfPositions && listOfPossibleSpawns.size() > 0; count++)
		{
			int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
			//Get random position between 0 and size of list
			finalList.add(listOfPossibleSpawns.get(randomIndex));
			listOfPossibleSpawns.remove(randomIndex);
		}
		return finalList;
	}

	/**
	 * Finds the location of the nearest block given a BlockPos predicate.
	 * @param worldIn The world
	 * @param origin The origin of the search location
	 * @param pDistance The search distance
	 * @return The position of the block
	 */
	protected static ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, double pDistance)
	{
		ArrayList<BlockPos> list = new ArrayList<>();

		//Search area for block
		for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
		{
			for(int j = 0; (double)j < pDistance; ++j)
			{
				for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
				{
					for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
					{
						//blockpos$mutable.setWithOffset(origin, k, i - 1, l);
						BlockPos temp = new BlockPos(origin.getX() + k, origin.getY() + i-1, origin.getZ() + l);

						//If the block is close enough and is the right blockstate
						if (origin.closerThan(temp, pDistance)
								&& isValidSpawnPosition(worldIn, temp))
						{
							list.add(temp); //add position
						}
					}
				}
			}
		}
		//else return empty
		return list;
	}
}
