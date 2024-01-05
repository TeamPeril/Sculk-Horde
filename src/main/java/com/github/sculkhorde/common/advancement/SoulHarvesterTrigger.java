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

public class SoulHarvesterTrigger extends SimpleCriterionTrigger<SoulHarvesterTrigger.SoulHarvesterCriterion> implements CustomCriterionTrigger{

    public static final SoulHarvesterTrigger INSTANCE = new SoulHarvesterTrigger();
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "soul_harvester");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public SoulHarvesterCriterion createInstance(JsonObject jsonObject, ContextAwarePredicate awarePredicate, DeserializationContext deserializationContext) {
        return new SoulHarvesterCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class SoulHarvesterCriterion extends AbstractCriterionTriggerInstance {

        public SoulHarvesterCriterion(ContextAwarePredicate awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
