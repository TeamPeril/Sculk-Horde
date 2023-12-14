package com.github.sculkhorde.core;

import net.minecraft.world.item.alchemy.Potion;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ModPotions {
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, SculkHorde.MOD_ID);

    //public static final RegistryObject<Potion> PURITY_POTION = POTIONS.register("purity_potion", () -> new Potion(new MobEffectInstance(ModMobEffects.SCULK_INFECTION.get(), 200, 0)));

    public static void register(IEventBus eventBus) {
        POTIONS.register(eventBus);
    }
}
