package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class FleshyCompostBlockEntity extends BlockEntity {

    /**
     * storedSculkMass is the value of sculk mass was this block has.
     * This value is used to determine the mobs that are spawened or the area
     * that will be infected by the sculk.
     * storedSculkMassIdentifier is the string used to identify storedSculkMass
     * in CompoundNBT. It allows us to read/write to it.<br>
     */
    protected int storedSculkMass = 0;
    protected String storedSculkMassIdentifier = "storedSculkMass";

    protected long lastTickTime = 0;

    protected int tickInterval = TickUnits.convertSecondsToTicks(3);


    /**
     * The Constructor that takes in properties
     */
    public FleshyCompostBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLESHY_COMPOST_BLOCK_ENTITY.get(), pos, state);
    }

    /**
     * Called when loading block entity from world.
     * @param compoundNBT Where NBT data is stored.
     */
    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
        this.storedSculkMass = compoundNBT.getInt(storedSculkMassIdentifier);
    }

    /**
     * ???
     * @param compoundNBT Where NBT data is stored??
     * @return ???
     */
    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        compoundNBT.putInt(storedSculkMassIdentifier, this.storedSculkMass);
        super.saveAdditional(compoundNBT);
    }

    public int getStoredSculkMass()
    {
        return storedSculkMass;
    }

    public void setStoredSculkMass(int value)
    {
        storedSculkMass = Math.max(0, value);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, FleshyCompostBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide)
        {
            return;
        }
        // Tick every 10 seconds
        if(level.getGameTime() - blockEntity.lastTickTime < blockEntity.tickInterval)
        {
            return;
        }
        blockEntity.lastTickTime = level.getGameTime();

        int massTribute = Math.min(5, blockEntity.getStoredSculkMass());


        SculkHorde.savedData.addSculkAccumulatedMass(massTribute);
        SculkHorde.statisticsData.addTotalMassFromFleshyCompost(massTribute);
        blockEntity.setStoredSculkMass(blockEntity.getStoredSculkMass() - massTribute);
        ((ServerLevel)level).sendParticles(ParticleTypes.SCULK_SOUL, blockPos.getX() + 0.5D, blockPos.getY() + 1.15D, blockPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        ((ServerLevel)level).playSound(null, blockPos, SoundEvents.PLAYER_BURP, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);

        if(blockEntity.getStoredSculkMass() <= 0)
        {
            level.setBlockAndUpdate(blockPos, Blocks.AIR.defaultBlockState());
        }

    }


}
