package com.github.sculkhoard.client;

import com.github.sculkhoard.client.renderer.entity.SculkZombieEntityRenderer;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.sculkhoard;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(modid = sculkhoard.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEventSubscriber {

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenders(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityRegistry.SCULK_ZOMBIE
                .get(), SculkZombieEntityRenderer::new);
    }
}
