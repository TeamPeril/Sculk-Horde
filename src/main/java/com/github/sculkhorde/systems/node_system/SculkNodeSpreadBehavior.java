package com.github.sculkhorde.systems.node_system;

import com.github.sculkhorde.common.blockentity.SculkNodeBlockEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.node_infestation.NodeAtmosphereInfestationSystem;
import com.github.sculkhorde.systems.infestation_systems.node_infestation.NodeBranchingInfestationSystem;
import com.github.sculkhorde.util.DifficultyUtil;

public class SculkNodeSpreadBehavior extends SculkNodeBehavior {

    public static NodeBranchingInfestationSystem branchingInfestationHandler;
    public NodeAtmosphereInfestationSystem matureInfestationSystem;

    public SculkNodeSpreadBehavior(String id, SculkNodeBlockEntity node) {
        super(id, node);
    }

    public SculkNodeSpreadBehavior(SculkNodeBlockEntity node) {
        this("spread_behavior", node);
    }

    @Override
    public boolean canExecute() {
        return true;
    }

    @Override
    protected BehaviorStatus onTick() {
        InfestationHandlerTick(node);
        return BehaviorStatus.RUNNING;
    }

    @Override
    protected void onStart() {
        super.onStart();
        initializeInfectionHandler();
        matureInfestationSystem = new NodeAtmosphereInfestationSystem(node);
    }

    protected void initializeInfectionHandler()
    {
        if(branchingInfestationHandler == null)
        {
            branchingInfestationHandler = new NodeBranchingInfestationSystem(node, node.getBlockPos(), false);
            branchingInfestationHandler.spawnOnSurface = false;
        }
    }

    protected void InfestationHandlerTick(SculkNodeBlockEntity blockEntity)
    {
        if(SculkHorde.gravemind.isEvolutionInMatureState() && DifficultyUtil.isCurrentDifficultyGreaterThanEasy() && ModConfig.isExperimentalFeaturesEnabled())
        {
            matureInfestationSystem.serverTick();
            return;
        }

        // Initialize the infection handler
        if(branchingInfestationHandler == null)
        {
            initializeInfectionHandler();
        }

        if(branchingInfestationHandler.canBeActivated() && blockEntity.isActive())
        {
            branchingInfestationHandler.activate();
        }

        if(!blockEntity.isActive())
        {
            branchingInfestationHandler.deactivate();
        }

        branchingInfestationHandler.tick();
    }
}
