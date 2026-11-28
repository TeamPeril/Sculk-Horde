package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.Difficulty;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public class DifficultyUtil {

    public static Difficulty getCurrentDifficulty()
    {
        String modConfigDifficulty = ModConfig.SERVER.difficulty_mode.get();

        // Edge case this gets called on client
        if(ServerLifecycleHooks.getCurrentServer() == null)
        {
            SculkHorde.LOGGER.error("DifficultyUtil | ERROR: getCurrentDifficulty getting called on client, rather than server.");
            return Difficulty.NORMAL;
        }


        if(modConfigDifficulty.equals("EASY"))
        {
            return Difficulty.EASY;
        }
        else if(modConfigDifficulty.equals("NORMAL"))
        {
            return Difficulty.NORMAL;
        }
        else if(modConfigDifficulty.equals("HARD"))
        {
            return Difficulty.HARD;
        }
        else
        {
            return ServerLifecycleHooks.getCurrentServer().overworld().getDifficulty();
        }


    }

    public static boolean isCurrentDifficultyEqualToOrGreaterThan(Difficulty difficulty)
    {
        return getCurrentDifficulty().getId() >= difficulty.getId();
    }

    public static boolean isCurrentDifficultyGreaterThanEasy()
    {
        return getCurrentDifficulty().getId() > Difficulty.EASY.getId();
    }

    public static boolean isCurrentDifficultyEasy()
    {
        return getCurrentDifficulty().getId() == Difficulty.EASY.getId();
    }

    public static boolean isCurrentDifficultyLessThanNormal()
    {
        return getCurrentDifficulty().getId() < Difficulty.NORMAL.getId();
    }

    public static boolean isCurrentDifficultyGreaterThanNormal()
    {
        return getCurrentDifficulty().getId() > Difficulty.NORMAL.getId();
    }

    public static boolean isCurrentDifficultyNormal()
    {
        return getCurrentDifficulty().getId() == Difficulty.NORMAL.getId();
    }

    public static boolean isCurrentDifficultyLessThanHard()
    {
        return getCurrentDifficulty().getId() < Difficulty.HARD.getId();
    }

    public static boolean isCurrentDifficultyHard()
    {
        return getCurrentDifficulty().getId() == Difficulty.HARD.getId();
    }


}
