package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModMobEffects;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.BlockAlgorithms;
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
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

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
                .then(Commands.literal("toggleVeil")
                        .executes((commandStack) -> {
                            return toggleVeil(commandStack.getSource());
                        })
                )
                .then(Commands.literal("summon_reinforcements")
                        .executes((commandStack) -> {
                            return summonReinforcementsCommand(commandStack.getSource());
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

    private static int toggleVeil(CommandSourceStack context) throws CommandSyntaxException {

        if(context.getPlayer() == null)
        {
            context.sendFailure(Component.literal("Command can only be executed by player."));
        }

        // Check if player has profile with gravemind
        ModSavedData.PlayerProfileEntry playerProfile = PlayerProfileHandler.getOrCreatePlayerProfile(context.getPlayer());

        if(playerProfile.isVessel())
        {
            if(playerProfile.isActiveVessel())
            {
                playerProfile.setActiveVessel(false);
                context.getPlayer().removeEffect(ModMobEffects.SCULK_VESSEL.get());
                context.sendSuccess(() -> { return Component.literal("Your powers are hidden. You now appear normal.");}, false);
                return 1;
            }

            playerProfile.setActiveVessel(true);
            MobEffectInstance effectInstance = new MobEffectInstance(ModMobEffects.SCULK_VESSEL.get(), Integer.MAX_VALUE);
            context.getPlayer().addEffect(effectInstance);
            context.sendSuccess(() -> { return Component.literal("Your powers are now active. You now appear as a vessel.");}, false);
            return 1;
        }
        else
        {
            context.sendFailure(Component.literal("You are not a vessel of the Gravemind. Cannot Toggle Veil."));
            return 0;
        }
    }

    public static int summonReinforcementsCommand(CommandSourceStack context)
    {
        context.sendSuccess(()->Component.literal(
                "Reinforcement Request Received."
        ), false);

        Vec3 reinforcementPosition = context.getPosition();

        summonReinforcement(context.getLevel(), BlockPos.containing(reinforcementPosition));

        return 0;
    }

    private static void summonReinforcement(ServerLevel level, BlockPos blockPos)
    {
        ((ServerLevel)level).sendParticles(ParticleTypes.SCULK_SOUL, blockPos.getX() + 0.5D, blockPos.getY() + 1.15D, blockPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
        ((ServerLevel)level).playSound((Player)null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
        //Choose spawn positions
        ArrayList<BlockPos> possibleSpawnPositions = getSpawnPositionsInCube((ServerLevel) level, blockPos, 5, 10);

        BlockPos[] finalizedSpawnPositions = new BlockPos[10];

        //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
        for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
        {
            finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
        }

        //If the array is empty, just spawn above block
        if (possibleSpawnPositions.isEmpty()) {
            finalizedSpawnPositions[0] = blockPos.above();
        }

        //Give gravemind context to our request to make more informed situations
        ReinforcementRequest request = new ReinforcementRequest((ServerLevel) level, finalizedSpawnPositions);
        request.sender = ReinforcementRequest.senderType.Summoner;
        request.is_aggressor_nearby = true;

        //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
        SculkHorde.entityFactory.createReinforcementRequestFromSummoner(level, blockPos, false, request);
    }


    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public static boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
    {
        boolean isBlockBelowSolid = BlockAlgorithms.isSolid(worldIn, pos.below());
        boolean isBaseBlockReplaceable = worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER);
        boolean isBlockAboveReplaceable = worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

        return isBlockBelowSolid && isBaseBlockReplaceable && isBlockAboveReplaceable;

    }

    public static ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions)
    {
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && listOfPossibleSpawns.size() > 0; count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            finalList.add(listOfPossibleSpawns.get(randomIndex));
            listOfPossibleSpawns.remove(randomIndex);
        }
        return finalList;
    }

    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param pDistance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, double pDistance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        //Search area for block
        for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
        {
            for(int j = 0; (double)j < pDistance; ++j)
            {
                for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
                {
                    for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
                    {
                        //blockpos$mutable.setWithOffset(origin, k, i - 1, l);
                        BlockPos temp = new BlockPos(origin.getX() + k, origin.getY() + i-1, origin.getZ() + l);

                        //If the block is close enough and is the right blockstate
                        if (origin.closerThan(temp, pDistance)
                                && isValidSpawnPosition(worldIn, temp))
                        {
                            list.add(temp); //add position
                        }
                    }
                }
            }
        }
        //else return empty
        return list;
    }


}
