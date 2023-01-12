package com.github.sculkhoard.common.block.BlockInfestation;

import com.github.sculkhoard.util.BlockAlgorithms;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class SpreadingTile extends TileEntity implements ITickableTileEntity {

    /**
     * maxSpreadAttempts is the max number of times this block can
     * try to spread. It is not guaranteed that every attempt is successful. <br>
     * maxSpreadAttemptsIdentifier is the string used to identify maxSpreadAttempts
     * in CompoundNBT. It allows us to read/write to it.<br><br>
     *
     * spreadAttempts tracks the number of times the block has tried to spread.<br>
     * spreadAttemptsIdentifier is the string used to read/write spreadAttempts
     * from/to CompoundNBT. <br><br>
     *
     * chanceToNotDegrade is the chance that a spread attempt wont count towards
     * the spreadAttempts counter.<br>
     * chanceToNotDegradeIdentifier is the string used to read/write from/to CompoundNBT.
     */
    public int maxSpreadAttempts = -1;
    public String maxSpreadAttemptsIdentifier = "maxSpreadAttempts";
    public int spreadAttempts = 0;
    public String spreadAttemptsIdentifier = "spreadAttempts";
    public int chanceToNotDegrade = 1/500;
    public String chanceToNotDegradeIdentifier = "chanceToNotDegrade";

    private final int MAX_SPREAD_INVERVAL_SECONDS = 60;
    private final int MIN_SPREAD_INVERVAL_SECONDS = 30;
    private int spreadIntervalInSeconds = -1; //KEEP THIS -1
    private long tickedAt = System.nanoTime();
    private int allowedSpreadAttemptsPerTick = 1;
    boolean isSpreadingComplete = false;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SpreadingTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public SpreadingTile() {

        this(TileEntityRegistry.SPREADING_BLOCK_TILE.get());
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
        this.chanceToNotDegrade = compoundNBT.getInt(chanceToNotDegradeIdentifier);
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
        compoundNBT.putInt(chanceToNotDegradeIdentifier, this.chanceToNotDegrade);

        return compoundNBT;
    }


    /**
     * This changes the behavior of how this block spreads. If true, will choose random blocks to spread to.
     * If false, will attempt to spread to all nearby blocks.
     * @return
     */
    protected boolean doesSpreadRandomly()
    {
        return true;
    }

    /** ~~~~~~~~ Getters ~~~~~~~~ **/

    /**
     * Returns an integer representing how many times this block can initially spread.
     * @return An int
     */
    public static int getDefaultMaxSpreadAttempts()
    {
        return 30;
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

    @Override
    public void tick()
    {
        if(this.level == null || this.level.isClientSide)
        {
            return;
        }

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - tickedAt, TimeUnit.NANOSECONDS);
        if(spreadIntervalInSeconds == -1)
        {
            Random rng = new Random();
            spreadIntervalInSeconds = rng.nextInt(MAX_SPREAD_INVERVAL_SECONDS + MIN_SPREAD_INVERVAL_SECONDS) + MIN_SPREAD_INVERVAL_SECONDS;
        }
        else if(timeElapsed >= spreadIntervalInSeconds)
        {
            tickedAt = System.nanoTime();
            spreadRoutine(doesSpreadRandomly());
        }
    }


    /**
     * Will attempt to spread to nearby neighbors.
     * @param isSpreadingRandomly Should the block attempt to spread through all neighbors, or choose randomly
     */
    public void spreadRoutine(boolean isSpreadingRandomly)
    {
        if(this.level == null || this.level.isClientSide)
        {
            return;
        }
        //Once done spreading, convert to dormant variant
        if (isSpreadingComplete || SculkHoard.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0)
        {
            SculkHoard.infestationConversionTable.convertToDormant((ServerWorld) this.level, this.getBlockPos());
            return;
        }

        //If we have initialized and are out of spreading attempts
        if(getMaxSpreadAttempts() != -1 && getSpreadAttempts() >= getMaxSpreadAttempts())
        {
            isSpreadingComplete = true;
            return;
        }

        //If this block has not attempted to spread before
        if (getMaxSpreadAttempts() == -1)
        {
            setMaxSpreadAttempts(getDefaultMaxSpreadAttempts());//Set to default
        }

        //If spreading randomly and if there is sculk mass
        if (isSpreadingRandomly)
        {
            //Attempt to spread Randomly
            for (int spreadAttemptsThisTick = 0;  spreadAttemptsThisTick < allowedSpreadAttemptsPerTick && spreadAttempts < getMaxSpreadAttempts(); spreadAttemptsThisTick++)
            {
                //Attempt to spread to this position
                SculkHoard.infestationConversionTable.addToConversionQueue(BlockAlgorithms.getRandomNeighbor((ServerWorld) this.level, this.getBlockPos()), getMaxSpreadAttempts() - 1, true, false, false);
                spreadAttempts++;
            }

        }
        //If we are checking every vailid position instead of random ones
        else if (!isSpreadingRandomly)
        {
            //Get all neighbors and check if we can spread to all possible positions
            ArrayList<BlockPos> allNeighbors = BlockAlgorithms.getNeighborsCube(this.getBlockPos());
            for (int i = 0; i < allNeighbors.size(); i++)
            {
                //Attempt to spread to this position
                SculkHoard.infestationConversionTable.addToConversionQueue(allNeighbors.get(i), getMaxSpreadAttempts() - 1, true, false, false);
            }
            isSpreadingComplete = true;
        }

    }
}
