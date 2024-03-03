package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.SculkHorde;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class GravemindCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("gravemind")
                .then(Commands.literal("state")
                        .then(Commands.literal("advance").requires(command -> command.hasPermission(1))
                                .executes((context -> adjustGravemindState(context, "advance"))
                                ))
                        .then(Commands.literal("deadvance").requires(command -> command.hasPermission(1))
                                .executes((context -> adjustGravemindState(context, "deadvance"))
                                ))
                        .then(Commands.literal("get")
                                .executes((context -> adjustGravemindState(context, "get"))
                                )));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int adjustGravemindState(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        switch (operation) {
            case "advance" -> {
                SculkHorde.gravemind.advanceState();
            }
            case "deadvance" -> {
                SculkHorde.gravemind.deadvanceState();
            }
        }

        context.getSource().sendSuccess(()->Component.literal("Gravemind is in the state: " + SculkHorde.gravemind.getEvolutionState().toString()), true);
        return 0;
    }


}
