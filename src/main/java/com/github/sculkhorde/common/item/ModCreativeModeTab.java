package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ItemRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    public static CreativeModeTab CREATIVE_TAB;

    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        CREATIVE_TAB = event.registerCreativeModeTab(new ResourceLocation(SculkHorde.MOD_ID, "sculk_horde_tab"),
                builder -> builder.icon(() -> new ItemStack(ItemRegistry.SCULK_MATTER.get())).title(Component.literal("Sculk Horde")).build());
    }
}
