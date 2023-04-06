package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.entity.LivingEntity;

public interface ISculkSmartEntity {

    TargetParameters getTargetParameters();

    boolean isIdle();

    void remove();


}

