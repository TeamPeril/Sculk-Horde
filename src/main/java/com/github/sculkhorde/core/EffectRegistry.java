package com.github.sculkhorde.core;

import com.github.sculkhorde.common.effect.SculkInfectionEffect;
import net.minecraft.potion.Effect;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectRegistry {

    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, SculkHorde.MOD_ID);

    public static final RegistryObject<SculkInfectionEffect> SCULK_INFECTION = EFFECTS.register("sculk_infected", () -> new SculkInfectionEffect());

}
