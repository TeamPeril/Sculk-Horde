package com.github.sculkhorde.common.block.InfestationEntries;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

public class BlockInfestationTable{

    private List<IBlockInfestationEntry> entries;

    public BlockInfestationTable()
    {
        entries = new ArrayList<>();
    }


    /**
     * Adds a new entry to the table.
     * @param normalVariant The normal variant of the block.
     * @param infectedVariant The infected variant of the block.
     */
    public void addEntry(Block normalVariant, BlockState infectedVariant)
    {
        entries.add(new BlockInfestationTableEntry(normalVariant, infectedVariant));
    }

    public void addEntry(TagKey<Block> normalTag, ITagInfestedBlock infectedVariant)
    {
        entries.add(new BlockTagInfestationTableEntry(normalTag, infectedVariant));
    }

    public void addEntry(TagKey<Block> toolRequired, Tier tier, ITagInfestedBlock infectedVariant)
    {
        entries.add(new ToolTaglInfestationTableEntry(toolRequired, tier, infectedVariant));
    }

    public BlockState getInfestedVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isNormalVariant(blockState))
            {
                return entry.getInfectedVariant(level, blockPos, blockState);
            }
        }
        return null;
    }

    public BlockState getNormalVariant(Level level, BlockPos blockPos, BlockState blockState)
    {
        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isInfectedVariant(blockState))
            {
                return entry.getNormalVariant(level, blockPos, blockState);
            }
        }
        return null;
    }

    /**
     * Checks if a block is a normal variant.
     * @param blockState The block to check.
     * @return True if the block is a normal variant.
     */
    public boolean isInfectable(BlockState blockState)
    {
        if(blockState.is(ModBlocks.BlockTags.NOT_INFESTABLE))
        {
            return false;
        }
        else if( blockState.is(ModBlocks.BlockTags.INFESTED_BLOCK))
        {
            return false;
        }

        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isNormalVariant(blockState))
            {
                return true;
            }
        }
        return false;
    }

    /**
     * Checks if a block is an infected variant.
     * @param blockState The block to check.
     * @return True if the block is an infected variant.
     */
    public boolean isCurable(BlockState blockState)
    {
        for(IBlockInfestationEntry entry : entries)
        {
            if(entry.isInfectedVariant(blockState))
            {
                return true;
            }
        }
        return false;
    }

    private void removeNearbyVein(ServerLevel world, BlockPos position)
    {
        // Update each adjacent block if it is a sculk vein
        // This is to prevent vein from staying on blocks that it does not belong on.
        List<BlockPos> adjacentBlockPos = BlockAlgorithms.getAdjacentNeighbors(position);
        for(BlockPos neighbors : adjacentBlockPos)
        {
            BlockState blockState = world.getBlockState(neighbors);
            if(blockState.getBlock() == ModBlocks.TENDRILS.get())
            {
                if(!blockState.getBlock().canSurvive(blockState, world, neighbors))
                    world.destroyBlock(neighbors, false);

            }
        }
    }

    private void placeSculkFlora(ServerLevel world, BlockPos position)
    {
        // Given a 25% chance, place down sculk flora on block
        if (world.random.nextInt(4) <= 0)
        {
            BlockAlgorithms.tryPlaceSculkFlora(position.above(), world);
        }
    }

    /**
     * This Method serves the purpose of converting a victim block into a dormant variant.
     * This is only a temporary method until I fully design the new infestation system.
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean infectBlock(ServerLevel world, BlockPos targetPos)
    {
        if(world == null || !isInfectable(world.getBlockState(targetPos)))
        {
            return false;
        }

        BlockState oldBlock = world.getBlockState(targetPos);
        BlockState newBlock = getInfestedVariant(world, targetPos, world.getBlockState(targetPos));

        if(newBlock == null)
        {
            return false;
        }


        world.setBlockAndUpdate(targetPos, newBlock);
        world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        world.playSound((Player)null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        if(newBlock.getBlock() instanceof ITagInfestedBlock)
        {
            ((ITagInfestedBlock) newBlock.getBlock()).getTagInfestedBlockEntity(world, targetPos).setNormalBlockState(oldBlock);
        }


        removeNearbyVein(world, targetPos);

        placeSculkFlora(world, targetPos);

        // If the block we are placing is an Infested Log, place down sculk flora around it.
        if(newBlock.getBlock() == ModBlocks.INFESTED_LOG.get())
        {
            //BlockAlgorithms.placeFloraAroundLog(world, targetPos);
        }

        // Chance to place a sculk node above the block
        SculkNodeBlock.tryPlaceSculkNode(world, targetPos, true);

        // Chance to place a vein patch above the block
        //BlockAlgorithms.placePatchesOfVeinAbove(world, targetPos);

        // Chance to place a sculk bee hive above the block
        BlockAlgorithms.tryPlaceSculkBeeHive(world, targetPos.above());

        return true;
    }

    /**
     * Converts a an active or dormant variant into a victim variant
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean cureBlock(ServerLevel world, BlockPos targetPos)
    {
        if(world == null || !isCurable(world.getBlockState(targetPos)))
        {
            return false;
        }

        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState curedVersion = getNormalVariant(world, targetPos, targetBlock);

        if(curedVersion == null)
        {
            //SculkHorde.LOGGER.error("Error Deinfecting Block: " + targetBlock.getBlock().toString());
            return false;
        }

        BlockState victimVariant = curedVersion;

        if(victimVariant != null)
        {
            world.setBlockAndUpdate(targetPos, victimVariant);
            return true;
        }
        return false;
    }
}
