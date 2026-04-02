package com.github.sculkhorde.common.entity.components;

/**
 * Enum representing types of entities that can be targeted.
 * This replaces individual boolean flags with a more modular approach.
 */
public enum TargetFilter
{
    SCULK_HORDE_ENTITY,

    ALLIED_TO_SCULK_HORDE,

    FROM_INFECTION_MOD,

    /** Target hostile mobs */
    HOSTILE_TO_SCULK,

    /** Target passive mobs */
    PASSIVE_TO_SCULK,

    /** Target infected entities */
    INFECTED_BY_SCULK,

    /** Target swimming entities */
    SWIMMERS,

    /** Target entities that walk (non-water bound) */
    WALKERS,


    FLIERS
}

