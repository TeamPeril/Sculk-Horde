package com.github.sculkhorde.core;

import com.github.sculkhorde.common.command.*;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.RegisterCommandsEvent;
import net.neoforged.bus.api.EventPriority;

public class ModCommands {

    public static void init() {
        NeoForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, ModCommands::registerCommands);
    }

    private static void registerCommands(final RegisterCommandsEvent ev) {
        registerSubCommands(ev.getDispatcher(), ev.getBuildContext());
    }

    public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> sculkHordeCommands = Commands.literal(SculkHorde.MOD_ID)
                .then(MassCommand.register(dispatcher, buildContext))
                .then(GravemindCommand.register(dispatcher, buildContext))
                .then(StatusCommand.register(dispatcher, buildContext))
                .then(StatusAllCommand.register(dispatcher, buildContext))
                .then(StatisticsCommand.register(dispatcher, buildContext))
                .then(PlayerStatusCommand.register(dispatcher, buildContext))
                .then(ConfigCommand.register(dispatcher, buildContext))
                .then(NodesStatusCommand.register(dispatcher, buildContext));


        LiteralArgumentBuilder<CommandSourceStack> sculkHordeDevCommands = Commands.literal("shd")
                .requires(command -> command.hasPermission(2))
                .then(DebugCommand.register(dispatcher, buildContext))
                .then(InfestChunkCommand.register(dispatcher, buildContext))
                .then(InfestChunksCommand.register(dispatcher, buildContext))
                .then(VesselCommand.register(dispatcher, buildContext))
                .then(ResetCommand.register(dispatcher, buildContext))
                .then(DevCommand.register(dispatcher, buildContext))
                .then(SummonReinforcementsCommand.register(dispatcher, buildContext))
                .then(RaidCommand.register(dispatcher, buildContext))
                .then(SoulReaperCommand.register(dispatcher, buildContext))
                .then(HitSquadCommand.register(dispatcher, buildContext))
                .then(NodeCommand.register(dispatcher, buildContext))
                .then(ClearCursorsCommand.register(dispatcher, buildContext));

        dispatcher.register(sculkHordeCommands);
        dispatcher.register(sculkHordeDevCommands);
    }
}
