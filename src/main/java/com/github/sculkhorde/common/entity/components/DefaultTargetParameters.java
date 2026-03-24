package com.github.sculkhorde.common.entity.components;

import com.github.sculkhorde.util.TickUnits;

public class DefaultTargetParameters {

    public static TargetParameters create() {
        return new TargetParameters();
    }

    public final static TargetParameters DefaultGroundMeleeCombat = create()
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED, TargetFilter.WALKERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)));

    public final static TargetParameters DefaultGroundRangedCombat = create()
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED, TargetFilter.WALKERS, TargetFilter.FLIERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)));

    public final static TargetParameters DefaultGroundMeleeInfector = create()
            .filterBy(TargetFilter.WALKERS, TargetFilter.PASSIVES)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));

    public final static TargetParameters DefaultSwimmerMeleeCombat = create()
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED, TargetFilter.SWIMMERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)));

    public final static TargetParameters DefaultSwimmerRangedCombat = DefaultSwimmerMeleeCombat.copy();

    public final static TargetParameters DefaultSwimmerMeleeInfector = create()
            .filterBy(TargetFilter.SWIMMERS, TargetFilter.PASSIVES)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));

    public final static TargetParameters DefaultFlyerMeleeCombat = create()
            .addCondition(TargetCondition.mustSee())
            .filterBy(TargetFilter.HOSTILES, TargetFilter.INFECTED, TargetFilter.WALKERS, TargetFilter.FLIERS)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(10)));

    public final static TargetParameters DefaultFlyerRangedCombat = DefaultFlyerMeleeCombat.copy();

    public final static TargetParameters DefaultFlyerMeleeInfector = create()
            .filterBy(TargetFilter.WALKERS, TargetFilter.FLIERS, TargetFilter.PASSIVES)
            .addRetentionRule(TargetRetention.lineOfSightTimeout(TickUnits.convertSecondsToTicks(30)));
}
