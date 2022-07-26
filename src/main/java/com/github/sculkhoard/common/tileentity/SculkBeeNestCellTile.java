package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.common.block.SculkBrainBlock;
import com.github.sculkhoard.core.TileEntityRegistry;
import com.github.sculkhoard.util.ChunkLoaderUtil;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.play.server.SUpdateTileEntityPacket;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.concurrent.TimeUnit;

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
