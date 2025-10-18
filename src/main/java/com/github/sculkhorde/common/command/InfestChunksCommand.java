package com.github.sculkhorde.common.command;

import com.github.sculkhorde.util.InfestationUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class InfestChunksCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("infest_chunks")
                .then(Commands.literal("circle")
                        .then(Commands.argument("center", BlockPosArgument.blockPos())
                                .then(Commands.argument("radius", IntegerArgumentType.integer(1))
                                        .executes(InfestChunksCommand::infestChunks)
                                )
                        )
                );
    }



    protected static int infestChunks(CommandContext<CommandSourceStack> context)
    {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "center");
        int radius = IntegerArgumentType.getInteger(context, "radius");
        ServerLevel level = context.getSource().getLevel();

        InfestationUtil.infestChunksInCircle(level, pos, radius);
        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
