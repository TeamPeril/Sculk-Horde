package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Optional;

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
                        .executes((commandStack) -> {
                            return getVessels(commandStack.getSource());
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
            Optional<ModSavedData.PlayerProfileEntry> playerProfile = PlayerProfileHandler.getPlayerProfile(player);
            if(playerProfile.isPresent())
            {
                playerProfile.get().setVessel(value);
                playerProfile.get().setActiveVessel(true);
                context.sendSuccess(() -> { return Component.literal(playerProfile.get().getPlayer().get().getName() + " Vessel Status: " + playerProfile.get().isVessel());}, true);
            }
            else
            {
                context.sendFailure(Component.literal("Gravemind has no player profile for this user. Creating One"));
                SculkHorde.savedData.getPlayerProfileEntries().add(new ModSavedData.PlayerProfileEntry(player));
                context.sendFailure(Component.literal("Created Profile for Gravemind. Please re-run command."));
            }
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
            String result = "Vessels: [";

            for(ServerPlayer player : vessels)
            {
                result += player.getName().toString() + ", ";
            }
            result += "]";
            String finalResult = result;
            context.sendSuccess(() -> { return Component.literal(finalResult);}, true);
        }
        return vessels.size();
    }


}
