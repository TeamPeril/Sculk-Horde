package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class GravemindEvolveImmatureTrigger extends SimpleCriterionTrigger<GravemindEvolveImmatureTrigger.GravemindEvoleImmatureCriterion> implements CustomCriterionTrigger{

    public static final GravemindEvolveImmatureTrigger INSTANCE = new GravemindEvolveImmatureTrigger();

    /**
     * Need to be registered in {@link com.github.sculkhorde.util.ModEventSubscriber}.
     */
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "gravemind_evolve_immature_trigger");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public GravemindEvoleImmatureCriterion createInstance(JsonObject jsonObject, ContextAwarePredicate awarePredicate, DeserializationContext deserializationContext) {
        return new GravemindEvoleImmatureCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class GravemindEvoleImmatureCriterion extends AbstractCriterionTriggerInstance {

        public GravemindEvoleImmatureCriterion(ContextAwarePredicate awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
