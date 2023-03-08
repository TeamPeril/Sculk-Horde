package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import software.bernie.geckolib3.GeckoLib;
//HOW TO EXPORT MOD: https://www.youtube.com/watch?v=x3wKsiQ37Wc

//The @Mod tag is here to let the compiler know that this is our main mod class
//It takes in our mod id so it knows what mod it is loading.
@Mod(SculkHorde.MOD_ID)
public class SculkHorde {

    //Here I've created a variable of our mod id so we can use it throughout our project
    public static final String MOD_ID = "sculkhorde";
    //The file name in the world data folder.
    public static final String SAVE_DATA_ID = SculkHorde.MOD_ID + "_gravemind_memory";
    //The Creative Tab that all the items appear in
    public static final ItemGroup SCULK_GROUP = new CreativeTabGroup("sculkhorde_tab");
    public static boolean DEBUG_MODE = false;
    public static EntityFactory entityFactory = new EntityFactory();
    public static Gravemind gravemind;
    public static InfestationConversionHandler infestationConversionTable;
    public static PoolBlocks randomSculkFlora;

    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHorde()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        GeckoLib.initialize();

        ItemRegistry.ITEMS.register(bus); //Load Items
        TileEntityRegistry.register(bus); //Load Tile Entities
        BlockRegistry.BLOCKS.register(bus); //Load Blocks
        EntityRegistry.register(bus); //Load Entities (this may not be necessary anymore)
        bus.register(EntityRegistry.class); //Load Entities

        EffectRegistry.EFFECTS.register(bus); //Load Effects
        ParticleRegistry.PARTICLE_TYPES.register(bus); //Load Particles

        //If dev environment
        if(!FMLEnvironment.production)
        {
            DEBUG_MODE = true;
        }
    }

    //Add Creative Item Tab
    public static class CreativeTabGroup extends ItemGroup
    {
        public CreativeTabGroup(String label) {
            super(label);
        }
        @Override
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.SCULK_MATTER.get());
        }
    }

}