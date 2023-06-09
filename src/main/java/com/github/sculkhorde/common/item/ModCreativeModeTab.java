package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ItemRegistry;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    // TODO Port
    //public static final DeferredRegister<CreativeModeTab> CREATIVE_TAB = DeferredRegister.create(ForgeRegistries., SculkHorde.MOD_ID);

    /*
    @SubscribeEvent
    public static void registerCreativeModeTabs(CreativeModeTabEvent.Register event) {
        CREATIVE_TAB = event.registerCreativeModeTab(new ResourceLocation(SculkHorde.MOD_ID, "sculk_horde_tab"),
                builder -> builder.icon(() -> new ItemStack(ItemRegistry.SCULK_MATTER.get())).title(Component.literal("Sculk Horde")).build());
    }

     */
}
