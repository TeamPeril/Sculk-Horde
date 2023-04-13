package com.github.sculkhorde.core;

import com.github.sculkhorde.common.effect.SculkInfectionEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EffectRegistry {

    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SculkHorde.MOD_ID);

    public static final RegistryObject<SculkInfectionEffect> SCULK_INFECTION = EFFECTS.register("sculk_infected", () -> new SculkInfectionEffect());

}
