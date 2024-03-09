package com.github.sculkhorde.common.command;

import com.github.sculkhorde.common.block.SculkNodeBlock;
import com.github.sculkhorde.core.SculkHorde;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class NodesStatusCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("nodestatus")
                .executes(new NodesStatusCommand());

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context)
    {
        context.getSource().sendSuccess(()->Component.literal(
                "Sculk Nodes Present: " + SculkHorde.savedData.getNodeEntries().size()
                        + "\n"
                        + " Are there too many nodes? " + (SculkHorde.savedData.getNodeEntries().size() >= SculkHorde.gravemind.sculk_node_limit)
                        + "\n"
                        + "Is Node Cooldown Over: " + SculkHorde.savedData.isNodeSpawnCooldownOver()
                        + "\n"
                        + "Minutes Remaining on Cooldown: " + SculkHorde.savedData.getMinutesRemainingUntilNodeSpawn()
                        + "\n"
                        + "Mass Needed for Node Spawn: " + (SculkNodeBlock.SPAWN_NODE_COST + SculkNodeBlock.SPAWN_NODE_BUFFER)
                        + "\n"
                        + "Is Enough Mass Present for Node Spawn: " + (SculkHorde.savedData.getSculkAccumulatedMass() >= SculkNodeBlock.SPAWN_NODE_COST + SculkNodeBlock.SPAWN_NODE_BUFFER)
                        + "\n"
                        + "Is the Horde Defeated: " + SculkHorde.savedData.isHordeDefeated()
                ), false);
        return 0;
    }

}
