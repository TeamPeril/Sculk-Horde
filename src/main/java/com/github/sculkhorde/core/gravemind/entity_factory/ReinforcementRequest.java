package com.github.sculkhorde.core.gravemind.entity_factory;

import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;

import java.util.ArrayList;

public class ReinforcementRequest {

    /**
     * budget - The maximum allotted budget. if -1, this means unlimited. <br>
     * remaining_balance - Only used for scullk mass. <br>
     * is_player_nearby - Indicates if a player is near the sender. <br>
     * is_non_sculk_mob_nearby - Indicates if a possible infection target is near the sender. <br>
     * locationX - The X coordinate of where the reinforcement is being requested. <br>
     * locationY - The X coordinate of where the reinforcement is being requested.
     */
    public int budget = -1;
    public int remaining_balance = -1;
    public boolean is_aggressor_nearby;
    public boolean is_non_sculk_mob_nearby;
    public BlockPos position;
    public long creationTime;

    /**
     * senderType list all possible senders of a reinforcement request. <br>
     * sender indicates who sent this request.
     */
    public enum senderType {Developer, SculkMass, SculkCocoon}
    public senderType sender;

    /**
     * isRequestViewed - Indicates if the gravemind has viewed this request yet. <br>
     * isRequestApproved - Indicates if the reinforcement request is approved. <br>
     * approvedMobTypes - All approved mob types to spawn.
     */
    public boolean isRequestViewed = false;
    public boolean isRequestApproved = false;
    public ArrayList<EntityFactory.StrategicValues>  approvedMobTypes;
    public LivingEntity spawnedEntity = null;

    /**
     * Default Constructor
     * @param x The x position of where we want the reinforcement
     * @param y The y position of where we want the reinforcement
     */
    public ReinforcementRequest(BlockPos blockPosIn)
    {
        is_aggressor_nearby = false;
        is_non_sculk_mob_nearby = false;
        sender = null;
        position = blockPosIn;
        approvedMobTypes = new ArrayList<EntityFactory.StrategicValues>();
        creationTime = System.nanoTime();
    }

    public boolean equals(ReinforcementRequest context)
    {
        if(budget != context.budget
        || is_aggressor_nearby != context.is_aggressor_nearby
        || is_non_sculk_mob_nearby != context.is_non_sculk_mob_nearby
        || position != context.position
        || sender != context.sender
        || isRequestViewed != context.isRequestViewed
        || isRequestApproved != context.isRequestApproved
        || !approvedMobTypes.equals(context.approvedMobTypes))
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
                ", position=" + position.toString() +
                ", sender=" + sender +
                ", isRequestViewed=" + isRequestViewed +
                ", isRequestApproved=" + isRequestApproved +
                ", approvedMobTypes=" + approvedMobTypes +
                '}';
    }
}
