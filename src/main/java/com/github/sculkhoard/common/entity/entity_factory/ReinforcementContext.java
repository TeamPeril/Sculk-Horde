package com.github.sculkhoard.common.entity.entity_factory;

public class ReinforcementContext {

    public int priority = 0; //Used to determine how important this request is

    public boolean is_player_nearby;
    public int weight_is_player_nearby = 2;

    public boolean is_non_sculk_mob_nearby;
    public int weight_is_non_sculk_mob_nearby = 1;

    public enum senderType {Developer, SculkMass, SculkCocoon};
    public senderType sender;

    public double locationX; //X coordinate of where we want reinforcement
    public double locationY; //Y coordinate of where we want reinforcement

    public ReinforcementContext(double x, double y)
    {
        is_player_nearby = false;
        is_non_sculk_mob_nearby = false;
        sender = null;
        locationX = x;
        locationY = y;
    }

    public void calculatePriority() {
        if(is_player_nearby) priority += weight_is_player_nearby;
        if(is_non_sculk_mob_nearby) priority += weight_is_non_sculk_mob_nearby;
    }


}
