package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.procedural.structures.SculkLivingRockProceduralStructure;
import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.state.BlockState;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkLivingRockRootTile extends BlockEntity
{
    private long tickedAt = System.nanoTime();

    private SculkLivingRockProceduralStructure proceduralStructure;

    //Repair routine will restart after an hour
    private final long repairIntervalInMinutes = 60;
    //Keep track of last time since repair so we know when to restart
    private long lastTimeSinceRepair = -1;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkLivingRockRootTile(BlockPos blockPos, BlockState blockState)
    {
        super(TileEntityRegistry.SCULK_LIVING_ROCK_ROOT_TILE.get(), blockPos, blockState);
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    public void tick()
    {
        if(this.level == null || this.level.isClientSide)
        {
            return;
        }

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - tickedAt, TimeUnit.NANOSECONDS);
        if(timeElapsed < 0.1) { return;}

        tickedAt = System.nanoTime();

        /** Building Shell Process **/
        long repairTimeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - lastTimeSinceRepair, TimeUnit.NANOSECONDS);

        //If the Bee Nest Structure hasnt been initialized yet, do it
        if(proceduralStructure == null)
        {
            //Create Structure
            proceduralStructure = new SculkLivingRockProceduralStructure((ServerLevel) this.level, this.getBlockPos());
            proceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(proceduralStructure.isCurrentlyBuilding())
        {
            proceduralStructure.buildTick();
            lastTimeSinceRepair = System.nanoTime();
        }
        //If enough time has passed, or we havent built yet, start build
        else if(repairTimeElapsed >= repairIntervalInMinutes || lastTimeSinceRepair == -1)
        {
            proceduralStructure.startBuildProcedure();
        }
    }
}
