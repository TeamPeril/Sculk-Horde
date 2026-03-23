# TargetParameters Quick Reference

## At a Glance

```java
// Create with multi-target support
TargetParameters params = new TargetParameters(mob, 2);

// Configure what to attack
params.filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED);

// Add custom validation
params.addCondition(TargetCondition.healthAbove(25));

// Add target forgetting logic
params.addRetentionRule(TargetRetention.maxDistance(40));

// Enable smart target switching
params.enableTargetPrioritization(TargetPrioritizer.byHealth(), 10);

// Use it
params.updateTargets(); // Validate + recheck priority
LivingEntity primary = params.getPrimaryTarget();
List<LivingEntity> secondary = params.getSecondaryTargets();
```

---

## Filters (What to Attack)

| Filter | Meaning |
|--------|---------|
| `HOSTILES` | Attack aggressive mobs |
| `PASSIVES` | Attack peaceful mobs |
| `INFECTED` | Attack infected entities |
| `SWIMMERS` | Attack water-based mobs |
| `WALKERS` | Attack land-based mobs |
| `ENTITIES_IN_WATER` | Attack mobs in water |

```java
.filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED)  // Multiple
.excludeFilter(TargetFilter.WALKERS)                      // Remove
.isFilterEnabled(TargetFilter.SWIMMERS)                   // Check
```

---

## Conditions (Custom Rules)

```java
// Built-in factories
.addCondition(TargetCondition.healthBelow(50))
.addCondition(TargetCondition.healthAbove(30))
.addCondition(TargetCondition.withinDistance(16))
.addCondition(TargetCondition.mustSee())

// Custom lambda
.addCondition((target, isExisting, mob) -> target.isNoAi() == false)

// Remove
.removeCondition(someCondition)
```

---

## Retention Rules (When to Forget)

```java
// Built-in factories
.addRetentionRule(TargetRetention.maxDistance(32))
.addRetentionRule(TargetRetention.always())

// Custom lambda
.addRetentionRule((target, mob) -> {
    return mob.distanceTo(target) < 40;
})

// Validate every tick
.updateTargets(); // Drops targets that fail rules
```

---

## Prioritizers (Smart Switching)

```java
// Built-in prioritizers
TargetPrioritizer.byDistance()      // Closer = higher priority
TargetPrioritizer.byHealth()        // Weaker = higher priority

// Composite (weighted)
TargetPrioritizer.composite(
    [byDistance(), byHealth()],
    [0.6, 0.4]  // 60% distance, 40% health
)

// Custom
(target, mob) -> {
    return mob.distanceTo(target) + target.getHealth();
}

// Enable
.enableTargetPrioritization(prioritizer, 10)  // Check every 10 ticks
.disableTargetPrioritization()

// Recheck
params.updateTargets();
```

---

## Multi-Targeting

```java
// Create with secondary capacity
new TargetParameters(mob, 3)  // 1 primary + 3 secondary

// Use
params.setPrimaryTarget(main);
params.addSecondaryTarget(ally1);
params.addSecondaryTarget(ally2);

// Retrieve
LivingEntity primary = params.getPrimaryTarget();
List<LivingEntity> all = params.getAllTargets();
List<LivingEntity> sec = params.getSecondaryTargets();
List<LivingEntity> first2 = params.getSecondaryTargets(2);

// Smart promotion
params.getTargetStack().promoteSecondaryTarget(target);
```

---

## Common Patterns

### Attack Hostiles Only
```java
new TargetParameters(mob)
    .filterBy(TargetFilter.HOSTILES)
```

### Attack Everything Weak
```java
new TargetParameters(mob)
    .filterBy(TargetFilter.HOSTILES, TargetFilter.PASSIVES)
    .addCondition(TargetCondition.healthBelow(50))
```

### Intelligent Multi-Targeting
```java
new TargetParameters(mob, 2)
    .filterBy(TargetFilter.HOSTILES)
    .enableTargetPrioritization(TargetPrioritizer.byDistance(), 5)
    .addRetentionRule(TargetRetention.maxDistance(32))
```

### Sophisticated Hunting
```java
new TargetParameters(mob, 3)
    .filterBy(TargetFilter.PASSIVES)
    .addCondition(TargetCondition.healthBelow(60))
    .addCondition((t, _, m) -> m.level().getEntities(null, 
        t.getBoundingBox().inflate(16)).size() < 3)  // Isolated
    .enableTargetPrioritization(TargetPrioritizer.byHealth(), 8)
    .addRetentionRule(TargetRetention.maxDistance(40))
```

---

## Backward Compatibility

Old code still works unchanged:

```java
new TargetParameters(mob)
    .enableTargetHostiles()           // ✅ Works
    .disableTargetWalkers()           // ✅ Works
    .enableMustReachTarget()          // ✅ Works
    .ignoreTargetBelow50PercentHealth() // ✅ Works
```

Internally maps to new system - no changes needed!

---

## Performance Tips

1. **Call `updateTargets()` sparingly** (not every tick)
   ```java
   if (tickCounter % 5 == 0) params.updateTargets();
   ```

2. **Use configurable priority check intervals**
   ```java
   .enableTargetPrioritization(prioritizer, 20)  // Every 20 ticks
   ```

3. **Keep conditions simple** (O(1) operations)
   - Health checks ✅
   - Distance checks ✅
   - Pathfinding ❌ (expensive)

4. **Filter before conditions**
   - Filters are O(1), run first
   - Conditions only on filtered entities

---

## Example: Complete Setup

```java
public class MyEntity extends Mob {
    private TargetParameters params;
    
    public MyEntity(EntityType<? extends MyEntity> type, Level level) {
        super(type, level);
        params = new TargetParameters(this, 2)
            .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED)
            .addCondition(TargetCondition.withinDistance(32))
            .addCondition(TargetCondition.healthAbove(10))
            .addRetentionRule((target, mob) -> {
                return mob.distanceTo(target) < 48;
            })
            .enableTargetPrioritization(TargetPrioritizer.byHealth(), 8);
    }
    
    @Override
    public void tick() {
        super.tick();
        
        // Validate targets once per tick or less
        if (this.tickCount % 2 == 0) {
            params.updateTargets();
        }
        
        // Use primary target
        LivingEntity target = params.getPrimaryTarget();
        if (target != null) {
            this.getNavigation().moveTo(target, 1.0);
            // Attack logic...
        }
    }
}
```

---

## Documentation Files

- **TARGETPARAMETERS_USAGE_GUIDE.md** - Comprehensive feature guide
- **MIGRATION_EXAMPLES.md** - Real entity examples + migration strategy
- **ARCHITECTURE.md** - Design deep-dive + rationale
- **IMPLEMENTATION_SUMMARY.md** - This implementation

---

## API Summary

### TargetParameters Methods
```
filterBy(...)                                    // Add filters
excludeFilter(...)                               // Remove filters
isFilterEnabled(filter)                          // Check filter
addCondition(condition)                          // Add condition
removeCondition(condition)                       // Remove condition
addRetentionRule(rule)                           // Add retention rule
removeRetentionRule(rule)                        // Remove rule
enableTargetPrioritization(prioritizer, ticks)   // Enable priority
disableTargetPrioritization()                    // Disable priority
setMaxTargetUnseenTime(millis)                   // Config timeout
updateTargets()                                  // Validate + recheck
getPrimaryTarget() / setPrimaryTarget(target)    // Primary target
getSecondaryTargets() / addSecondaryTarget()     // Secondary targets
getAllTargets()                                  // All targets
hasTargets()                                     // Check if any targets
getTargetStack()                                 // Direct access
isEntityValidTarget(entity, isExisting)          // Validate entity
isOnBlackList(mob) / addToBlackList(mob)        // Blacklist management
```

### Factory Methods

**TargetCondition**
- `healthBelow(percent)`
- `healthAbove(percent)`
- `withinDistance(blocks)`
- Custom via lambda

**TargetRetention**
- `lineOfSightTimeout(ticks)`
- `maxDistance(blocks)`
- `always()`
- Custom via lambda

**TargetPrioritizer**
- `byDistance()`
- `byHealth()`
- `composite(prioritizers, weights)`
- Custom via lambda

**TargetFilter** (Enum)
- `HOSTILES`, `PASSIVES`, `INFECTED`
- `SWIMMERS`, `WALKERS`, `ENTITIES_IN_WATER`

---

## Migration Checklist

- [ ] New code compiles without errors
- [ ] Existing entities still work (backward compat)
- [ ] New features available for use
- [ ] Documentation read and understood
- [ ] Plan migration path (phased recommended)
- [ ] Test complex scenarios
- [ ] Profile performance impact
- [ ] Adopt new patterns incrementally

---

**Ready to use! Start simple, enhance gradually.**


