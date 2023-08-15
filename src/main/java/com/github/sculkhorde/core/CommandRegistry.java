package com.github.sculkhorde.core;

import com.github.sculkhorde.common.command.SculkHordeCommand;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegisterCommandsEvent;
import net.minecraftforge.eventbus.api.EventPriority;

public class CommandRegistry {

    public static void init() {
        MinecraftForge.EVENT_BUS.addListener(EventPriority.NORMAL, false, RegisterCommandsEvent.class, CommandRegistry::registerCommands);
    }

    private static void registerCommands(final RegisterCommandsEvent ev) {
        registerSubCommands(ev.getDispatcher(), ev.getBuildContext());
    }

    public static void registerSubCommands(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        LiteralArgumentBuilder<CommandSourceStack> cmd = Commands.literal(SculkHorde.MOD_ID)
                .then(SculkHordeCommand.register(dispatcher, buildContext));

        dispatcher.register(cmd);
    }
}
