package com.github.sculkhorde.core;

import com.electronwill.nightconfig.core.file.CommentedFileConfig;
import com.electronwill.nightconfig.core.io.WritingMode;
import net.minecraftforge.common.ForgeConfigSpec;
import org.apache.commons.lang3.tuple.Pair;

import java.io.File;

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

        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_immature_stage;
        public final ForgeConfigSpec.ConfigValue<Integer> gravemind_mass_goal_for_mature_stage;

        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_chunkload_radius;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_node_spawn_cooldown_hours;

        public final ForgeConfigSpec.ConfigValue<Boolean> should_sculk_mites_spawn_in_deep_dark;

        public final ForgeConfigSpec.ConfigValue<Boolean> sculk_raid_enabled;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_enderman_scouting_duration_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_global_cooldown_between_raids_minutes;
        public final ForgeConfigSpec.ConfigValue<Integer> sculk_raid_no_raid_zone_duration_minutes;

        public final ForgeConfigSpec.ConfigValue<Boolean> experimental_features_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> sculk_phantoms_enabled;
        public final ForgeConfigSpec.ConfigValue<Boolean> squad_mechanics_enabled;


        public final ForgeConfigSpec.ConfigValue<Double> infestation_speed_multiplier;
        public final ForgeConfigSpec.ConfigValue<Double> purification_speed_multiplier;
        public final ForgeConfigSpec.ConfigValue<Integer> infestation_purifier_range;


        public Server(ForgeConfigSpec.Builder builder) {

            builder.push("Mod Compatability");
            target_faw_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'From Another World'? (Default false)").define("target_faw_entities",false);
            target_spore_entities = builder.comment("Should the Sculk Horde attack mobs from the mod 'Fungal Infection:Spore'? (Default false)").define("target_spore_entities",false);
            builder.pop();

            builder.push("General Variables");
            block_infestation_enabled = builder.comment("Should the Sculk Horde infest blocks? (Default true)").define("block_infestation_enabled",true);
            chunk_loading_enabled = builder.comment("Should the Sculk Horde load chunks? If disabled, and will ruin the intended experience. For example, raids wont work properly (Default true)").define("chunk_loading_enabled",true);
            builder.pop();

            builder.push("Infestation / Purification Variables");
            infestation_speed_multiplier = builder.comment("How much faster or slower should infestation spread? (Default 0)").defineInRange("infestation_speed_multiplier",0f, -10f, 10f);
            purification_speed_multiplier = builder.comment("How much faster or slower should purification spread? (Default 0)").defineInRange("purification_speed_multiplier",0f, -10f, 10f);
            infestation_purifier_range = builder.comment("How far should the infestation purifier reach? (Default 5)").defineInRange("purifier_range",48, 0, 100);
            builder.pop();

            builder.push("Gravemind Variables");
            gravemind_mass_goal_for_immature_stage = builder.comment("How much mass is needed for the Gravemind to enter the immature stage? (Default 5000)").defineInRange("gravemind_mass_goal_for_immature_stage",5000, 0, Integer.MAX_VALUE);
            gravemind_mass_goal_for_mature_stage = builder.comment("How much mass is needed for the Gravemind to enter the mature stage? (Default 20000)").defineInRange("gravemind_mass_goal_for_mature_stage",20000, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Node Variables");
            sculk_node_chunkload_radius = builder.comment("How many chunks should be loaded around a sculk node? (Default 15)").defineInRange("sculk_node_chunkload_radius",15, 0, 15);
            sculk_node_spawn_cooldown_hours = builder.comment("How many hours should pass before another Sculk node can spawn? (Default 8)").defineInRange("sculk_node_spawn_cooldown_hours",8, 0, Integer.MAX_VALUE);
            builder.pop();

            builder.push("Sculk Mite Variables");
            should_sculk_mites_spawn_in_deep_dark = builder.comment("Should sculk mites spawn in deep dark? (Default false)").define("should_sculk_mites_spawn_in_deep_dark",false);
            builder.pop();

            builder.push("Experimental Features");
            experimental_features_enabled = builder.comment("Should experimental features be enabled? (Default false)").define("experimental_features_enabled",false);
            sculk_phantoms_enabled = builder.comment("Should sculk phantoms be enabled? (Default true)").define("sculk_phantoms_enabled",true);
            squad_mechanics_enabled = builder.comment("Should squad mechanics be enabled? (Default true)").define("squad_mechanics_enabled",true);
            builder.pop();

            builder.push("Sculk Raid Variables");
            sculk_raid_enabled = builder.comment("Should sculk raids be enabled? (Default true)").define("sculk_raid_enabled",true);
            sculk_raid_enderman_scouting_duration_minutes = builder.comment("How long should the Sculk Enderman scout for? (Default 8)").defineInRange("sculk_raid_enderman_scouting_duration_minutes",8, 0, Integer.MAX_VALUE);
            sculk_raid_global_cooldown_between_raids_minutes = builder.comment("How long should the global cooldown between raids be in minutes? (Default 300)").defineInRange("sculk_raid_global_cooldown_between_raids_minutes", 300 , 0, Integer.MAX_VALUE);
            sculk_raid_no_raid_zone_duration_minutes = builder.comment("How long should the no raid zone last at a location in minutes? This occurs when a raid succeeds or fails so that the same location is not raided for a while. (Default 480)").defineInRange("sculk_raid_no_raid_zone_duration_minutes", 480 , 0, Integer.MAX_VALUE);
            builder.pop();
        }
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
