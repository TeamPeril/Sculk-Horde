package com.github.sculkhorde.systems.infestation_systems.node_infestation;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.common.effect.DiseasedAtmosphereEffect;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.InfestationUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.LinkedHashSet;
import java.util.Set;

public class NodeAtmosphereInfestationSystem {
    // The parent tile entity
    protected SculkNodeBlockEntity parent = null;
    protected long timeOfLastInfestationTick = 0;

    // Cooldown between processing chunks (e.g., 1 second per batch of coordinates)
    protected long INFESTATION_TICK_COOLDOWN = TickUnits.convertSecondsToTicks(0.25F);

    protected long timeOfLastDiseasedAtmosphereTick = 0;
    protected long DISEASED_ATMOSPHERE_TICK_COOLDOWN = TickUnits.convertSecondsToTicks(1);

    // --- Fractional Radius & Chunking State Variables ---
    // Maximum number of coordinates held in memory at once per batch
    protected static final int CHUNK_BATCH_SIZE = 64;

    // Stepping by 0.5 guarantees overlapping integer coordinates so none are missed
    protected static final double RADIUS_STEP = 0.5;

    protected double currentBlockInfestationRadius = 0.0;
    protected int currentX = 0;
    protected int symmetryStep = 0;

    // LinkedHashSet automatically filters out duplicate coordinates so we don't
    // flood SculkHorde.cursorSystem with 8 identical cursors on the same block!
    protected final Set<BlockPos> coordinateChunkBuffer = new LinkedHashSet<>(CHUNK_BATCH_SIZE);

    public NodeAtmosphereInfestationSystem(SculkNodeBlockEntity parent) {
        this.parent = parent;
    }

    public void serverTick() {
        if (parent.getLevel() == null) {
            return;
        }

        applyDiseasedAtmosphereTick();
        blockInfestationTick();
    }

    protected void applyDiseasedAtmosphereTick() {
        if (Math.abs(parent.getLevel().getGameTime() - timeOfLastDiseasedAtmosphereTick) < DISEASED_ATMOSPHERE_TICK_COOLDOWN) {
            return;
        }
        timeOfLastDiseasedAtmosphereTick = parent.getLevel().getGameTime();

        for (Player player : ServerLifecycleHooks.getCurrentServer().getPlayerList().getPlayers()) {
            if (EntityAlgorithms.isInvalidTargetForSculkHorde(player)) {
                continue;
            }

            if (BlockAlgorithms.getBlockDistance(player.blockPosition(), parent.getBlockPos()) < currentBlockInfestationRadius) {
                DiseasedAtmosphereEffect.applyToEntity(player, TickUnits.convertSecondsToTicks(30));
            }
        }
    }

    protected void blockInfestationTick() {
        if (!ModConfig.SERVER.block_infestation_enabled.get()) {
            return;
        }

        if (Math.abs(parent.getLevel().getGameTime() - timeOfLastInfestationTick) < INFESTATION_TICK_COOLDOWN && timeOfLastInfestationTick != 0) {
            return;
        }

        timeOfLastInfestationTick = parent.getLevel().getGameTime();

        // 1. Gather the next chunk of coordinates up to CHUNK_BATCH_SIZE
        gatherNextCoordinateChunk();

        // 2. Process all coordinates in the gathered chunk
        processCoordinateChunk((ServerLevel) parent.getLevel(), coordinateChunkBuffer);

        // 3. Clear the buffer to free memory for the next cycle
        coordinateChunkBuffer.clear();
    }

    /**
     * Gathers up to CHUNK_BATCH_SIZE coordinates using the Fractional Radius Step algorithm,
     * pausing and resuming seamlessly across ticks via state variables.
     */
    protected void gatherNextCoordinateChunk() {
        BlockPos center = parent.getBlockPos();

        while (coordinateChunkBuffer.size() < CHUNK_BATCH_SIZE) {
            // Check if we have completed the full circle
            if (currentBlockInfestationRadius > Gravemind.MINIMUM_DISTANCE_BETWEEN_NODES) {
                currentBlockInfestationRadius = 0.0;
                currentX = 0;
                symmetryStep = 0;
                parent.setActive(false);
                break;
            }

            // Handle the very center coordinate (r = 0)
            if (currentBlockInfestationRadius == 0.0) {
                coordinateChunkBuffer.add(center);
                currentBlockInfestationRadius += RADIUS_STEP;
                currentX = 0;
                symmetryStep = 0;
                continue;
            }

            // Calculate corresponding Y (Z in Minecraft's 2D horizontal plane)
            int currentZ = (int) Math.round(Math.sqrt((currentBlockInfestationRadius * currentBlockInfestationRadius) - (currentX * currentX)));

            // Generate the current point out of the 8-way symmetry
            int targetX = center.getX();
            int targetZ = center.getZ();

            switch (symmetryStep) {
                case 0 -> { targetX += currentX; targetZ += currentZ; }
                case 1 -> { targetX -= currentX; targetZ += currentZ; }
                case 2 -> { targetX += currentX; targetZ -= currentZ; }
                case 3 -> { targetX -= currentX; targetZ -= currentZ; }
                case 4 -> { targetX += currentZ; targetZ += currentX; }
                case 5 -> { targetX -= currentZ; targetZ += currentX; }
                case 6 -> { targetX += currentZ; targetZ -= currentX; }
                case 7 -> { targetX -= currentZ; targetZ -= currentX; }
            }

            coordinateChunkBuffer.add(new BlockPos(targetX, center.getY(), targetZ));

            // Advance to the next symmetry point or next X step
            symmetryStep++;
            if (symmetryStep >= 8) {
                symmetryStep = 0;
                currentX++;

                // Once X exceeds floor(r), we move outward to the next fractional ring
                if (currentX > (int) Math.floor(currentBlockInfestationRadius)) {
                    currentX = 0;
                    currentBlockInfestationRadius += RADIUS_STEP;
                }
            }
        }
    }

    /**
     * Processes the collected chunk of coordinates.
     */
    protected void processCoordinateChunk(ServerLevel level, Set<BlockPos> chunk) {
        for (BlockPos pos : chunk) {

            float chanceOfSuccess = 100.0F;
            float distanceFromCenter = BlockAlgorithms.getBlockDistanceXZ(pos, parent.getBlockPos());
            float distanceToEdge = Gravemind.MINIMUM_DISTANCE_BETWEEN_NODES - distanceFromCenter;

            if(distanceToEdge <= 100)
            {
                chanceOfSuccess = distanceToEdge;
            }

            boolean willInfectBlock = (parent.getLevel().random.nextFloat() * 100) <= chanceOfSuccess;

            if(willInfectBlock)
            {
                BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare(level, pos, 3, 1, TickUnits.convertSecondsToTicks(3));
                // Replace this with your specific per-coordinate / per-column infestation call
                InfestationUtil.infestArea(level, pos, pos);
            }
        }
    }
}