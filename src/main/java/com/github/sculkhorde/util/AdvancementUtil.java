package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.*;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

public final class AdvancementUtil {

    public static AdvancementHolder getAdvancement(ResourceLocation id) {
        return ServerLifecycleHooks.getCurrentServer().getAdvancements().get(id);
    }

    public static boolean completeAdvancement(ServerPlayer player, ResourceLocation id, String criterion) {
        AdvancementHolder adv = getAdvancement(id);

        if (adv != null)
            return player.getAdvancements().award(adv, criterion);

        return false;
    }

    public static boolean isAdvancementCompleted(ServerPlayer player, ResourceLocation id) {
        AdvancementHolder adv = ServerLifecycleHooks.getCurrentServer().getAdvancements().get(id);

        if (adv != null)
        {
            return player.getAdvancements().getOrStartProgress(adv).isDone();
        }

        return false;
    }

    public static void giveAdvancementToAllPlayers(ServerLevel level, CustomCriterionTrigger trigger) {
        for (ServerPlayer player : level.players()) {
            trigger.trigger(player);
        }
    }

    public static void giveAdvancementToPlayer(ServerPlayer player, CustomCriterionTrigger trigger) {
        trigger.trigger(player);
    }


    public static void advancementHandlingTick(ServerLevel level)
    {
        if(SculkHorde.gravemind == null)
        {
            return;
        }

        // If Immature, give all players advancement
        if(SculkHorde.gravemind.getEvolutionState().ordinal() >= Gravemind.evolution_states.Immature.ordinal())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(level, GravemindEvolveImmatureTrigger.INSTANCE);
        }

        // If Immature, give all players advancement
        if(SculkHorde.gravemind.getEvolutionState().ordinal() >= Gravemind.evolution_states.Mature.ordinal())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(level, GravemindEvolveMatureTrigger.INSTANCE);
        }

        // If Immature, give all players advancement
        if(ModSavedData.getSaveData().isHordeDefeated())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(level, SculkHordeDefeatTrigger.INSTANCE);
        }

        if(!ModSavedData.getSaveData().getNodeEntries().isEmpty())
        {
            AdvancementUtil.giveAdvancementToAllPlayers(level, SculkNodeSpawnTrigger.INSTANCE);
        }

    }

}
