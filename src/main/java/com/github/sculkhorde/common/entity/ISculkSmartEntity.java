package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.entity.Mob;

public interface ISculkSmartEntity {

    default boolean canParticipatingInRaid() {
        return SculkHorde.raidHandler.isRaidActive() && BlockAlgorithms.getBlockDistanceXZ(((Mob) this).blockPosition(), SculkHorde.raidHandler.getRaidLocation()) <= SculkHorde.raidHandler.getRaidRadius() * 2;
    }

    boolean isParticipatingInRaid();

    void setParticipatingInRaid(boolean isParticipatingInRaidIn);

    TargetParameters getTargetParameters();

    boolean isIdle();
}

