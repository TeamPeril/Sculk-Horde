package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorInfector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.LevelChunk;

public class InfestationUtil {

    public static void infestChunk(ServerLevel level, BlockPos pos)
    {

        LevelChunk chunk = level.getChunkAt(pos);


        BlockPos minChunkCorner = new BlockPos(
                chunk.getPos().getMinBlockX(), // Chunk X * 16
                // We use the Y coordinate from the input 'pos' to ensure the cursor starts within the chunk's vertical space.
                pos.getY(),
                chunk.getPos().getMinBlockZ()  // Chunk Z * 16
        );

        // The chunk's maximum corner (max X, max Z) is 15 blocks (or 16 blocks - 1) away from the minimum.
        BlockPos maxChunkCorner = new BlockPos(
                chunk.getPos().getMaxBlockX(), // (Chunk X * 16) + 15
                pos.getY(),
                chunk.getPos().getMaxBlockZ()  // (Chunk Z * 16) + 15
        );

        // Set pos1 and pos2 to the opposite corners of the chunk's X/Z bounds
        BlockPos pos1 = minChunkCorner;
        BlockPos pos2 = maxChunkCorner;

        ChunkCursorInfector infector = ChunkCursorInfector.of()
                .level(level)
                .pos1(pos1)
                .pos2(pos2)
                .blocksPerTick(2);

        SculkHorde.cursorSystem.addVirtualCursor(infector);
    }

    public static void infestChunksInCircle(ServerLevel level, BlockPos center, int radius)
    {
        // Convert the center BlockPos to chunk coordinates (ChunkPos)
        ChunkPos centerChunk = new ChunkPos(center);
        int centerChunkX = centerChunk.x;
        int centerChunkZ = centerChunk.z;
        int chunksInfested = 0;

        // A chunk's extent is 16 blocks (0 to 15). The radius is measured in chunks.
        // We iterate from (centerChunkX - radius) to (centerChunkX + radius) and similarly for Z.
        // This creates a square area, and we use a distance check to make it a circle.
        for (int chunkX = centerChunkX - radius; chunkX <= centerChunkX + radius; chunkX++)
        {
            for (int chunkZ = centerChunkZ - radius; chunkZ <= centerChunkZ + radius; chunkZ++)
            {
                // Calculate the distance squared in chunks.
                // distance^2 = (deltaX)^2 + (deltaZ)^2
                int deltaX = chunkX - centerChunkX;
                int deltaZ = chunkZ - centerChunkZ;
                double distanceSq = (double)deltaX * deltaX + (double)deltaZ * deltaZ;

                // Check if the chunk is within the circle (radius squared).
                // Using radius * radius for the comparison avoids a costly square root operation.
                if (distanceSq <= (double)radius * radius)
                {
                    // Create a BlockPos within the current chunk to pass to infestChunk.
                    // We use the chunk's min X, a fixed Y (like the center's Y), and min Z.
                    // Since infestChunk only uses the X and Z to get the chunk and the Y
                    // for the vertical starting point, this is sufficient.
                    BlockPos chunkPos = new BlockPos(chunkX * 16, center.getY(), chunkZ * 16);

                    // Call the existing method to infest this specific chunk
                    infestChunk(level, chunkPos);
                }
            }
        }
    }
}
