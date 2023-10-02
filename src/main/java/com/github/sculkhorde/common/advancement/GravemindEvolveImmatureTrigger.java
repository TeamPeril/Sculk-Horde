package com.github.sculkhorde.common.advancement;

import com.github.sculkhorde.core.SculkHorde;
import com.google.common.base.Predicates;
import com.google.gson.JsonObject;
import net.minecraft.advancements.critereon.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.GsonHelper;

public class GravemindEvolveImmatureTrigger extends SimpleCriterionTrigger<GravemindEvolveImmatureTrigger.GravemindEvoleImmatureCriterion> implements CustomCriterionTrigger{

    public static final GravemindEvolveImmatureTrigger INSTANCE = new GravemindEvolveImmatureTrigger();
    static final ResourceLocation ID = new ResourceLocation(SculkHorde.MOD_ID, "gravemind_evolve_immature");

    @Override
    public ResourceLocation getId() {
        return ID;
    }

    public void trigger(ServerPlayer player) {
        this.trigger(player, Predicates.alwaysTrue());
    }

    @Override
    protected GravemindEvoleImmatureCriterion createInstance(JsonObject pJson, EntityPredicate.Composite pPlayer, DeserializationContext pContext) {
        return new GravemindEvoleImmatureCriterion(pPlayer);
    }

    public static class GravemindEvoleImmatureCriterion extends AbstractCriterionTriggerInstance {

        public GravemindEvoleImmatureCriterion(EntityPredicate.Composite pPlayer) {
            super(ID, pPlayer);
        }
    }
}
