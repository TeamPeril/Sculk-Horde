package com.github.sculkhorde.core;

import com.electronwill.nightconfig.core.Config;
import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

public class ModConfig {

    public static final Server SERVER;
    public static final ForgeConfigSpec SERVER_SPEC;

    public static final DataGen DATAGEN;
    public static final ForgeConfigSpec DATAGEN_SPEC;

    public static class Server {

        public final ForgeConfigSpec.ConfigValue<Boolean> target_faw_entities;
        public final ForgeConfigSpec.ConfigValue<Boolean> target_spore_entities;

        public final ForgeConfigSpec.ConfigValue<Boolean> block_infestation_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> chunk_loading_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> disable_defeating_sculk_horde;
        public final ForgeConfigSpec.ConfigValue<Integer> max_unit_population;
        public final ForgeConfigSpec.ConfigValue<Boolean> trigger_ancient_node_automatically;
        public final ForgeConfigSpec.ConfigValue<Integer> trigger_ancient_node_wait_days;
        public final ForgeConfigSpec.ConfigValue<Integer> trigger_ancient_node_time_of_day;

        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_immature_stage;
        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_mature_stage;

        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_chunkload_radius;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_spawn_cooldown_minutes;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_sculk_mites_spawn_in_deep_dark;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_phantoms_load_chunks;

        public final ForgeConfigSpec.ConfigValue<Boolean> sculk_raid_enabled;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_enderman_scouting_duration_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_global_cooldown_between_raids_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_no_raid_zone_duration_minutes;

        public final ForgeConfigSpec.ConfigValue<Boolean> experimental_features_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> disable_sculk_horde_unless_activated;
        public final ForgeConfigSpec.ConfigValue<Double> purification_speed_multiplier;
        public final ForgeConfigSpec.ConfigValue<Integer> infestation_purifier_range;
        private final ForgeConfigSpec.ConfigValue<List<? extends String>> items_infection_cursors_can_eat;
        public static final HashMap<String, Boolean> infection_cursor_item_eat_list = new HashMap<>();

        private final ForgeConfigSpec.ConfigValue<List<? extends String>> make_block_infestable;
        public static final HashMap<String, Boolean> manually_configured_infestable_blocks = new HashMap<>();

        public final ForgeConfigSpec.ConfigValue<Integer> max_infector_cursor_population;
        public final ForgeConfigSpec.ConfigValue<Integer> max_nodes_active;
        public final ForgeConfigSpec.ConfigValue<Integer> performance_mode_cursor_threshold;
        public final ForgeConfigSpec.ConfigValue<Integer> performance_mode_cursors_to_tick_per_tick;
        public final ForgeConfigSpec.ConfigValue<Integer> performance_mode_delay_between_cursor_ticks;
        public final ForgeConfigSpec.ConfigValue<Boolean> performance_mode_thanos_snap_cursors;

        public void loadItemsInfectionCursorsCanEat()
        {
            infection_cursor_item_eat_list.clear();
            for(String item : ModConfig.SERVER.items_infection_cursors_can_eat.get())
            {
                infection_cursor_item_eat_list.put(item, true);
            }
        }

        public boolean isItemEdibleToCursors(ItemEntity itemEntity)
        {
            ItemStack itemStack = itemEntity.getItem();
            Item item = itemStack.getItem();
            ResourceLocation itemResourceLocation = BuiltInRegistries.ITEM.getKey(item);


            if(itemResourceLocation == null)
            {
                return false;
            }

            String itemName = itemResourceLocation.toString();
            if(infection_cursor_item_eat_list.containsKey(itemName))
            {
                return true;
            }

            if(item.isEdible())
            {
                return true;
            }

            if (itemName.contains("sapling")) {
                return true;
            }

            return false;
        }

        public void loadConfiguredInfestableBlocks()
        {
            manually_configured_infestable_blocks.clear();
            for(String block : ModConfig.SERVER.make_block_infestable.get())
            {
                manually_configured_infestable_blocks.put(block, true);
            }
        }

        public boolean isBlockConfiguredToBeInfestable(BlockState blockState)
        {
            Block block = blockState.getBlock();
            ResourceLocation itemResourceLocation = BuiltInRegistries.BLOCK.getKey(block);


            if(itemResourceLocation == null)
            {
                return false;
            }

            String blockName = itemResourceLocation.toString();
            if(manually_configured_infestable_blocks.containsKey(blockName))
            {
                return true;
            }

            return false;
        }

        public Server(ForgeConfigSpec.Builder builder) {

            Config.setInsertionOrderPreserved(true);

            builder.push("Performance Settings");
            max_unit_population = builder.comment("How many sculk mobs should be allowed to exist at one time? (Default 200)").defineInRange("max_unit_population",200, 0, 1000);
            max_infector_cursor_population = builder.comment("How many infector cursors should be allowed to exist at one time? (Default 200)").defineInRange("max_infector_cursor_population",200, 0, 1000);
            max_nodes_active = builder.comment("How many nodes can be active at once? (Default 1)").defineInRange("max_nodes_active",1, 0, 1000);
            performance_mode_cursor_threshold = builder.comment("How many cursors need to exist for performance mode to kick in. (Default 100)").defineInRange("performance_mode_cursor_threshold", 100, 0, 1000);
            performance_mode_cursors_to_tick_per_tick = builder.comment("How many cursors should we tick, per in game tick. (Default 50)").defineInRange("performance_mode_cursors_to_tick_per_tick", 50, 0, 100);
            performance_mode_delay_between_cursor_ticks = builder.comment("How many ticks should there be between intervals of ticking cursors. (Default 1)").defineInRange("performance_mode_delay_between_cursor_ticks", 1, 0, 100);
            performance_mode_thanos_snap_cursors = builder.comment("50% Chance for cursors to discard themselves upon reaching threshold. (Default false)").define("performance_mode_thanos_snap_cursors", false);
            builder.pop();

            builder.push("Mod Compatability");
            target_faw_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'From Another World'? (Default false)").define("target_faw_entities",false);
            target_spore_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Fungal Infection:Spore'? (Default false)").define("target_spore_entities",false);
            builder.pop();

            builder.push("General Variables");
            block_infestation_enabled = builder.comment("Should the Sculk Horde infest blocks? (Default true)").define("block_infestation_enabled",true);
            chunk_loading_enabled = builder.comment("Should the Sculk Horde load chunks? If disabled, and will ruin the intended experience. For example, raids wont work properly (Default true)").define("chunk_loading_enabled",true);
            disable_defeating_sculk_horde = builder.comment("Should players be able to defeat the Sculk Horde?").define("disable_defeating_sculk_horde",false);
            builder.pop();

            builder.push("Trigger Automatically Variables");
            trigger_ancient_node_automatically = builder.comment("Should the Sculk Horde start automatically? Requires that chunk loading is enabled to work reliably, otherwise will only trigger if the ancient node's chunk is loaded. If enabled on a save where previously disabled, the node will trigger automatically if the time conditions are met. (Default false)").define("trigger_ancient_node_automatically", false);
            trigger_ancient_node_wait_days = builder.comment("How many days to wait before triggering the ancient node? (Default 0)").defineInRange("trigger_ancient_node_wait_days", 0, 0, Integer.MAX_VALUE);
            trigger_ancient_node_time_of_day = builder.comment("What time of day in ticks must pass before triggering the ancient node after the wait days have elapsed? If wait days is set to 0, set time of day to a time greater than 1000 ticks to allow for world startup and lag to finish (Default 2000)").defineInRange("trigger_ancient_node_time_of_day", 2000, 0, 23999);
            builder.pop();

            builder.push("Infestation / Purification Variables");
            purification_speed_multiplier = builder.comment("How much faster or slower should purification spread? (Default 0)").defineInRange("purification_speed_multiplier",0f, -10f, 10f);
            infestation_purifier_range = builder.comment("How far should the infestation purifier reach? (Default 5)").defineInRange("purifier_range",48, 0, 100);
            items_infection_cursors_can_eat = builder.comment("What dropped items should cursors eat? This prevents lag and boosts their lifespan.").defineList("items_infection_cursors_can_eat", Arrays.asList("minecraft:wheat_seeds", "minecraft:bamboo", "minecraft:stick", "minecraft:poppy", "minecraft:dandelion", "minecraft:blue_orchid", "minecraft:allium", "minecraft:azure_bluet", "minecraft:red_tulip", "minecraft:orange_tulip", "minecraft:white_tulip", "minecraft:pink_tulip", "minecraft:oxeye_daisy", "minecraft:cornflower", "minecraft:lily_of_the_valley", "minecraft:sunflower", "minecraft:lilac", "minecraft:rose_bush", "minecraft:peony"), entry -> true);
            make_block_infestable = builder.comment("Add blocks to this list to make them infestable. I.E. minecraft:dirt. Be careful what you put in here, this can potentially lead to issues. This will not work with blocks that are air, have a block entity, are already considered an infested block, or have the not infestable tag.").defineList("make_block_infestable", Arrays.asList(""), entry -> true);
            builder.pop();

            builder.push("Gravemind Variables");
            gravemind_mass_goal_for_immature_stage = builder.comment("How much mass is needed for the Gravemind to enter the immature stage? (Default 5000)").defineInRange("gravemind_mass_goal_for_immature_stage",5000, 0, Integer.MAX_VALUE);
            gravemind_mass_goal_for_mature_stage = builder.comment("How much mass is needed for the Gravemind to enter the mature stage? (Default 20000)").defineInRange("gravemind_mass_goal_for_mature_stage",20000, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Node Variables");
            sculk_node_chunkload_radius = builder.comment("How many chunks should be loaded around a sculk node? (Default 15)").defineInRange("sculk_node_chunkload_radius",15, 0, 15);
            sculk_node_spawn_cooldown_minutes = builder.comment("How many minutes should pass before another Sculk node can spawn? (Default 120)").defineInRange("sculk_node_spawn_cooldown_minutes",120, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Mite Variables");
            should_sculk_mites_spawn_in_deep_dark = builder.comment("Should sculk mites spawn in deep dark? (Default false)").define("should_sculk_mites_spawn_in_deep_dark",false);
            builder.pop();

            builder.push("Sculk Phantom Variables");
            should_phantoms_load_chunks = builder.comment("Should sculk phantoms load chunks? (Default true)").define("should_phantoms_load_chunks",true);
            builder.pop();

            builder.push("Experimental Features");
            experimental_features_enabled = builder.comment("Should experimental features be enabled? (Default false)").define("experimental_features_enabled",false);
            disable_sculk_horde_unless_activated = builder.comment("Should the Sculk Horde be unable to function without Ancient Node Activation (Default false).").define("disable_sculk_horde_unless_activated", false);
            builder.pop();

            builder.push("Sculk Raid Variables");
            sculk_raid_enabled = builder.comment("Should sculk raids be enabled? (Default true)").define("sculk_raid_enabled",true);
            sculk_raid_enderman_scouting_duration_minutes = builder.comment("How long should the Sculk Enderman scout for? (Default 8)").defineInRange("sculk_raid_enderman_scouting_duration_minutes",8, 0, Integer.MAX_VALUE);
            sculk_raid_global_cooldown_between_raids_minutes = builder.comment("How long should the global cooldown between raids be in minutes? (Default 300)").defineInRange("sculk_raid_global_cooldown_between_raids_minutes", 300 , 0, Integer.MAX_VALUE);
            sculk_raid_no_raid_zone_duration_minutes = builder.comment("How long should the no raid zone last at a location in minutes? This occurs when a raid succeeds or fails so that the same location is not raided for a while. (Default 480)").defineInRange("sculk_raid_no_raid_zone_duration_minutes", 480 , 0, Integer.MAX_VALUE);
            builder.pop();
        }
    }

    public static boolean isExperimentalFeaturesEnabled() {
        return SERVER.experimental_features_enabled.get();
    }

    public static class DataGen {

        public DataGen(ForgeConfigSpec.Builder builder){

        }

    }

    static {
        Pair<Server, ForgeConfigSpec> commonSpecPair = new ForgeConfigSpec.Builder().configure(Server::new);
        SERVER = commonSpecPair.getLeft();
        SERVER_SPEC = commonSpecPair.getRight();

        Pair<DataGen , ForgeConfigSpec> commonPair = new ForgeConfigSpec.Builder().configure(DataGen::new);
        DATAGEN = commonPair.getLeft();
        DATAGEN_SPEC = commonPair.getRight();

    }

    public static void loadConfig(ForgeConfigSpec config, String path) {
        final CommentedFileConfig file = CommentedFileConfig.builder(new File(path)).sync().autosave().writingMode(WritingMode.REPLACE).build();
        file.load();
        config.setConfig(file);
    }
}
