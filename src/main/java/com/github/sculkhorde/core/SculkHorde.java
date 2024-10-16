package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.loot.ModLootModifier;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.gravemind.*;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.events.EventHandler;
import com.github.sculkhorde.misc.contributions.ContributionHandler;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import com.github.sculkhorde.util.DeathAreaInvestigator;
import com.github.sculkhorde.misc.StatisticsData;
import com.mojang.logging.LogUtils;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLPaths;
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
    private static boolean DEBUG_MODE = false;
    public static EntityFactory entityFactory = new EntityFactory();
    public static Gravemind gravemind;
    public static ModSavedData savedData;
    public static BlockInfestationTable explicitInfectableBlocks;
    public static BlockInfestationTable tagInfectableBlocks;
    public static BlockInfestationTable tagInfectableNonFullBlocks;
    public static BlockInfestationTable configInfectableBlocks;

    public static BlockInfestationTable[] INFESTATION_TABLES;

    public static PoolBlocks randomSculkFlora;
    public static DeathAreaInvestigator deathAreaInvestigator;
    public static RaidHandler raidHandler;
    public static EventHandler eventHandler;
    public static CursorHandler cursorHandler = new CursorHandler();
    public static SculkNodesHandler sculkNodesHandler;
    public static StatisticsData statisticsData;
    public static BlockEntityChunkLoaderHelper blockEntityChunkLoaderHelper;
    public static EntityChunkLoaderHelper entityChunkLoaderHelper = new EntityChunkLoaderHelper();
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final ContributionHandler contributionHandler = new ContributionHandler();
    public static final SculkPopulationHandler populationHandler = new SculkPopulationHandler();

    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHorde()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.register(this);

        ModConfig.loadConfig(ModConfig.SERVER_SPEC, FMLPaths.CONFIGDIR.get().resolve(MOD_ID + "_config.toml").toString());

        GeckoLib.initialize();
        ModItems.ITEMS.register(bus); //Load Items
        ModBlockEntities.register(bus); //Load Tile Entities
        ModBlocks.BLOCKS.register(bus); //Load Blocks
        ModEntities.register(bus); //Load Entities (this may not be necessary anymore)
        bus.register(ModEntities.class); //Load Entities
        ModStructures.STRUCTURES.register(bus); //Load Structures
        ModStructures.STRUCTURE_PIECES.register(bus); //Load Structure Pieces
        ModStructureProcessors.PROCESSORS.register(bus); //Load Processors
        ModCommands.init();
        ModPotions.register(bus); //Load Potions
        ModMenuTypes.register(bus); //Load Menus
        ModMobEffects.EFFECTS.register(bus); //Load Effects
        ModParticles.PARTICLE_TYPES.register(bus); //Load Particles
        ModSounds.SOUND_EVENTS.register(bus); //Load Sounds
        ModCreativeModeTab.TABS.register(bus); //Load Creative Tabs
        ModRecipes.register(bus); //Load Recipes
        ModLootModifier.register(bus);
    }

    public static boolean isDebugMode() {
        return DEBUG_MODE;
    }

    public static void setDebugMode(boolean debugMode) {
        DEBUG_MODE = debugMode;
        savedData.setDirty();
    }

}