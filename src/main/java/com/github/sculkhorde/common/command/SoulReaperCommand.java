package com.github.sculkhorde.common.command;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.PlayerProfileHandler;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;

public class SoulReaperCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("spawn_soul_reaper")

                .requires((commandStack) -> commandStack.hasPermission(2))
                .then(Commands.argument("difficulty", IntegerArgumentType.integer(1, 3))
                        .executes((commandStack) -> {
                            return spawnSoulReaper(
                                    commandStack.getSource(),
                                    IntegerArgumentType.getInteger(commandStack, "difficulty")
                            );
                        })
                );


    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int spawnSoulReaper(CommandSourceStack context, int value) throws CommandSyntaxException {

        SculkSoulReaperEntity.spawnWithDifficulty(context.getLevel(), context.getPosition(), value);

        return 1;
    }

    private static int getVessels(CommandSourceStack context) throws CommandSyntaxException {

        ArrayList<ServerPlayer> vessels = PlayerProfileHandler.getVessels();
        if(vessels.isEmpty())
        {
            context.sendFailure(Component.literal("No Vessels exist or are online."));
        }
        else
        {
            StringBuilder output = new StringBuilder();
            output.append("Vessels: [");

            for(ServerPlayer player : vessels)
            {
                output.append(player.getName());
                output.append(", ");
            }
            output.append("]");
            context.sendSuccess(() -> { return Component.literal(output.toString());}, false);
        }
        return vessels.size();
    }

    private static int recieveEnlightenment(CommandSourceStack context) throws CommandSyntaxException {

        if(context.getPlayer() == null)
        {
            context.sendFailure(Component.literal("Command can only be executed by player."));
        }

        // Check if player has profile with gravemind
        ModSavedData.PlayerProfileEntry playerProfile = PlayerProfileHandler.getOrCreatePlayerProfile(context.getPlayer());

        if(playerProfile.isVessel())
        {
            context.getPlayer().getInventory().add(new ItemStack(ModItems.TOME_OF_VEIL.get()));
            context.sendSuccess(() -> { return Component.literal("Your now poses the Tome.");}, false);
            return 1;
        }
        else
        {
            context.sendFailure(Component.literal("You are not Worthy"));
            return 0;
        }
    }

}
