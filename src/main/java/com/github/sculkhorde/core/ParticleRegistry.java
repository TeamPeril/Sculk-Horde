package com.github.sculkhorde.core;

import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SculkHorde.MOD_ID);

    public static final RegistryObject<BasicParticleType> SCULK_CRUST_PARTICLE = PARTICLE_TYPES.register("sculk_crust_particle", () -> new BasicParticleType(false));

}
