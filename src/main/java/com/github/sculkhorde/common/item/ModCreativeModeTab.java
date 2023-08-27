package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModItems;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.loading.FMLLoader;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

@Mod.EventBusSubscriber(modid = SculkHorde.MOD_ID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SculkHorde.MOD_ID);

    public static final RegistryObject<CreativeModeTab> SCULK_HORDE_TAB = TABS.register("sculk_horde_tab", () -> CreativeModeTab.builder()
            .title(Component.translatable("itemGroup.sculkhorde_tab"))
            .icon(() -> new ItemStack(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get()))
            .displayItems((enabledFeatures, event) -> {
                if(!FMLLoader.isProduction()) event.accept(ModItems.WARDEN_BEEF.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_NODE_SPAWNER.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_CONVERSION_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ModItems.DEV_RAID_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get());
                event.accept(ModItems.SCULK_SPORE_SPEWER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_MITE_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_MITE_AGGRESSOR_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_ZOMBIE_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_SPITTER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_CREEPER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_HATCHER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_VINDICATOR_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_RAVAGER_SPAWN_EGG.get());
                event.accept(ModItems.SCULK_ENDERMAN_SPAWN_EGG.get());
                event.accept(ModItems.INFESTATION_PURIFIER.get());
                event.accept(ModItems.PURIFICATION_FLASK_ITEM.get());
                event.accept(ModItems.ESSENCE_OF_PURITY.get());
                event.accept(ModBlocks.INFESTATION_WARD_BLOCK.get());
                event.accept(ModItems.PURE_SOULS.get());
                event.accept(ModItems.SCULK_ACIDIC_PROJECTILE.get());
                event.accept(ModItems.SCULK_RESIN.get());
                event.accept(ModItems.CALCITE_CLUMP.get());
                event.accept(ModItems.SCULK_MATTER.get());
                event.accept(ModItems.CRYING_SOULS.get());
                event.accept(ModBlocks.SCULK_NODE_BLOCK.get());
                event.accept(ModBlocks.SCULK_ARACHNOID.get());
                event.accept(ModBlocks.SCULK_DURA_MATTER.get());
                event.accept(ModBlocks.SCULK_BEE_NEST_BLOCK.get());
                event.accept(ModBlocks.SCULK_BEE_NEST_CELL_BLOCK.get());
                event.accept(ModBlocks.SCULK_LIVING_ROCK_ROOT_BLOCK.get());
                event.accept(ModBlocks.SCULK_LIVING_ROCK_BLOCK.get());
                event.accept(ModBlocks.CALCITE_ORE.get());
                event.accept(ModBlocks.SCULK_SUMMONER_BLOCK.get());
                event.accept(Blocks.SCULK_CATALYST);
                event.accept(Blocks.SCULK_SHRIEKER);
                event.accept(Blocks.SCULK_SENSOR);
                event.accept(ModBlocks.SCULK_MASS.get());
                event.accept(ModBlocks.GRASS.get());
                event.accept(ModBlocks.GRASS_SHORT.get());
                event.accept(ModBlocks.SCULK_SHROOM_CULTURE.get());
                event.accept(ModBlocks.SMALL_SHROOM.get());
                event.accept(ModBlocks.SPIKE.get());
                event.accept(ModBlocks.TENDRILS.get());
                event.accept(Blocks.SCULK);
                event.accept(ModBlocks.INFESTED_CRYING_OBSIDIAN.get());
                event.accept(ModBlocks.INFESTED_LOG.get());
                event.accept(ModBlocks.INFESTED_SAND.get());
                event.accept(ModBlocks.INFESTED_RED_SAND.get());
                event.accept(ModBlocks.INFESTED_SANDSTONE.get());
                event.accept(ModBlocks.INFESTED_GRAVEL.get());
                event.accept(ModBlocks.INFESTED_STONE.get());
                event.accept(ModBlocks.INFESTED_COBBLESTONE.get());
                event.accept(ModBlocks.INFESTED_DEEPSLATE.get());
                event.accept(ModBlocks.INFESTED_COBBLED_DEEPSLATE.get());
                event.accept(ModBlocks.INFESTED_ANDESITE.get());
                event.accept(ModBlocks.INFESTED_DIORITE.get());
                event.accept(ModBlocks.INFESTED_GRANITE.get());
                event.accept(ModBlocks.INFESTED_TUFF.get());
                event.accept(ModBlocks.INFESTED_CALCITE.get());
                event.accept(ModBlocks.INFESTED_SNOW.get());
                event.accept(ModBlocks.INFESTED_MOSS.get());
                event.accept(ModBlocks.INFESTED_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BLACK_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BLUE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_BROWN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_CYAN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_GRAY_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_GREEN_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIGHT_BLUE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIGHT_GRAY_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_LIME_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_MAGENTA_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_ORANGE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_PINK_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_PURPLE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_RED_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_WHITE_TERRACOTTA.get());
                event.accept(ModBlocks.INFESTED_YELLOW_TERRACOTTA.get());
            })
            .build());
}
