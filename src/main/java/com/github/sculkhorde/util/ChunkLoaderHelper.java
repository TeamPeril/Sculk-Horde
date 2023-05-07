package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraftforge.common.world.ForgeChunkManager;

public class ChunkLoaderHelper
{
    public static void forceLoadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, true, true);
    }

    public static void forceLoadChunksInRadius(ServerLevel world, BlockPos owner, int chunkOriginX, int chunkOriginZ, int radius)
    {
        /*
        If radius is 3, this is what the area of chunk loading will look like.
            ooooooo
            ooo*ooo
            ooooooo
        This means that the length of any side is (CHUNK_LOAD_RADIUS * 2) + 1.
         */

        int startChunkX = chunkOriginX - radius;
        int startChunkZ = chunkOriginZ - radius;

        for(int xOffset = 0; xOffset < (radius * 2) + 1; xOffset++)
        {
            for(int zOffset = 0; zOffset < (radius * 2) + 1; zOffset++)
            {
                forceLoadChunk(world, owner, startChunkX + xOffset, startChunkZ + zOffset, true);
            }
        }
    }

    public static void unloadChunk(ServerLevel world, BlockPos owner, int chunkX, int chunkZ, boolean tickingWithoutPlayer) {

        ForgeChunkManager.forceChunk(world, SculkHorde.MOD_ID, owner, chunkX, chunkZ, false, false);
    }

    public static void unloadChunksInRadius(ServerLevel world, BlockPos owner, int chunkOriginX, int chunkOriginZ, int radius)
    {
        /*
        If radius is 3, this is what the area of chunk loading will look like.
            ooooooo
            ooo*ooo
            ooooooo
        This means that the length of any side is (CHUNK_LOAD_RADIUS * 2) + 1.
         */

        int startChunkX = chunkOriginX - radius;
        int startChunkZ = chunkOriginZ - radius;

        for(int xOffset = 0; xOffset < (radius * 2) + 1; xOffset++)
        {
            for(int zOffset = 0; zOffset < (radius * 2) + 1; zOffset++)
            {
                unloadChunk(world, owner, startChunkX + xOffset, startChunkZ + zOffset, true);
            }
        }
    }
}
