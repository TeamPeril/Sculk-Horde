package com.github.sculkhorde.common.command;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.SculkHorde;
import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.BoolArgumentType;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.builder.ArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;

public class ConfigCommand implements Command<CommandSourceStack> {

    public static ArgumentBuilder<CommandSourceStack, ?> register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext buildContext) {
        return Commands.literal("config")
                .then(performance(dispatcher))
                .then(gravemindConfig(dispatcher))
                .then(generalConfig(dispatcher))
                .then(triggerAutomaticallyConfig(dispatcher))
                .then(sculkRaidConfig(dispatcher))
                .then(infestationAndPurificationConfig(dispatcher))
                .then(sculkMiteConfig(dispatcher))
                .then(sculkPhantomConfig(dispatcher))
                .then(modCompatibilityConfig(dispatcher))
                .then(sculkNodeConfig(dispatcher))
                .then(experimentalFeaturesConfig(dispatcher));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> integerConfigOption(String configKey, int min, int max) {

        String key;
        if(min > max) {
            return Commands.literal(configKey)
                    .then(Commands.argument("value", IntegerArgumentType.integer(1))
                            .executes(context -> setConfigValue(context, "Invalid Argument")));
        } else
        {
            return Commands.literal(configKey)
                    .then(Commands.argument("value", IntegerArgumentType.integer(1))
                            .executes(context -> setConfigValue(context, configKey)));
        }
    }

    private static ArgumentBuilder<CommandSourceStack, ?> doubleConfigOption(String configKey, double min, double max) {

        String key;
        if(min > max) {
            return Commands.literal(configKey)
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(-10))
                            .executes(context -> setConfigValue(context, "Invalid Argument")));
        } else
        {
            return Commands.literal(configKey)
                    .then(Commands.argument("value", DoubleArgumentType.doubleArg(-10))
                            .executes(context -> setConfigValue(context, configKey)));
        }
    }

    private static ArgumentBuilder<CommandSourceStack, ?> booleanConfigOption(String configKey) {
        return Commands.literal(configKey)
                .then(Commands.argument("value", BoolArgumentType.bool())
                        .executes(context -> setConfigValue(context, configKey)));
    }

    // Create a new method for the difficulty command with specific options
    private static ArgumentBuilder<CommandSourceStack, ?> difficultyConfigOption(String configKey) {
        return Commands.literal(configKey)
                // The command logic is attached directly to each final option
                .then(Commands.literal("AUTO").executes(context -> setDifficultyModeConfig(context, "AUTO")))
                .then(Commands.literal("EASY").executes(context -> setDifficultyModeConfig(context, "EASY")))
                .then(Commands.literal("NORMAL").executes(context -> setDifficultyModeConfig(context, "NORMAL")))
                .then(Commands.literal("HARD").executes(context -> setDifficultyModeConfig(context, "HARD")));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> performance(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("performance")
                .then(booleanConfigOption("disable_auto_performance_system"))
                .then(integerConfigOption("max_unit_population", 0, 1000))
                .then(integerConfigOption("max_nodes_active", 0, 1000))
                .then(integerConfigOption("minutes_required_for_performance_increase", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("seconds_required_for_performance_decrease", 0, Integer.MAX_VALUE));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> modCompatibilityConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("mod_compatibility")
                .then(booleanConfigOption("target_spore_entities"))
                .then(booleanConfigOption("target_faw_entities"))
                .then(booleanConfigOption("target_dulling_entities"))
                .then(booleanConfigOption("target_deeper_and_darker_entities"))
                .then(booleanConfigOption("target_mi_alliance_entities"))
                .then(booleanConfigOption("target_scape_and_run_parasites_entities"))
                .then(booleanConfigOption("target_the_flesh_that_hates_entities"))
                .then(booleanConfigOption("target_dawn_of_the_flood_entities"))
                .then(booleanConfigOption("target_prion_infection_entities"))
                .then(booleanConfigOption("target_withering_away_reborn_entities"))
                .then(booleanConfigOption("target_entomophobia_entities"))
                .then(booleanConfigOption("target_abominations_infection_entities"))
                .then(booleanConfigOption("target_another_dimension_infection_entities"))
                .then(booleanConfigOption("target_phayriosis_entities"))
                .then(booleanConfigOption("target_complete_distortion_entities"))
                .then(booleanConfigOption("target_swarm_infection_entities"))
                .then(booleanConfigOption("target_bulbus_infection_entities"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> generalConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("general")
                .then(difficultyConfigOption("difficulty_mode"))
                .then(booleanConfigOption("should_all_other_mobs_attack_the_sculk_horde"))
                .then(booleanConfigOption("should_animals_and_villagers_avoid_the_sculk_horde"))
                .then(booleanConfigOption("chunk_loading_enabled"))
                .then(booleanConfigOption("block_infestation_enabled"))
                .then(booleanConfigOption("disable_defeating_sculk_horde"))
                .then(booleanConfigOption("enable_gpu_compatibility_mode"))
                .then(booleanConfigOption("isHordeActiveWithNoPlayers"))
                .then(booleanConfigOption("hit_squad_event_enabled"))
                .then(booleanConfigOption("ghast_deployment_event_enabled"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> triggerAutomaticallyConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("trigger_automatically")
                .then(booleanConfigOption("trigger_ancient_node_automatically"))
                .then(integerConfigOption("trigger_ancient_node_wait_days", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("trigger_ancient_node_time_of_day", 0, 24000));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> infestationAndPurificationConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("infestation_and_purification")
                .then(doubleConfigOption("infestation_speed_multiplier", 0.001, 10))
                .then(doubleConfigOption("purification_speed_multiplier", 0.001, 10))
                .then(integerConfigOption("purifier_range", 0, 100));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> gravemindConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("gravemind")
                .then(integerConfigOption("gravemind_mass_goal_for_mature_stage", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("gravemind_mass_goal_for_immature_stage", 0, Integer.MAX_VALUE));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> sculkNodeConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sculk_node")
                .then(integerConfigOption("sculk_node_spawn_cooldown_minutes", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("sculk_node_chunkload_radius", 0, 15))
                .then(booleanConfigOption("enable_node_relocation"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> sculkMiteConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sculk_mite")
                .then(booleanConfigOption("should_sculk_mites_spawn_in_deep_dark"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> sculkPhantomConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sculk_phantom")
                .then(booleanConfigOption("should_phantoms_load_chunks"))
                .then(booleanConfigOption("should_sculk_nodes_and_raids_spawn_phantoms"))
                .then(booleanConfigOption("should_ancient_node_spawn_phantoms"));
    }

    private static ArgumentBuilder<CommandSourceStack, ?> experimentalFeaturesConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("experimental_features")
                .then(booleanConfigOption("experimental_features_enabled"))
                .then(booleanConfigOption("experimental_brood_hatcher_enabled"));

    }

    private static ArgumentBuilder<CommandSourceStack, ?> sculkRaidConfig(CommandDispatcher<CommandSourceStack> dispatcher) {
        return Commands.literal("sculk_raid")
                .then(booleanConfigOption("sculk_raid_enabled"))
                .then(integerConfigOption("sculk_raid_enderman_scouting_duration_minutes", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("sculk_raid_global_cooldown_between_raids_minutes", 0, Integer.MAX_VALUE))
                .then(integerConfigOption("sculk_raid_no_raid_zone_duration_minutes", 0, Integer.MAX_VALUE));
    }

    /**
     * Executes the configuration change for difficulty_mode.
     * @param context The command context.
     * @param difficulty The literal string value passed from the command (e.g., "AUTO", "EASY").
     * @return The result code for the command execution (1 for success).
     */
    private static int setDifficultyModeConfig(CommandContext<CommandSourceStack> context, String difficulty) {
        // 1. Convert the input to uppercase for case-insensitive comparison
        difficulty = difficulty.toUpperCase();
        String finalDifficulty = difficulty;

        // 2. Validate and set the configuration value
        if(difficulty.equals("EASY"))
        {
            ModConfig.SERVER.difficulty_mode.set(difficulty);
            SculkHorde.LOGGER.info("Difficulty set to " + difficulty);

            context.getSource().sendSuccess(() -> Component.literal("Config option updated successfully. " + "difficulty_mode" + " is now: " + finalDifficulty), false);
            return 1;
        }
        else if(difficulty.equals("NORMAL"))
        {
            ModConfig.SERVER.difficulty_mode.set(difficulty);
            SculkHorde.LOGGER.info("Difficulty set to " + difficulty);
            context.getSource().sendSuccess(() -> Component.literal("Config option updated successfully. " + "difficulty_mode" + " is now: " + finalDifficulty), false);
            return 1;
        }
        else if(difficulty.equals("HARD"))
        {
            ModConfig.SERVER.difficulty_mode.set(difficulty);
            SculkHorde.LOGGER.info("Difficulty set to " + difficulty);
            context.getSource().sendSuccess(() -> Component.literal("Config option updated successfully. " + "difficulty_mode" + " is now: " + finalDifficulty), false);
            return 1;
        }
        else
        {
            ModConfig.SERVER.difficulty_mode.set("AUTO");
            SculkHorde.LOGGER.info("Difficulty set to AUTO");
            context.getSource().sendSuccess(() -> Component.literal("Config option updated successfully. " + "difficulty_mode" + " is now: " + finalDifficulty), false);
            return 1;
        }
    }


    private static int setConfigValue(CommandContext<CommandSourceStack> context, String configKey) {
        boolean success = false;
        Object rawValue;

        // Assuming you have a Config class with get() and set() methods
        if (context.getArgument("value", Object.class) != null) {
            // Get the provided value from the command arguments
            rawValue = context.getArgument("value", Object.class);

            // Validate the type of the provided value
            Class<?> valueType = rawValue.getClass();

            // Modify the config based on the provided key and value
            switch (configKey) {

                //#### Performance Settings ####
                case "disable_auto_performance_system":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.disable_auto_performance_system.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "max_unit_population":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.max_unit_population.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "max_nodes_active":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.max_nodes_active.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "minutes_required_for_performance_increase":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.minutes_required_for_performance_increase.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "seconds_required_for_performance_decrease":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.seconds_required_for_performance_decrease.set((Integer) rawValue);
                        success = true;
                    }
                    break;

                //#### Mod Compatibility Config ####
                case "target_spore_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_spore_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_faw_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_faw_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_dulling_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_dulling_entities.set((Boolean) rawValue);
                        success = true;
                    }
                case "target_deeper_and_darker_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_deeper_and_darker_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_mi_alliance_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_mi_alliance_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_scape_and_run_parasites_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_scape_and_run_parasites_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_the_flesh_that_hates_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_the_flesh_that_hates_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_dawn_of_the_flood_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_dawn_of_the_flood_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_prion_infection_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_prion_infection_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_withering_away_reborn_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_withering_away_reborn_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_entomophobia_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_entomophobia_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_abominations_infection_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_abominations_infection_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_phayriosis_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_phayriosis_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_complete_distortion_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_complete_distortion_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_swarm_infection_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_swarm_infection_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "target_bulbus_infection_entities":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.target_bulbus_infection_entities.set((Boolean) rawValue);
                        success = true;
                    }
                    break;



                //#### General Config ####
                case "should_all_other_mobs_attack_the_sculk_horde":
                    if(valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_all_other_mobs_attack_the_sculk_horde.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "should_animals_and_villagers_avoid_the_sculk_horde":
                    if(valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_animals_and_villagers_avoid_the_sculk_horde.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "block_infestation_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.block_infestation_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "chunk_loading_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.chunk_loading_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "disable_defeating_sculk_horde":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.disable_defeating_sculk_horde.set((Boolean) rawValue);
                        success = true;
                    }
                case "enable_gpu_compatibility_mode":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.enable_gpu_compatibility_mode.set((Boolean) rawValue);
                        context.getSource().sendSystemMessage(Component.literal("Game restart required to take effect."));
                        success = true;
                    }
                case "isHordeActiveWithNoPlayers":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.isHordeActiveWithNoPlayers.set((Boolean) rawValue);
                        success = true;
                    }
                case "hit_squad_event_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.hit_squad_event_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                case "ghast_deployment_event_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.ghast_deployment_event_enabled.set((Boolean) rawValue);
                        success = true;
                    }



                //#### Trigger Automatically Config ####
                case "trigger_ancient_node_automatically":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.trigger_ancient_node_automatically.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "trigger_ancient_node_wait_days":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.trigger_ancient_node_wait_days.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "trigger_ancient_node_time_of_day":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.trigger_ancient_node_time_of_day.set((Integer) rawValue);
                        success = true;
                    }
                    break;


                //#### Infestation / Purification Variables ####
                case "infestation_speed_multiplier":
                    if (valueType.equals(Double.class)) {
                        ModConfig.SERVER.infection_speed_multiplier.set((Double) rawValue);
                        success = true;
                    }
                case "purification_speed_multiplier":
                    if (valueType.equals(Double.class)) {
                        ModConfig.SERVER.purification_speed_multiplier.set((Double) rawValue);
                        success = true;
                    }
                    break;
                case "purifier_range":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.infestation_purifier_range.set((Integer) rawValue);
                        success = true;
                    }
                    break;



                //#### Gravemind Config ####
                case "gravemind_mass_goal_for_mature_stage":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.gravemind_mass_goal_for_mature_stage.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "gravemind_mass_goal_for_immature_stage":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.gravemind_mass_goal_for_immature_stage.set((Integer) rawValue);
                        success = true;
                    }
                    break;



                //#### Sculk Node Config ####
                case "sculk_node_spawn_cooldown_minutes":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.sculk_node_spawn_cooldown_minutes.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "sculk_node_chunkload_radius":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.sculk_node_chunkload_radius.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "enable_node_relocation":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.enable_node_relocation.set((Boolean) rawValue);
                        success = true;
                    }
                    break;

                //#### Sculk Mite Config ####
                case "should_sculk_mites_spawn_in_deep_dark":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_sculk_mites_spawn_in_deep_dark.set((Boolean) rawValue);
                        success = true;
                    }
                    break;



                //#### Sculk Phantom Config ####
                case "should_phantoms_load_chunks":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_phantoms_load_chunks.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "should_sculk_nodes_and_raids_spawn_phantoms":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_sculk_nodes_and_raids_spawn_phantoms.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "should_ancient_node_spawn_phantoms":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.should_ancient_node_spawn_phantoms.set((Boolean) rawValue);
                        success = true;
                    }
                    break;



                //#### Experimental Features Config ####
                case "experimental_features_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.experimental_features_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "experimental_brood_hatcher_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.experimental_brood_hatcher_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                    break;


                //####  Sculk Raid Config ####
                case "sculk_raid_enabled":
                    if (valueType.equals(Boolean.class)) {
                        ModConfig.SERVER.sculk_raid_enabled.set((Boolean) rawValue);
                        success = true;
                    }
                    break;
                case "sculk_raid_enderman_scouting_duration_minutes":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.sculk_raid_enderman_scouting_duration_minutes.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "sculk_raid_global_cooldown_between_raids_minutes":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.sculk_raid_global_cooldown_between_raids_minutes.set((Integer) rawValue);
                        success = true;
                    }
                    break;
                case "sculk_raid_no_raid_zone_duration_minutes":
                    if (valueType.equals(Integer.class)) {
                        ModConfig.SERVER.sculk_raid_no_raid_zone_duration_minutes.set((Integer) rawValue);
                        success = true;
                    }
                    break;



                case "Invalid Argument":
                    context.getSource().sendFailure(Component.literal("Invalid Arguments"));
                    break;

            }
        } else {
            rawValue = null;
        }

        // Provide feedback to the player
        if (success) {
            context.getSource().sendSuccess(() -> Component.literal("Config option updated successfully. " + configKey + " is now: " + rawValue), false);
        } else {
            context.getSource().sendFailure(Component.literal("Failed to update config option. Check your input."));
        }

        return success ? 1 : 0;
    }

    @Override
    public int run(CommandContext<CommandSourceStack> context) throws CommandSyntaxException {
        return 0;
    }
}

