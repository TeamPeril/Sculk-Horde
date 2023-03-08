package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.block.BlockInfestation.SpreadingTile;
import com.github.sculkhorde.core.TileEntityRegistry;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public class InfestedStoneActiveTile extends SpreadingTile implements IForgeTileEntity {

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public InfestedStoneActiveTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public InfestedStoneActiveTile() {
        this(TileEntityRegistry.INFESTED_STONE_ACTIVE_TILE.get());
    }

}
