package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.entity.Mob;

public interface ISculkSmartEntity {

    default boolean canParticipatingInRaid() {
        return RaidHandler.isRaidActive() && BlockAlgorithms.getBlockDistance(((Mob)this).blockPosition(), RaidHandler.getRaidLocation()) <= RaidHandler.getRaidRadius() * 2;
    }
    
    boolean isParticipatingInRaid();

    void setParticipatingInRaid(boolean isParticipatingInRaidIn);

    TargetParameters getTargetParameters();

    boolean isIdle();
}

