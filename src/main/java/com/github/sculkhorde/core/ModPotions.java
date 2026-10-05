package com.github.sculkhorde.core;

import net.minecraft.core.registries.Registries;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.Potions;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.brewing.RegisterBrewingRecipesEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import com.github.sculkhorde.util.TickUnits;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(Registries.POTION, SculkHorde.MOD_ID);

    public static final DeferredHolder<Potion, Potion> CORRODED_POTION = POTIONS.register(
            "corroded_potion",
            () -> new Potion(new MobEffectInstance(ModMobEffects.CORRODED, TickUnits.convertMinutesToTicks(6), 0))
    );

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }

    public static void registerRecipes(RegisterBrewingRecipesEvent event) {
        event.getBuilder().addMix(
                Potions.THICK,
                ModItems.SCULK_ACIDIC_PROJECTILE.get(),
                CORRODED_POTION
        );
    }
}
