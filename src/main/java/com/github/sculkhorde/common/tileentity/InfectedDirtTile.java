package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.block.BlockInfestation.SpreadingTile;
import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.tileentity.TileEntityType;

public class InfectedDirtTile extends SpreadingTile {

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public InfectedDirtTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public InfectedDirtTile() {
        this(TileEntityRegistry.INFECTED_DIRT_TILE.get());
    }

}
