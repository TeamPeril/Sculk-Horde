package com.github.sculkhorde.util;

import com.github.sculkhorde.core.ModConfig;
import net.minecraft.world.Difficulty;
import net.minecraftforge.server.ServerLifecycleHooks;

public class DifficultyUtil {

    public static Difficulty getCurrentDifficulty()
    {
        String modConfigDifficulty = ModConfig.SERVER.difficulty_mode.get();
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
