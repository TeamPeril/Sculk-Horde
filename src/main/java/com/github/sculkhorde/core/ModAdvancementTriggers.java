package com.github.sculkhorde.core;

import com.github.sculkhorde.common.advancement.*;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModAdvancementTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGERS = DeferredRegister.create(Registries.TRIGGER_TYPE, SculkHorde.MOD_ID);

    public static final DeferredHolder<CriterionTrigger<?>, GravemindEvolveImmatureTrigger> GRAVEMIND_EVOLVE_IMMATURE = TRIGGERS.register("gravemind_evolve_immature_trigger", () -> GravemindEvolveImmatureTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, GravemindEvolveMatureTrigger> GRAVEMIND_EVOLVE_MATURE = TRIGGERS.register("gravemind_evolve_mature_trigger", () -> GravemindEvolveMatureTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, SculkHordeStartTrigger> SCULK_HORDE_START = TRIGGERS.register("sculk_horde_start", () -> SculkHordeStartTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, SculkNodeSpawnTrigger> SCULK_NODE_SPAWN = TRIGGERS.register("sculk_node_spawn", () -> SculkNodeSpawnTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, SoulHarvesterTrigger> SOUL_HARVESTER = TRIGGERS.register("soul_harvester_trigger", () -> SoulHarvesterTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, SculkHordeDefeatTrigger> SCULK_HORDE_DEFEAT = TRIGGERS.register("sculk_horde_defeat_trigger", () -> SculkHordeDefeatTrigger.INSTANCE);
    public static final DeferredHolder<CriterionTrigger<?>, ContributeTrigger> CONTRIBUTE = TRIGGERS.register("contribute_trigger", () -> ContributeTrigger.INSTANCE);

    public static void register(IEventBus eventBus) {
        TRIGGERS.register(eventBus);
    }
}
