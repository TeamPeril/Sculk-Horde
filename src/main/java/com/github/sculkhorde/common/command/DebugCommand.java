package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

import java.util.Collection;

public class DebugCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("debug")
                .then(Commands.literal("True")
                    .executes((context -> setDebugMode(context, true))
                    )
                )
                .then(Commands.literal("False")
                        .executes((context -> setDebugMode(context, false))
                        )
                )
                .then(Commands.literal("cursor")
                        .then(Commands.literal("toggle")
                                .executes((DebugCommand::toggleCursor))
                        )
                        .then(Commands.literal("debug")
                                .executes((DebugCommand::toggleCursorDebug))
                        )
                )
                .then(Commands.literal("entity")
                        .then(Commands.literal("toggle")
                                .executes((DebugCommand::toggleEntity))
                        )
                        .then(Commands.literal("debug")
                                .executes((DebugCommand::toggleEntityDebug))
                        )
                );
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private static int setDebugMode(CommandContext<CommandSourceStack> context, boolean operation) throws CommandSyntaxException {
        SculkHorde.setDebugMode(operation);
        context.getSource().sendSuccess(()->Component.literal("Debug Mode=" + SculkHorde.isDebugMode()), false);
        return 1;
    }

    private static int toggleCursor(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.cursorDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("CursorDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.cursorDebuggerModule.isActive())
        {
            DebuggerSystem.cursorDebuggerModule.setActive(false);
            context.getSource().sendSuccess(()->Component.literal("CursorDebuggerModule | Active=" + DebuggerSystem.cursorDebuggerModule.isActive()), false);
        }
        else
        {
            DebuggerSystem.cursorDebuggerModule.setActive(true);
            context.getSource().sendSuccess(()->Component.literal("CursorDebuggerModule | Active=" + DebuggerSystem.cursorDebuggerModule.isActive()), false);
        }
        return 1;
    }

    private static int toggleCursorDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.cursorDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("CursorDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.cursorDebuggerModule.isDebuggingEnabled())
        {
            DebuggerSystem.cursorDebuggerModule.setDebuggingEnabled(false);
            context.getSource().sendSuccess(()->Component.literal("CursorDebuggerModule | Debugging=" + DebuggerSystem.cursorDebuggerModule.isDebuggingEnabled()), false);
        }
        else
        {
            DebuggerSystem.cursorDebuggerModule.setDebuggingEnabled(true);
            context.getSource().sendSuccess(()->Component.literal("CursorDebuggerModule | Debugging=" + DebuggerSystem.cursorDebuggerModule.isDebuggingEnabled()), false);
        }
        return 1;
    }

    private static int toggleEntity(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.entityDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("EntityDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.entityDebuggerModule.isActive())
        {
            DebuggerSystem.entityDebuggerModule.setActive(false);
            context.getSource().sendSuccess(()->Component.literal("EntityDebuggerModule | Active=" + DebuggerSystem.entityDebuggerModule.isActive()), false);
        }
        else
        {
            DebuggerSystem.entityDebuggerModule.setActive(true);
            context.getSource().sendSuccess(()->Component.literal("EntityDebuggerModule | Active=" + DebuggerSystem.entityDebuggerModule.isActive()), false);
        }
        return 1;
    }

    private static int toggleEntityDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.entityDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("EntityDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.entityDebuggerModule.isDebuggingEnabled())
        {
            DebuggerSystem.entityDebuggerModule.setDebuggingEnabled(false);
            context.getSource().sendSuccess(()->Component.literal("EntityDebuggerModule | Debugging=" + DebuggerSystem.entityDebuggerModule.isDebuggingEnabled()), false);
        }
        else
        {
            DebuggerSystem.entityDebuggerModule.setDebuggingEnabled(true);
            context.getSource().sendSuccess(()->Component.literal("EntityDebuggerModule | Debugging=" + DebuggerSystem.entityDebuggerModule.isDebuggingEnabled()), false);
        }
        return 1;
    }



}
