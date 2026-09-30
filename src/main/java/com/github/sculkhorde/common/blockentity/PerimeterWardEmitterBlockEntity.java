package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.PerimeterWardEmitterBlock;
import com.github.sculkhorde.common.block.PerimeterWardRelayBlock;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.ColorUtil;
import com.github.sculkhorde.util.ParticleUtil;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.WardZoneUtil;
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

import static com.github.sculkhorde.util.WardZoneUtil.findNextRelay;
import static com.github.sculkhorde.util.WardZoneUtil.findPreviousRelay;

public class PerimeterWardEmitterBlockEntity extends PerimeterWardRelayBlockEntity {


    /**
     * The Constructor that takes in properties
     *
     * @param pos
     * @param state
     */
    public PerimeterWardEmitterBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
        parentRelayPos = Optional.of(getBlockPos());
    }

    @Override
    public boolean isRelayingWard()
    {
        return true;
    }

    @Override
    public void getSignalFromPreviousRelay()
    {

    }
    @Override
    public void setRelayingWard(boolean value)
    {

    }

    @Override
    public void updateConnections() {
        parentRelayPos = Optional.of(getBlockPos());
        super.updateConnections();
    }
}
