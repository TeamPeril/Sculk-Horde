package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.ContextAwarePredicate;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class GravemindEvolveMatureTrigger extends SimpleCriterionTrigger<GravemindEvolveMatureTrigger.GravemindEvolveMatureCriterion> implements CustomCriterionTrigger{

    public static final GravemindEvolveMatureTrigger INSTANCE = new GravemindEvolveMatureTrigger();

    /**
     * Need to be registered in {@link com.github.sculkhorde.util.ModEventSubscriber}.
     */
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "gravemind_evolve_mature_trigger");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public GravemindEvolveMatureCriterion createInstance(JsonObject jsonObject, ContextAwarePredicate awarePredicate, DeserializationContext deserializationContext) {
        return new GravemindEvolveMatureCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class GravemindEvolveMatureCriterion extends AbstractCriterionTriggerInstance {

        public GravemindEvolveMatureCriterion(ContextAwarePredicate awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
