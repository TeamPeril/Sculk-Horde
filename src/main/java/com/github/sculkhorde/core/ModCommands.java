package com.github.sculkhorde.core;

import com.github.sculkhorde.common.command.GravemindCommand;
import com.github.sculkhorde.common.command.MassCommand;
import com.github.sculkhorde.common.command.StatusAllCommand;
import com.github.sculkhorde.common.command.StatusCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public class ModCommands {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, ModCommands::registerCommands);
    }

    private static void registerCommands(final RegisterCommandsEvent ev) {
        registerSubCommands(ev.getDispatcher(), ev.getBuildContext());
    }

    public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(SculkHorde.MOD_ID)
                .then(MassCommand.register(dispatcher, buildContext))
                .then(GravemindCommand.register(dispatcher, buildContext))
                .then(StatusCommand.register(dispatcher, buildContext))
                .then(StatusAllCommand.register(dispatcher, buildContext));

        dispatcher.register(cmd);
    }
}
