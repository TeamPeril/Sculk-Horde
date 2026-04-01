package com.github.sculkhorde.common.entity.components;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Represents a composable targeting condition that can be evaluated for a given entity.
 * This allows for flexible, custom validation rules beyond the built-in filters.
 */
@FunctionalInterface
public interface TargetCondition
{
    /**
     * Evaluates if a target meets this condition.
     *
     * @param target The entity being evaluated
     * @param isCurrentTarget Whether this is validating an existing target (optimization hint)
     * @param mob The mob doing the targeting (for context, may be null)
     * @return true if the condition is met, false otherwise
     */
    boolean isMet(LivingEntity target, Mob mob);

    /**
     * Creates a condition that checks if health is below a threshold percentage.
     *
     * @param healthPercentage The health threshold (0-100)
     * @return A TargetCondition for health checks
     */
    static TargetCondition healthBelow(float healthPercentage)
    {
        return (target, mob) -> target.getHealth() < (target.getMaxHealth() * healthPercentage / 100f);
    }

    /**
     * Creates a condition that checks if health is above a threshold percentage.
     *
     * @param healthPercentage The health threshold (0-100)
     * @return A TargetCondition for health checks
     */
    static TargetCondition healthAbove(float healthPercentage)
    {
        return (target, mob) -> target.getHealth() >= (target.getMaxHealth() * healthPercentage / 100f);
    }

    /**
     * Creates a condition that checks distance from the targeting mob.
     *
     * @param maxDistance The maximum distance allowed
     * @return A TargetCondition for distance checks
     */
    static TargetCondition withinDistance(double maxDistance)
    {
        return (target, mob) -> mob != null && mob.distanceTo(target) <= maxDistance;
    }

    /**
     * Creates a condition that checks if the target is visible.
     *
     * @return A TargetCondition for visibility checks
     */
    static TargetCondition mustSee()
    {
        return (target, mob) -> mob != null && mob.getSensing().hasLineOfSight(target);
    }
}


