package com.github.sculkhorde.common.advancement;

import com.google.common.base.Predicates;
import net.minecraft.server.level.ServerPlayer;

public interface CustomCriterionTrigger {

    void trigger(ServerPlayer player);
}
