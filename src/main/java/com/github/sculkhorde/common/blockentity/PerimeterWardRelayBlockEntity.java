package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.PerimeterWardEmitterBlock;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.WardZoneUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.joml.Vector3f;

import java.util.Optional;

import static com.github.sculkhorde.util.WardZoneUtil.findNextRelay;
import static com.github.sculkhorde.util.WardZoneUtil.findPreviousRelay;

public class PerimeterWardRelayBlockEntity extends BlockEntity {

    public static final String parentWardBlockPosID = "parentWardBlockPos";

    protected long lastTickTime = 0;

    protected int tickInterval = TickUnits.convertSecondsToTicks(3);

    public Optional<BlockPos> parentRelayPos = Optional.empty();
    public Optional<BlockPos> previousRelayPos = Optional.empty();
    public Optional<BlockPos> nextRelayPos = Optional.empty();


    /**
     * The Constructor that takes in properties
     */
    public PerimeterWardRelayBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PERIMETER_WARD_RELAY_BLOCK_ENTITY.get(), pos, state);
    }

    public static void tick(Level level, BlockPos blockPos, BlockState blockState, PerimeterWardRelayBlockEntity blockEntity)
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

        blockEntity.updateConnections();

        blockEntity.drawParticlesFromPreviousRelay();
        blockEntity.getSignalFromPreviousRelay();

        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();
        blockEntity.spawnPurityParticlesIfRelayingWard();

    }

    public void setRelayingWard(boolean value)
    {
        if (level == null) {
            return;
        }

        BlockState blockState = getBlockState();
        if (!WardZoneUtil.canRelayWard(blockState)) {
            return;
        }

        if(isRelayingWard() != value)
        {
            level.setBlock(getBlockPos(), blockState.setValue(WardZoneUtil.IS_RELAYING_WARD, value), 3);
            setChanged();
            DebuggerSystem.cursorDebuggerModule.logDebug("PerimeterWardRelayBlock " + getBlockPos().toShortString() + " | isRelaying?=" + isRelayingWard());
        }
    }

    public boolean isRelayingWard()
    {
        if (level == null) {
            return false;
        }
        return WardZoneUtil.isBlockRelayingWard(level, getBlockPos());
    }

    public void getSignalFromPreviousRelay()
    {
        if(!isPreviousRelayValid() || previousRelayPos.isEmpty() || level == null)
        {
            return;
        }

        setRelayingWard(WardZoneUtil.isBlockRelayingWard(level, previousRelayPos.get()));
        parentRelayPos = WardZoneUtil.getParent(level, previousRelayPos.get());
    }

    public static boolean isRelayValid(Level level, BlockPos pos)
    {
        if(pos == null || level == null || level.isClientSide)
        {
            return false;
        }

        return level.getBlockEntity(pos, ModBlockEntities.PERIMETER_WARD_RELAY_BLOCK_ENTITY.get()).isPresent();
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

    public void updateConnections()
    {
        if(!isPreviousRelayValid())
        {
            previousRelayPos = Optional.empty();
            previousRelayPos = findPreviousRelay(level, getBlockPos());
            return;
        }

        if(!isNextRelayValid())
        {
            nextRelayPos = Optional.empty();
            nextRelayPos = findNextRelay(level, getBlockPos());
            return;
        }

        // If the previous relay is an emitter, then that is our new parent.
        if(previousRelayPos.isPresent() && level.getBlockState(previousRelayPos.get()).getBlock() instanceof PerimeterWardEmitterBlock)
        {
            parentRelayPos = previousRelayPos;
            DebuggerSystem.cursorDebuggerModule.logDebug("PerimeterWardRelayBlock " + getBlockPos().toShortString() + " | now has a parent " + parentRelayPos.get().toShortString());
        }
    }

    public void drawParticlesFromPreviousRelay() {
        if (!isNextRelayValid()) {
            return;
        }

        // Draw particles from previous relay to this relay
        if (isRelayingWard()) {
            ParticleUtil.spawnParticleBeam((ServerLevel) level, ParticleTypes.END_ROD, getBlockPos().getCenter(), nextRelayPos.get().getCenter(), 0.5F, 5);
        }
    }

    public void spawnPurityParticlesIfRelayingWard()
    {
        if(level == null || !isRelayingWard())
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

        if(compoundNBT.contains(parentWardBlockPosID))
        {
            parentRelayPos = Optional.of(BlockPos.of(compoundNBT.getLong(parentWardBlockPosID)));
        }
    }

    @Override
    public void saveAdditional(CompoundTag compoundNBT) {
        super.saveAdditional(compoundNBT);

        if(parentRelayPos.isPresent())
        {
            compoundNBT.putLong(parentWardBlockPosID, parentRelayPos.get().asLong());
        }
    }
}
