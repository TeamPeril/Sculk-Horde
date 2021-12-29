package com.github.sculkhoard.common.block;

import com.github.sculkhoard.core.BlockRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

public class BlockAlgorithms {

    public static void placeSculkFlora(BlockPos pos, ServerWorld world)
    {
        Block[] commonFlora = {
                BlockRegistry.GRASS.get(),
                BlockRegistry.GRASS_SHORT.get()
        };
        Block targetBlock = world.getBlockState(pos).getBlock();

        if(targetBlock == Blocks.AIR)
        {
            Block selectedFlora;
            // A 1/100 chance to be a spike, else just a random common flora
            if(world.random.nextInt(100) == 0) selectedFlora = BlockRegistry.SPIKE.get();
            else selectedFlora = commonFlora[world.random.nextInt(commonFlora.length)];

            world.setBlockAndUpdate(pos, selectedFlora.defaultBlockState());
        }
    }
}
