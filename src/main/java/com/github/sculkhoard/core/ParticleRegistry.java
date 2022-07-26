package com.github.sculkhoard.core;

import com.github.sculkhoard.client.particle.SculkCrustParticle;
import com.github.sculkhoard.common.item.DevConversionWand;
import net.minecraft.particles.BasicParticleType;
import net.minecraft.particles.ParticleType;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.ObjectHolder;

public class ParticleRegistry {
    public static final DeferredRegister<ParticleType<?>> PARTICLE_TYPES =  DeferredRegister.create(ForgeRegistries.PARTICLE_TYPES, SculkHoard.MOD_ID);

    public static final RegistryObject<BasicParticleType> SCULK_CRUST_PARTICLE = PARTICLE_TYPES.register("sculk_crust_particle", () -> new BasicParticleType(false));

}
