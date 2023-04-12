package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.procedural.structures.ProceduralStructure;
import com.github.sculkhorde.common.procedural.structures.SculkNodeCaveHallwayProceduralStructure;
import com.github.sculkhorde.common.procedural.structures.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.world.level.block.entity.TickableBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterTile extends BlockEntity implements TickableBlockEntity
{

    private long tickedAt = System.nanoTime();

    private ProceduralStructure proceduralStructure;


    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public DevStructureTesterTile(BlockEntityType<?> type)
    {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public DevStructureTesterTile() {

        this(TileEntityRegistry.DEV_STRUCTURE_TESTER_TILE.get());
    }

    /** Accessors **/


    /** Modifiers **/


    /** Events **/

    @Override
    public void tick()
    {

        if(this.level == null || this.level.isClientSide)
        {
            return;
        }

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - tickedAt, TimeUnit.NANOSECONDS);
        //if(timeElapsed < 1) { return;}

        tickedAt = System.nanoTime();

        /** Building Process **/

        //If the Structure hasnt been initialized yet, do it
        if(proceduralStructure == null)
        {
            //Create Structure
            proceduralStructure = new SculkNodeCaveHallwayProceduralStructure((ServerLevel) this.level, this.getBlockPos(), 5, 10, Direction.NORTH);
            proceduralStructure.generatePlan();
        }

        //If currently building, call build tick.
        if(!proceduralStructure.isStructureComplete() && proceduralStructure.isCurrentlyBuilding())
        {
            proceduralStructure.buildTick();
        }

        //If structure not complete, start build
        if(!proceduralStructure.isStructureComplete() && !proceduralStructure.isCurrentlyBuilding())
        {
            proceduralStructure.startBuildProcedure();
        }


    }

}
