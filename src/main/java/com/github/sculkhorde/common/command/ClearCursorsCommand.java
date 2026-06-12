package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;

public class ClearCursorsCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("clear_cursors")
                .requires((commandStack) -> commandStack.hasPermission(2))
                .executes((commandStack) -> {
                    return resetSculkHorde(commandStack.getSource());
                });
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int resetSculkHorde(CommandSourceStack context) {

        if(context.getLevel().isClientSide())
        {
            return 0;
        }

        SculkHorde.cursorSystem.clearCursors();

        return 1;
    }
}
