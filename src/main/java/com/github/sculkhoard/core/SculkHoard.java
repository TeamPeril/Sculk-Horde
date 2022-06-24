package com.github.sculkhoard.core;

import com.github.sculkhoard.common.block.BlockInfestation.InfestationConversionTable;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactory;
import com.github.sculkhoard.common.entity.entity_factory.EntityFactoryEntry;
import com.github.sculkhoard.common.entity.gravemind.Gravemind;
import com.github.sculkhoard.common.pools.PoolBlocks;
import com.github.sculkhoard.util.PacketToggleChunk;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.simple.SimpleChannel;
import software.bernie.geckolib3.GeckoLib;

import java.util.ArrayList;
//HOW TO EXPORT MOD: https://www.youtube.com/watch?v=x3wKsiQ37Wc

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
    public static Gravemind gravemind;
    public static InfestationConversionTable infestationConversionTable;
    public static PoolBlocks randomSculkFlora;

    //This is something related to chunk loaders
    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(new ResourceLocation("sculkhoard", "main"), () -> "1", "1"::equals, "1"::equals);

    //This is the instance of our class, and we register it to the ModEventBus (which I have stored in a variable).
    public SculkHoard() {
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

        //Something related to chunk loading
        CHANNEL.registerMessage(0, PacketToggleChunk.class, PacketToggleChunk::encode, PacketToggleChunk::decode, PacketToggleChunk::handle);
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