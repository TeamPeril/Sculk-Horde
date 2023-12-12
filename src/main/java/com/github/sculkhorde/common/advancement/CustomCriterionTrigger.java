package com.github.sculkhorde.common.advancement;

import net.minecraft.server.level.ServerPlayer;

public interface CustomCriterionTrigger {

    void trigger(ServerPlayer player);
}
