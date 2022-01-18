package com.github.sculkhoard.core;
/**
 * Learned from: <br>
 * https://github.com/WayofTime/BloodMagic/blob/13ba49a92a03fb7a0aa2fe167cdc36ca206b60fe/src/main/java/wayoftime/bloodmagic/core/data/BMWorldSavedData.java#L13 <br>
 * https://github.com/WayofTime/BloodMagic/blob/13ba49a92a03fb7a0aa2fe167cdc36ca206b60fe/src/main/java/wayoftime/bloodmagic/util/helper/NetworkHelper.java
 */
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.World;
import net.minecraft.world.server.ServerWorld;
import net.minecraft.world.storage.DimensionSavedDataManager;
import net.minecraft.world.storage.WorldSavedData;
import javax.annotation.Nullable;

/**
 * This Class is what we use to store data in the world.
 * Storing data in the overworld allows us to have persistent
 * data since the overworld is never unloaded. <br>
 */
public class SculkWorldData extends WorldSavedData {

    /**
     * sculkAccumulatedMass is the amount of mass that the sculk hoard has accumulated.
     * sculkAccumulatedMassIdentifier is used to write/read nbt data to/from the world.
     */
    private static int sculkAccumulatedMass = 0;
    private static final String sculkAccumulatedMassIdentifier = "sculkAccumulatedMass";

    /**
     * Default Constructor
     * @param fileName The file name in the world data folder.
     */
    public SculkWorldData(String fileName) {
        super(fileName);
    }

    /**
     * Simple Constructor. This one should be the only one used.
     */
    public SculkWorldData() {
        this(SculkHoard.SAVE_DATA_ID);
    }

    /**
     * This just returns the SculkWorldData structure, if it exists yet.
     * @param world The world to check.
     * @return The Data Structure
     */
    @Nullable
    public static SculkWorldData get(World world)
    {
        if(!world.isClientSide())
        {
            DimensionSavedDataManager dataManager = ((ServerWorld) world).getDataStorage();
            return dataManager.computeIfAbsent(SculkWorldData::new, SculkHoard.SAVE_DATA_ID);
        }

        return null;
    }

    /**
     * A function called when the world is loaded.
     * @param compoundNBT The NBT Data
     */
    @Override
    public void load(CompoundNBT compoundNBT) {
        this.sculkAccumulatedMass = compoundNBT.getInt(sculkAccumulatedMassIdentifier);
    }

    /**
     * A function called when the world is saved.
     * @param compoundNBT The NBT Data
     * @return The NBT Data
     */
    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        compoundNBT.putInt(sculkAccumulatedMassIdentifier, sculkAccumulatedMass);
        return compoundNBT;
    }

    /**
     * Returns the sculkAccumulatedMass
     * @return The amount of sculk accumulated.
     */
    public int getSculkAccumulatedMass()
    {
        setDirty();
        return sculkAccumulatedMass;
    }

    /**
     * Adds to the sculk accumulated mass
     * @param amount The amount you want to add
     */
    public void addSculkAccumulatedMass(int amount)
    {
        setDirty();
        sculkAccumulatedMass += amount;
    }

    /**
     * Subtracts from the Sculk Accumulate Mass
     * @param amount The amount to substract
     */
    public void subtractSculkAccumulatedMass(int amount)
    {
        setDirty();
        sculkAccumulatedMass -= amount;
    }

    /**
     * Sets the value of sculk accumulate mass.
     * @param amount The amount to set it to.
     */
    public void setSculkAccumulatedMass(int amount)
    {
        setDirty();
        sculkAccumulatedMass = amount;
    }
}
