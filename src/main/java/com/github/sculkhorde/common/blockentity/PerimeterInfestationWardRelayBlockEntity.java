package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.PerimeterInfestationWardRelayBlock;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.client.particle.Particle;
import net.minecraft.client.particle.ParticleProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

public class PerimeterInfestationWardRelayBlockEntity extends BlockEntity {

    protected long lastTickTime = 0;

    protected int tickInterval = TickUnits.convertSecondsToTicks(3);

    Optional<BlockPos> parentRelayPos = Optional.empty();
    Optional<BlockPos> previousRelayPos = Optional.empty();
    Optional<BlockPos> nextRelayPos = Optional.empty();

    public boolean isRelayingWard = false;


    /**
     * The Constructor that takes in properties
     */
    public PerimeterInfestationWardRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.GOLEM_OF_WRATH_ANIMATOR_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, PerimeterInfestationWardRelayBlockEntity blockEntity)
    {
        // If world is not a server world, return
        if(level.isClientSide)
        {
            return;
        }
        if(level.getGameTime() - blockEntity.lastTickTime < blockEntity.tickInterval)
        {
            return;
        }
        blockEntity.lastTickTime = level.getGameTime();

        blockEntity.verifyConnection();
        blockEntity.checkAndSetRelay();
        blockEntity.drawParticlesFromPreviousRelay();

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
                return Optional.of(checkPos);
            }
        }
        return Optional.empty();
    }

    public void checkAndSetRelay()
    {
        if(nextRelayPos.isEmpty())
        {
            nextRelayPos = findNextRelay();
            return;
        }

        BlockState nextRelayState = level.getBlockState(nextRelayPos.get());
        if(nextRelayState.getBlock() instanceof PerimeterInfestationWardRelayBlock &&
                level.getBlockEntity(nextRelayPos.get()) instanceof PerimeterInfestationWardRelayBlockEntity nextRelayBlockEntity &&
        nextRelayBlockEntity.previousRelayPos.isEmpty())
        {
            nextRelayBlockEntity.previousRelayPos = Optional.of(getBlockPos());
            nextRelayBlockEntity.parentRelayPos = this.parentRelayPos;
            DebuggerSystem.cursorDebuggerModule.logDebug("Relay at " + getBlockPos().toShortString() + " found next relay at " + nextRelayPos.get().toShortString());
        }
    }

    public boolean verifyConnection()
    {
        if(previousRelayPos.isEmpty())
        {
            return false;
        }

        BlockState previousRelayState = level.getBlockState(previousRelayPos.get());

        if(!(previousRelayState.getBlock() instanceof PerimeterInfestationWardRelayBlock))
        {
            return false;
        }

        PerimeterInfestationWardRelayBlockEntity previousRelayBlockEntity = (PerimeterInfestationWardRelayBlockEntity) level.getBlockEntity(previousRelayPos.get());


        isRelayingWard = previousRelayBlockEntity.isRelayingWard;
        return true;
    }

    public void drawParticlesFromPreviousRelay() {
        if (previousRelayPos.isEmpty()) {
            return;
        }

        BlockState previousRelayState = level.getBlockState(previousRelayPos.get());

        if (!(previousRelayState.getBlock() instanceof PerimeterInfestationWardRelayBlock)) {
            return;
        }

        PerimeterInfestationWardRelayBlockEntity previousRelayBlockEntity = (PerimeterInfestationWardRelayBlockEntity) level.getBlockEntity(previousRelayPos.get());

        // Draw particles from previous relay to this relay
        if (previousRelayBlockEntity.isRelayingWard) {
            ParticleUtil.spawnParticleBeam((ServerLevel) level, ParticleTypes.END_ROD, getBlockPos().getCenter(), previousRelayPos.get().getCenter(), 0.5F, 5);
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


    @Override
    public void load(CompoundTag compoundNBT) {
        super.load(compoundNBT);
    }

    @Override
    public void saveAdditional(CompoundTag compoundNBT) {

        super.saveAdditional(compoundNBT);
    }
}
