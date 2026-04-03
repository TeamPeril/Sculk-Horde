package com.github.sculkhorde.common.entity.components;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.squad_system.Squad;
import com.github.sculkhorde.systems.squad_system.SquadSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.Vec3;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.EntityAlgorithms.*;

/**
 * Advanced targeting system for Sculk entities.
 * Supports modular filter configuration, custom conditions, multi-target tracking,
 * target retention rules, and priority-based target revaluation.
 */
public class TargetParameters
{
    public Mob mob;

    // Modular filter system (replaces individual booleans)
    protected final Set<TargetFilter> enabledFilters = new HashSet<>();

    protected final Set<TargetFilter> excludedFilters = new HashSet<>();

    // Custom composable conditions
    protected final List<TargetCondition> customConditions = new ArrayList<>();

    // Target retention rules
    protected final List<TargetRetention> retentionRules = new ArrayList<>();

    // Blacklist management
    protected final HashMap<UUID, Long> blacklist = new HashMap<>();
    protected boolean canBlackListMobs = true;

    // Multi-target tracking
    protected final TargetStack targetStack;

    // Priority-based revaluation
    protected TargetPrioritizer prioritizer = null;
    protected int priorityCheckInterval = 0; // 0 = disabled
    protected int priorityCheckCounter = 0;

    // Line-of-sight timeout tracking
    protected final Map<UUID, Integer> targetTicksSinceSeen = new HashMap<>();
    protected long maxTargetUnseenTimeMillis = TimeUnit.SECONDS.toMillis(30);

    protected boolean isSculkHordeAllied;
    /**
     * Creates a TargetParameters with no mob (useful for validation-only usage).
     * Default: 0 secondary targets allowed.
     */
    public TargetParameters(boolean isSculkHordeAllied)
    {
        this(null, isSculkHordeAllied, 0);
    }

    public TargetParameters(boolean isSculkHordeAllied, int maxSecondaryTargets)
    {
        this(null, isSculkHordeAllied, maxSecondaryTargets);
    }

    /**
     * Creates a TargetParameters for a specific mob.
     * Default: 0 secondary targets allowed.
     *
     * @param mob The mob using these parameters
     */
    public TargetParameters(Mob mob, boolean isSculkHordeAllied)
    {
        this(mob, isSculkHordeAllied, 0);
    }

    /**
     * Creates a TargetParameters with custom secondary target capacity.
     *
     * @param mob The mob using these parameters
     * @param maxSecondaryTargets Maximum number of secondary targets to track
     */
    public TargetParameters(Mob mob, boolean isSculkHordeAllied, int maxSecondaryTargets)
    {
        this.mob = mob;
        this.targetStack = new TargetStack(this, maxSecondaryTargets);
        this.isSculkHordeAllied = isSculkHordeAllied;
    }


    // ==================== Configuration Builder Methods ====================

    /**
     * Adds target filters for allowed entity types.
     * Replaces individual enable/disable methods with a modular approach.
     *
     * @param filters The filters to enable
     * @return This TargetParameters for chaining
     */
    public TargetParameters filterBy(TargetFilter... filters)
    {
        enabledFilters.addAll(Arrays.asList(filters));
        return this;
    }

    /**
     * Removes target filters.
     *
     * @param filters The filters to disable
     * @return This TargetParameters for chaining
     */
    public TargetParameters excludeFilter(TargetFilter... filters)
    {
        excludedFilters.addAll(Arrays.asList(filters));
        return this;
    }

    /**
     * Checks if a filter is enabled.
     *
     * @param filter The filter to check
     * @return true if the filter is enabled
     */
    public boolean isFilterEnabled(TargetFilter filter)
    {
        return enabledFilters.contains(filter);
    }

    /**
     * Checks if a filter is excluded.
     *
     * @param filter The filter to check
     * @return true if the filter is excluded
     */
    public boolean isFilterExcluded(TargetFilter filter)
    {
        return excludedFilters.contains(filter);
    }

    /**
     * Adds a custom targeting condition.
     * Conditions are evaluated in addition to standard filters.
     *
     * @param condition The condition to add
     * @return This TargetParameters for chaining
     */
    public TargetParameters addCondition(TargetCondition condition)
    {
        if (condition != null)
        {
            customConditions.add(condition);
        }
        return this;
    }

    /**
     * Removes a previously added condition.
     *
     * @param condition The condition to remove
     * @return This TargetParameters for chaining
     */
    public TargetParameters removeCondition(TargetCondition condition)
    {
        customConditions.remove(condition);
        return this;
    }

    /**
     * Adds a target retention rule.
     * Retention rules determine if existing targets should be kept.
     *
     * @param retention The retention rule to add
     * @return This TargetParameters for chaining
     */
    public TargetParameters addRetentionRule(TargetRetention retention)
    {
        if (retention != null)
        {
            retentionRules.add(retention);
        }
        return this;
    }

    /**
     * Removes a previously added retention rule.
     *
     * @param retention The retention rule to remove
     * @return This TargetParameters for chaining
     */
    public TargetParameters removeRetentionRule(TargetRetention retention)
    {
        retentionRules.remove(retention);
        return this;
    }

    /**
     * Enables priority-based target revaluation.
     * Periodically checks if the current target remains the highest priority.
     *
     * @param prioritizer The prioritizer to use for ranking targets
     * @param checkIntervalTicks How often to recheck priority (in ticks, 0 = every tick)
     * @return This TargetParameters for chaining
     */
    public TargetParameters enableTargetPrioritization(TargetPrioritizer prioritizer, int checkIntervalTicks)
    {
        this.prioritizer = prioritizer;
        this.priorityCheckInterval = Math.max(0, checkIntervalTicks);
        return this;
    }

    /**
     * Disables priority-based target revaluation.
     *
     * @return This TargetParameters for chaining
     */
    public TargetParameters disableTargetPrioritization()
    {
        this.prioritizer = null;
        this.priorityCheckInterval = 0;
        return this;
    }

    /**
     * Sets the maximum time a target can be unseen before being forgotten.
     * Only relevant if using line-of-sight conditions.
     *
     * @param millis Time in milliseconds
     * @return This TargetParameters for chaining
     */
    public TargetParameters setMaxTargetUnseenTime(long millis)
    {
        this.maxTargetUnseenTimeMillis = Math.max(0, millis);
        return this;
    }


    // ==================== Blacklist Management ====================

    /**
     * Enables/disables the ability to blacklist mobs.
     *
     * @return This TargetParameters for chaining
     */
    public TargetParameters enableBlackListMobs()
    {
        canBlackListMobs = true;
        return this;
    }

    public TargetParameters disableBlackListMobs()
    {
        canBlackListMobs = false;
        return this;
    }

    public boolean canBlackListMobs()
    {
        return canBlackListMobs;
    }


    // ==================== Multi-Target Management ====================

    /**
     * Gets the primary target (same as mob.getTarget()).
     *
     * @return The primary target or null
     */
    public LivingEntity getPrimaryTarget()
    {
        return targetStack.getPrimaryTarget();
    }

    /**
     * Sets the primary target.
     *
     * @param target The target to set
     */
    public void setPrimaryTarget(LivingEntity target)
    {
        targetStack.setPrimaryTarget(target);
    }

    /**
     * Adds a secondary target for multi-target tracking.
     *
     * @param target The target to add
     */
    public void addSecondaryTarget(LivingEntity target)
    {
        targetStack.addSecondaryTarget(target);
    }

    /**
     * Gets secondary targets.
     *
     * @return List of secondary targets
     */
    public List<LivingEntity> getSecondaryTargets()
    {
        return targetStack.getSecondaryTargets();
    }

    /**
     * Gets up to N secondary targets.
     *
     * @param count Number of targets to retrieve
     * @return List of secondary targets
     */
    public List<LivingEntity> getSecondaryTargets(int count)
    {
        return targetStack.getSecondaryTargets(count);
    }

    /**
     * Gets all targets (primary + secondary).
     *
     * @return List of all targets
     */
    public List<LivingEntity> getAllTargets()
    {
        return targetStack.getAllTargets();
    }

    /**
     * Checks if we have any targets.
     *
     * @return true if primary or secondary targets exist
     */
    public boolean hasTargets()
    {
        return targetStack.hasTargets();
    }

    /**
     * Gets the TargetStack for direct manipulation if needed.
     *
     * @return The TargetStack
     */
    public TargetStack getTargetStack()
    {
        return targetStack;
    }

    /**
     * Gets all targets in the target stack that are within the given distance from the origin.
     *
     * @param origin   The block position to measure distance from
     * @param distance The maximum distance
     * @return List of targets within range
     */
    public ArrayList<LivingEntity> getTargetsWithin(BlockPos origin, float distance)
    {
        ArrayList<LivingEntity> result = new ArrayList<>();
        for (LivingEntity target : targetStack.getAllTargets())
        {
            if (BlockAlgorithms.getBlockDistance(origin, target.blockPosition()) <= distance)
            {
                result.add(target);
            }
        }
        return result;
    }


    // ==================== Core Targeting Logic ====================

    // Predicate to test if valid target
    public final Predicate<LivingEntity> isPossibleNewTargetValid = new Predicate<LivingEntity>() {
        @Override
        public boolean test(LivingEntity livingEntity) {

            if(isSculkHordeAllied)
            {
                return isEntityValidSculkHordeTarget(livingEntity);
            }

            return isEntityValidTarget(livingEntity);
        }
    };

    public List<LivingEntity> getNewTargetsWithin(Vec3 origin, int range)
    {
        List<LivingEntity> possibleTargets =
                this.mob.level().getEntitiesOfClass(
                        LivingEntity.class,
                        HitboxUtil.createBoundingBoxCubeAtBlockPos(origin, range * 2),
                        isPossibleNewTargetValid);

        return possibleTargets;
    }


    public void debugPrint(LivingEntity e, String message)
    {
        String Header = "isEntityValid | ";
        String mobName = this.mob == null ? "null " : this.mob.getClass().getSimpleName();

        if(this.mob != null && DebuggerSystem.entityDebuggerModule.isMobBeingDebugged(this.mob))
        {
            DebuggerSystem.entityDebuggerModule.logDebug(Header + mobName + e.getClass().getSimpleName() + " " + message);
        }
    }


    /**
     * Core validation logic for targeting entities.
     * Uses modular filters, custom conditions, and legacy settings.
     *
     * @param e The entity to validate
     * @return true if the entity is a valid target
     */
    public boolean isEntityValidSculkHordeTarget(LivingEntity e)
    {

        // Check explicit deny list
        if (EntityAlgorithms.isInvalidTargetForSculkHorde(e))
        {
            debugPrint(e, "is explicitly denied.");
            return false;
        }

        // Check blacklist first (fast path)
        if (e instanceof Mob && isOnBlackList((Mob) e))
        {
            debugPrint(e, "is on Blacklist. Denied.");
            return false;
        }

        // Special entity types are always valid
        if (e instanceof InfestationPurifierEntity)
        {
            debugPrint(e, "is Infestation Purifier. Approved.");
            return true;
        }

        if (e instanceof Player)
        {
            debugPrint(e, "is Player. Approved.");
            return true;
        }

        // Check built-in filters
        if (!checkBuiltInFilters(e))
        {
            return false;
        }

        if(mob != null)
        {
            // Check custom conditions
            for (TargetCondition condition : customConditions)
            {
                if (!condition.isMet(e, mob))
                {
                    debugPrint(e, "failed custom condition. Denied.");
                    return false;
                }
            }
        }
        return true;
    }

    public boolean isEntityValidTarget(LivingEntity e)
    {

        if(isEntityUntargetable(e))
        {
            return false;
        }

        // Check blacklist first (fast path)
        if (e instanceof Mob && isOnBlackList((Mob) e))
        {
            debugPrint(e, "is on Blacklist. Denied.");
            return false;
        }

        // Check built-in filters
        if(!checkBuiltInFilters(e))
        {
            return false;
        }

        if(mob != null)
        {
            // Check custom conditions
            for (TargetCondition condition : customConditions)
            {
                if (!condition.isMet(e, mob))
                {
                    debugPrint(e, "failed custom condition. Denied.");
                    return false;
                }
            }
        }
        return true;
    }

    /**
     * Checks built-in filter configuration against entity properties.
     *
     * @param e The entity to check
     * @return true if entity passes all enabled filters
     */
    private boolean checkBuiltInFilters(LivingEntity e)
    {
        // Check swimmer/walker filters
        boolean isSwimmer = isLivingEntitySwimmer(e);
        boolean isFlier = isLivingEntityFlying(e);
        boolean isWalker = !isLivingEntityFlying(e) && !isSwimmer;

        if(isSculkLivingEntity.test(e) && !isFilterEnabled(TargetFilter.SCULK_HORDE_ENTITY))
        {
            debugPrint(e, "is sculk horde entity. Denied.");
            return false;
        }

        if(EntityAlgorithms.isLivingEntityAllyToSculkHorde(e) && !isFilterEnabled(TargetFilter.ALLIED_TO_SCULK_HORDE))
        {
            debugPrint(e, "is allied sculk horde. Denied.");
            return false;
        }

        if (isSwimmer && !isFilterEnabled(TargetFilter.SWIMMERS))
        {
            debugPrint(e, "is swimmer. Denied.");
            return false;
        }

        if (isWalker && !isFilterEnabled(TargetFilter.WALKERS))
        {
            debugPrint(e, "is walker. Denied.");
            return false;
        }

        if(isFlier && !isFilterEnabled(TargetFilter.FLIERS))
        {
            debugPrint(e, "is flier. Denied.");
            return false;
        }

        // Check infection status
        boolean isInfected = isLivingEntityInfected(e);
        if (isInfected && !isFilterEnabled(TargetFilter.INFECTED_BY_SCULK))
        {
            debugPrint(e, "is infected but we don't target infected. Denied.");
            return false;
        }

        // Check hostility status
        boolean isHostile = isLivingEntityHostile(e);

        if (isHostile && !isFilterEnabled(TargetFilter.HOSTILE_TO_SCULK))
        {
            debugPrint(e, "is hostile but we don't target hostiles. Denied.");
            return false;
        }

        if (!isHostile && !isSculkLivingEntity.test(e) && !EntityAlgorithms.isLivingEntityAllyToSculkHorde(e) && !isFilterEnabled(TargetFilter.PASSIVE_TO_SCULK))
        {
            debugPrint(e, "is passive but we don't target passives. Denied.");
            return false;
        }

        return true;
    }

    /**
     * Updates target validity based on retention rules.
     * Called periodically to validate that current targets should be kept.
     * Also handles priority revaluation if enabled.
     */
    public void updateTargets()
    {
        if (mob == null)
        {
            return;
        }

        // Only run if mob is a leader of a squad or were not in a squad at all
        Optional<Squad> squad = SquadSystem.getSquadOfLivingEntity(mob);
        if(squad.isPresent()) {

            boolean isLeaderOfSquad = squad.get().isLeader(mob.getUUID());
            if (!isLeaderOfSquad) {
                return;
            }
        }

        // Update line-of-sight tracking for all targets
        for (LivingEntity target : targetStack.getAllTargets())
        {
            if (mob.getSensing().hasLineOfSight(target))
            {
                targetTicksSinceSeen.put(target.getUUID(), 0);
            }
            else
            {
                targetTicksSinceSeen.put(target.getUUID(), targetTicksSinceSeen.getOrDefault(target.getUUID(), 0) + 1);
            }
        }

        // Check if primary target should be retained
        LivingEntity primary = targetStack.getPrimaryTarget();
        if (primary != null)
        {
            // Check retention rules
            boolean shouldKeep = true;
            for (TargetRetention rule : retentionRules)
            {
                if (!rule.shouldRetain(primary, mob))
                {
                    DebuggerSystem.entityDebuggerModule.logDebug("TargetRetention | " + mob.getClass().getSimpleName() + " | Removing Target for rule " + rule.getClass().getSimpleName());
                    shouldKeep = false;
                    break;
                }
            }

            if (!shouldKeep)
            {
                targetStack.setPrimaryTarget(null);
            }
        }

        // Clean up invalid secondary targets
        List<LivingEntity> secondaryTargets = targetStack.getSecondaryTargets();
        secondaryTargets.removeIf(target -> {
            for (TargetRetention rule : retentionRules)
            {
                if (!rule.shouldRetain(target, mob))
                {

                    return true;
                }
            }
            return false;
        });

        // Handle priority revaluation if enabled
        if (prioritizer != null && priorityCheckInterval >= 0)
        {
            priorityCheckCounter++;
            if (priorityCheckCounter >= priorityCheckInterval || priorityCheckInterval == 0)
            {
                priorityCheckCounter = 0;
                revaluateTargetPriority();
            }
        }
    }

    /**
     * Revaluates if the current primary target remains the highest priority.
     * If not, promotes a better secondary target or clears primary target.
     */
    private void revaluateTargetPriority()
    {
        if (prioritizer == null || mob == null)
        {
            return;
        }

        List<LivingEntity> allTargets = targetStack.getAllTargets();
        if (allTargets.isEmpty())
        {
            return;
        }

        // Find the highest priority target
        LivingEntity bestTarget = allTargets.get(0);
        double bestScore = prioritizer.getPriorityScore(bestTarget, mob);

        for (LivingEntity target : allTargets)
        {
            double score = prioritizer.getPriorityScore(target, mob);
            if (score < bestScore)
            {
                bestScore = score;
                bestTarget = target;
            }
        }

        // If best target is not the primary, promote it
        if (bestTarget != targetStack.getPrimaryTarget() && targetStack.getSecondaryTargets().contains(bestTarget))
        {
            targetStack.promoteSecondaryTarget(bestTarget);
        }
    }

    // ==================== Utility Methods ====================

    /**
     * Checks if an entity can be reached via pathfinding.
     * Used for reach-based targeting constraints.
     *
     * @param pTarget The target entity
     * @return true if reachable within 50 blocks
     */
    private boolean canReach(LivingEntity pTarget)
    {
        if(mob == null)
        {
            return false;
        }

        Path path = this.mob.getNavigation().createPath(pTarget, 0);
        if (path == null)
        {
            return false;
        }

        Node pathpoint = path.getEndNode();
        if (pathpoint == null)
        {
            return false;
        }

        int i = pathpoint.x - Mth.floor(pTarget.getX());
        int j = pathpoint.z - Mth.floor(pTarget.getZ());
        return (double)(i * i + j * j) <= 50;
    }

    // ==================== Blacklist Management ====================

    /**
     * Adds an entity to the blacklist.
     *
     * @param entity The entity to blacklist
     */
    public void addToBlackList(Mob entity)
    {
        if (canBlackListMobs && entity != null && mob != null)
        {
            blacklist.put(entity.getUUID(), entity.level().getGameTime());
            DebuggerSystem.entityDebuggerModule.logDebug("Blacklist | " + mob.getClass().getSimpleName() + " | " + entity.getClass().getSimpleName() + " was added to Blacklist");
        }
    }

    /**
     * Removes an entity from the blacklist.
     *
     * @param entity The entity to remove
     */
    public void removeFromBlackList(Mob entity)
    {
        if (entity != null && mob != null)
        {
            blacklist.remove(entity.getUUID());
        }
    }

    /**
     * Checks if an entity is on the blacklist.
     *
     * @param entity The entity to check
     * @return true if blacklisted
     */
    public boolean isOnBlackList(Mob entity)
    {
        return entity != null && blacklist.containsKey(entity.getUUID());
    }

    /**
     * Clears the blacklist completely.
     */
    public void clearBlackList()
    {
        blacklist.clear();
    }

    /**
     * Gets the size of the blacklist.
     *
     * @return Number of blacklisted entities
     */
    public int getBlacklistSize()
    {
        return blacklist.size();
    }

    /**
     * Gets the number of ticks since a target was last seen.
     *
     * @param target The target to check
     * @return Ticks since last seen, or 0 if never tracked or currently seen
     */
    public int getTicksSinceTargetLastSeen(LivingEntity target)
    {
        return targetTicksSinceSeen.getOrDefault(target.getUUID(), 0);
    }

    /**
     * Creates a deep copy of this TargetParameters instance.
     * All settings, filters, conditions, and current targets are copied.
     *
     * @return A new TargetParameters instance with the same configuration
     */
    public TargetParameters copy()
    {
        TargetParameters copy = new TargetParameters(this.mob, this.isSculkHordeAllied, this.targetStack.getMaxSecondaryTargets());

        // Copy enabled filters
        copy.enabledFilters.addAll(this.enabledFilters);

        // Copy custom conditions
        copy.customConditions.addAll(this.customConditions);

        // Copy retention rules
        copy.retentionRules.addAll(this.retentionRules);

        // Copy blacklist
        copy.blacklist.putAll(this.blacklist);

        // Copy boolean flags
        copy.canBlackListMobs = this.canBlackListMobs;

        // Copy secondary targets
        for (LivingEntity sec : this.targetStack.getSecondaryTargets())
        {
            copy.targetStack.addSecondaryTarget(sec);
        }

        // Copy primary target
        copy.setPrimaryTarget(this.getPrimaryTarget());

        // Copy prioritizer settings
        copy.prioritizer = this.prioritizer;
        copy.priorityCheckInterval = this.priorityCheckInterval;
        copy.priorityCheckCounter = this.priorityCheckCounter;

        // Copy target tracking data
        copy.targetTicksSinceSeen.putAll(this.targetTicksSinceSeen);
        copy.maxTargetUnseenTimeMillis = this.maxTargetUnseenTimeMillis;

        return copy;
    }

    public TargetParameters copy(Mob mobIn)
    {
        TargetParameters copy = this.copy();
        copy.mob = mobIn;
        return copy;
    }

}
