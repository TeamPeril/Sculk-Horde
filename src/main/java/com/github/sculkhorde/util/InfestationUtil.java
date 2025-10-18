package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.chunk_cursor_system.ChunkCursorInfector;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.chunk.LevelChunk;

public class InfestationUtil {

    public static int infestChunk(ServerLevel level, BlockPos pos)
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
                .pos2(pos2);

        SculkHorde.chunkInfestationSystem.addChunkInfector(infector);
        return 1;
    }
}
