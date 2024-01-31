package com.github.sculkhorde.core.gravemind.entity_factory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;

import java.util.ArrayList;
import java.util.Arrays;

public class ReinforcementRequest {


    public int budget = -1; // The maximum allotted budget. if -1, this means unlimited.
    public int remaining_balance = -1; // Only used for sculk mass.
    public boolean is_aggressor_nearby; // Indicates if a hostile is nearby
    public boolean is_non_sculk_mob_nearby; // Indicates if a possible infection target is near the sender.

    public BlockPos[] positions; // The positions of where the reinforcements are being requested.
    public long creationTime; // The time this request was created.
    public enum senderType {Developer, SculkMass, Summoner, BossReinforcement, Raid} // All possible senders.
    public senderType sender; // The sender of the request.
    public boolean isRequestViewed = false; // If the Gravemind has viewed this request.
    public boolean isRequestApproved = false; // If the reinforcement request is approved.
    public final ArrayList<EntityFactoryEntry.StrategicValues> approvedStrategicValues; // All approved mob types to spawn.
    public LivingEntity[] spawnedEntities; // All entities spawned by this request.

    public ServerLevel dimension;


    public ReinforcementRequest(ServerLevel dimension, BlockPos blockPosIn)
    {
        this.dimension = dimension;
        is_aggressor_nearby = false;
        is_non_sculk_mob_nearby = false;
        sender = null;
        positions = new BlockPos[]{blockPosIn};
        spawnedEntities = new LivingEntity[positions.length];
        approvedStrategicValues = new ArrayList<EntityFactoryEntry.StrategicValues>();
        creationTime = System.nanoTime();
    }


    public ReinforcementRequest(ServerLevel dimension, BlockPos[] positions)
    {
        this.dimension = dimension;
        is_aggressor_nearby = false;
        is_non_sculk_mob_nearby = false;
        sender = null;
        this.positions = positions;
        spawnedEntities = new LivingEntity[positions.length];
        approvedStrategicValues = new ArrayList<EntityFactoryEntry.StrategicValues>();
        creationTime = System.nanoTime();
    }

    public boolean equals(ReinforcementRequest context)
    {
        return budget == context.budget
                && is_aggressor_nearby == context.is_aggressor_nearby
                && is_non_sculk_mob_nearby == context.is_non_sculk_mob_nearby
                && Arrays.equals(positions, context.positions)
                && sender == context.sender
                && isRequestViewed == context.isRequestViewed
                && isRequestApproved == context.isRequestApproved
                && approvedStrategicValues.equals(context.approvedStrategicValues)
                && !dimension.toString().equals(context.dimension.toString());
    }

    @Override
    public String toString() {
        return "ReinforcementContext{" +
                "budget=" + budget +
                ", remaining_balance=" + remaining_balance +
                ", is_aggressor_nearby=" + is_aggressor_nearby +
                ", is_non_sculk_mob_nearby=" + is_non_sculk_mob_nearby +
                ", positions=" + Arrays.toString(positions) +
                ", sender=" + sender +
                ", isRequestViewed=" + isRequestViewed +
                ", isRequestApproved=" + isRequestApproved +
                ", approvedMobTypes=" + approvedStrategicValues.toString() +
                '}';
    }
}
