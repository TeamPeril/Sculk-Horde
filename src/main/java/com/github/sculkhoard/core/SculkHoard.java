package com.github.sculkhoard.core;

import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactoryEntry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import software.bernie.geckolib3.GeckoLib;

import java.util.ArrayList;

//The @Mod tag is here to let the compiler know that this is our main mod class
//It takes in our mod id so it knows what mod it is loading.
@Mod(SculkHoard.MOD_ID)
public class SculkHoard {

    //Here I've created a variable of our mod id so we can use it throughout our project
    public static final String MOD_ID = "sculkhoard";
    public static final String SAVE_DATA_ID = MOD_ID;
    public static final ItemGroup SCULK_GROUP = new CreativeTabGroup("sculkhoard_tab");
    public static boolean DEBUG_MODE = false;
    public static EntityFactory entityFactory = new EntityFactory();


    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHoard() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        GeckoLib.initialize();

        ItemRegistry.ITEMS.register(bus); //Load Items
        TileEntityRegistry.register(bus); //Load Tile Entities
        BlockRegistry.BLOCKS.register(bus); //Load Blocks
        EntityRegistry.register(bus); //Load Entities
        EffectRegistry.EFFECTS.register(bus); //Load Effects

        //If dev environment
        if(!FMLEnvironment.production)
        {
            DEBUG_MODE = true;
        }

        /**
         * This data structure will be used to spawn entities for the Sculk, but at a cost.
         * Every entity in this list has a cost associated with it.
         */
        //entityFactory =
        //entityFactory.addEntry(EntityRegistry.SCULK_ZOMBIE.get(), 20);
        //entityFactory.addEntry(EntityRegistry.SCULK_MITE.get(), 1);

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