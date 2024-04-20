package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.SculkHorde;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StatisticsCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("statistics")
                .executes(new StatisticsCommand());

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendSuccess(()->Component.literal(
                printIfNotZero("Total Blocks Infested", SculkHorde.statisticsData.getTotalBlocksInfested())
                        + printIfNotZero("Total Units Spawned", SculkHorde.statisticsData.getTotalUnitsSpawned())
                        + printIfNotZero("Total Units Killed", SculkHorde.statisticsData.getTotalUnitDeaths())
                        + printIfNotZero("Total Victims Infested", SculkHorde.statisticsData.getTotalVictimsInfested())
                        + printIfNotZero("Total Mass Gained From Burrowed", SculkHorde.statisticsData.getTotalMassFromBurrowed())
                        + printIfNotZero("Total Mass Gained From Diseased Cysts", SculkHorde.statisticsData.getTotalMassFromDiseasedCysts())
                        + printIfNotZero("Total Mass Gained From Bees", SculkHorde.statisticsData.getTotalMassFromBees())
                        + printIfNotZero("Total Mass From Nodes", SculkHorde.statisticsData.getTotalMassFromNodes())
                        + printIfNotZero("Total Mass From Fleshy Compost", SculkHorde.statisticsData.getTotalMassFromFleshyCompost())
                ), false);
        return 0;
    }

    private String printIfNotZero(String name, long stat)
    {
        if(stat == 0)
        {
            return "";
        }
        else
        {
            return name + ": " + String.valueOf(stat) + "\n";
        }
    }

}
