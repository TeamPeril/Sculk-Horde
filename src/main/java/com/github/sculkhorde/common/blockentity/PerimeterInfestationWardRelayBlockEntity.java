package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.PerimeterInfestationWardRelayBlock;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.PerimeterInfestationWardZoneUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Optional;

public class PerimeterInfestationWardRelayBlockEntity extends BlockEntity {

    protected long lastTickTime = 0;

    protected int tickInterval = TickUnits.convertSecondsToTicks(3);

    public Optional<BlockPos> parentRelayPos = Optional.empty();
    public Optional<BlockPos> previousRelayPos = Optional.empty();
    public Optional<BlockPos> nextRelayPos = Optional.empty();
    public boolean isRelayingWard = false;


    /**
     * The Constructor that takes in properties
     */
    public PerimeterInfestationWardRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERIMETER_INFESTATION_WARD_RELAY_BLOCK_ENTITY.get(), pos, state);
    }

    public boolean areWeTheParent()
    {
        if(parentRelayPos.isEmpty())
        {
            return true;
        }


        return parentRelayPos.get().equals(getBlockPos());
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, PerimeterInfestationWardRelayBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide && blockEntity == null || blockEntity.level == null)
        {
            return;
        }
        if(!TickUnits.hasTicksPassed(blockEntity.lastTickTime, blockEntity.level, blockEntity.tickInterval))
        {
            return;
        }
        blockEntity.lastTickTime = level.getGameTime();

        blockEntity.verifyAndUpdateConnection();

        if(blockEntity.areWeTheParent())
        {
            if(!PerimeterInfestationWardZoneUtil.doesZoneExist(blockEntity.getBlockPos()))
            {
                PerimeterInfestationWardZoneUtil.getOrCreatePerimeterInfestationWardZone(blockEntity.getBlockPos());
            }
        }

        blockEntity.drawParticlesFromPreviousRelay();
        blockEntity.relaySignalToNextRelay();

        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();

    }


    /**
     * Will check the facing direction for a max of 32 blocks for another relay block.
     * If it finds one, it will return the position of that block.
     * If it doesn't find one, it will return null.
     * @return The position of the previous relay block, or null if it doesn't find one.
     */
    public Optional<BlockPos> findNextRelay()
    {
        Direction facingDirection = this.getBlockState().getValue(PerimeterInfestationWardRelayBlock.FACING);
        for(int i = 1; i <= 32; i++)
        {
            BlockPos checkPos = worldPosition.relative(facingDirection, i);
            if(level.getBlockState(checkPos).getBlock() instanceof PerimeterInfestationWardRelayBlock)
            {
                DebuggerSystem.cursorDebuggerModule.logDebug("Relay at " + getBlockPos().toShortString() + " found next relay at " + checkPos.toShortString());
                return Optional.of(checkPos);
            }
        }
        return Optional.empty();
    }

    public void relaySignalToNextRelay()
    {
        if(getNextRelayBlockEntity().isEmpty())
        {
            return;
        }

        PerimeterInfestationWardRelayBlockEntity nextRelay = getNextRelayBlockEntity().get();

        nextRelay.previousRelayPos = Optional.of(getBlockPos());
        nextRelay.parentRelayPos = this.parentRelayPos;
        nextRelay.isRelayingWard = isRelayingWard;
        DebuggerSystem.cursorDebuggerModule.logDebug("Relay " + getBlockPos().toShortString() + " is relaying it's power.");
    }

    public static boolean isRelayValid(Level level, BlockPos pos)
    {
        if(pos == null || level == null || level.isClientSide)
        {
            return false;
        }

        return level.getBlockEntity(pos, ModBlockEntities.PERIMETER_INFESTATION_WARD_RELAY_BLOCK_ENTITY.get()).isPresent();
    }

    public boolean isNextRelayValid()
    {
        if(nextRelayPos.isEmpty())
        {
            return false;
        }

        return isRelayValid(getLevel(), nextRelayPos.get());
    }

    public boolean isPreviousRelayValid()
    {
        if(previousRelayPos.isEmpty())
        {
            return false;
        }

        return isRelayValid(getLevel(), previousRelayPos.get());
    }

    public Optional<PerimeterInfestationWardRelayBlockEntity> getNextRelayBlockEntity()
    {
        if(isNextRelayValid())
        {
            return Optional.of((PerimeterInfestationWardRelayBlockEntity) level.getBlockEntity(nextRelayPos.get()));
        }
        return Optional.empty();
    }

    public Optional<PerimeterInfestationWardRelayBlockEntity> getPreviousRelayBlockEntity()
    {
        if(isPreviousRelayValid())
        {
            return Optional.of((PerimeterInfestationWardRelayBlockEntity) level.getBlockEntity(previousRelayPos.get()));
        }
        return Optional.empty();
    }

    public void checkAndSetNextRelay()
    {
        if(!isNextRelayValid())
        {
            nextRelayPos = findNextRelay();
            return;
        }
    }

    public void verifyAndUpdateConnection()
    {
        if(!isPreviousRelayValid())
        {
            previousRelayPos = Optional.empty();
            isRelayingWard = isPoweredByRedstone();
            //DebuggerSystem.cursorDebuggerModule.logDebug("Relay " + getBlockPos().toShortString() + "'s Previous Relay is no longer valid.");
        }

        if(!isNextRelayValid())
        {
            nextRelayPos = Optional.empty();
        }

        checkAndSetNextRelay();


    }

    public void drawParticlesFromPreviousRelay() {
        if (!isNextRelayValid()) {
            return;
        }

        PerimeterInfestationWardRelayBlockEntity nextRelayBlockEntity = getNextRelayBlockEntity().get();

        // Draw particles from previous relay to this relay
        if (isRelayingWard) {
            ParticleUtil.spawnParticleBeam((ServerLevel) level, ParticleTypes.END_ROD, getBlockPos().getCenter(), nextRelayBlockEntity.worldPosition.getCenter(), 0.5F, 5);
        }
    }


    public boolean isPoweredByRedstone()
    {
        if(level == null)
        {
            return false;
        }
        boolean powered = level.hasNeighborSignal(worldPosition);
        if(powered)
        {
            // If the block is powered by redstone, do something
            DebuggerSystem.cursorDebuggerModule.logDebug("Perimeter Infestation Ward Relay Block at " + worldPosition + " is powered by redstone.");
        }

        return powered;
    }

    public void spawnPurityParticlesIfRelayingWard()
    {
        if(level == null || !isRelayingWard)
        {
            return;
        }
        //Get random spawnX, y, and z, around block position
        Vector3f spawnPos = new Vector3f(
                (float) worldPosition.getX() + (level.getRandom().nextFloat() * 2),
                (float) worldPosition.getY() + (level.getRandom().nextFloat() * 2),
                (float) worldPosition.getZ() + (level.getRandom().nextFloat() * 2)
        );

        ParticleUtil.spawnColoredDustParticleOnServer((ServerLevel) level, ColorUtil.getRandomPurityColor(level.getRandom()), 1.0F, spawnPos);
    }



    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
    }

    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        super.saveAdditional(compoundNBT);
    }
}
