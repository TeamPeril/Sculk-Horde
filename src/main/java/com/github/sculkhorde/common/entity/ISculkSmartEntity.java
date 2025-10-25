package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.common.entity.components.TargetParameters;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.systems.event_system.EventSystem;
import com.github.sculkhorde.util.SquadHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Mob;

import java.util.Optional;

public interface ISculkSmartEntity {

    default boolean canParticipatingInRaid() {
        return EventSystem.howManyActiveRaids() > 0 && isParticipatingInRaid();
    }

    default Optional<ModSavedData.NodeEntry> getClosestNode() {
        return ModSavedData.getSaveData().getClosestNodeEntry((ServerLevel) ((Mob) this).level(), ((Mob) this).blockPosition());
    }

    default Optional<BlockPos> getClosestNodePosition() {
        if(getClosestNode().isEmpty()) { return Optional.empty(); }

        return Optional.ofNullable(getClosestNode().get().getPosition());
    }

    SquadHandler getSquad();

    boolean isParticipatingInRaid();

    void setParticipatingInRaid(boolean isParticipatingInRaidIn);

    TargetParameters getTargetParameters();

    boolean isIdle();
}

