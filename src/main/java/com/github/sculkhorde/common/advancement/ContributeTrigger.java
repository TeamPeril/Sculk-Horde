package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.AbstractCriterionTriggerInstance;
import net.minecraft.advancements.critereon.DeserializationContext;
import net.minecraft.advancements.critereon.EntityPredicate;
import net.minecraft.advancements.critereon.SimpleCriterionTrigger;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;

public class ContributeTrigger extends SimpleCriterionTrigger<ContributeTrigger.ContributeCriterion> implements CustomCriterionTrigger{

    public static final ContributeTrigger INSTANCE = new ContributeTrigger();

    /**
     * Need to be registered in {@link com.github.sculkhorde.util.ModEventSubscriber}.
     */
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "contribute_trigger");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    @Override
    protected ContributeCriterion createInstance(JsonObject jsonObject, EntityPredicate.Composite composite, DeserializationContext deserializationContext) {
        return null;
    }

    public static class ContributeCriterion extends AbstractCriterionTriggerInstance {

        public ContributeCriterion(EntityPredicate.Composite awarePredicate) {
            super(ID, awarePredicate);
        }
    }
}
