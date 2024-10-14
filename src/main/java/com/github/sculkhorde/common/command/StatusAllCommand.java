package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.TPSHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class StatusAllCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("statusall")
                .executes(new StatusAllCommand());

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendSuccess(()->Component.literal(
                "Horde State: " + SculkHorde.savedData.getHordeState().toString()
                        + "\n"
                        + "Gravemind State: " + SculkHorde.gravemind.getEvolutionState().toString()
                        + "\n"
                        + "Sculk Mass Accumulated: " + SculkHorde.savedData.getSculkAccumulatedMass()
                        + "\n"
                        + "Sculk Nodes Present: " + SculkHorde.savedData.getNodeEntries().size()
                        + "\n"
                        + "Nests Count: " + SculkHorde.savedData.getBeeNestEntries().size()
                        + "\n"
                        + "Mob Types Considered Hostile Count: " + SculkHorde.savedData.getHostileEntries().size()
                        + "\n"
                        + "Death Area Reports Count: " + SculkHorde.savedData.getDeathAreaEntries().size()
                        + "\n"
                        + "Areas of Interest Count: " + SculkHorde.savedData.getAreasOfInterestEntries().size()
                        + "\n"
                        + "No Raid Zone Entries Count: " + SculkHorde.savedData.getNoRaidZoneEntries().size()
                        + "\n"
                        + "Entity Chunk Load Requests: " + SculkHorde.entityChunkLoaderHelper.getEntityChunkLoadRequests().size()
                        + "\n"
                        + "BlockEntity Chunk load Requests: " + SculkHorde.blockEntityChunkLoaderHelper.getBlockChunkLoadRequests().size()
                        + "\n"
                        + "Events in Queue: " + SculkHorde.eventHandler.getEvents().size()
                        + "\n"
                        + "Performance Mode Enabled?: " + SculkHorde.cursorHandler.isManualControlOfTickingEnabled()
                        + "\n"
                        + "Performance Mode Cursor Threshold?: " + ModConfig.SERVER.performance_mode_cursor_threshold.get()
                        + "\n"
                        + "Performance Mode TPS Threshold?: " + TPSHandler.getTps() + " / 15 TPS"
                        + "\n"
                        + "Cursors being Ticked: " + SculkHorde.cursorHandler.getSizeOfCursorList() + " / " + ModConfig.SERVER.max_infector_cursor_population.get()
                        + "\n"
                        + "Sculk Unit Population: " + SculkHorde.populationHandler.getPopulationSize() + " / " + SculkHorde.populationHandler.getMaxPopulation()
                ), false);
        return 0;
    }

}
