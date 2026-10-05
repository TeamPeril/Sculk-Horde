package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

/** Codec-backed custom advancement trigger for 1.21.1. */
public class ContributeTrigger extends SimpleCriterionTrigger<ContributeTrigger.ContributeCriterion> implements CustomCriterionTrigger {
    public static final ContributeTrigger INSTANCE = new ContributeTrigger();
    static final ResourceLocation ID = ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "contribute_trigger");

    @Override
    public Codec<ContributeCriterion> codec() {
        return ContributeCriterion.CODEC;
    }

    @Override
    public void trigger(ServerPlayer player) {
        trigger(player, Predicates.alwaysTrue());
    }

    public static record ContributeCriterion(java.util.Optional<ContextAwarePredicate> player)
            implements SimpleCriterionTrigger.SimpleInstance {
        public static final Codec<ContributeCriterion> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                EntityPredicate.ADVANCEMENT_CODEC.optionalFieldOf("player")
                        .forGetter(ContributeCriterion::player)
        ).apply(instance, ContributeCriterion::new));
    }
}
