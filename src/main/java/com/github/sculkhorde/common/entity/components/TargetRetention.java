package com.github.sculkhorde.common.entity.components;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
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
     * @return A TargetRetention that drops targets after timeout
     */
    static TargetRetention lineOfSightTimeout(int maxUnseeableTicksAllowed)
    {
        return (target, mob) -> {
            if (mob instanceof ISculkSmartEntity smartMob)
            {
                boolean hasNotReachedTimeout = smartMob.getTargetParameters().getTicksSinceTargetLastSeen(target) < maxUnseeableTicksAllowed;

                if(!hasNotReachedTimeout)
                {
                    DebuggerSystem.entityDebuggerModule.logDebug("TargetRetention | " + mob.getClass().getSimpleName() + " | Removing Target. Unable to see for " + maxUnseeableTicksAllowed + " ticks.");
                }
                return hasNotReachedTimeout;
            }
            return true;
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


