package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkBeeNestCellTile extends TileEntity implements ITickableTileEntity {

    private boolean isMature = false;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkBeeNestCellTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public SculkBeeNestCellTile() {
        this(TileEntityRegistry.SCULK_BEE_NEST_CELL_TILE.get());
    }


    /** Accessors **/

    public boolean isMature()
    {
        return isMature;
    }

    /** Modifiers **/

    public void setMature()
    {
        isMature = true;
    }

    public void setImmature()
    {
        isMature = false;
    }

    /** Events **/
    @Override
    public void tick()
    {

    }

}
