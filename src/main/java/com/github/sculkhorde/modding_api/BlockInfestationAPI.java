package com.github.sculkhorde.modding_api;

import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.BlockInfestationTable;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.infestation_entries.ITagInfestedBlock;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

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
        if(!BlockInfestationSystem.INFESTATION_TABLES.contains(table)) {
            BlockInfestationSystem.INFESTATION_TABLES.add(table);
            BlockInfestationSystem.INFESTATION_TABLES.sort(Comparator.comparing(BlockInfestationTable::getPriority));
        }
        return table;
    }


    /**
     * Retrieves the table of explicitly defined block infestations.
     * <p>
     * This table maps a specific block to its corresponding infested variant, such as
     * Stone -> Infested Stone. This explicit mapping
     * takes the second-highest priority in the infestation and purification process.
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
     * Retrieves the table of explicitly defined block entity infestations.
     * <p>
     * This table maps a specific block (with a block entity) to its corresponding infested variant, such as
     * Crafting Table -> Infested Stone. This explicit mapping
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
    public static BlockInfestationTable getExplicitBlockEntityInfestationTable()
    {
        return BlockInfestationSystem.explicitInfectableBlockEntityBlocks;
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

    // --- Convenience add-entry wrappers for modders ---

    /**
     * Adds an explicit mapping from a normal block to an infected block state.
     * Use this to map a specific vanilla or mod block to an infested BlockState.
     * The mapping will be inserted into the explicit infestation table and will
     * be considered by the infestation system when attempting to infect blocks.
     *
     * @param priority numeric priority used for matching order (entries are sorted by priority)
     * @param normalVariant the uninfected Block to match
     * @param infectedVariant the BlockState that will replace the normal block when infected
     */
    public static void addExplicitEntry(float priority, Block normalVariant, BlockState infectedVariant)
    {
        getExplicitBlockInfestationTable().addEntry(priority, normalVariant, infectedVariant);
    }

    /**
     * Adds an explicit mapping for blocks that contain a block entity.
     * This behaves like {@link #addExplicitEntry(float, Block, BlockState)} but targets
     * the table used for blocks with BlockEntities. Use this to support blocks that
     * require a BlockEntity when infested.
     *
     * @param priority numeric priority used for matching order
     * @param normalVariant the uninfected Block (with a BlockEntity) to match
     * @param infectedVariant the BlockState to place when infected
     */
    public static void addExplicitBlockEntityEntry(float priority, Block normalVariant, BlockState infectedVariant)
    {
        getExplicitBlockEntityInfestationTable().addEntry(priority, normalVariant, infectedVariant);
    }

    /**
     * Adds a mapping for entries that are "only curable". These entries are used by the
     * system when a block has an infected form but only the cured (normal) form should be restored
     * by the curing logic.
     *
     * @param priority numeric priority used for matching order
     * @param normalVariant the block state that should be restored when curing (normal variant)
     * @param infectedVariant the infected block (the variant that exists in-world while infected)
     */
    public static void addExplicitOnlyCurableEntry(float priority, Block normalVariant, Block infectedVariant)
    {
        BlockInfestationSystem.explicitCurableBlocks.addOnlyCurableEntry(priority, normalVariant, infectedVariant);
    }

    /**
     * Adds a mapping for an "only curable" entry using registry IDs. Use this when
     * you only have the registry names (for compatibility with other mods).
     *
     * @param priority numeric priority used for matching order
     * @param normalBlockID registry id (namespace:name) of the normal block
     * @param infectedBlockID registry id (namespace:name) of the infected block
     */
    public static void addExplicitOnlyCurableEntryById(float priority, String normalBlockID, String infectedBlockID)
    {
        getExplicitBlockInfestationTable().addOnlyCurableEntry(priority, normalBlockID, infectedBlockID);
    }

    /**
     * Adds an entry that maps a Block Tag to an infested variant. This is useful when you
     * want to support many blocks that share the same tag (for example, all logs) without
     * registering each block individually.
     *
     * @param priority numeric priority used for matching order
     * @param normalTag the TagKey representing the set of normal blocks to match
     * @param infectedVariant an implementation of ITagInfestedBlock that knows how to create the infected BlockState
     * @param defaultNormalVariant a default fallback Block used for inference when the exact normal block cannot be determined
     */
    public static void addTagBlockEntry(float priority, TagKey<Block> normalTag, ITagInfestedBlock infectedVariant, Block defaultNormalVariant)
    {
        getTagBlockInfestationTable().addBlockTagEntry(priority, normalTag, infectedVariant, defaultNormalVariant);
    }

    /**
     * Adds an entry for non-full (partial) blocks by tag. Use this for slabs, stairs,
     * fences, and other blocks that are not full cubes and need special handling.
     *
     * @param priority numeric priority used for matching order
     * @param normalTag the TagKey of the non-full blocks to match
     * @param infectedVariant implementation that produces the infected BlockState
     * @param defaultNormalVariant a default fallback Block used if the specific block cannot be inferred
     */
    public static void addTagNonFullBlockTagEntry(float priority, TagKey<Block> normalTag, ITagInfestedBlock infectedVariant, Block defaultNormalVariant)
    {
        getTagNonFullBlockInfestationTable().addBlockTagEntry(priority, normalTag, infectedVariant, defaultNormalVariant);
    }

    /**
     * Adds an entry that requires a tool tag and tier to recover the block.
     * This is useful when the infected variant should only be curable with a tool of
     * a certain tier (for example, iron or better).
     *
     * @param priority numeric priority used for matching order
     * @param toolRequired TagKey representing the tool-required tag (e.g., BlockTags.MINEABLE_WITH_PICKAXE)
     * @param tier the minimum Tier required to mine/restore this block
     * @param infectedVariant implementation describing the infected BlockState
     * @param defaultNormalVariant a default fallback Block used for inference when necessary
     */
    public static void addToolTagEntry(float priority, TagKey<Block> toolRequired, Tier tier, ITagInfestedBlock infectedVariant, Block defaultNormalVariant)
    {
        getTagBlockInfestationTable().addToolTagEntry(priority, toolRequired, tier, infectedVariant, defaultNormalVariant);
    }

    /**
     * Adds an entry that matches two tags (e.g., a block that matches both a material tag and a mineable-with tag)
     * and optionally requires a minimum tool tier to be considered curable.
     *
     * @param priority numeric priority used for matching order
     * @param tag1 the primary TagKey to match
     * @param tag2 the secondary TagKey to match
     * @param tier minimum Tier required for mining/curing
     * @param infestedVariant implementation describing the infected BlockState
     * @param defaultNormalVariant a default fallback Block used when inference is necessary
     */
    public static void addMultiTagEntry(float priority, TagKey<Block> tag1, TagKey<Block> tag2, Tier tier, ITagInfestedBlock infestedVariant, Block defaultNormalVariant)
    {
        getTagNonFullBlockInfestationTable().addMultiTagEntry(priority, tag1, tag2, tier, infestedVariant, defaultNormalVariant);
    }

    /**
     * Adds an entry produced from config-driven data. The config entry handler (ITagInfestedBlock)
     * should know how to construct an infected BlockState for matching blocks.
     *
     * @param infectedVariant an ITagInfestedBlock implementation obtained from config parsing
     */
    public static void addConfigEntry(ITagInfestedBlock infectedVariant)
    {
        getConfigBlockInfestationTable().addConfigEntry(infectedVariant);
    }

    /**
     * Adds a flora block to be spawned on top of Sculk Infected Blocks.
     * Please make sure you only call this once (typically during mod initialization).
     * The supplied block will be added to the global Sculk flora pool with the
     * provided spawn weight which affects selection probability.
     *
     * @param flora The Block to spawn on top of infected blocks (e.g., sculk plants)
     * @param spawnWeight Relative weight for selection when choosing which flora to spawn
     * @see com.github.sculkhorde.core.SculkHorde#randomSculkFlora
     */
    public static void addSculkFloraEntry(Block flora, int spawnWeight)
    {
        SculkHorde.randomSculkFlora.addEntry(flora, spawnWeight);
    }

    /**
     * Registers an item ID so that infection cursors can "eat" that item.
     * This method updates the config-backed map used by the infection cursor logic.
     * Use this to make custom items consumable by infection cursors.
     *
     * @param itemID the item's registry ID in the format "namespace:name" to register
     */
    public static void addToListOfItemsCursorsCanEat(String itemID)
    {
        ModConfig.Server.infection_cursor_item_eat_list.put(itemID, true);
    }

}
