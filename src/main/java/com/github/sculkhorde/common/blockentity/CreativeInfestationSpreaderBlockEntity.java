package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.infestation_systems.node_infestation.CreativeInfestationSpreaderBranchingInfestationSystem;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CreativeInfestationSpreaderBlockEntity extends BlockEntity
{

    private long lastTickTime = 0;

    public static final int tickIntervalSeconds = 1;

    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler1;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler2;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler3;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler4;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler5;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler6;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler7;
    private CreativeInfestationSpreaderBranchingInfestationSystem infectionHandler8;

    public CreativeInfestationSpreaderBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.CREATIVE_INFESTATION_SPREADER_BLOCK_ENTITY.get(), blockPos, blockState);
    }

    /** Accessors **/

    /** Modifiers **/

    /** Events **/

    private void initializeInfectionHandler()
    {
        if(infectionHandler1 == null)
        {
            infectionHandler1 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler1.activate();
        }
        if(infectionHandler2 == null)
        {
            infectionHandler2 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler2.activate();
        }
        if(infectionHandler3 == null)
        {
            infectionHandler3 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler3.activate();
        }
        if(infectionHandler4 == null)
        {
            infectionHandler4 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler4.activate();
        }
        if(infectionHandler5 == null)
        {
            infectionHandler5 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler5.activate();
        }
        if(infectionHandler6 == null)
        {
            infectionHandler6 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler6.activate();
        }
        if(infectionHandler7 == null)
        {
            infectionHandler7 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler7.activate();
        }
        if(infectionHandler8 == null)
        {
            infectionHandler8 = new CreativeInfestationSpreaderBranchingInfestationSystem(this, getBlockPos(), false);
            infectionHandler8.activate();
        }

    }
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, CreativeInfestationSpreaderBlockEntity blockEntity)
    {
        if(level.isClientSide || !ModConfig.SERVER.block_infestation_enabled.get())
        {
            return;
        }

        // Initialize the infection handler
        if(blockEntity.infectionHandler1 == null || blockEntity.infectionHandler2 == null || blockEntity.infectionHandler3 == null || blockEntity.infectionHandler4 == null || blockEntity.infectionHandler5 == null || blockEntity.infectionHandler6 == null || blockEntity.infectionHandler7 == null || blockEntity.infectionHandler8 == null)
        {
            blockEntity.initializeInfectionHandler();
        }

        // If the time elapsed is less than the tick interval, return
        if(!TickUnits.hasTicksPassed(blockEntity.lastTickTime, level.getGameTime(), TickUnits.convertSecondsToTicks(tickIntervalSeconds)))
        {
            return;
        }

        blockEntity.infectionHandler1.tick();
        blockEntity.infectionHandler2.tick();
        blockEntity.infectionHandler3.tick();
        blockEntity.infectionHandler4.tick();
        blockEntity.infectionHandler5.tick();
        blockEntity.infectionHandler6.tick();
        blockEntity.infectionHandler7.tick();
        blockEntity.infectionHandler8.tick();

        // Update the lastTickTime time
        blockEntity.lastTickTime = level.getGameTime();

    }
}
