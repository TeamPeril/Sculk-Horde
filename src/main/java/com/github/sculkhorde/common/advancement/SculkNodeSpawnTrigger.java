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

public class SculkNodeSpawnTrigger extends SimpleCriterionTrigger<SculkNodeSpawnTrigger.SculkNodeSpawnCriterion> implements CustomCriterionTrigger{

    public static final SculkNodeSpawnTrigger INSTANCE = new SculkNodeSpawnTrigger();
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "sculk_node_spawn");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public SculkNodeSpawnCriterion createInstance(JsonObject jsonObject, ContextAwarePredicate awarePredicate, DeserializationContext deserializationContext) {
        return new SculkNodeSpawnCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class SculkNodeSpawnCriterion extends AbstractCriterionTriggerInstance {

        public SculkNodeSpawnCriterion(ContextAwarePredicate awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
