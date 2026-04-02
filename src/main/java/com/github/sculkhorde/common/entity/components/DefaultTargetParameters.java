package com.github.sculkhorde.common.entity.components;

import com.github.sculkhorde.util.TickUnits;

public class DefaultTargetParameters {

    public static TargetParameters create(boolean isSculkHordeAlly) {
        return new TargetParameters(isSculkHordeAlly);
    }
    public static TargetParameters create(boolean isSculkHordeAlly, int maxSecondaryTargets) {
        return new TargetParameters(isSculkHordeAlly, maxSecondaryTargets);
    }

    public final static TargetPrioritizer DefaultCombatComposite = TargetPrioritizer.composite(
            new TargetPrioritizer[] {
                    TargetPrioritizer.byDistance(),
                    TargetPrioritizer.byHealth()
            },
            new double[] { 0.6, 0.4 } // 60% distance, 40% health
    );

    public final static TargetParameters DefaultGroundMeleeCombat = create(true, 5)
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILE_TO_SCULK, TargetFilter.INFECTED_BY_SCULK, TargetFilter.WALKERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)))
            .enableTargetPrioritization(DefaultCombatComposite, TickUnits.convertSecondsToTicks(1));

    public final static TargetParameters DefaultGroundRangedCombat = create(true, 10)
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILE_TO_SCULK, TargetFilter.INFECTED_BY_SCULK, TargetFilter.WALKERS, TargetFilter.FLIERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)))
            .enableTargetPrioritization(DefaultCombatComposite, TickUnits.convertSecondsToTicks(1));

    public final static TargetParameters DefaultGroundMeleeInfector = create(true, 1)
            .filterBy(TargetFilter.WALKERS, TargetFilter.PASSIVE_TO_SCULK, TargetFilter.HOSTILE_TO_SCULK)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));

    public final static TargetParameters DefaultSwimmerMeleeCombat = create(true, 5)
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILE_TO_SCULK, TargetFilter.INFECTED_BY_SCULK, TargetFilter.SWIMMERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)));

    public final static TargetParameters DefaultSwimmerRangedCombat = DefaultSwimmerMeleeCombat.copy();

    public final static TargetParameters DefaultSwimmerMeleeInfector = create(true, 1)
            .filterBy(TargetFilter.SWIMMERS, TargetFilter.PASSIVE_TO_SCULK, TargetFilter.HOSTILE_TO_SCULK)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));

    public final static TargetParameters DefaultFlyerMeleeCombat = create(true, 10)
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILE_TO_SCULK, TargetFilter.INFECTED_BY_SCULK, TargetFilter.WALKERS, TargetFilter.FLIERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)))
            .enableTargetPrioritization(DefaultCombatComposite, TickUnits.convertSecondsToTicks(1));

    public final static TargetParameters DefaultFlyerRangedCombat = DefaultFlyerMeleeCombat.copy();

    public final static TargetParameters DefaultFlyerMeleeInfector = create(true, 1)
            .filterBy(TargetFilter.WALKERS, TargetFilter.FLIERS, TargetFilter.PASSIVE_TO_SCULK, TargetFilter.HOSTILE_TO_SCULK)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));

    public final static TargetParameters DefaultPurityGroundMeleeCombat = create(false, 10)
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.SCULK_HORDE_ENTITY, TargetFilter.ALLIED_TO_SCULK_HORDE, TargetFilter.WALKERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)))
            .enableTargetPrioritization(DefaultCombatComposite, TickUnits.convertSecondsToTicks(0.5F));
}
