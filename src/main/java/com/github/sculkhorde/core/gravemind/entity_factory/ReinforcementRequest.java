package com.github.sculkhorde.core.gravemind.entity_factory;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.core.BlockPos;

public class ReinforcementRequest {


    public int budget = -1; // The maximum allotted budget. if -1, this means unlimited.
    public int remaining_balance = -1; // Only used for scullk mass.
    public boolean is_aggressor_nearby; // Indicates if a hostile is near by
    public boolean is_non_sculk_mob_nearby; // Indicates if a possible infection target is near the sender.
    public BlockPos[] positions; // The positions of where the reinforcements is being requested.
    public long creationTime; // The time this request was created.
    public enum senderType {Developer, SculkMass, Summoner, BossReinforcement} // All possible senders.
    public senderType sender; // The sender of the request.
    public boolean isRequestViewed = false; // If the Gravemind has viewed this request.
    public boolean isRequestApproved = false; // If the reinforcement request is approved.
    public EntityFactoryEntry.StrategicValues[]  approvedMobTypes; // All approved mob types to spawn.
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
        approvedMobTypes = new EntityFactoryEntry.StrategicValues[]{};
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
        approvedMobTypes = new EntityFactoryEntry.StrategicValues[]{};
        creationTime = System.nanoTime();
    }

    public boolean equals(ReinforcementRequest context)
    {
        if(budget != context.budget
        || is_aggressor_nearby != context.is_aggressor_nearby
        || is_non_sculk_mob_nearby != context.is_non_sculk_mob_nearby
        || !positions.equals(context.positions)
        || sender != context.sender
        || isRequestViewed != context.isRequestViewed
        || isRequestApproved != context.isRequestApproved
        || !approvedMobTypes.equals(context.approvedMobTypes)
        || dimension.toString().equals(context.dimension.toString()))
            return false;

        return true;
    }

    @Override
    public String toString() {
        return "ReinforcementContext{" +
                "budget=" + budget +
                ", remaining_balance=" + remaining_balance +
                ", is_aggressor_nearby=" + is_aggressor_nearby +
                ", is_non_sculk_mob_nearby=" + is_non_sculk_mob_nearby +
                ", positions=" + positions.toString() +
                ", sender=" + sender +
                ", isRequestViewed=" + isRequestViewed +
                ", isRequestApproved=" + isRequestApproved +
                ", approvedMobTypes=" + approvedMobTypes +
                '}';
    }
}
