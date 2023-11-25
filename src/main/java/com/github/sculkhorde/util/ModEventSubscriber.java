package com.github.sculkhorde.util;

import com.github.sculkhorde.common.advancement.GravemindEvolveImmatureTrigger;
import com.github.sculkhorde.common.advancement.SculkHordeStartTrigger;
import com.github.sculkhorde.common.advancement.SculkNodeSpawnTrigger;
import com.github.sculkhorde.common.block.InfestationEntries.BlockInfestationTable;
import com.github.sculkhorde.common.block.InfestationEntries.ITagInfestedBlock;
import com.github.sculkhorde.common.entity.*;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.SculkEndermanEntity;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.EntityFactory;
import com.github.sculkhorde.core.gravemind.Gravemind;
import com.github.sculkhorde.common.pools.PoolBlocks;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.SpawnPlacements;
import net.minecraft.world.item.Tiers;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModEventSubscriber {

    @SubscribeEvent
    public static void onCommonSetup(FMLCommonSetupEvent event)
    {
        //Add entries to the entity factory (please add them in order of cost, I don't want to sort)
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_SPORE_SPEWER.get(), (int) SculkSporeSpewerEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Immature).setLimit(1);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_RAVAGER.get(), (int) SculkRavagerEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature).setLimit(1);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_HATCHER.get(), (int) SculkHatcherEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_CREEPER.get(), 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_SPITTER.get(), 20, EntityFactory.StrategicValues.Ranged, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_ZOMBIE.get(), 20, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_VINDICATOR.get(), (int) SculkVindicatorEntity.MAX_HEALTH, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Immature);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_MITE_AGGRESSOR.get(), 6, EntityFactory.StrategicValues.Melee, Gravemind.evolution_states.Undeveloped);
        SculkHorde.entityFactory.addEntry(ModEntities.SCULK_MITE.get(), (int) SculkMiteEntity.MAX_HEALTH, EntityFactory.StrategicValues.Infector, Gravemind.evolution_states.Undeveloped);

        SculkHorde.blockInfestationTable = new BlockInfestationTable();

        // Add Log Tag
        SculkHorde.blockInfestationTable.addEntry(Blocks.DIRT, Blocks.SCULK.defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.COARSE_DIRT, Blocks.SCULK.defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.GRASS_BLOCK, Blocks.SCULK.defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.PODZOL, Blocks.SCULK.defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.CLAY, ModBlocks.INFESTED_CLAY.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.STONE, ModBlocks.INFESTED_STONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.DEEPSLATE, ModBlocks.INFESTED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.COBBLED_DEEPSLATE, ModBlocks.INFESTED_COBBLED_DEEPSLATE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.SAND, ModBlocks.INFESTED_SAND.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.SANDSTONE, ModBlocks.INFESTED_SANDSTONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.RED_SAND, ModBlocks.INFESTED_RED_SAND.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.DIORITE, ModBlocks.INFESTED_DIORITE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.GRANITE, ModBlocks.INFESTED_GRANITE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.ANDESITE, ModBlocks.INFESTED_ANDESITE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.TUFF, ModBlocks.INFESTED_TUFF.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.CALCITE, ModBlocks.INFESTED_CALCITE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.COBBLESTONE, ModBlocks.INFESTED_COBBLESTONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.MOSSY_COBBLESTONE, ModBlocks.INFESTED_MOSSY_COBBLESTONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.GRAVEL, ModBlocks.INFESTED_GRAVEL.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.MUD, ModBlocks.INFESTED_MUD.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.PACKED_MUD, ModBlocks.INFESTED_PACKED_MUD.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.MUD_BRICKS, ModBlocks.INFESTED_MUD_BRICKS.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.SNOW_BLOCK, ModBlocks.INFESTED_SNOW.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.MOSS_BLOCK, ModBlocks.INFESTED_MOSS.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.TERRACOTTA, ModBlocks.INFESTED_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.BLACK_TERRACOTTA, ModBlocks.INFESTED_BLACK_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.BLUE_TERRACOTTA, ModBlocks.INFESTED_BLUE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.BROWN_TERRACOTTA, ModBlocks.INFESTED_BROWN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.CYAN_TERRACOTTA, ModBlocks.INFESTED_CYAN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.GRAY_TERRACOTTA, ModBlocks.INFESTED_GRAY_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.GREEN_TERRACOTTA, ModBlocks.INFESTED_GREEN_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.LIGHT_BLUE_TERRACOTTA, ModBlocks.INFESTED_LIGHT_BLUE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.LIGHT_GRAY_TERRACOTTA, ModBlocks.INFESTED_LIGHT_GRAY_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.LIME_TERRACOTTA, ModBlocks.INFESTED_LIME_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.MAGENTA_TERRACOTTA, ModBlocks.INFESTED_MAGENTA_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.ORANGE_TERRACOTTA, ModBlocks.INFESTED_ORANGE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.PINK_TERRACOTTA, ModBlocks.INFESTED_PINK_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.PURPLE_TERRACOTTA, ModBlocks.INFESTED_PURPLE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.RED_TERRACOTTA, ModBlocks.INFESTED_RED_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.WHITE_TERRACOTTA, ModBlocks.INFESTED_WHITE_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.YELLOW_TERRACOTTA, ModBlocks.INFESTED_YELLOW_TERRACOTTA.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.CRYING_OBSIDIAN, ModBlocks.INFESTED_CRYING_OBSIDIAN.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.NETHERRACK, ModBlocks.INFESTED_NETHERRACK.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.CRIMSON_NYLIUM, ModBlocks.INFESTED_CRIMSON_NYLIUM.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.WARPED_NYLIUM, ModBlocks.INFESTED_WARPED_NYLIUM.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.BLACKSTONE, ModBlocks.INFESTED_BLACKSTONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.BASALT, ModBlocks.INFESTED_BASALT.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.SMOOTH_BASALT, ModBlocks.INFESTED_SMOOTH_BASALT.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(Blocks.END_STONE, ModBlocks.INFESTED_ENDSTONE.get().defaultBlockState());
        SculkHorde.blockInfestationTable.addEntry(net.minecraft.tags.BlockTags.LOGS, ModBlocks.INFESTED_LOG.get());


        SculkHorde.blockInfestationTable.addEntry(BlockTags.MINEABLE_WITH_AXE, Tiers.IRON, ModBlocks.INFESTED_WOOD_MASS.get());
        SculkHorde.blockInfestationTable.addEntry(BlockTags.MINEABLE_WITH_PICKAXE, Tiers.IRON, ModBlocks.INFESTED_STURDY_MASS.get());
        SculkHorde.blockInfestationTable.addEntry(BlockTags.MINEABLE_WITH_SHOVEL, Tiers.IRON, ModBlocks.INFESTED_CRUMPLED_MASS.get());
        SculkHorde.blockInfestationTable.addEntry(BlockTags.MINEABLE_WITH_HOE, Tiers.IRON, ModBlocks.INFESTED_COMPOST_MASS.get());


        SculkHorde.randomSculkFlora = new PoolBlocks();
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_CATALYST, 1);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SUMMONER_BLOCK.get(), 2);
        SculkHorde.randomSculkFlora.addEntry(Blocks.SCULK_SENSOR, 3);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SPIKE.get(), 4);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SMALL_SHROOM.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.SCULK_SHROOM_CULTURE.get(), 6);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS_SHORT.get(), 200);
        SculkHorde.randomSculkFlora.addEntry(ModBlocks.GRASS.get(), 200);

        event.enqueueWork(() -> {
            SpawnPlacements.register(ModEntities.SCULK_MITE.get(),
                    SpawnPlacements.Type.ON_GROUND, Heightmap.Types.MOTION_BLOCKING_NO_LEAVES,
                    SculkMiteEntity::additionalSpawnCheck);
            afterCommonSetup();
        });
    }

    // runs on main thread after common setup event
    // adding things to unsynchronized registries (i.e. most vanilla registries) can be done here
    private static void afterCommonSetup()
    {
        CriteriaTriggers.register(GravemindEvolveImmatureTrigger.INSTANCE);
        CriteriaTriggers.register(SculkHordeStartTrigger.INSTANCE);
        CriteriaTriggers.register(SculkNodeSpawnTrigger.INSTANCE);
    }

    /* entityAttributes
     * @Description Registers entity attributes for a living entity with forge
     */
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.SCULK_ZOMBIE.get(), SculkZombieEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE.get(), SculkMiteEntity.createAttributes().build());
        event.put(ModEntities.SCULK_MITE_AGGRESSOR.get(), SculkMiteAggressorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPITTER.get(), SculkSpitterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_INFECTOR.get(), SculkBeeInfectorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_BEE_HARVESTER.get(), SculkBeeHarvesterEntity.createAttributes().build());
        event.put(ModEntities.SCULK_HATCHER.get(), SculkHatcherEntity.createAttributes().build());
        event.put(ModEntities.SCULK_SPORE_SPEWER.get(), SculkSporeSpewerEntity.createAttributes().build());
        event.put(ModEntities.SCULK_RAVAGER.get(), SculkRavagerEntity.createAttributes().build());
        event.put(ModEntities.INFESTATION_PURIFIER.get(), InfestationPurifierEntity.createAttributes().build());
        event.put(ModEntities.SCULK_VINDICATOR.get(), SculkVindicatorEntity.createAttributes().build());
        event.put(ModEntities.SCULK_CREEPER.get(), SculkCreeperEntity.createAttributes().build());
        event.put(ModEntities.SCULK_ENDERMAN.get(), SculkEndermanEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM.get(), SculkPhantomEntity.createAttributes().build());
        event.put(ModEntities.SCULK_PHANTOM_CORPSE.get(), SculkPhantomCorpseEntity.createAttributes().build());
    }
}

