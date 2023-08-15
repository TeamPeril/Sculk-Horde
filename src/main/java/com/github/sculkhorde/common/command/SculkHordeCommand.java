package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.LocaleUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;

public class SculkHordeCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("mass")
                .then(Commands.literal("add").requires(command -> command.hasPermission(4))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000))
                                .executes((context -> adjustMass(context, "add"))
                                )))
                .then(Commands.literal("subtract").requires(command -> command.hasPermission(4))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(1, 1000000))
                                .executes((context -> adjustMass(context, "subtract"))
                                )))
                .then(Commands.literal("set").requires(command -> command.hasPermission(4))
                        .then(Commands.argument("amount", IntegerArgumentType.integer(0, 1000000))
                                .executes((context -> adjustMass(context, "set"))
                                )))
                .then(Commands.literal("get")
                        .executes((context -> adjustMass(context, "get"))
                                ));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        context.getSource().sendSuccess(()->Component.literal("Sculk Mass is Now: " + SculkHorde.savedData.getSculkAccumulatedMass()), true);

        return 0;
    }


    private static int adjustMass(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        int value;

        switch (operation) {
            case "add" -> {
                value = Math.abs(IntegerArgumentType.getInteger(context, "amount"));
                SculkHorde.savedData.addSculkAccumulatedMass(value);
                SculkHorde.gravemind.calulateCurrentState();
            }
            case "subtract" -> {
                value = Math.abs(IntegerArgumentType.getInteger(context, "amount"));
                SculkHorde.savedData.subtractSculkAccumulatedMass(value);
                SculkHorde.gravemind.calulateCurrentState();
            }
            case "set" -> {
                value = Math.abs(IntegerArgumentType.getInteger(context, "amount"));
                SculkHorde.savedData.setSculkAccumulatedMass(value);
                SculkHorde.gravemind.calulateCurrentState();
            }
        }
        context.getSource().sendSuccess(()->Component.literal("Sculk Mass is Now: " + SculkHorde.savedData.getSculkAccumulatedMass()), true);
        return 0;
    }

    private static int adjustGravemindState(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        context.getSource().sendSuccess(()->Component.literal("Gravemind is in the state: " + SculkHorde.gravemind.getEvolutionState().toString()), true);
        return 0;
    }


}
