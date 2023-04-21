package com.github.sculkhorde.common.block.BlockInfestation;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.util.ForgeEventSubscriber;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.tags.BlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;

public class InfestationConversionHandler
{
    /**
     * This is a queue used to convert blocks in a manner that prevents lag for nodes.
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent}
     */
    public ArrayList<BlockPos> convertToVictimNodeQueue;
    public ArrayList<BlockPos> convertToInfectedNodeQueue;
    public final int conversionAmountPerInterval = 1;

    public ArrayList<ConversionRequest> conversionQueue;

    public InfestationTable infestationTable;

    /**
     * Default Constructor
     */
    public InfestationConversionHandler()
    {
        convertToVictimNodeQueue = new ArrayList<>();
        convertToInfectedNodeQueue = new ArrayList<>();
        conversionQueue = new ArrayList<>();
        infestationTable = new InfestationTable();
    }

    /* Accessor Methods */

    /* Modifier Methods */

    /* Boolean Methods */

    /** Event Methods **/

    /**
     * This Method serves the purpose of converting a victim block into a dormant variant.
     * This is only a temporary method until I fully design the new infestation system.
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean infectBlock(ServerLevel world, BlockPos targetPos)
    {

        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState newBlock = null;

        newBlock = infestationTable.getInfestedVariant(targetBlock);

        // Special Condition for Infested Logs because I do not care right now
        if(targetBlock.is(BlockTags.LOGS))
        {
            newBlock = BlockRegistry.INFESTED_LOG.get().defaultBlockState();
        }


        if(newBlock == null) { return false; }

        world.setBlockAndUpdate(targetPos, newBlock);
        world.sendParticles(ParticleTypes.SCULK_CHARGE_POP, targetPos.getX() + 0.5D, targetPos.getY() + 1.15D, targetPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        world.playSound((Player)null, targetPos, SoundEvents.SCULK_BLOCK_SPREAD, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        // Update each adjacent block if it is a sculk vein
        // This is to prevent vein from staying on blocks that it does not belong on.
        List<BlockPos> adjacentBlockPos = BlockAlgorithms.getAdjacentNeighbors(targetPos);
        for(BlockPos pos : adjacentBlockPos)
        {
            BlockState blockState = world.getBlockState(pos);
            if(blockState.getBlock() == BlockRegistry.TENDRILS.get())
            {
                if(!blockState.getBlock().canSurvive(blockState, world, pos))
                    world.destroyBlock(pos, false);

            }
        }

        // Given a 25% chance, place down sculk flora on block
        if (world.random.nextInt(4) <= 0)
            BlockAlgorithms.placeSculkFlora(targetPos.above(), world);

        // If the block we are placing is an Infested Log, place down sculk flora around it.
        if(newBlock.getBlock() == BlockRegistry.INFESTED_LOG.get())
            BlockAlgorithms.placeFloraAroundLog(world, targetPos);

        // Chance to place a sculk node above the block
        SculkHorde.gravemind.placeSculkNode(world, targetPos.above(), true);

        // Chance to place a vein patch above the block
        BlockAlgorithms.placePatchesOfVeinAbove(world, targetPos);

        // Chance to place a sculk bee hive above the block
        BlockAlgorithms.placeSculkBeeHive(world, targetPos.above());

        return true;
    }

    /**
     * Converts a an active or dormant variant into a victim variant
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean deinfectBlock(ServerLevel world, BlockPos targetPos)
    {
        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState victimVariant = infestationTable.getNormalVariant(targetBlock).defaultBlockState();

        // Special Condition for Infested Logs because I do not care right now
        if(targetBlock.is(BlockRegistry.INFESTED_LOG.get()))
        {
            victimVariant = Blocks.AIR.defaultBlockState();
        }

        if(victimVariant != null)
        {
            world.setBlockAndUpdate(targetPos, victimVariant);
            return true;
        }
        return false;
    }

    /**
     * Only process a specific amount every time this is called. <br>
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent}
     * @param world The world
     */
    public void processDeInfectionQueue(ServerLevel world)
    {
        if(!world.isClientSide())
        {
            for(int i = 0; i < conversionAmountPerInterval && i < convertToVictimNodeQueue.size(); i++)
            {
                BlockAlgorithms.replaceSculkFlora(world, convertToVictimNodeQueue.get(i)); //Remove any flora
                deinfectBlock(world, convertToVictimNodeQueue.get(i)); //convert
                convertToVictimNodeQueue.remove(i);
                i--;
            }
        }
    }

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    public class ConversionRequest
    {
        //The target position
        private BlockPos position;
        //If true, we are convert to an infested variant,
        private boolean convertToInfested;
        //If true, we are convert to a normal variant,
        private boolean convertToNormal;

        public ConversionRequest(BlockPos positionIn)
        {
            position = positionIn;
        }

        public BlockPos getPosition()
        {
            return  position;
        }

    }

    /**
     * A table that holds all the variants of a block that can be infected.
     */
    public class InfestationTable
    {
        private List<InfestationTableEntry> entries;

        public InfestationTable()
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
            entries.add(new InfestationTableEntry(normalVariant, infectedVariant));
        }

        /**
         * Gets the infected variant of a block.
         * @param normalVariant The normal variant of the block.
         * @return The infected variant of the block.
         */
        public BlockState getInfestedVariant(BlockState normalVariant)
        {
            for(InfestationTableEntry entry : entries)
            {
                if(entry.getNormalVariant() == normalVariant.getBlock())
                {
                    return entry.getInfectedVariant();
                }
            }
            return null;
        }

        /**
         * Gets the normal variant of a block.
         * @param infectedVariant The infected variant of the block.
         * @return The normal variant of the block.
         */
        public Block getNormalVariant(BlockState infectedVariant)
        {
            for(InfestationTableEntry entry : entries)
            {
                if(entry.getInfectedVariant() == infectedVariant)
                {
                    return entry.getNormalVariant();
                }
            }
            return null;
        }

        /**
         * Checks if a block is a normal variant.
         * @param blockState The block to check.
         * @return True if the block is a normal variant.
         */
        public boolean isNormalVariant(BlockState blockState)
        {
            // Special Condition for Infested Logs because I do not care right now
            if(blockState.is(BlockTags.LOGS)) { return true;}

            for(InfestationTableEntry entry : entries)
            {
                if(entry.getNormalVariant() == blockState.getBlock())
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
        public boolean isInfectedVariant(BlockState blockState)
        {
            // Special Condition for Infested Logs because I do not care right now
            if(blockState.is(BlockRegistry.INFESTED_LOG.get())) { return true;}

            for(InfestationTableEntry entry : entries)
            {
                if(entry.getInfectedVariant() == blockState)
                {
                    return true;
                }
            }
            return false;
        }
    }

    /**
     *  A single entry in the infestation table.
     */
    public class InfestationTableEntry
    {
        private Block normalVariant;
        private BlockState infectedVariant;

        // Default constructor
        public InfestationTableEntry(Block normalVariantIn, BlockState infectedVariantIn)
        {
            normalVariant = normalVariantIn;
            infectedVariant = infectedVariantIn;
        }

        public Block getNormalVariant()
        {
            return normalVariant;
        }

        public BlockState getInfectedVariant()
        {
            return infectedVariant;
        }
    }
}
