package com.github.sculkhoard.common.world;

import com.github.sculkhoard.common.world.gen.ModEntityGen;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHoard.MOD_ID)
public class ModWorldEvents {
    /* biomeLoadingEvent()
     * @Description Registers stuff when biomeLoadingEvent is called.
     */
    @SubscribeEvent
    public static void biomeLoadingEvent(final BiomeLoadingEvent event)
    {
        ModEntityGen.onEntitySpawn(event);
    }
}
