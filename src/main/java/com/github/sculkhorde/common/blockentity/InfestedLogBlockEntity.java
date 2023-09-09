package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlockEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfaceInfectorEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import net.minecraft.core.BlockPos;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class InfestedLogBlockEntity extends BlockEntity implements ITagInfestedBlockEntity {

    /**
     * storedSculkMass is the value of sculk mass was this block has.
     * This value is used to determine the mobs that are spawened or the area
     * that will be infected by the sculk.
     * storedSculkMassIdentifier is the string used to identify storedSculkMass
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    protected BlockState storedNormalVariant = null;
    protected String storedNormalVariantIdentifier = "storedNormalVariant";


    /**
     * The Constructor that takes in properties
     */
    public InfestedLogBlockEntity(BlockPos pos, BlockState state) {
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
        this.storedNormalVariant = NbtUtils.readBlockState(level.holderLookup(Registries.BLOCK), compoundNBT.getCompound(storedNormalVariantIdentifier));
    }

    /**
     * Save Data
     * @param compoundNBT Where NBT data is stored
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.put(storedNormalVariantIdentifier, NbtUtils.writeBlockState(storedNormalVariant));
        super.saveAdditional(compoundNBT);
    }

    @Override
    public void setNormalBlockState(BlockState blockState) {
        this.storedNormalVariant = blockState;
    }

    @Override
    public BlockState getNormalBlockState() {
        return this.storedNormalVariant;
    }
}
