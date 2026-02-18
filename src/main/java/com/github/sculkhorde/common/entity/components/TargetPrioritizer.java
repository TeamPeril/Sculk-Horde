package com.github.sculkhorde.common.entity.components;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

/**
 * Ranks targets by priority, enabling smarter target selection and revaluation.
 * Lower scores are higher priority (following common sorting conventions).
 */
@FunctionalInterface
public interface TargetPrioritizer
{
    /**
     * Calculates a priority score for a target.
     * Lower scores = higher priority.
     *
     * @param target The entity to evaluate
     * @param mob The mob doing the targeting
     * @return Priority score (lower = higher priority)
     */
    double getPriorityScore(LivingEntity target, Mob mob);

    /**
     * Creates a prioritizer based on distance (closer = higher priority).
     *
     * @return A TargetPrioritizer favoring closer targets
     */
    static TargetPrioritizer byDistance()
    {
        return (target, mob) -> mob != null ? mob.distanceTo(target) : Double.MAX_VALUE;
    }

    /**
     * Creates a prioritizer based on health (lower health = higher priority).
     *
     * @return A TargetPrioritizer favoring weaker targets
     */
    static TargetPrioritizer byHealth()
    {
        return (target, __) -> target.getHealth();
    }

    /**
     * Creates a composite prioritizer combining multiple prioritizers.
     * Uses weighted average of all prioritizers.
     *
     * @param prioritizers The prioritizers to combine
     * @param weights The weights for each prioritizer (should sum to 1.0)
     * @return A combined TargetPrioritizer
     */
    static TargetPrioritizer composite(TargetPrioritizer[] prioritizers, double[] weights)
    {
        if (prioritizers.length != weights.length)
        {
            throw new IllegalArgumentException("Prioritizers and weights must have same length");
        }

        return (target, mob) -> {
            double score = 0;
            double maxScore = 0;

            for (int i = 0; i < prioritizers.length; i++)
            {
                double rawScore = prioritizers[i].getPriorityScore(target, mob);
                score += rawScore * weights[i];
                maxScore += weights[i];
            }

            return score / maxScore;
        };
    }
}

