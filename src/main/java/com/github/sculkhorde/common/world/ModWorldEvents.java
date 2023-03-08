package com.github.sculkhorde.common.world;

import com.github.sculkhorde.common.world.gen.ModEntityGen;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraftforge.event.world.BiomeLoadingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID)
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
