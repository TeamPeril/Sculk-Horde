package com.github.sculkhorde.common.entity.components;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Determines when a target should be forgotten/dropped.
 * This allows configurable rules for target retention beyond just validation.
 */
@FunctionalInterface
public interface TargetRetention
{
    /**
     * Checks if a target should be retained.
     *
     * @param target The current target to evaluate
     * @param mob The mob holding the target
     * @return true if the target should be kept, false if it should be forgotten
     */
    boolean shouldRetain(LivingEntity target, Mob mob);

    /**
     * Creates a retention rule based on line of sight timeout.
     *
     * @param maxUnseeableTicksAllowed The maximum number of ticks allowed without seeing the target
     * @param currentTicksSinceLastSeen The current ticks since last seen the target
     * @return A TargetRetention that drops targets after timeout
     */
    static TargetRetention lineOfSightTimeout(int maxUnseeableTicksAllowed, long currentTicksSinceLastSeen)
    {
        return (target, mob) -> {
            if (mob != null && mob.getSensing().hasLineOfSight(target))
            {
                return true; // Can see target, keep it
            }
            return currentTicksSinceLastSeen < maxUnseeableTicksAllowed; // Drop if timeout exceeded
        };
    }

    /**
     * Creates a retention rule based on distance.
     *
     * @param maxDistance The maximum distance to retain target
     * @return A TargetRetention that drops targets exceeding distance
     */
    static TargetRetention maxDistance(double maxDistance)
    {
        return (target, mob) -> mob != null && mob.distanceTo(target) <= maxDistance;
    }

    /**
     * Creates a retention rule that always keeps the target.
     *
     * @return A TargetRetention that never drops targets
     */
    static TargetRetention always()
    {
        return (target, mob) -> true;
    }
}


