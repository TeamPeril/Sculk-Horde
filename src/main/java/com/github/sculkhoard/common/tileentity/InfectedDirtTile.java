package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.VineBlock;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public class InfectedDirtTile extends TileEntity implements IForgeTileEntity {

    /**
     * maxSpreadAttempts is the max number of times the InfectedDirt can
     * try to spread. It is not guaranteed that every attempt is successful. <br>
     * maxSpreadAttemptsIdentifier is the string used to identify maxSpreadAttempts
     * in CompoundNBT. It allows us to read/write to it.<br>
     * spreadAttempts tracks the number of times the block has tried to spread.<br>
     * spreadAttemptsIdentifier is the string used to read/write spreadAttempts
     * from/to CompoundNBT.
     */
    public int maxSpreadAttempts = -1;
    public String maxSpreadAttemptsIdentifier = "maxSpreadAttempts";
    public int spreadAttempts = 0;
    public String spreadAttemptsIdentifier = "spreadAttempts";

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

    /**
     * ???
     * @param blockState The blocks current blockstate
     * @param compoundNBT Where NBT data is stored??
     */
    @Override
    public void load(BlockState blockState, CompoundNBT compoundNBT) {
        super.load(blockState, compoundNBT);
        this.maxSpreadAttempts = compoundNBT.getInt(maxSpreadAttemptsIdentifier);
        this.spreadAttempts = compoundNBT.getInt(spreadAttemptsIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        super.save(compoundNBT);
        compoundNBT.putInt(maxSpreadAttemptsIdentifier, this.maxSpreadAttempts);
        compoundNBT.putInt(spreadAttemptsIdentifier, this.spreadAttempts);

        return compoundNBT;
    }

    /**
     * Sets the value of maxSpreadAttempts
     * @param value The value
     */
    public void setMaxSpreadAttempts(int value)
    {
        this.maxSpreadAttempts = value;
    }

    /**
     * Sets the value of spreadAttempts
     * @param value The value
     */
    public void setSpreadAttempts(int value)
    {
        this.spreadAttempts = value;
    }

    /**
     * Returns the value of maxSpreadAttempts
     * @return The value
     */
    public int getMaxSpreadAttempts()
    {
        return this.maxSpreadAttempts;
    }

    /**
     * Returns the value of spreadAttempts
     * @return The value
     */
    public int getSpreadAttempts()
    {
        return this.spreadAttempts;
    }

}
