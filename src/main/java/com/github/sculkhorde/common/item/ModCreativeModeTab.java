package com.github.sculkhorde.common.item;

import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.ItemRegistry;
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
            .icon(() -> new ItemStack(BlockRegistry.SCULK_ANCIENT_NODE_BLOCK.get()))
            .displayItems((enabledFeatures, event) -> {
                if(!FMLLoader.isProduction()) event.accept(ItemRegistry.WARDEN_BEEF.get());
                if(!FMLLoader.isProduction()) event.accept(ItemRegistry.DEV_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ItemRegistry.DEV_NODE_SPAWNER.get());
                if(!FMLLoader.isProduction()) event.accept(ItemRegistry.DEV_CONVERSION_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(ItemRegistry.DEV_RAID_WAND.get());
                if(!FMLLoader.isProduction()) event.accept(BlockRegistry.SCULK_ANCIENT_NODE_BLOCK.get());
                event.accept(ItemRegistry.SCULK_SPORE_SPEWER_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_MITE_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_MITE_AGGRESSOR_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_ZOMBIE_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_SPITTER_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_CREEPER_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_HATCHER_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_VINDICATOR_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_RAVAGER_SPAWN_EGG.get());
                event.accept(ItemRegistry.SCULK_ENDERMAN_SPAWN_EGG.get());
                event.accept(ItemRegistry.INFESTATION_PURIFIER.get());
                event.accept(ItemRegistry.PURIFICATION_FLASK_ITEM.get());
                event.accept(ItemRegistry.ESSENCE_OF_PURITY.get());
                event.accept(BlockRegistry.INFESTATION_WARD_BLOCK.get());
                event.accept(ItemRegistry.PURE_SOULS.get());
                event.accept(ItemRegistry.SCULK_ACIDIC_PROJECTILE.get());
                event.accept(ItemRegistry.SCULK_RESIN.get());
                event.accept(ItemRegistry.CALCITE_CLUMP.get());
                event.accept(ItemRegistry.SCULK_MATTER.get());
                event.accept(ItemRegistry.CRYING_SOULS.get());
                event.accept(BlockRegistry.SCULK_NODE_BLOCK.get());
                event.accept(BlockRegistry.SCULK_ARACHNOID.get());
                event.accept(BlockRegistry.SCULK_DURA_MATTER.get());
                event.accept(BlockRegistry.SCULK_BEE_NEST_BLOCK.get());
                event.accept(BlockRegistry.SCULK_BEE_NEST_CELL_BLOCK.get());
                event.accept(BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get());
                event.accept(BlockRegistry.SCULK_LIVING_ROCK_BLOCK.get());
                event.accept(BlockRegistry.CALCITE_ORE.get());
                event.accept(BlockRegistry.SCULK_SUMMONER_BLOCK.get());
                event.accept(Blocks.SCULK_CATALYST);
                event.accept(Blocks.SCULK_SHRIEKER);
                event.accept(Blocks.SCULK_SENSOR);
                event.accept(BlockRegistry.SCULK_MASS.get());
                event.accept(BlockRegistry.GRASS.get());
                event.accept(BlockRegistry.GRASS_SHORT.get());
                event.accept(BlockRegistry.SCULK_SHROOM_CULTURE.get());
                event.accept(BlockRegistry.SMALL_SHROOM.get());
                event.accept(BlockRegistry.SPIKE.get());
                event.accept(BlockRegistry.TENDRILS.get());
                event.accept(Blocks.SCULK);
                event.accept(BlockRegistry.INFESTED_CRYING_OBSIDIAN.get());
                event.accept(BlockRegistry.INFESTED_LOG.get());
                event.accept(BlockRegistry.INFESTED_SAND.get());
                event.accept(BlockRegistry.INFESTED_RED_SAND.get());
                event.accept(BlockRegistry.INFESTED_SANDSTONE.get());
                event.accept(BlockRegistry.INFESTED_GRAVEL.get());
                event.accept(BlockRegistry.INFESTED_STONE.get());
                event.accept(BlockRegistry.INFESTED_COBBLESTONE.get());
                event.accept(BlockRegistry.INFESTED_DEEPSLATE.get());
                event.accept(BlockRegistry.INFESTED_COBBLED_DEEPSLATE.get());
                event.accept(BlockRegistry.INFESTED_ANDESITE.get());
                event.accept(BlockRegistry.INFESTED_DIORITE.get());
                event.accept(BlockRegistry.INFESTED_GRANITE.get());
                event.accept(BlockRegistry.INFESTED_TUFF.get());
                event.accept(BlockRegistry.INFESTED_CALCITE.get());
                event.accept(BlockRegistry.INFESTED_SNOW.get());
                event.accept(BlockRegistry.INFESTED_MOSS.get());
                event.accept(BlockRegistry.INFESTED_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_BLACK_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_BLUE_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_BROWN_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_CYAN_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_GRAY_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_GREEN_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_LIGHT_BLUE_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_LIGHT_GRAY_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_LIME_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_MAGENTA_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_ORANGE_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_PINK_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_PURPLE_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_RED_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_WHITE_TERRACOTTA.get());
                event.accept(BlockRegistry.INFESTED_YELLOW_TERRACOTTA.get());
            })
            .build());
}
