package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadEvent;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

import static com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadDispatcherSystem.MAX_RELATIONSHIP;
import static com.github.sculkhorde.systems.event_system.events.HitSquadEvent.HitSquadDispatcherSystem.MIN_NODES_DESTROYED;

public class HitSquadCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("hitsquad")
                .then(Commands.literal("attempt_hitsquad_event")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes((commandStack -> sendHitSquad(
                                        commandStack.getSource(),
                                        EntityArgument.getPlayers(commandStack, "targets")))
                                )
                        )
                );

    }

    protected static int sendHitSquad(CommandSourceStack context, Collection<ServerPlayer> players)
    {

        if(ModSavedData.getSaveData().getNodeEntries().isEmpty())
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("FAILURE, No nodes currently exist.");
            context.sendFailure(Component.literal(stringBuilder.toString()));
            return 0;
        }

        if(!SculkHorde.gravemind.isEvolutionInMatureState())
        {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("FAILURE, Gravemind state is less than mature.");
            context.sendFailure(Component.literal(stringBuilder.toString()));
            return 0;
        }

        for(ServerPlayer player : players)
        {
            StringBuilder stringBuilder = new StringBuilder();

            ModSavedData.PlayerProfileEntry playerProfile = PlayerProfileHandler.getOrCreatePlayerProfile(player);

            if(EntityAlgorithms.isInvalidTargetForSculkHorde(playerProfile.getPlayer().get()))
            {
                stringBuilder.append("FAILURE, " + player.getScoreboardName() + " is an explicitly denied target.");
                context.sendFailure(Component.literal(stringBuilder.toString()));
                return 0;
            }


            playerProfile.setTimeOfLastHit(0);
            if(playerProfile.getRelationshipToTheHorde() > MAX_RELATIONSHIP)
            {
                playerProfile.setRelationshipToTheHorde(MAX_RELATIONSHIP - 1);
            }

            if(playerProfile.getRelationshipToTheHorde() > MIN_NODES_DESTROYED)
            {
                playerProfile.setNodesDestroyed(MIN_NODES_DESTROYED + 1);
            }

            SculkHorde.eventSystem.addEvent(new HitSquadEvent(player.level().dimension(), player.getUUID()));

            stringBuilder.append(playerProfile.toString());
            stringBuilder.append("SUCCESS, " + player.getScoreboardName() + " is being hunted.");
            context.sendSuccess(() -> { return Component.literal(stringBuilder.toString());}, false);
        }
        return 1;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

}
