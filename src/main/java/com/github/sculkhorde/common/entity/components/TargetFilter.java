package com.github.sculkhorde.common.entity.components;

/**
 * Enum representing types of entities that can be targeted.
 * This replaces individual boolean flags with a more modular approach.
 */
public enum TargetFilter
{
    /** Target hostile mobs */
    HOSTILES,

    /** Target passive mobs */
    PASSIVES,

    /** Target infected entities */
    INFECTED,

    /** Target swimming entities */
    SWIMMERS,

    /** Target entities that walk (non-water bound) */
    WALKERS,


    FLIERS
}

