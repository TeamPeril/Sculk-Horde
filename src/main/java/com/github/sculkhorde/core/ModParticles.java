package com.github.sculkhorde.core;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SculkHorde.MOD_ID);

    public static final RegistryObject<SimpleParticleType> SCULK_CRUST_PARTICLE = PARTICLE_TYPES.register("sculk_crust_particle", () -> new SimpleParticleType(false));

}
