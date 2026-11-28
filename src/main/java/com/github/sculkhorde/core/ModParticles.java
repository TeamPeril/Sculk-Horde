package com.github.sculkhorde.core;

import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.core.particles.ParticleType;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;

public class ModParticles {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =  DeferredRegister.create(Registries.PARTICLE_TYPE, SculkHorde.MOD_ID);

    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> SCULK_CRUST_PARTICLE = PARTICLE_TYPES.register("sculk_crust_particle", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> BURROWED_BURST_PARTICLE = PARTICLE_TYPES.register("burrowed_burst_particle", () -> new SimpleParticleType(false));
    public static final DeferredHolder<ParticleType<?>, SimpleParticleType> ANCIENT_DIALECT_PARTICLE = PARTICLE_TYPES.register("ancient_dialect_particle", () -> new SimpleParticleType(false));

}
