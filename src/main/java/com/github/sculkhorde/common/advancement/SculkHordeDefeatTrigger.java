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

public class SculkHordeDefeatTrigger extends SimpleCriterionTrigger<SculkHordeDefeatTrigger.SculkHordeDefeatCriterion> implements CustomCriterionTrigger{

    public static final SculkHordeDefeatTrigger INSTANCE = new SculkHordeDefeatTrigger();

    /**
     * Need to be registered in {@link com.github.sculkhorde.util.ModEventSubscriber}.
     */
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "sculk_horde_defeat_trigger");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    @Override
    public SculkHordeDefeatCriterion createInstance(JsonObject jsonObject, ContextAwarePredicate awarePredicate, DeserializationContext deserializationContext) {
        return new SculkHordeDefeatCriterion(awarePredicate);
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    public static class SculkHordeDefeatCriterion extends AbstractCriterionTriggerInstance {

        public SculkHordeDefeatCriterion(ContextAwarePredicate awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
