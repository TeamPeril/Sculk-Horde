package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.Collection;

public class VesselCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("vessel")
                .then(Commands.literal("set")
                        .requires((commandStack) -> commandStack.hasPermission(2))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .then(Commands.argument("value", BoolArgumentType.bool())
                                        .executes((commandStack) -> {
                                            return setPlayerVesselStatus(
                                                    commandStack.getSource(),
                                                    EntityArgument.getPlayers(commandStack, "targets"),
                                                    BoolArgumentType.getBool(commandStack, "value")
                                            );
                                        })
                                )
                        )
                )
                .then(Commands.literal("get")
                        .requires((commandStack) -> commandStack.hasPermission(2))
                        .executes((commandStack) -> {
                            return getVessels(commandStack.getSource());
                        })
                )
                .then(Commands.literal("receive_enlightenment")
                        .executes((commandStack) -> {
                            return recieveEnlightenment(commandStack.getSource());
                        })
                );


    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int setPlayerVesselStatus(CommandSourceStack context, Collection<ServerPlayer> players, boolean value) throws CommandSyntaxException {
        for(ServerPlayer player : players)
        {
            ModSavedData.PlayerProfileEntry playerProfile = PlayerProfileHandler.getOrCreatePlayerProfile(player);
            playerProfile.setVessel(value);
            playerProfile.setActiveVessel(true);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(playerProfile.getPlayer().get().getScoreboardName());
            stringBuilder.append(" Vessel Status: ");
            stringBuilder.append(playerProfile.isVessel());

            context.sendSuccess(() -> { return Component.literal(stringBuilder.toString());}, false);
        }
        return players.size();
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
