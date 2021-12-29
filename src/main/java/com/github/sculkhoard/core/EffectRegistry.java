package com.github.sculkhoard.core;

import com.github.sculkhoard.common.effect.SculkInfectionEffect;
import net.minecraft.entity.EntityType;
import net.minecraft.potion.Effect;
import net.minecraft.potion.EffectType;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class EffectRegistry {

    public static final DeferredRegister<Effect> EFFECTS = DeferredRegister.create(ForgeRegistries.POTIONS, SculkHoard.MOD_ID);

    public static final RegistryObject<SculkInfectionEffect> SCULK_INFECTION = EFFECTS.register("sculk_infected", () -> new SculkInfectionEffect());

}
