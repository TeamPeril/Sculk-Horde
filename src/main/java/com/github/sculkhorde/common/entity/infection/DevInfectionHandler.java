package com.github.sculkhorde.common.entity.infection;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;

public class DevInfectionHandler {

    // The parent tile entity
    private BlockEntity parent = null;
    private ServerLevel world = null;
    private BlockPos origin = null;

    // The infection trees
    private DevInfectionTree northInfectionTree;
    private DevInfectionTree southInfectionTree;
    private DevInfectionTree eastInfectionTree;
    private DevInfectionTree westInfectionTree;
    private DevInfectionTree upInfectionTree;
    private DevInfectionTree downInfectionTree;


    public DevInfectionHandler(BlockEntity parent, BlockPos origin) {
        this.parent = parent;
        this.world = (ServerLevel) parent.getLevel();
        this.origin = origin;

        northInfectionTree = new DevInfectionTree(world, Direction.NORTH, origin);
        northInfectionTree.activate();

        southInfectionTree = new DevInfectionTree(world, Direction.SOUTH, origin);
        southInfectionTree.activate();

        eastInfectionTree = new DevInfectionTree(world, Direction.EAST, origin);
        eastInfectionTree.activate();

        westInfectionTree = new DevInfectionTree(world, Direction.WEST, origin);
        westInfectionTree.activate();

        upInfectionTree = new DevInfectionTree(world, Direction.UP, origin);
        upInfectionTree.activate();

        downInfectionTree = new DevInfectionTree(world, Direction.DOWN, origin);
        downInfectionTree.activate();

    }



    public void tick() {

        northInfectionTree.tick();
        southInfectionTree.tick();
        eastInfectionTree.tick();
        westInfectionTree.tick();
        upInfectionTree.tick();
        downInfectionTree.tick();
    }
}
