package com.github.sculkhorde.common.entity.components;

import net.minecraft.world.entity.LivingEntity;

import java.util.*;

/**
 * Manages multiple targets with priority ordering.
 * Maintains a primary target and optional secondary targets for more sophisticated targeting behavior.
 */
public class TargetStack
{
    protected final LinkedList<LivingEntity> secondaryTargets;
    protected final int maxSecondaryTargets;
    protected final TargetParameters targetParameters;

    /**
     * Creates a new TargetStack.
     *
     * @param maxSecondaryTargets Maximum number of secondary targets to track (0 = only primary)
     */
    public TargetStack(TargetParameters targetParameters, int maxSecondaryTargets)
    {
        this.maxSecondaryTargets = Math.max(0, maxSecondaryTargets);
        this.secondaryTargets = new LinkedList<>();
        this.targetParameters = targetParameters;
    }

    /**
     * Gets the primary/current target.
     *
     * @return The primary target, or null if none
     */
    public LivingEntity getPrimaryTarget()
    {
        if(targetParameters.mob != null && targetParameters.mob.isAlive())
        {
            return targetParameters.mob.getTarget();
        }
        return null;
    }

    /**
     * Sets the primary target.
     *
     * @param target The new primary target (null to clear)
     */
    public void setPrimaryTarget(LivingEntity target)
    {
        if(targetParameters.mob != null && targetParameters.mob.isAlive())
        {
            targetParameters.mob.setTarget(target);
        }
    }

    /**
     * Adds a secondary target.
     * If max secondary targets reached, oldest is removed.
     * If target is already primary or in secondary list, no duplicate is added.
     *
     * @param target The target to add
     */
    public void addSecondaryTarget(LivingEntity target)
    {
        if (target == null || target == getPrimaryTarget() || secondaryTargets.contains(target))
        {
            return;
        }

        secondaryTargets.addLast(target);
        if (secondaryTargets.size() > maxSecondaryTargets)
        {
            secondaryTargets.removeFirst();
        }
    }

    /**
     * Removes a specific secondary target.
     *
     * @param target The target to remove
     * @return true if the target was removed
     */
    public boolean removeSecondaryTarget(LivingEntity target)
    {
        return secondaryTargets.remove(target);
    }

    /**
     * Gets a copy of secondary targets in order.
     *
     * @return List of secondary targets
     */
    public List<LivingEntity> getSecondaryTargets()
    {
        return new ArrayList<>(secondaryTargets);
    }

    /**
     * Gets up to N secondary targets.
     *
     * @param count Number of targets to retrieve
     * @return List of up to count secondary targets
     */
    public List<LivingEntity> getSecondaryTargets(int count)
    {
        List<LivingEntity> result = new ArrayList<>();
        Iterator<LivingEntity> iter = secondaryTargets.iterator();
        for (int i = 0; i < count && iter.hasNext(); i++)
        {
            result.add(iter.next());
        }
        return result;
    }

    /**
     * Gets all targets (primary + secondary) in order.
     *
     * @return List of all targets
     */
    public List<LivingEntity> getAllTargets()
    {
        List<LivingEntity> all = new ArrayList<>();
        if (getPrimaryTarget() != null)
        {
            all.add(getPrimaryTarget());
        }
        all.addAll(secondaryTargets);
        return all;
    }

    /**
     * Checks if this stack has any targets.
     *
     * @return true if primary or secondary targets exist
     */
    public boolean hasTargets()
    {
        return getPrimaryTarget() != null || !secondaryTargets.isEmpty();
    }

    /**
     * Clears all targets.
     */
    public void clear()
    {
        setPrimaryTarget(null);
        secondaryTargets.clear();
    }

    public int getMaxSecondaryTargets()
    {
        return maxSecondaryTargets;
    }

    /**
     * Checks if there's room for more secondary targets.
     *
     * @return true if we can add another secondary target
     */
    public boolean hasSecondarySlotsAvailable()
    {
        return secondaryTargets.size() < maxSecondaryTargets;
    }

    /**
     * Gets the number of secondary targets.
     *
     * @return Count of secondary targets
     */
    public int getSecondaryTargetCount()
    {
        return secondaryTargets.size();
    }

    /**
     * Promotes a secondary target to primary, if it exists.
     *
     * @param target The target to promote
     * @return true if promotion succeeded
     */
    public boolean promoteSecondaryTarget(LivingEntity target)
    {
        if (secondaryTargets.remove(target))
        {
            LivingEntity oldPrimary = getPrimaryTarget();
            setPrimaryTarget(target);
            if (oldPrimary != null)
            {
                secondaryTargets.addLast(oldPrimary);
                if (secondaryTargets.size() > maxSecondaryTargets)
                {
                    secondaryTargets.removeFirst();
                }
            }
            return true;
        }
        return false;
    }
}

