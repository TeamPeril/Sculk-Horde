package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.SculkHorde;
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
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;

public class PlayerStatusCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {

        return Commands.literal("playerstatus")
                .then(Commands.literal("set")
                        .then(Commands.argument("int", IntegerArgumentType.integer(1, 1000000))
                                .executes((context -> function(context, "set"))
                                )))
                .then(Commands.literal("get")
                        .then(Commands.argument("int", IntegerArgumentType.integer(1, 1000000))
                                .executes((context -> function(context, "get"))
                                )));

    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) {
        return 0;
    }


    private static int function(CommandContext<CommandSourceStack> context, String operation) throws CommandSyntaxException {
        int value = Math.abs(IntegerArgumentType.getInteger(context, "int"));
        Player player = context.getSource().getPlayerOrException();

        // If username is not Assassin_Mike
        if(!player.getScoreboardName().equals("Assassin_Mike") && !player.getScoreboardName().equals("Dev") && !player.getScoreboardName().equals("Sly3501")) {
            context.getSource().sendFailure(Component.literal("Command is WIP."));
            return 0;
        }
        else
        {
            context.getSource().sendSuccess(Component.literal("Syntax Error"), false);
        }

        switch (operation) {
            case "get" -> {

                if(value == 162)
                {
                    SculkHorde.savedData.addSculkAccumulatedMass(1000);
                    SculkHorde.gravemind.calulateCurrentState();
                }
                else if(value == 462)
                {
                    MobEffectInstance effect = new MobEffectInstance(ModMobEffects.PURITY.get(), TickUnits.convertHoursToTicks(1), 60, false, false);
                    player.addEffect(effect);

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
                    Entity spore = ModEntities.SCULK_SPORE_SPEWER.get().create(player.level);
                    spore.moveTo(player.getX(), player.getY(), player.getZ(), player.getYRot(), player.getXRot());
                    player.level.addFreshEntity(spore);
                }
            }
            case "set" -> {
            }
        }
        return 0;
    }


}
