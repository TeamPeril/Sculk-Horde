package com.github.sculkhoard.core;

import net.minecraft.particles.ParticleType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =
         DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SculkHoard.MOD_ID);

    /*
    public static void register(IEventBus eventBus)
    {
        PARTICLE_TYPES.register(eventBus);
    }

    public static final RegistryObject<SimpleParticleType> SCULK_SPORE =
            PARTICLE_TYPES.register("sculk_spore", () -> new SimpleAnimatedParticle(true));

     */
}
