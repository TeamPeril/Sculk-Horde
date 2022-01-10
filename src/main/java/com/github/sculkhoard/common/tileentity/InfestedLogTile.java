package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public class InfestedLogTile extends TileEntity implements IForgeTileEntity {

    /**
     * maxSpreadAttempts is the max number of times the InfectedDirt can
     * try to spread. It is not guaranteed that every attempt is successful. <br>
     * maxSpreadAttemptsIdentifier is the string used to identify maxSpreadAttempts
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    public boolean hasSpread = false;
    public String hasSpreadIdentifier = "hasSpread";


    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public InfestedLogTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public InfestedLogTile() {
        this(TileEntityRegistry.INFESTED_LOG_TILE.get());
    }

    /**
     * ???
     * @param blockState The blocks current blockstate
     * @param compoundNBT Where NBT data is stored??
     */
    @Override
    public void load(BlockState blockState, CompoundNBT compoundNBT) {
        super.load(blockState, compoundNBT);
        this.hasSpread = compoundNBT.getBoolean(hasSpreadIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        super.save(compoundNBT);
        compoundNBT.putBoolean(hasSpreadIdentifier, this.hasSpread);

        return compoundNBT;
    }

    /**
     * Sets the value of maxSpreadAttempts
     * @param value The value
     */
    public void setHasSpread(boolean value)
    {
        this.hasSpread = value;
    }

    /**
     * Returns the value of maxSpreadAttempts
     * @return The value
     */
    public boolean getHasSpread()
    {
        return this.hasSpread;
    }


}
