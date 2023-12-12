package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlockEntity;
import com.github.sculkhorde.core.ModBlockEntities;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedTagBlockEntity extends BlockEntity implements ITagInfestedBlockEntity {

    /**
     * storedSculkMass is the value of sculk mass was this block has.
     * This value is used to determine the mobs that are spawened or the area
     * that will be infected by the sculk.
     * storedSculkMassIdentifier is the string used to identify storedSculkMass
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    protected BlockState storedNormalVariant = Blocks.OAK_LOG.defaultBlockState();
    protected String storedNormalVariantIdentifier = "storedNormalVariant";


    /**
     * The Constructor that takes in properties
     */
    public InfestedTagBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.INFESTED_LOG_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        if(!compoundNBT.contains(storedNormalVariantIdentifier))
        {
            return;
        }
        // This insufferable piece of code gave me a fucking headache. I borrowed this from piston code. For whatever reason, it works.
        // This is needed because level is null on load and this allows it to save data properly
        setNormalBlockState(NbtUtils.readBlockState(compoundNBT.getCompound(storedNormalVariantIdentifier)));
        //SculkHorde.LOGGER.debug("Infested Log Loaded State: " + storedNormalVariant.toString());
    }

    /**
     * Save Data
     * @param compoundNBT Where NBT data is stored
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.put(storedNormalVariantIdentifier, NbtUtils.writeBlockState(getNormalBlockState()));
        super.saveAdditional(compoundNBT);
        //SculkHorde.LOGGER.debug("Infested Log Saved State: " + NbtUtils.writeBlockState(storedNormalVariant));
    }

    @Override
    public void setNormalBlockState(BlockState blockState) {
        this.storedNormalVariant = blockState;
        //SculkHorde.LOGGER.debug("setNormalBlockState: " + blockState.toString());
    }

    @Override
    public BlockState getNormalBlockState() {
        //SculkHorde.LOGGER.debug("getNormalBlockState: " + storedNormalVariant);
        return this.storedNormalVariant;
    }
}
