# TargetParameters Refactoring - Usage Guide

## Overview

The refactored `TargetParameters` system is now **modular, composable, and extensible**. It supports:

- ✅ **Modular filters** via `TargetFilter` enum (replaces 6+ boolean fields)
- ✅ **Custom conditions** for specialized targeting logic
- ✅ **Multi-target tracking** with primary + secondary targets
- ✅ **Target retention rules** to decide when to forget targets
- ✅ **Priority-based revaluation** to smartly swap targets when better ones exist
- ✅ **Backward compatibility** with existing code

---

## Quick Start Examples

### Basic Configuration (Old Way - Still Works)

```java
// Old API - still fully supported
private TargetParameters TARGET_PARAMETERS = new TargetParameters(this)
    .enableTargetHostiles()
    .enableTargetInfected()
    .enableMustReachTarget();
```

### New Modular Approach (Recommended)

```java
// New API - cleaner, more flexible
private TargetParameters TARGET_PARAMETERS = new TargetParameters(this, 2) // Support 2 secondary targets
    .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED)
    .enableMustReachTarget();
```

---

## Feature 1: Modular Filters

Instead of 6+ boolean flags, use the `TargetFilter` enum:

```java
// Filter by entity type
params.filterBy(TargetFilter.HOSTILES);          // Attack hostile mobs
params.filterBy(TargetFilter.PASSIVES);          // Attack passive mobs  
params.filterBy(TargetFilter.INFECTED);          // Attack infected entities
params.filterBy(TargetFilter.SWIMMERS);          // Attack water-based mobs
params.filterBy(TargetFilter.WALKERS);           // Attack land-based mobs
params.filterBy(TargetFilter.ENTITIES_IN_WATER); // Attack mobs currently in water

// Combine multiple filters
params.filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED, TargetFilter.SWIMMERS);

// Remove filters
params.excludeFilter(TargetFilter.WALKERS);

// Check if filter is active
if (params.isFilterEnabled(TargetFilter.HOSTILES)) { /* ... */ }
```

---

## Feature 2: Custom Conditions

Add specialized validation logic for any requirement:

```java
// Health-based conditions
params.addCondition(TargetCondition.healthBelow(50));   // Only targets below 50% health
params.addCondition(TargetCondition.healthAbove(30));   // Only targets above 30% health

// Visibility-based conditions
params.addCondition(TargetCondition.mustSee());         // Only targets in line-of-sight

// Distance-based conditions
params.addCondition(TargetCondition.withinDistance(16)); // Only targets within 16 blocks

// Custom conditions
params.addCondition((target, isExisting, mob) -> {
    // Only target entities NOT wearing armor
    return target instanceof LivingEntity && 
           ((LivingEntity)target).getArmorValue() == 0;
});

// Lambda version (shorter)
params.addCondition((target, _, __) -> target.isNoAi() == false);

// Remove conditions
params.removeCondition(someCondition);
```

### Real-World Example: "Siege" Target Pattern

```java
// Prioritize weak, isolated targets
params.filterBy(TargetFilter.PASSIVES)
    .addCondition(TargetCondition.healthBelow(50))      // Weak targets only
    .addCondition((target, _, mob) -> {                 // Isolated check
        int nearbyEntities = 0;
        for (Entity e : mob.level().getEntities(null, target.getBoundingBox().inflate(16))) {
            if (e != target && e.isAlive()) nearbyEntities++;
        }
        return nearbyEntities < 3; // Less than 3 allies nearby
    });
```

---

## Feature 3: Multi-Target Tracking

Maintain multiple targets for sophisticated behavior patterns:

```java
// Create with capacity for 3 secondary targets
TargetParameters params = new TargetParameters(mob, 3);

// Set primary target
params.setPrimaryTarget(target1);

// Add secondary targets
params.addSecondaryTarget(target2);
params.addSecondaryTarget(target3);
params.addSecondaryTarget(target4);

// Retrieve targets
LivingEntity primary = params.getPrimaryTarget();
List<LivingEntity> secondary = params.getSecondaryTargets();
List<LivingEntity> all = params.getAllTargets(); // [primary, secondary...]

// Get first N secondary targets
List<LivingEntity> closest2 = params.getSecondaryTargets(2);

// Direct manipulation (if needed)
TargetStack stack = params.getTargetStack();
stack.promoteSecondaryTarget(target3); // Swap: target3 becomes primary
```

### Use Case: "Divide and Conquer" AI

```java
// Squad behavior: everyone attacks the primary, but has awareness of others
List<LivingEntity> enemies = findAllEnemiesNearby();
if (!enemies.isEmpty()) {
    params.setPrimaryTarget(enemies.get(0)); // Focus fire
    for (int i = 1; i < Math.min(3, enemies.size()); i++) {
        params.addSecondaryTarget(enemies.get(i)); // Awareness of flankers
    }
}
```

---

## Feature 4: Target Retention Rules

Control when targets should be forgotten:

```java
// Line-of-sight timeout
params.addRetentionRule(
    TargetRetention.lineOfSightTimeout(30)
);

// Distance-based
params.addRetentionRule(
    TargetRetention.maxDistance(32) // Forget if >32 blocks away
);

// Custom retention logic
params.addRetentionRule((target, mob) -> {
    // Keep target if: still alive AND not frozen
    return target.isAlive() && !target.hasEffect(MobEffects.MOVEMENT_SLOWDOWN);
});

// Always keep (useful for combining rules)
params.addRetentionRule(TargetRetention.always());

// Remove a rule
params.removeRetentionRule(someRule);

// Call this every tick or periodically to validate
params.updateTargets(); // Checks all retention rules
```

### Real-World Example: "Forget Fleeing Enemies"

```java
// Drop targets that flee too far or for too long
params.addRetentionRule((target, mob) -> {
    double distance = mob.distanceTo(target);
    boolean canSee = mob.getSensing().hasLineOfSight(target);
    
    if (!canSee) {
        // Lost sight - 10 second timeout
        return System.currentTimeMillis() - lastSightTime < 10_000;
    }
    
    // Can see - must stay within 40 blocks
    return distance < 40;
});
```

---

## Feature 5: Priority-Based Target Revaluation

Automatically switch to better targets:

```java
// Enable priority revaluation
TargetPrioritizer prioritizer = TargetPrioritizer.byDistance();
params.enableTargetPrioritization(prioritizer, 20); // Check every 20 ticks

// Built-in prioritizers
TargetPrioritizer.byDistance();    // Closer = higher priority
TargetPrioritizer.byHealth();      // Lower health = higher priority

// Composite prioritizer (weighted scoring)
TargetPrioritizer composite = TargetPrioritizer.composite(
    new TargetPrioritizer[] {
        TargetPrioritizer.byDistance(),
        TargetPrioritizer.byHealth()
    },
    new double[] { 0.6, 0.4 } // 60% distance, 40% health
);
params.enableTargetPrioritization(composite, 10);

// Disable priority checking
params.disableTargetPrioritization();

// Custom prioritizer
params.enableTargetPrioritization((target, mob) -> {
    // Lower score = higher priority
    double distScore = mob.distanceTo(target);
    double healthScore = target.getHealth() / target.getMaxHealth() * 100;
    return distScore + healthScore; // Combined score
}, 15);

// Call this every tick
params.updateTargets(); // Also runs priority revaluation if enabled
```

### Real-World Example: "Smart Damage Control"

```java
// Prioritize damaged allies, but favor closer ones
TargetPrioritizer smartDefense = (target, mob) -> {
    double distance = mob.distanceTo(target);
    double healthPercent = (target.getHealth() / target.getMaxHealth()) * 100;
    
    // Score = distance + "badness" of health
    // Low health = high priority, even if farther
    return distance * 0.5 + (100 - healthPercent) * 2;
};

params.enableTargetPrioritization(smartDefense, 5);
```

---

## Complete Configuration Example

Sophisticated "intelligent hunter" entity:

```java
private TargetParameters TARGET_PARAMETERS = new TargetParameters(this, 2)
    // Filter to attack hostiles and infected passives
    .filterBy(TargetFilter.HOSTILES, TargetFilter.PASSIVES, TargetFilter.INFECTED)
    
    // Ignore weak targets and targets in water
    .excludeFilter(TargetFilter.ENTITIES_IN_WATER)
    .addCondition(TargetCondition.healthAbove(25))
    
    // Must be within 32 blocks
    .addCondition(TargetCondition.withinDistance(32))
    
    // Must be able to reach
    .enableMustReachTarget()
    
    // Forget targets that flee
    .addRetentionRule((target, mob) -> {
        double dist = mob.distanceTo(target);
        return dist < 48 && mob.getSensing().hasLineOfSight(target);
    })
    
    // Recheck priority every 15 ticks - smart target switching
    .enableTargetPrioritization(
        TargetPrioritizer.composite(
            new TargetPrioritizer[] {
                TargetPrioritizer.byDistance(),
                TargetPrioritizer.byHealth()
            },
            new double[] { 0.7, 0.3 }
        ),
        15
    );

// Call once per AI tick (NOT every tick)
public void tickAi() {
    TARGET_PARAMETERS.updateTargets(); // Validate retention + recheck priority
    // ... rest of AI logic
}
```

---

## Migration Guide: Old → New

### Single Boolean → Filter

```java
// OLD
private boolean targetHostiles = false;
// ...
enableTargetHostiles()
isTargetingHostiles()

// NEW
.filterBy(TargetFilter.HOSTILES)
.isFilterEnabled(TargetFilter.HOSTILES)
```

### Health Threshold → Condition

```java
// OLD
ignoreTargetBelow50PercentHealth()
isIgnoringTargetBelow50PercentHealth()

// NEW
.addCondition(TargetCondition.healthAbove(50))
// Check in custom condition
```

### Manual Reach Check → Retention Rule

```java
// OLD - Had to check manually
if (params.mustReachTarget() && !params.canReach(target)) {
    // Drop target
}

// NEW - Automatic in updateTargets()
.addRetentionRule((t, mob) -> {
    // canReach logic...
    return true; // or false to drop
})
```

---

## Performance Considerations

1. **updateTargets()** - Call sparingly (not every tick)
   - Check retention rules: O(num_targets * num_rules)
   - Priority revaluation: O(num_targets * 2) scoring ops

2. **Custom conditions** - Evaluated during targeting
   - Simple checks (health, distance): O(1)
   - Complex checks (raytraces, pathfinding): Avoid per-tick
   - Use `validatingExistingTarget` hint to skip expensive checks

3. **isEntityValidTarget()** - Called frequently
   - Fast path: blacklist check first
   - Avoid expensive pathfinding in NEW target validation
   - Filter checks before condition checks

---

## Debugging

Enable debug output (commented in code):

```java
// In TargetParameters.debugPrint():
if(SculkHorde.isDebugMode()) { 
    SculkHorde.LOGGER.debug(Header + mob + checkType + e.getScoreboardName() + " " + message); 
}
```

---

## Key Improvements Over Original

| Aspect | Old | New |
|--------|-----|-----|
| **Type Filtering** | 6+ boolean fields | 1 enum + Set |
| **Custom Logic** | None (hardcoded) | TargetCondition interface |
| **Multiple Targets** | None | TargetStack with 0-N secondary |
| **Target Retention** | Hardcoded checks | Composable TargetRetention rules |
| **Priority Switching** | None | TargetPrioritizer + revaluation |
| **Backward Compat** | N/A | Full - all old methods work |
| **Extensibility** | Low | High - interfaces throughout |


