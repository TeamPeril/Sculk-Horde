package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.procedural.structures.SculkNodeProceduralStructure;
import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.world.server.ServerWorld;

import java.util.concurrent.TimeUnit;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class DevStructureTesterTile extends TileEntity implements ITickableTileEntity
{

    private long tickedAt = System.nanoTime();

    private SculkNodeProceduralStructure proceduralStructure;


    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public DevStructureTesterTile(TileEntityType<?> type)
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
        if(timeElapsed < 10) { return;}

        tickedAt = System.nanoTime();

        /** Building Process **/

        //If the Structure hasnt been initialized yet, do it
        if(proceduralStructure == null)
        {
            //Create Structure
            proceduralStructure = new SculkNodeProceduralStructure((ServerWorld) this.level, this.getBlockPos());
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
