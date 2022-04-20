package com.github.sculkhoard.common.block.BlockInfestation;

import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.ArrayList;

public class InfestationConversionTable {

    private static boolean DEBUG_THIS = false;
    private ArrayList<SpreadingBlock> entries;

    /**
     * Default Constructor
     */
    public InfestationConversionTable()
    {
        entries = new ArrayList<SpreadingBlock>();
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
        if(DEBUG_THIS)
        {
            System.out.println("All entries: ");
            for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
            {
                System.out.println(entry.toString());
            }
        }

        //Loop through each entry until we find the appropriate one.s
        for(SpreadingBlock entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //System.out.println(entry.toString());
            //If the victim blocks is the same block as the entry
            if(entry.isValidVictim(victimBlock))
            {
                return entry;
            }
            else if (DEBUG_THIS)
            {
                //System.out.println(victimBlock + " != " + entry.getVictimBlock());
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
            if(DEBUG_THIS) System.out.println("Could not find active spreader for " + targetBlock);
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

            BlockAlgorithms.placeSculkNode(world, targetPos.above());

            BlockAlgorithms.placePatchesOfVeinAbove(world, targetPos);

            return true;
        }
        else
        {
            if(DEBUG_THIS) System.out.println("Could not find dormant for " + targetBlock);
            return false;
        }
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

}
