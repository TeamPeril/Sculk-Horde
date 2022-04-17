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
    private ArrayList<InfestationConversionEntry> entries;

    /**
     * Default Constructor
     */
    public InfestationConversionTable()
    {
        entries = new ArrayList<InfestationConversionEntry>();
    }

    /**Accessor Methods**/

    /**
     * Returns a list of all the entries.
     * @return
     */
    public ArrayList<InfestationConversionEntry> getEntries()
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
            for(InfestationConversionEntry entry : SculkHoard.infestationConversionTable.getEntries())
            {
                System.out.println(entry.toString());
            }
        }

        //Loop through each entry until we find the appropriate one.s
        for(InfestationConversionEntry entry : SculkHoard.infestationConversionTable.getEntries())
        {
            //System.out.println(entry.toString());
            //If the victim blocks is the same block as the entry
            if(entry.isValidVictim(victimBlock))
            {
                return entry.getActiveSpreadingBlock();
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
    private Block getDormantVariant(Block activeBlock)
    {
        //Loop through each entry until we find the appropriate one.s
        for(InfestationConversionEntry entry : entries)
        {
            //If the victim blocks is the same block as the entry
            if(entry.getActiveSpreadingBlock() == activeBlock)
            {
                return entry.getDormantBlock();
            }
        }
        return null;
    }

    /**Modifier Methods**/

    /**
     * Adds an entry into the table.
     * @param active_spreading_block_in This is the block that the victim will turn into.
     * @param dormant_block_in Once the active block is done spreading, this is what it will turn into.
     */
    public void addEntry(SpreadingBlock active_spreading_block_in, Block dormant_block_in)
    {
        entries.add(new InfestationConversionEntry(active_spreading_block_in, dormant_block_in));
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
        Block dormantVariant = getDormantVariant(targetBlock);

        if(dormantVariant != null)
        {
            world.setBlockAndUpdate(targetPos, dormantVariant.defaultBlockState());

            //Given a 50% chance, place down sculk flora on block
            if (world.random.nextInt(2) <= 0)
                BlockAlgorithms.placeSculkFlora(targetPos.above(), world);

            if(dormantVariant == BlockRegistry.INFESTED_LOG_DORMANT.get())
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

}
