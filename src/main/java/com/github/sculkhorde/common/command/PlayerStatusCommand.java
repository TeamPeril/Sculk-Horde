package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
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

public class PlayerStatusCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("player_profile")
                .then(Commands.literal("set")
                        .then(Commands.argument("int", IntegerArgumentType.integer(1, 1000000))
                                .executes((context -> setPlayerProfile(context, "set"))
                                )
                        )
                )
                .then(Commands.literal("get")
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes((commandStack -> getPlayerProfile(
                                        commandStack.getSource(),
                                        EntityArgument.getPlayers(commandStack, "targets")))
                                )
                        )
                );

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }

    protected static int getPlayerProfile(CommandSourceStack context, Collection<ServerPlayer> players)
    {
        for(ServerPlayer player : players)
        {
            ModSavedData.PlayerProfileEntry playerProfile = PlayerProfileHandler.getOrCreatePlayerProfile(player);

            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(playerProfile.toString());

            context.sendSuccess(() -> { return Component.literal(stringBuilder.toString());}, false);
        }
        return players.size();
    }

    private static int setPlayerProfile(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        int value = Math.abs(IntegerArgumentType.getInteger(context, "int"));
        Player player = context.getSource().getPlayerOrException();

        // If username is not Assassin_Mike
        if(!player.getScoreboardName().equals("Assassin_Mike") && !player.getScoreboardName().equals("Dev") && !player.getScoreboardName().equals("Sly3501")) {
            context.getSource().sendFailure(Component.literal("Command is WIP."));
            return 0;
        }
        else
        {
            context.getSource().sendSuccess(()->Component.literal("Syntax Error"), false);
        }

        switch (operation) {
            case "set" -> {

                if(value == 162)
                {
                    SculkHorde.savedData.addSculkAccumulatedMass(1000);
                    SculkHorde.gravemind.calulateCurrentState();
                }
                else if(value == 462)
                {
                    ModSavedData.PlayerProfileEntry profile = PlayerProfileHandler.getOrCreatePlayerProfile(player);
                    profile.setVessel(true);

                }
                else if(value == 562)
                {
                    MobEffectInstance effect = new MobEffectInstance(MobEffects.INVISIBILITY, TickUnits.convertHoursToTicks(1), 0, false, false);
                    player.addEffect(effect);
                }
                else if(value == 684)
                {
                    player.removeAllEffects();
                }
                else if(value == 782)
                {
                    Entity spore = ModEntities.SCULK_SPORE_SPEWER.get().create(player.level());
                    spore.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    player.level().addFreshEntity(spore);
                }

            }
            case "get" -> {
            }
        }
        return 0;
    }


}
