package com.github.sculkhoard.core;

import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import software.bernie.geckolib3.GeckoLib;

//The @Mod tag is here to let the compiler know that this is our main mod class
//It takes in our mod id so it knows what mod it is loading.
@Mod(SculkHoard.MOD_ID)
public class SculkHoard {

    //Here I've created a variable of our mod id so we can use it throughout our project
    public static final String MOD_ID = "sculkhoard";
    public static final ItemGroup SCULK_GROUP = new CreativeTabGroup("sculkhoard_tab");
    public static boolean DEBUG_MODE = false;

    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHoard() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        GeckoLib.initialize();

        ItemRegistry.ITEMS.register(bus); //Load Items
        TileEntityRegistry.register(bus); //Load Tile Entities
        BlockRegistry.BLOCKS.register(bus); //Load Blocks
        EntityRegistry.register(bus); //Load Entities

        //If dev environment
        if(!FMLEnvironment.production)
        {
            DEBUG_MODE = true;
        }
    }

    //Add Creative Item Tab
    public static class CreativeTabGroup extends ItemGroup {
        public CreativeTabGroup(String label) {
            super(label);
        }
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.SCULK_MATTER.get());
        }
    }

}