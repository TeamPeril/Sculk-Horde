package com.github.sculkhorde.common.command;

import com.github.sculkhorde.util.InfestationUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.coordinates.BlockPosArgument;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

public class InfestChunkCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("infest_chunk")
                .then(Commands.argument("pos", BlockPosArgument.blockPos())
                        .executes(InfestChunkCommand::infestChunk)
                );
    }



    protected static int infestChunk(CommandContext<CommandSourceStack> context)
    {
        BlockPos pos = BlockPosArgument.getBlockPos(context, "pos");
        ServerLevel level = context.getSource().getLevel();

        InfestationUtil.infestChunk(level, pos);
        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }
}
