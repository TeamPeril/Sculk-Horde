package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.item.ModCreativeModeTab;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.util.DeathAreaInvestigator;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.loading.FMLLoader;
import software.bernie.geckolib.GeckoLib;
import org.slf4j.Logger;
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
    public static boolean DEBUG_MODE = !FMLLoader.getLaunchHandler().isProduction();
    public static EntityFactory entityFactory = new EntityFactory();
    public static Gravemind gravemind;
    public static ModSavedData savedData;
    public static InfestationConversionHandler infestationConversionTable;
    public static PoolBlocks randomSculkFlora;
    public static DeathAreaInvestigator deathAreaInvestigator;
    public static RaidHandler raidHandler;
    public static final Logger LOGGER = LogUtils.getLogger();

    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHorde()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);
        GeckoLib.initialize();
        ItemRegistry.ITEMS.register(bus); //Load Items
        BlockEntityRegistry.register(bus); //Load Tile Entities
        BlockRegistry.BLOCKS.register(bus); //Load Blocks
        EntityRegistry.register(bus); //Load Entities (this may not be necessary anymore)
        bus.register(EntityRegistry.class); //Load Entities
        StructureRegistry.STRUCTURES.register(bus); //Load Structures
        StructureRegistry.STRUCTURE_PIECES.register(bus); //Load Structure Pieces
        ProcessorRegistry.PROCESSORS.register(bus); //Load Processors


        EffectRegistry.EFFECTS.register(bus); //Load Effects
        ParticleRegistry.PARTICLE_TYPES.register(bus); //Load Particles
        SoundRegistry.SOUND_EVENTS.register(bus); //Load Sounds

        ModCreativeModeTab.TABS.register(bus); //Load Creative Tabs

        //If dev environment
        if(!FMLEnvironment.production)
        {
            DEBUG_MODE = true;
        }
    }

}