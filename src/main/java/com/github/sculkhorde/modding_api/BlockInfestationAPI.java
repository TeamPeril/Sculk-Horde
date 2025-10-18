package com.github.sculkhorde.modding_api;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.BlockInfestationTable;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import net.minecraft.world.level.block.Block;

import java.util.Comparator;

public class BlockInfestationAPI {

    /**
     * Allows you to add your own block infestation table.
     * Block Infestation tables hold entries of valid blocks that can be infested
     * and what they will turn into. Each table serves a different purpose.
     * Only add a table once.
     * @param table The table you wish to insert.
     * @return The table you added.
     */
    public static BlockInfestationTable addBlockInfestationTable(BlockInfestationTable table)
    {
        BlockInfestationSystem.INFESTATION_TABLES.add(table);
        BlockInfestationSystem.INFESTATION_TABLES.sort(Comparator.comparing(BlockInfestationTable::getPriority));
        return table;
    }

    /**
     * Retrieves the table of explicitly defined block infestations.
     * <p>
     * This table maps a specific block to its corresponding infested variant, such as
     * Stone -> Infested Stone. This explicit mapping
     * takes the highest priority in the infestation and purification process.
     * <p>
     * The returned block state for infestation or purification will always be the default
     * block state of the target block.
     * <p>
     * This method is intended for adding custom entries to the table. It is highly
     * recommended to retrieve the table and add your custom mapping <b>only once</b>,
     * typically during your mod's initialization phase (e.g., in a static initializer or
     * during mod setup events).
     *
     * @return A {@link BlockInfestationTable} containing explicit block mappings.
     */
    public static BlockInfestationTable getExplicitBlockInfestationTable()
    {
        return BlockInfestationSystem.explicitInfectableBlocks;
    }

    /**
     * Retrieves the table of tag defined block infestations.
     * <p>
     * This table maps a specific block tag to its corresponding infested variant, such as
     * BlockTags.MINEABLE_WITH_PICKAXE -> Infested Sturdy Mass.
     * <p>
     * Blocks that are infested get their blockstate stored in the block entity of the
     * infested block. When purified, the block returns back to its exact blockstate
     * before infestation.
     * <p>
     * This method is intended for adding custom entries to the table. It is highly
     * recommended to retrieve the table and add your custom mapping <b>only once</b>,
     * typically during your mod's initialization phase (e.g., in a static initializer or
     * during mod setup events).
     *
     * @return A {@link BlockInfestationTable} containing tag block mappings.
     */
    public static BlockInfestationTable getTagBlockInfestationTable()
    {
        return BlockInfestationSystem.tagInfectableBlocks;
    }

    /**
     * Retrieves the table of tag defined non-full block infestations.
     * <p>
     * This table maps a specific non-full block tag to its corresponding infested variant, such as
     * stairs mineable via pickaxes -> infested sturdy mass stairs.
     * <p>
     * Blocks that are infested get their blockstate stored in the block entity of the
     * infested block. This infested block then gets the same properties applied to it
     * that the block had before infestation.When purified, the block returns back to
     * its exact blockstate before infestation.
     * <p>
     * This method is intended for adding custom entries to the table. It is highly
     * recommended to retrieve the table and add your custom mapping <b>only once</b>,
     * typically during your mod's initialization phase (e.g., in a static initializer or
     * during mod setup events).
     *
     * @return A {@link BlockInfestationTable} containing tag nonfull block mappings.
     */
    public static BlockInfestationTable getTagNonFullBlockInfestationTable()
    {
        return BlockInfestationSystem.tagInfectableNonFullBlocks;
    }

    /**
     * Retrieves the table of config defined block infestations.
     * <p>
     * This table contains all the entries added via the config.
     * <p>
     * All blocks that are infested get turned into infested sturdy mass.
     * <p>
     * This method is intended for adding custom entries to the table. It is highly
     * recommended to retrieve the table and add your custom mapping <b>only once</b>,
     * typically during your mod's initialization phase (e.g., in a static initializer or
     * during mod setup events).
     *
     * @return A {@link BlockInfestationTable} containing config block mappings.
     */
    public static BlockInfestationTable getConfigBlockInfestationTable()
    {
        return BlockInfestationSystem.configInfectableBlocks;
    }

    /**
     * Adds a flora block to be spawned on top of Sculk Infected Blocks.
     * Please make sure you only call this once or I'll have your head.
     * Check the weights used in {@link BlockInfestationSystem} <br>
     *     WEIGHT_SCULK_CATALYST <br>
     *     WEIGHT_SCULK_SUMMONER <br>
     *     WEIGHT_SCULK_SENSOR <br>
     *     WEIGHT_SPIKE <br>
     *     WEIGHT_SHROOMS <br>
     *     WEIGHT_GRASS <br>
     * @param flora The Block you want to spawn.
     * @param spawnWeight The chance it has of spawning.
     */
    public static void addSculkFloraEntry(Block flora, int spawnWeight)
    {
        SculkHorde.randomSculkFlora.addEntry(flora, spawnWeight);
    }

    /**
     * Adds an item to the list of items that infection cursors can eat. Only call this once per item.
     * @param itemID The registry ID of the item to add.
     */
    public static void addToListOfItemsCursorsCanEat(String itemID)
    {
        ModConfig.Server.infection_cursor_item_eat_list.put(itemID, true);
    }
}
