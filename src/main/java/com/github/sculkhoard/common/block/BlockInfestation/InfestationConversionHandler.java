package com.github.sculkhoard.common.block.BlockInfestation;

import com.github.sculkhoard.util.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.util.ForgeEventSubscriber;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class InfestationConversionHandler
{
    private ArrayList<SpreadingBlock> entries;

    /**
     * This is a queue used to convert blocks in a manner that prevents lag for nodes.
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent}
     */
    public ArrayList<BlockPos> convertToVictimNodeQueue;
    public ArrayList<BlockPos> convertToInfectedNodeQueue;
    public final int conversionAmountPerInterval = 1;

    public ArrayList<ConversionRequest> conversionQueue;



    /**
     * Default Constructor
     */
    public InfestationConversionHandler()
    {
        entries = new ArrayList<>();
        convertToVictimNodeQueue = new ArrayList<>();
        convertToInfectedNodeQueue = new ArrayList<>();
        conversionQueue = new ArrayList<>();
    }

    /**Accessor Methods**/

    /**
     * Returns a list of all the entries.
     * @return
     */
    public ArrayList<SpreadingBlock> getEntries()
    {
        return entries;
    }

    /**
     * Returns what a victim block should convert into. <br>
     * This returned block should be a block that actively spreads.
     * @param victimBlock The block that needs to be converted.
     * @return The actively spreading block variant.
     */
    @Nullable
    private Block getActiveSpreadingVariant(BlockState victimBlock)
    {
        //Loop through each entry until we find the appropriate one
        for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //If the victim blocks is the same block as the entry
            if(entry.isValidVictim(victimBlock))
            {
                return entry;
            }
        }
        return null;
    }


    /**
     * Returns what an active spreading block should convert into. <br>
     * This returned block should be a block that is the final evolution.
     * @param activeBlock The block that needs to be converted.
     * @return The dormant block variant.
     */
    @Nullable
    private BlockState getDormantVariant(Block activeBlock)
    {
        //Loop through each entry until we find the appropriate one.s
        for(SpreadingBlock entry : entries)
        {
            //If the victim blocks is the same block as the entry
            if(entry == activeBlock)
            {
                return entry.getDormantVariant();
            }
        }
        return null;
    }


    /**
     * Returns what an block should convert back into if sculk is removed. <br>
     * @param targetBlock The block that needs to be converted.
     * @return The dormant block variant.
     */
    @Nullable
    private BlockState getVictimVariant(Block targetBlock)
    {
        //Loop through each entry until we find the appropriate one.s
        for(SpreadingBlock entry : entries)
        {
            //If the target block is the active or dormant variant
            if(entry.getBlock() == targetBlock || entry.getDormantVariant().getBlock() == targetBlock)
            {
                return entry.getVictimVariant();
            }
        }
        return null;
    }

    /**Modifier Methods**/

    /**
     * Adds an entry into the table.
     * @param active_spreading_block_in This is the block that the victim will turn into.
     */
    public void addEntry(SpreadingBlock active_spreading_block_in)
    {
        entries.add(active_spreading_block_in);
    }

    /** Boolean Methods **/


    /**
     * Will check all entries in the infestation conversion table to see if
     * the given block is considered a victim to any entry.
     * @param blockStateIn The block to check
     * @return True if the block can be a victim, false otherwise.
     */
    //TODO this can probably be optimized with a dictionary
    @Nullable
    public boolean isConsideredVictim(BlockState blockStateIn)
    {
        //Loop through each entry until we find the appropriate one
        for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //If the victim blocks is the same block as the entry
            if(entry.isValidVictim(blockStateIn))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Will check all entries in the infestation conversion table to see if
     * the given block is considered a victim to any entry.
     * @param blockStateIn The block to check
     * @return True if the block can be a victim, false otherwise.
     */
    //TODO this can probably be optimized with a dictionary
    @Nullable
    public boolean isConsideredActiveSpreader(BlockState blockStateIn)
    {
        //Loop through each entry until we find the appropriate one
        for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //System.out.println(entry.toString());
            //If the victim blocks is the same block as the entry
            if(blockStateIn.is(entry))
            {
                return true;
            }
        }
        return false;
    }


    /**
     * Will check all entries in the infestation conversion table to see if
     * the given block is considered a victim to any entry.
     * @param blockStateIn The block to check
     * @return True if the block can be a victim, false otherwise.
     */
    //TODO this can probably be optimized with a dictionary
    @Nullable
    public boolean isConsideredDormantSpreader(BlockState blockStateIn)
    {
        //Loop through each entry until we find the appropriate one
        for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //If the victim blocks is the same block as the entry
            if(blockStateIn.is(entry.getDormantVariant().getBlock()))
            {
                return true;
            }
        }
        return false;
    }


    /** Event Methods **/

    /**
     * Converts a target block into an actively spreading variant
     * @param world The world the block is in.
     * @param targetPos The position of the target block.
     * @return True if converted, false otherwise.
     */
    public boolean convertToActiveSpreader(ServerWorld world, BlockPos targetPos)
    {
        BlockState targetBlock = world.getBlockState(targetPos);
        Block activeVariant = getActiveSpreadingVariant(targetBlock);
        if(activeVariant != null)
        {
            world.setBlockAndUpdate(targetPos, activeVariant.defaultBlockState());
            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Converts an active spreader into a dormant variant
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean convertToDormant(ServerWorld world, BlockPos targetPos)
    {

        Block targetBlock = world.getBlockState(targetPos).getBlock();
        BlockState dormantVariant = getDormantVariant(targetBlock);

        if(dormantVariant != null)
        {
            world.setBlockAndUpdate(targetPos, dormantVariant);

            //Given a 50% chance, place down sculk flora on block
            if (world.random.nextInt(2) <= 0)
                BlockAlgorithms.placeSculkFlora(targetPos.above(), world);

            if(dormantVariant.getBlock() == BlockRegistry.INFESTED_LOG_DORMANT.get())
                BlockAlgorithms.placeFloraAroundLog(world, targetPos);

            SculkHoard.gravemind.placeSculkNode(world, targetPos.above(), true);

            BlockAlgorithms.placePatchesOfVeinAbove(world, targetPos);

            BlockAlgorithms.placeSculkBeeHive(world, targetPos.above());

            return true;
        }
        else
        {
            return false;
        }
    }

    /**
     * Only process a specific amount every time this is called. <br>
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent}
     * @param world The world
     */
    public void proccessConversionToVictimQueue(ServerWorld world)
    {
        if(!world.isClientSide())
        {
            for(int i = 0; i < conversionAmountPerInterval && i < convertToVictimNodeQueue.size(); i++)
            {
                BlockAlgorithms.replaceSculkFlora(world, convertToVictimNodeQueue.get(i)); //Remove any flora
                convertToVictim(world, convertToVictimNodeQueue.get(i)); //convert
                convertToVictimNodeQueue.remove(i);
                i--;
            }
        }
    }


    /**
     * Only process a specific amount every time this is called. <br>
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent} <br>
     * NOTE: POTENTIAL ISSUE : since conversions are delayed, if the block changes, there can be unintended behavior
     * @param world The world
     */
    public void processConversionQueue(ServerWorld world)
    {
        if(!world.isClientSide())
        {
            for(int i = 0; i < conversionAmountPerInterval && i < conversionQueue.size(); i++)
            {
                if(conversionQueue.get(i).convertToActiveSpreader)
                {
                    convertToActiveSpreader(world, conversionQueue.get(i).position); //convert
                    if(isConsideredActiveSpreader(world.getBlockState(conversionQueue.get(i).position)))
                    {
                        SpreadingTile childTile = (SpreadingTile) world.getWorldServer().getBlockEntity(conversionQueue.get(i).position); //Get new block tile entity

                        if(childTile != null)
                        {
                            childTile.setMaxSpreadAttempts(conversionQueue.get(i).getAttemptsToAssignChild());
                        }
                    }
                }
                else if(conversionQueue.get(i).convertToDormantSpreader)
                {
                    convertToDormant(world, conversionQueue.get(i).position); //convert
                }
                else
                {
                    convertToVictim(world, conversionQueue.get(i).position); //convert
                }
                conversionQueue.remove(i);
                i--;
            }
        }
    }


    /**
     * Only process a specific amount every time this is called. <br>
     * This gets called in {@link ForgeEventSubscriber#WorldTickEvent}
     * @param world The world
     */
    public void processConversionToInfectedQueue(ServerWorld world)
    {
        if(!world.isClientSide())
        {
            for(int i = 0; i < conversionAmountPerInterval && i < convertToInfectedNodeQueue.size(); i++)
            {
                convertToActiveSpreader(world, convertToInfectedNodeQueue.get(i)); //convert
                convertToInfectedNodeQueue.remove(i);
                i--;
            }
        }
    }


    //TODO make this better so that you dont have to specify 3 booleans
    public void addToConversionQueue(BlockPos positionIn, int maxSpreadAttemptsIn, boolean convertToActive, boolean convertToDormant, boolean convertToNormal)
    {
        ConversionRequest request = new ConversionRequest(positionIn);
        request.attemptsToAssignChild = maxSpreadAttemptsIn;
        if(convertToActive) request.setConvertToActiveSpreader();
        else if(convertToDormant) request.setConvertToDormantSpreader();
        else request.setConvertToNormal();
        conversionQueue.add(request);
    }

    /**
     * Converts a an active or dormant variant into a victim variant
     * @param world The world of the block.
     * @param targetPos The position of the block we are trying to convert.
     */
    public boolean convertToVictim(ServerWorld world, BlockPos targetPos)
    {
        Block targetBlock = world.getBlockState(targetPos).getBlock();
        BlockState victimVariant = getVictimVariant(targetBlock);

        if(victimVariant != null)
        {
            world.setBlockAndUpdate(targetPos, victimVariant);
            return true;
        }
        return false;
    }

    /** ~~~~~~~~ CLASSES ~~~~~~~~ **/

    public class ConversionRequest
    {
        //The target position
        private BlockPos position;
        //How many spread attempts should child blocks get
        private int attemptsToAssignChild;
        //If true, we are convert to an active spreading variant,
        private boolean convertToActiveSpreader;
        //If true, we are convert to an active spreading variant,
        private boolean convertToDormantSpreader;
        //If true, we are convert to an active spreading variant,
        private boolean convertToNormal;

        public ConversionRequest(BlockPos positionIn)
        {
            position = positionIn;
        }

        public BlockPos getPosition()
        {
            return  position;
        }

        public int getAttemptsToAssignChild()
        {
            return  attemptsToAssignChild;
        }

        public void setAttemptsToAssignChild(int attemptsToAssignChildIn)
        {
            attemptsToAssignChild = attemptsToAssignChildIn;
        }

        public void setConvertToActiveSpreader()
        {
            convertToActiveSpreader = true;
        }

        public void setConvertToDormantSpreader()
        {
                convertToDormantSpreader = true;
        }

        public void setConvertToNormal()
        {
            convertToNormal = true;
        }
    }
}
