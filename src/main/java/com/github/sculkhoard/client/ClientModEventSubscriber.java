package com.github.sculkhoard.client;

import com.github.sculkhoard.client.renderer.entity.SculkMiteRenderer;
import com.github.sculkhoard.client.renderer.entity.SculkZombieRenderer;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.RenderTypeLookup;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenders(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ZOMBIE
                .get(), SculkZombieRenderer::new);

        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_MITE
                .get(), SculkMiteRenderer::new);

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.SPIKE.get(), RenderType.cutout());
        });

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.SMALL_SHROOM.get(), RenderType.cutout());
        });

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS.get(), RenderType.cutout());
        });

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.GRASS_SHORT.get(), RenderType.cutout());
        });

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.COCOON_GOUP.get(), RenderType.translucent());
        });

        event.enqueueWork(() -> {
            RenderTypeLookup.setRenderLayer(BlockRegistry.VEIN.get(), RenderType.translucent());
        });
    }
}
