package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.NodeUtil;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;

public class NodeCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("node")
                .then(Commands.literal("list")
                        .executes(NodeCommand::listNodes))
                .then(Commands.literal("deactivate_all")
                        .executes(NodeCommand::deactivateAll))
                .then(Commands.literal("activate_most_idle_node")
                        .executes(NodeCommand::activateMostIdle))
                .then(Commands.literal("activate_all")
                        .executes(NodeCommand::activateAll))
                .then(Commands.literal("move_most_inactive_node_to_me")
                        .executes(NodeCommand::moveMostInactiveNodeMe));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    private static int listNodes(CommandContext<CommandSourceStack> context) {
        if (ModSavedData.getSaveData().getNodeEntries().isEmpty()) {
            context.getSource().sendSuccess(() -> Component.literal("No nodes found."), false);
            return 0;
        }

        context.getSource().sendSuccess(() -> Component.literal("Sculk Nodes:"), false);

        for (ModSavedData.NodeEntry node : ModSavedData.getSaveData().getNodeEntries()) {
            if (!node.isEntryValid()) {
                continue;
            }

            int x = node.getPosition().getX();
            int y = node.getPosition().getY();
            int z = node.getPosition().getZ();
            String dimension = node.getDimension().dimension().location().toString();
            String command = "/execute in " + dimension + " run tp @s " + x + " " + y + " " + z;

            MutableComponent nodeText = Component.literal("Node [" + node.getPosition().toShortString() + " | " + dimension + "]");
            nodeText.withStyle(style -> style
                    .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command))
                    .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("Click to teleport to this node")))
                    .withUnderlined(true));

            context.getSource().sendSuccess(() -> nodeText, false);
        }

        return 0;
    }

    private static int deactivateAll(CommandContext<CommandSourceStack> context) {
        SculkHorde.sculkNodesSystem.DeactivateAllNodes();
        context.getSource().sendSuccess(() -> Component.literal("Deactivated all nodes."), true);
        return 0;
    }

    private static int activateMostIdle(CommandContext<CommandSourceStack> context) {
        SculkHorde.sculkNodesSystem.ActivateNodeWithLongestDurationOfInactivity();
        context.getSource().sendSuccess(() -> Component.literal("Activated the most idle node."), true);
        return 0;
    }

    private static int activateAll(CommandContext<CommandSourceStack> context) {
        SculkHorde.sculkNodesSystem.ActivateAllNodes();
        context.getSource().sendSuccess(() -> Component.literal("Activated all nodes."), true);
        return 0;
    }

    private static int moveMostInactiveNodeMe(CommandContext<CommandSourceStack> context) {
        NodeUtil.tryMoveOldestNodeTo(context.getSource().getLevel(), BlockPos.containing(context.getSource().getPosition()), true);
        context.getSource().sendSuccess(() -> Component.literal("Moved oldest node."), true);
        return 0;
    }
}
