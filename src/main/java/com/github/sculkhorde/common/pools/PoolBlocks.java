package com.github.sculkhorde.common.pools;

import net.minecraft.block.Block;

import java.util.ArrayList;
import java.util.Random;

/**
 * This class is used to randomly select a block from a given pool. <br>
 * NOTE: Weights need to be added from small to big.
 */
public class PoolBlocks {

    int totalWeight; //The sum of all weights
    ArrayList<PoolEntry> entries; //the array list of all entries

    /**
     * Default Constructor
     */
    public PoolBlocks()
    {
        totalWeight = 0;
        entries = new ArrayList<PoolEntry>();
    }

    /**
     * Adds an entrry
     * @param blockIn the block
     * @param weightIn the weight of the entry
     */
    public void addEntry(Block blockIn, int weightIn)
    {
        //Dont allow weights greater than 100
        if(weightIn > 100)
        {
            totalWeight += 100;
            entries.add(new PoolEntry(blockIn, 100));
        }
        else
        {
            totalWeight += weightIn;
            entries.add(new PoolEntry(blockIn, weightIn));
        }
    }

    /**
     * Returns a random entry.
     * @return a random block.
     * <br> Learned from: https://softwareengineering.stackexchange.com/questions/150616/get-weighted-random-item
     */
    public Block getRandomEntry()
    {
        int randomValue = new Random().nextInt(totalWeight);
        int cumulativeSum = 0;
        for(PoolEntry entry : entries)
        {
            cumulativeSum += entry.weight;
            if(cumulativeSum >= randomValue)
                return entry.block;
        }
        return entries.get(entries.size()-1).block; //If all else fails, just return most rare item
    }

}

class PoolEntry{

    Block block;
    int weight;

    /**
     * Default Constructor
     */
    public PoolEntry(Block blockIn, int weightIn)
    {
        block = blockIn;
        weight = weightIn;
    }

}
