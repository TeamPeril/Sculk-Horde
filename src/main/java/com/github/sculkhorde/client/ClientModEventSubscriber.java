package com.github.sculkhorde.client;

import com.github.sculkhorde.client.particle.SculkCrustParticle;
import com.github.sculkhorde.client.renderer.entity.*;
import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.core.ParticleRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.particle.ParticleManager;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ParticleFactoryRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    public final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();

    public <T extends Entity> void register(EntityType<T> p_229087_1_, EntityRenderer<? super T> p_229087_2_) {
        this.renderers.put(p_229087_1_, p_229087_2_);
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenders(final FMLClientSetupEvent event) {

        // Register Renderers for Entities
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ZOMBIE, SculkZombieRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_MITE, SculkMiteRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_MITE_AGGRESSOR, SculkMiteAggressorRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_SPITTER, SculkSpitterRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_BEE_INFECTOR, SculkBeeInfectorRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_BEE_HARVESTER, SculkBeeHarvesterRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.CUSTOM_ITEM_PROJECTILE_ENTITY, m -> new SpriteRenderer<CustomItemProjectileEntity>(m, Minecraft.getInstance().getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ACIDIC_PROJECTILE_ENTITY, m -> new SpriteRenderer<CustomItemProjectileEntity>(m, Minecraft.getInstance().getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_HATCHER, SculkHatcherRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.CURSOR_LONG_RANGE, CursorLongRangeRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.CURSOR_SHORT_RANGE, CursorShortRangeRenderer::new);

        // Register renderer for sculk crust partcile
        event.enqueueWork(() -> Minecraft.getInstance().particleEngine.register(ParticleRegistry.SCULK_CRUST_PARTICLE.get(), SculkCrustParticle.Factory::new));


        // Register render layers for blocks
        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SPIKE.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SMALL_SHROOM.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS_SHORT.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SCULK_SHROOM_CULTURE.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.COCOON.get(), RenderType.translucent()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.COCOON_ROOT.get(), RenderType.translucent()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.VEIN.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SCULK_SUMMONER_BLOCK.get(), RenderType.cutout()));

    }

    /**
     * Used to register custom particle renders.
     * Currently handles the Gorgon particle
     *
     * @param event the particle factory registry event
     **/
    @SubscribeEvent
    public static void registerFactories(final ParticleFactoryRegisterEvent event)
    {
        ParticleManager particles = Minecraft.getInstance().particleEngine;

        particles.register(ParticleRegistry.SCULK_CRUST_PARTICLE.get(), SculkCrustParticle.Factory::new);
        
    }
}
