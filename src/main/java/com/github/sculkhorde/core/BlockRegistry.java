package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.*;
import net.minecraft.world.level.block.SculkBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.material.Material;
import net.minecraft.world.level.material.MaterialColor;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.function.Supplier;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SculkHorde.MOD_ID);

    //Method to Register Blocks & Register them as items
	private static <T extends Block> RegistryObject<T> registerBlock(String name, Supplier<T> block)
	{
		RegistryObject<T> toReturn = BLOCKS.register(name, block);
		registerBlockItem(name, toReturn);
		return toReturn;
	}

	//helper method to register a given block as a holdable item
	private static <T extends Block> void registerBlockItem(String name, RegistryObject<T> block)
	{
		ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(),
				new Item.Properties()));
	}

    //NOTE: Learned from https://www.youtube.com/watch?v=4igJ_nsFAZs "Creating a Block - Minecraft Forge 1.16.4 Modding Tutorial"
    
    //Register Ancient Large Bricks
    public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS =
			registerBlock("ancient_large_bricks", () -> new Block(BlockBehaviour.Properties.of(
					Material.STONE, MaterialColor.TERRACOTTA_BLUE)
    				.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
    				.sound(SoundType.ANCIENT_DEBRIS)
    				));

    //Ancient Large Tile
    public static final RegistryObject<Block> ANCIENT_LARGE_TILE =
			registerBlock("ancient_large_tile", () -> new Block(BlockBehaviour.Properties.of(
					Material.STONE, MaterialColor.TERRACOTTA_BLUE)
    				.strength(15f, 30f)//Hardness & Resistance
    				.requiresCorrectToolForDrops()
    				.sound(SoundType.ANCIENT_DEBRIS)
    				));

	//Sculk Arachnoid
	public static final RegistryObject<Block> SCULK_ARACHNOID =
			registerBlock("sculk_arachnoid", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.QUARTZ)
					.strength(10f, 6f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.HONEY_BLOCK)
			));

	//Sculk Dura Matter
	public static final RegistryObject<Block> SCULK_DURA_MATTER =
			registerBlock("sculk_dura_matter", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.QUARTZ)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	//Sculk Dura Matter
	public static final RegistryObject<Block> CALCITE_ORE =
			registerBlock("calcite_ore", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.QUARTZ)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<Block> INFESTED_STONE =
			registerBlock("infested_stone", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.TERRACOTTA_BLACK)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<InfestedLogBlock> INFESTED_LOG =
			registerBlock("infested_log", () -> new InfestedLogBlock());

	public static final RegistryObject<Block> INFESTED_SAND =
			registerBlock("infested_sand", () -> new Block(BlockBehaviour.Properties.of(
							Material.SAND, MaterialColor.QUARTZ)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_DEEPSLATE =
			registerBlock("infested_deepslate", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.DEEPSLATE)
			));

	public static final RegistryObject<Block> INFESTED_SANDSTONE =
			registerBlock("infested_sandstone", () -> new Block(BlockBehaviour.Properties.of(
						Material.STONE, MaterialColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_DIORITE =
			registerBlock("infested_diorite", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRANITE =
			registerBlock("infested_granite", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ANDESITE =
			registerBlock("infested_andesite", () -> new Block(BlockBehaviour.Properties.of(
							Material.STONE, MaterialColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

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

	public static final RegistryObject<SculkMassBlock> SCULK_MASS =
			registerBlock("sculk_mass", () -> new SculkMassBlock());

	public static final RegistryObject<TendrilsBlock> TENDRILS =
			registerBlock("tendrils", () -> new TendrilsBlock());

	public static final RegistryObject<SculkNodeBlock> SCULK_NODE_BLOCK =
			registerBlock("sculk_node", () -> new SculkNodeBlock());

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
