package com.github.sculkhoard.core;

import net.minecraft.nbt.CompoundNBT;
import net.minecraft.world.storage.WorldSavedData;

public class WorldDataSaver extends WorldSavedData {

    private static int sculkAccumilatedMass = 0;
    private static final String sculkAccumilatedMassIdentifier = "sculkAccumilatedMass";

    public WorldDataSaver(String fileName) {
        super(fileName);
    }

    @Override
    public void load(CompoundNBT compoundNBT) {
        this.sculkAccumilatedMass = compoundNBT.getInt(sculkAccumilatedMassIdentifier);
    }

    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        compoundNBT.putInt(sculkAccumilatedMassIdentifier, sculkAccumilatedMass);
        return compoundNBT;
    }


    public int getSculkAccumilatedMass()
    {
        setDirty();
        return sculkAccumilatedMass;
    }

    public void addSculkAccumilatedMass(int amount)
    {
        setDirty();
        sculkAccumilatedMass += amount;
    }

    public void subtractSculkAccumilatedMass(int amount)
    {
        setDirty();
        sculkAccumilatedMass -= amount;
    }

    public void setSculkAccumilatedMass(int amount)
    {
        setDirty();
        sculkAccumilatedMass = amount;
    }
}
