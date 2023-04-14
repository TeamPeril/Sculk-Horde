package com.github.sculkhorde.common.entity.infection;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class SculkNodeInfectionHandler {

    // The parent tile entity
    private SculkNodeBlockEntity parent = null;
    private ServerLevel world = null;
    private BlockPos origin = null;

    // The infection trees
    private InfectionTree northInfectionTree;
    private InfectionTree southInfectionTree;
    private InfectionTree eastInfectionTree;
    private InfectionTree westInfectionTree;
    private InfectionTree upInfectionTree;
    private InfectionTree downInfectionTree;


    public SculkNodeInfectionHandler(SculkNodeBlockEntity parent) {
        this.parent = parent;
        this.world = (ServerLevel) parent.getLevel();
        this.origin = parent.getBlockPos();

        northInfectionTree = new InfectionTree(world, Direction.NORTH, origin);
        northInfectionTree.activate();

        southInfectionTree = new InfectionTree(world, Direction.SOUTH, origin);
        southInfectionTree.activate();

        eastInfectionTree = new InfectionTree(world, Direction.EAST, origin);
        eastInfectionTree.activate();

        westInfectionTree = new InfectionTree(world, Direction.WEST, origin);
        westInfectionTree.activate();

        upInfectionTree = new InfectionTree(world, Direction.UP, origin);
        upInfectionTree.activate();

        downInfectionTree = new InfectionTree(world, Direction.DOWN, origin);
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
