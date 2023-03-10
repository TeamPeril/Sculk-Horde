package com.github.sculkhorde.common.block.BlockInfestation;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.util.ForgeEventSubscriber;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
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
    public boolean infectBlock(ServerWorld world, BlockPos targetPos)
    {

        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState newBlock = null;

        newBlock = infestationTable.getInfestedVariant(targetBlock);

        if(newBlock == null) { return false; }

        world.setBlockAndUpdate(targetPos, newBlock);

        // Given a 25% chance, place down sculk flora on block
        if (world.random.nextInt(4) <= 0)
            BlockAlgorithms.placeSculkFlora(targetPos.above(), world);

        // If the block we are placing is an Infested Log, place down sculk flora around it.
        if(newBlock.getBlock() == BlockRegistry.INFESTED_LOG_DORMANT.get())
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
    public boolean deinfectBlock(ServerWorld world, BlockPos targetPos)
    {
        BlockState targetBlock = world.getBlockState(targetPos);
        BlockState victimVariant = infestationTable.getNormalVariant(targetBlock);

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
    public void processDeInfectionQueue(ServerWorld world)
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

        public void setConvertToInfested()
        {
            convertToInfested = true;
        }

        public void setConvertToNormal()
        {
            convertToNormal = true;
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
        public void addEntry(BlockState normalVariant, BlockState infectedVariant)
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
                if(entry.getNormalVariant() == normalVariant)
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
        public BlockState getNormalVariant(BlockState infectedVariant)
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
            for(InfestationTableEntry entry : entries)
            {
                if(entry.getNormalVariant() == blockState)
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
        private BlockState normalVariant;
        private BlockState infectedVariant;

        // Default constructor
        public InfestationTableEntry(BlockState normalVariantIn, BlockState infectedVariantIn)
        {
            normalVariant = normalVariantIn;
            infectedVariant = infectedVariantIn;
        }

        public BlockState getNormalVariant()
        {
            return normalVariant;
        }

        public BlockState getInfectedVariant()
        {
            return infectedVariant;
        }
    }
}
