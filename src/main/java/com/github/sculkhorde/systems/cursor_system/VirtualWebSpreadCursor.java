package com.github.sculkhorde.systems.cursor_system;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ParticleUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Position;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import org.joml.Vector3f;

import java.util.Random;

public class VirtualWebSpreadCursor extends VirtualCursor{


    public VirtualWebSpreadCursor(Level level, BlockPos blockPos)
    {
        super(level);
        cursorType = CursorType.MISC;
        moveTo(blockPos.getX(), blockPos.getY(), blockPos.getZ());
    }


    /**
     * Returns true if the block is considered a target.
     * @param pos the block position
     * @return true if the block is considered a target
     */
    @Override
    protected boolean isTarget(BlockPos pos)
    {
        // 1. If the current block is not Air, it is not a target.
        if(!level.getBlockState(pos).isAir())
        {
            return false;
        }

        return BlockAlgorithms.isTouchingASolidBlock((ServerLevel) level, pos);
    }
    /**
     * Transforms the block at the given position.
     * @param pos the position of the block
     */
    @Override
    protected void transformBlock(BlockPos pos)
    {

        // Start with the default Sculk Vein state
        BlockState veinState = ModBlocks.LIVING_WEB_BLOCK.get().defaultBlockState();
        boolean hasSupport = false;

        // Iterate through all 6 directions (UP, DOWN, NORTH, SOUTH, EAST, WEST)
        for (net.minecraft.core.Direction direction : net.minecraft.core.Direction.values()) {

            BlockPos neighborPos = pos.relative(direction);
            BlockState neighborState = level.getBlockState(neighborPos);

            // Check if the neighbor block can support a vein on the connecting face.
            // We check the OPPOSITE face of the neighbor.
            // Example: If we are checking UP, we look at the block above and check its DOWN face.
            if (neighborState.isFaceSturdy(level, neighborPos, direction.getOpposite())) {
                veinState = veinState.setValue(net.minecraft.world.level.block.MultifaceBlock.getFaceProperty(direction), true);
                hasSupport = true;
            }
        }

        // Only place the block if we found at least one supporting block
        if(hasSupport)
        {
            level.setBlock(pos, veinState, 3);
        }
    }


    @Override
    public void moveTo(double x, double y, double z) {
        super.moveTo(x, y, z);
    }

    /**
     * Returns true if the block is considered obstructed.
     * @param state the block state
     * @param pos the block position
     * @return true if the block is considered obstructed
     */
    @Override
    protected boolean isObstructed(BlockState state, BlockPos pos)
    {
        if(BlockAlgorithms.isAir(state) && !BlockAlgorithms.isTouchingASolidBlock((ServerLevel) level, pos))
        {
            return true;
        }
        // If we detect fluid
        else if(!state.getFluidState().isEmpty())
        {
            // If its water, its only obstructed if its the water source block or flowing water block
            if(state.getFluidState().is(Fluids.WATER) && state.is(Blocks.WATER))
            {
                return true;
            }

            if(!state.getFluidState().is(Fluids.WATER))
            {
                return true;
            }
        }
        else if(BlockAlgorithms.getBlockDistance(origin, pos) > MAX_RANGE)
        {
            return true;
        }

        // This is to prevent the entity from getting stuck in a loop
        if(visitedPositions.containsKey(pos.asLong()))
        {
            return true;
        }

        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir((ServerLevel) getLevel(), pos);

        if(isBlockNotExposedToAir)
        {
            return true;
        }

        return false;
    }

    @Override
    protected void spawnParticleEffects()
    {
        Random random = new Random();
        float maxOffset = 2;

        float randomXOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomYOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        float randomZOffset = random.nextFloat(maxOffset * 2) - maxOffset;
        Vector3f spawnPos = new Vector3f(getBlockPosition().getX() + randomXOffset, getBlockPosition().getY() + randomYOffset, getBlockPosition().getZ() + randomZOffset);
        Vector3f velocity = new Vector3f(randomXOffset * 0.1F, randomYOffset * 0.1F, randomZOffset * 0.1F);
        ClientLevel clientLevel = (ClientLevel) getLevel();
        ParticleUtil.spawnBlockParticleOnClient(clientLevel.getBlockState(getBlockPosition()), clientLevel, spawnPos, velocity);

        ParticleUtil.spawnPurityDustParticlesOnClient(clientLevel, BlockPos.containing((Position) spawnPos));

    }

}

