package com.github.sculkhoard.core;

import com.github.sculkhoard.common.block.*;
import com.github.sculkhoard.common.block.BlockInfestation.SpreadingBlock;
import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SculkHoard.MOD_ID);

    //Method to Register Blocks & Register them as items
	private static <T extends Block>RegistryObject<T> registerBlock(String name, Supplier<T> block)
	{
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		registerBlockItem(name, toReturn);
		return toReturn;
	}

	//helper method to register a given block as a holdable item
	private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block)
	{
		ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(),
				new Item.Properties().tab(SculkHoard.SCULK_GROUP)));
	}

    //NOTE: Learned from https://www.youtube.com/watch?v=4igJ_nsFAZs "Creating a Block - Minecraft Forge 1.16.4 Modding Tutorial"
    
    //Register Ancient Large Bricks
    public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS =
			registerBlock("ancient_large_bricks", () -> new Block(AbstractBlock.Properties.of(
					Material.STONE, MaterialColor.TERRACOTTA_BLUE)
    				.strength(15f, 30f)//Hardness & Resistance
    				.harvestTool(ToolType.PICKAXE) //Block Preferred Harvest Tool
    				.harvestLevel(3) //-1 = All Levels; 0 = Wood; 1 = Stone & Gold; 2 = Iron; 3 = Diamond; 4 = Netherite
    				.sound(SoundType.ANCIENT_DEBRIS)
    				));

    //Ancient Large Tile
    public static final RegistryObject<Block> ANCIENT_LARGE_TILE =
			registerBlock("ancient_large_tile", () -> new Block(AbstractBlock.Properties.of(
					CrustBlock.MATERIAL, CrustBlock.MAP_COLOR)
    				.strength(15f, 30f)//Hardness & Resistance
    				.harvestTool(ToolType.PICKAXE) 
    				.harvestLevel(3)
    				.sound(SoundType.ANCIENT_DEBRIS)
    				));

	//Sculk Arachnoid
	public static final RegistryObject<Block> SCULK_ARACHNOID =
			registerBlock("sculk_arachnoid", () -> new Block(AbstractBlock.Properties.of(
							Material.PLANT, MaterialColor.QUARTZ)
					.strength(10f, 6f)//Hardness & Resistance
					.harvestTool(ToolType.SHOVEL)
					.harvestLevel(3)
					.sound(SoundType.HONEY_BLOCK)
			));

	//Sculk Dura Matter
	public static final RegistryObject<Block> SCULK_DURA_MATTER =
			registerBlock("sculk_dura_matter", () -> new Block(AbstractBlock.Properties.of(
							Material.PLANT, MaterialColor.QUARTZ)
					.strength(15f, 30f)//Hardness & Resistance
					.harvestTool(ToolType.PICKAXE)
					.harvestLevel(3)
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	//Sculk Dura Matter
	public static final RegistryObject<Block> CALCITE_ORE =
			registerBlock("calcite_ore", () -> new Block(AbstractBlock.Properties.of(
							Material.PLANT, MaterialColor.QUARTZ)
					.strength(15f, 30f)//Hardness & Resistance
					.harvestTool(ToolType.PICKAXE)
					.harvestLevel(3)
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<CrustBlock> CRUST =
			registerBlock("crust", () -> new CrustBlock());

	public static final RegistryObject<InfectedDirtBlock> INFECTED_DIRT =
			registerBlock("infected_dirt", () -> new InfectedDirtBlock());

	public static final RegistryObject<InfestedStoneActiveBlock> INFESTED_STONE_ACTIVE =
			registerBlock("infested_stone_active", () -> new InfestedStoneActiveBlock());

	public static final RegistryObject<InfestedStoneDormantBlock> INFESTED_STONE_DORMANT =
			registerBlock("infested_stone_dormant", () -> new InfestedStoneDormantBlock());

	public static final RegistryObject<InfestedLogActiveBlock> INFESTED_LOG_ACTIVE =
			registerBlock("infested_log_active", () -> new InfestedLogActiveBlock());

	public static final RegistryObject<InfestedLogDormantBlock> INFESTED_LOG_DORMANT =
			registerBlock("infested_log_dormant", () -> new InfestedLogDormantBlock());

	public static final RegistryObject<SpikeBlock> SPIKE =
			registerBlock("spike", () -> new SpikeBlock());

	public static final RegistryObject<SmallShroomBlock> SMALL_SHROOM =
			registerBlock("small_shroom", () -> new SmallShroomBlock());

	public static final RegistryObject<SculkFloraBlock> GRASS =
			registerBlock("grass", () -> new SculkFloraBlock());

	public static final RegistryObject<SculkFloraBlock> GRASS_SHORT =
			registerBlock("grass_short", () -> new SculkFloraBlock());

	public static final RegistryObject<SculkShroomCultureBlock> SCULK_SHROOM_CULTURE =
			registerBlock("sculk_shroom_culture", () -> new SculkShroomCultureBlock());

	public static final RegistryObject<CocoonRootBlock> COCOON_ROOT =
			registerBlock("cocoon_root", () -> new CocoonRootBlock());

	public static final RegistryObject<CocoonBlock> COCOON =
			registerBlock("cocoon", () -> new CocoonBlock());

	public static final RegistryObject<SculkMassBlock> SCULK_MASS =
			registerBlock("sculk_mass", () -> new SculkMassBlock());

	public static final RegistryObject<VeinBlock> VEIN =
			registerBlock("vein", () -> new VeinBlock());

	public static final RegistryObject<SculkNodeBlock> SCULK_NODE_BLOCK =
			registerBlock("sculk_brain", () -> new SculkNodeBlock());

	public static final RegistryObject<SpreadingBlock> SPREADING_BLOCK =
			registerBlock("spreading_block", () -> new SpreadingBlock());

	public static final RegistryObject<SculkBeeNestBlock> SCULK_BEE_NEST_BLOCK =
			registerBlock("sculk_bee_nest", () -> new SculkBeeNestBlock());

	public static final RegistryObject<SculkBeeNestCellBlock> SCULK_BEE_NEST_CELL_BLOCK =
			registerBlock("sculk_bee_nest_cell", () -> new SculkBeeNestCellBlock());

	public static final RegistryObject<SculkSummonerBlock> SCULK_SUMMONER_BLOCK =
			registerBlock("sculk_summoner", () -> new SculkSummonerBlock());

	public static final RegistryObject<SculkLivingRockBlock> SCULK_LIVING_ROCK_BLOCK =
			registerBlock("sculk_living_rock", () -> new SculkLivingRockBlock());

	public static final RegistryObject<SculkLivingRockRootBlock> SCULK_LIVING_ROCK_ROOT_BLOCK =
			registerBlock("sculk_living_rock_root", () -> new SculkLivingRockRootBlock());

	public static final RegistryObject<DevStructureTesterBlock> DEV_STRUCTURE_TESTER_BLOCK =
			registerBlock("dev_structure_tester", () -> new DevStructureTesterBlock());
}
