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
                )
                .then(Commands.literal("chunk")
                        .then(Commands.literal("toggle")
                                .executes((DebugCommand::toggleChunk))
                        )
                        .then(Commands.literal("debug")
                                .executes((DebugCommand::toggleChunkDebug))
                        )
                )
                .then(Commands.literal("event")
                        .then(Commands.literal("toggle")
                                .executes((DebugCommand::toggleEvent))
                        )
                        .then(Commands.literal("debug")
                                .executes((DebugCommand::toggleEventDebug))
                        )
                )
                .then(Commands.literal("structure")
                        .then(Commands.literal("toggle")
                                .executes((DebugCommand::toggleStructure))
                        )
                        .then(Commands.literal("debug")
                                .executes((DebugCommand::toggleStructure))
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

    private static int toggleChunk(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.chunkLoaderDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("ChunkLoaderDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.chunkLoaderDebuggerModule.isActive())
        {
            DebuggerSystem.chunkLoaderDebuggerModule.setActive(false);
            context.getSource().sendSuccess(()->Component.literal("ChunkLoaderDebuggerModule | Active=" + DebuggerSystem.chunkLoaderDebuggerModule.isActive()), false);
        }
        else
        {
            DebuggerSystem.chunkLoaderDebuggerModule.setActive(true);
            context.getSource().sendSuccess(()->Component.literal("ChunkLoaderDebuggerModule | Active=" + DebuggerSystem.chunkLoaderDebuggerModule.isActive()), false);
        }
        return 1;
    }

    private static int toggleChunkDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.chunkLoaderDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("ChunkLoaderDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.chunkLoaderDebuggerModule.isDebuggingEnabled())
        {
            DebuggerSystem.chunkLoaderDebuggerModule.setDebuggingEnabled(false);
            context.getSource().sendSuccess(()->Component.literal("ChunkLoaderDebuggerModule | Debugging=" + DebuggerSystem.chunkLoaderDebuggerModule.isDebuggingEnabled()), false);
        }
        else
        {
            DebuggerSystem.chunkLoaderDebuggerModule.setDebuggingEnabled(true);
            context.getSource().sendSuccess(()->Component.literal("ChunkLoaderDebuggerModule | Debugging=" + DebuggerSystem.chunkLoaderDebuggerModule.isDebuggingEnabled()), false);
        }
        return 1;
    }

    private static int toggleEvent(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.eventDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("EventDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.eventDebuggerModule.isActive())
        {
            DebuggerSystem.eventDebuggerModule.setActive(false);
            context.getSource().sendSuccess(()->Component.literal("EventDebuggerModule | Active=" + DebuggerSystem.eventDebuggerModule.isActive()), false);
        }
        else
        {
            DebuggerSystem.eventDebuggerModule.setActive(true);
            context.getSource().sendSuccess(()->Component.literal("EventDebuggerModule | Active=" + DebuggerSystem.eventDebuggerModule.isActive()), false);
        }
        return 1;
    }

    private static int toggleEventDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.eventDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("EventDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.eventDebuggerModule.isDebuggingEnabled())
        {
            DebuggerSystem.eventDebuggerModule.setDebuggingEnabled(false);
            context.getSource().sendSuccess(()->Component.literal("EventDebuggerModule | Debugging=" + DebuggerSystem.eventDebuggerModule.isDebuggingEnabled()), false);
        }
        else
        {
            DebuggerSystem.eventDebuggerModule.setDebuggingEnabled(true);
            context.getSource().sendSuccess(()->Component.literal("EventDebuggerModule | Debugging=" + DebuggerSystem.eventDebuggerModule.isDebuggingEnabled()), false);
        }
        return 1;
    }

    private static int toggleStructure(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.structureDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("StructureDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.structureDebuggerModule.isActive())
        {
            DebuggerSystem.structureDebuggerModule.setActive(false);
            context.getSource().sendSuccess(()->Component.literal("StructureDebuggerModule | Active=" + DebuggerSystem.structureDebuggerModule.isActive()), false);
        }
        else
        {
            DebuggerSystem.structureDebuggerModule.setActive(true);
            context.getSource().sendSuccess(()->Component.literal("StructureDebuggerModule | Active=" + DebuggerSystem.structureDebuggerModule.isActive()), false);
        }
        return 1;
    }

    private static int toggleStructureDebug(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        if(DebuggerSystem.structureDebuggerModule == null)
        {
            context.getSource().sendFailure(Component.literal("StructureDebuggerModule is not initialized."));
            return 0;
        }

        if(DebuggerSystem.structureDebuggerModule.isDebuggingEnabled())
        {
            DebuggerSystem.structureDebuggerModule.setDebuggingEnabled(false);
            context.getSource().sendSuccess(()->Component.literal("StructureDebuggerModule | Debugging=" + DebuggerSystem.structureDebuggerModule.isDebuggingEnabled()), false);
        }
        else
        {
            DebuggerSystem.structureDebuggerModule.setDebuggingEnabled(true);
            context.getSource().sendSuccess(()->Component.literal("StructureDebuggerModule | Debugging=" + DebuggerSystem.structureDebuggerModule.isDebuggingEnabled()), false);
        }
        return 1;
    }

}
