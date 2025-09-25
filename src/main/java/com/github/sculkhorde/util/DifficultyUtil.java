package com.github.sculkhorde.util;

import net.minecraft.world.Difficulty;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DifficultyUtil {

    public static Difficulty getCurrentDifficulty()
    {
        return ServerLifecycleHooks.getCurrentServer().overworld().getDifficulty();
    }

    public static boolean isCurrentDifficultyEqualToOrGreaterThan(Difficulty difficulty)
    {
        return getCurrentDifficulty().getId() >= difficulty.getId();
    }


}
