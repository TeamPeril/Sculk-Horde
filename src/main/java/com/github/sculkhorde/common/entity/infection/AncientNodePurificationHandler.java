package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class AncientNodePurificationHandler {

    // The parent tile entity
    private BlockEntity parent = null;
    private ServerLevel world = null;
    private BlockPos origin = null;

    public boolean spawnOnSurface = true;

    private final int CHECK_FOR_DEFEAT_INTERVAL = TickUnits.convertMinutesToTicks(1);
    private int timeRemainingUntilNextDefeatCheck = CHECK_FOR_DEFEAT_INTERVAL;

    private boolean isActive = false;

    // The infection trees
    private PurificationTree northTree;
    private PurificationTree southTree;
    private PurificationTree eastTree;
    private PurificationTree westTree;
    private PurificationTree upTree;
    private PurificationTree downTree;

    protected int lastTimeSinceTick = 0;
    protected int TICK_COOLDOWN = TickUnits.convertSecondsToTicks(1);


    public AncientNodePurificationHandler(BlockEntity parent, BlockPos origin) {
        this.parent = parent;
        this.world = (ServerLevel) parent.getLevel();
        this.origin = origin;

        northTree = new PurificationTree(world, Direction.NORTH, origin);
        northTree.activate();

        southTree = new PurificationTree(world, Direction.SOUTH, origin);
        southTree.activate();

        eastTree = new PurificationTree(world, Direction.EAST, origin);
        eastTree.activate();

        westTree = new PurificationTree(world, Direction.WEST, origin);
        westTree.activate();

        upTree = new PurificationTree(world, Direction.UP, origin);
        upTree.activate();

        downTree = new PurificationTree(world, Direction.DOWN, origin);
        downTree.activate();

    }

    public BlockPos calculateSpawnPosition()
    {
        if(!spawnOnSurface)
        {
            return origin;
        }

        // Do ray trace from bottom to top of world. Return last known solid block
        BlockPos.MutableBlockPos checkPosition = new BlockPos.MutableBlockPos(origin.getX(), world.getMinBuildHeight(), origin.getZ());
        BlockPos lastKnownSolidBlock = null;
        while(checkPosition.getY() < world.getMaxBuildHeight())
        {
            checkPosition.setY(checkPosition.getY() + 1);
            if(BlockAlgorithms.isSolid(world, checkPosition))
            {
                lastKnownSolidBlock = checkPosition.immutable();
            }
        }

        if(lastKnownSolidBlock != null)
        {
            origin = lastKnownSolidBlock;
            //SculkHorde.LOGGER.debug("Sculk Node found InfestationHandler spawn position at " + lastKnownSolidBlock + " of blockstate " + world.getBlockState(lastKnownSolidBlock));
        }

        return lastKnownSolidBlock;
    }

    public boolean canBeActivated()
    {
        if(timeRemainingUntilNextDefeatCheck > 0)
        {
            timeRemainingUntilNextDefeatCheck--;
            return false;
        }

        if(!SculkHorde.savedData.isHordeDefeated()) {
            timeRemainingUntilNextDefeatCheck = CHECK_FOR_DEFEAT_INTERVAL;
            return false;
        }

        if(parent == null || world == null || origin == null)
        {
            timeRemainingUntilNextDefeatCheck = CHECK_FOR_DEFEAT_INTERVAL;
            return false;
        }

        if(calculateSpawnPosition() != null)
        {
            timeRemainingUntilNextDefeatCheck = CHECK_FOR_DEFEAT_INTERVAL;
            return true;
        }

        SculkHorde.LOGGER.info("Ancient Node at " + parent.getBlockPos() + " purification cannot be activated because it has no spawn position.");
        timeRemainingUntilNextDefeatCheck = CHECK_FOR_DEFEAT_INTERVAL;
        return false;
    }

    public void activate()
    {
        isActive = true;
        northTree.setOrigin(origin);
        southTree.setOrigin(origin);
        eastTree.setOrigin(origin);
        westTree.setOrigin(origin);
    }

    public void deactivate()
    {
        isActive = false;
    }

    public void tick() {
        if(!isActive || !ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        if(lastTimeSinceTick < TICK_COOLDOWN)
        {
            lastTimeSinceTick++;
            return;
        }
        lastTimeSinceTick = 0;

        northTree.tick();
        southTree.tick();
        eastTree.tick();
        westTree.tick();
        upTree.tick();
        downTree.tick();
    }
}
