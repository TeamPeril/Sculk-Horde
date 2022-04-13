package com.github.sculkhoard.common.block.BlockInfestation;

import net.minecraft.block.Block;
import net.minecraft.block.BlockState;

public class InfestationConversionEntry {

    /**
     * active_spreading_block - This is the block that the victim will turn into. <br>
     * IMPORTANT NOTE: the active_spreading_block determines what the victim blocks are.<br>
     * dormant_block - Once the active block is done spreading, this is what it will turn into.
     */
    private SpreadingBlock active_spreading_block;
    private Block dormant_block;

    public InfestationConversionEntry(SpreadingBlock active_spreading_block_in, Block dormant_block_in)
    {
        active_spreading_block = active_spreading_block_in;
        dormant_block = dormant_block_in;
        System.out.println("New Infestation Conversion Entry: " + active_spreading_block + ", " + dormant_block);
    }


    public Block getActiveSpreadingBlock() {return active_spreading_block;}

    public Block getDormantBlock(){return dormant_block;}

    public boolean isValidVictim(BlockState blockState)
    {
        return active_spreading_block.isValidVictim(blockState);
    }

    @Override
    public String toString() {
        return "InfestationConversionEntry{ active_spreading_block: " + active_spreading_block + ", dormant_block: " + dormant_block + "}";
    }
}
