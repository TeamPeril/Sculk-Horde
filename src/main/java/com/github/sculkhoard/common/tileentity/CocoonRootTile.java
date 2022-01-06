package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.core.TileEntityRegistry;
import net.minecraft.block.BlockState;
import net.minecraft.block.SpawnerBlock;
import net.minecraft.entity.LivingEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.common.extensions.IForgeTileEntity;

public class CocoonRootTile extends TileEntity implements IForgeTileEntity {

    /**
     * spawnAmount is the number of mobs spawn at one time. <br>
     * maximumAlive is the amount of mobs from this spawner that can be alive.
     * If this maximum is reached, the spawner will stop spawning.<br>
     * spawnCooldown is how much time before the spawner spawns another wave.<br>
     * activationDistance the minimum distance the player needs to be in proximity
     * with this spawner for it to turn on.
     */
    public int spawnAmount = 5;
    public int maximumAlive = 30;
    public int spawnCooldown = 200; //20 Ticks per second
    public int activationDistance = 64;
    public String spawnAmountIdentifier = "spawnAmount";
    public String maximumAliveIdentifier = "maximumAlive";
    public String spawnCooldownIdentifier = "spawnCooldown";
    public String activationDistanceIdentifier = "activationDistance";
    public LivingEntity[] spawnedMobs = {};
    public int cooldownRemaining = spawnCooldown;

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public CocoonRootTile(TileEntityType<?> type) {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public CocoonRootTile() {
        this(TileEntityRegistry.COCOON_ROOT_TILE.get());
    }

    /**
     * ???
     * @param blockState The blocks current blockstate
     * @param compoundNBT Where NBT data is stored??
     */
    @Override
    public void load(BlockState blockState, CompoundNBT compoundNBT) {
        super.load(blockState, compoundNBT);
        this.spawnAmount = compoundNBT.getInt(spawnAmountIdentifier);
        this.maximumAlive = compoundNBT.getInt(maximumAliveIdentifier);
        this.spawnCooldown = compoundNBT.getInt(spawnCooldownIdentifier);
        this.activationDistance = compoundNBT.getInt(activationDistanceIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public CompoundNBT save(CompoundNBT compoundNBT) {
        super.save(compoundNBT);
        compoundNBT.putInt(spawnAmountIdentifier, this.spawnAmount);
        compoundNBT.putInt(maximumAliveIdentifier, this.maximumAlive);
        compoundNBT.putInt(spawnCooldownIdentifier, this.spawnCooldown);
        compoundNBT.putInt(activationDistanceIdentifier, this.activationDistance);
        return compoundNBT;
    }

}
