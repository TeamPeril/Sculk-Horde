package com.github.sculkhorde.common.entity;

import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.entity.LivingEntity;

public interface ISculkSmartEntity {

    TargetParameters getTargetParameters();

    LivingEntity getTarget();

    void setTarget(LivingEntity target);

    void remove();


}

