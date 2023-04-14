package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.item.ModCreativeModeTab;
import com.github.sculkhorde.common.pools.PoolBlocks;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.mojang.logging.LogUtils;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.CreativeModeTabEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
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
    public static boolean DEBUG_MODE = false;
    public static EntityFactory entityFactory = new EntityFactory();
    public static Gravemind gravemind;
    public static InfestationConversionHandler infestationConversionTable;
    public static PoolBlocks randomSculkFlora;
    public static final Logger LOGGER = LogUtils.getLogger();

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

        bus.addListener(this::addCreative);

        //If dev environment
        if(!FMLEnvironment.production)
        {
            DEBUG_MODE = true;
        }
    }

    private void addCreative(CreativeModeTabEvent.BuildContents event) {
        if (event.getTab() == ModCreativeModeTab.CREATIVE_TAB) {
            event.accept(ItemRegistry.DEV_WAND);
            event.accept(ItemRegistry.DEV_NODE_SPAWNER);
            event.accept(ItemRegistry.INFESTATION_PURIFIER);
            event.accept(ItemRegistry.SCULK_ACIDIC_PROJECTILE);
            event.accept(ItemRegistry.SCULK_RESIN);
            event.accept(ItemRegistry.CALCITE_CLUMP);
            event.accept(ItemRegistry.SCULK_MATTER);
            event.accept(BlockRegistry.CALCITE_ORE);
            event.accept(BlockRegistry.CRUST);
            event.accept(Blocks.SCULK);
            event.accept(BlockRegistry.SCULK_NODE_BLOCK);
            event.accept(BlockRegistry.SCULK_ARACHNOID);
            event.accept(BlockRegistry.SCULK_DURA_MATTER);
            event.accept(BlockRegistry.SCULK_BEE_NEST_BLOCK);
            event.accept(BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK);
            event.accept(BlockRegistry.SCULK_SUMMONER_BLOCK);
            event.accept(Blocks.SCULK_CATALYST);
            event.accept(Blocks.SCULK_SHRIEKER);
            event.accept(Blocks.SCULK_SENSOR);
            event.accept(BlockRegistry.SCULK_MASS);
            event.accept(BlockRegistry.GRASS);
            event.accept(BlockRegistry.GRASS_SHORT);
            event.accept(BlockRegistry.SCULK_SHROOM_CULTURE);
            event.accept(BlockRegistry.SMALL_SHROOM);
            event.accept(BlockRegistry.SPIKE);
            event.accept(BlockRegistry.VEIN);
        }
    }





}