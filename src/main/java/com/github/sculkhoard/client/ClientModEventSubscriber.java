package com.github.sculkhoard.client;

import com.github.sculkhoard.client.renderer.entity.*;
import com.github.sculkhoard.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhoard.common.entity.projectile.SculkAcidicProjectileEntity;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import com.google.common.collect.Maps;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.SpriteRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

import java.util.Map;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    public final Map<EntityType<?>, EntityRenderer<?>> renderers = Maps.newHashMap();

    public <T extends Entity> void register(EntityType<T> p_229087_1_, EntityRenderer<? super T> p_229087_2_) {
        this.renderers.put(p_229087_1_, p_229087_2_);
    }


    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenders(final FMLClientSetupEvent event) {

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ZOMBIE, SculkZombieRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_MITE, SculkMiteRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_MITE_AGGRESSOR, SculkMiteAggressorRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_SPITTER, SculkSpitterRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_BEE_INFECTOR, SculkBeeInfectorRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_BEE_HARVESTER, SculkBeeHarvesterRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.CUSTOM_ITEM_PROJECTILE_ENTITY, m -> new SpriteRenderer<CustomItemProjectileEntity>(m, Minecraft.getInstance().getItemRenderer()));

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ACIDIC_PROJECTILE_ENTITY, m -> new SpriteRenderer<CustomItemProjectileEntity>(m, Minecraft.getInstance().getItemRenderer()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SPIKE.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SMALL_SHROOM.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS_SHORT.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.SCULK_SHROOM_CULTURE.get(), RenderType.cutout()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.COCOON.get(), RenderType.translucent()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.COCOON_ROOT.get(), RenderType.translucent()));

        event.enqueueWork(() -> RenderTypeLookup.setRenderLayer(BlockRegistry.VEIN.get(), RenderType.translucent()));



    }
}
