package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.entity.Mob;

public interface ISculkSmartEntity {

    default boolean canParticipatingInRaid() {
        return SculkHorde.raidHandler.isRaidActive() && isParticipatingInRaid();
    }

    boolean isParticipatingInRaid();

    void setParticipatingInRaid(boolean isParticipatingInRaidIn);

    TargetParameters getTargetParameters();

    boolean isIdle();
}

