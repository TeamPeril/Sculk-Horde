package com.github.sculkhorde.core;

import com.github.sculkhorde.common.block.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;
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
			registerBlock("ancient_large_bricks", () -> new Block(BlockBehaviour.Properties.of()
							.mapColor(MapColor.TERRACOTTA_BLUE)
							.strength(15f, 30f)
							.requiresCorrectToolForDrops()
							.sound(SoundType.ANCIENT_DEBRIS)
    					));

    //Ancient Large Tile
    public static final RegistryObject<Block> ANCIENT_LARGE_TILE =
			registerBlock("ancient_large_tile", () -> new Block(BlockBehaviour.Properties.of()
							.mapColor(MapColor.TERRACOTTA_BLUE)
							.strength(15f, 30f)//Hardness & Resistance
							.requiresCorrectToolForDrops()
							.sound(SoundType.ANCIENT_DEBRIS)
    					));

	//Sculk Arachnoid
	public static final RegistryObject<Block> SCULK_ARACHNOID =
			registerBlock("sculk_arachnoid", () -> new Block(BlockBehaviour.Properties.of()
						.mapColor(MapColor.COLOR_CYAN)
						.strength(10f, 6f)//Hardness & Resistance
						.requiresCorrectToolForDrops()
						.sound(SoundType.HONEY_BLOCK)
				));

	//Sculk Dura Matter
	public static final RegistryObject<Block> SCULK_DURA_MATTER =
			registerBlock("sculk_dura_matter", () -> new Block(BlockBehaviour.Properties.of()
						.mapColor(MapColor.QUARTZ)
						.strength(15f, 30f)//Hardness & Resistance
						.requiresCorrectToolForDrops()
						.sound(SoundType.ANCIENT_DEBRIS)
			));

	//Sculk Dura Matter
	public static final RegistryObject<Block> CALCITE_ORE =
			registerBlock("calcite_ore", () -> new Block(BlockBehaviour.Properties.of()
						.mapColor(MapColor.QUARTZ)
						.strength(15f, 30f)//Hardness & Resistance
						.requiresCorrectToolForDrops()
						.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<Block> INFESTED_STONE =
			registerBlock("infested_stone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<InfestedLogBlock> INFESTED_LOG =
			registerBlock("infested_log", () -> new InfestedLogBlock());

	public static final RegistryObject<Block> INFESTED_SAND =
			registerBlock("infested_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_RED_SAND =
			registerBlock("infested_red_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_DEEPSLATE =
			registerBlock("infested_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.DEEPSLATE)
			));

	public static final RegistryObject<Block> INFESTED_SANDSTONE =
			registerBlock("infested_sandstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_RED_SANDSTONE =
			registerBlock("infested_red_sandstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_DIORITE =
			registerBlock("infested_diorite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRANITE =
			registerBlock("infested_granite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ANDESITE =
			registerBlock("infested_andesite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_TUFF =
			registerBlock("infested_tuff", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CALCITE =
			registerBlock("infested_calcite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.CALCITE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLED_DEEPSLATE =
			registerBlock("infested_cobbled_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(15f, 30f)//Hardness & Resistance
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRAVEL =
			registerBlock("infested_gravel", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<Block> INFESTED_MOSS =
			registerBlock("infested_moss", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.GRASS)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MOSS)
			));

	public static final RegistryObject<Block> INFESTED_SNOW =
			registerBlock("infested_snow", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SNOW)
			));

	public static final RegistryObject<Block> INFESTED_TERRACOTTA =
			registerBlock("infested_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLACK_TERRACOTTA =
			registerBlock("infested_black_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLUE_TERRACOTTA =
			registerBlock("infested_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BROWN_TERRACOTTA =
			registerBlock("infested_brown_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CYAN_TERRACOTTA =
			registerBlock("infested_cyan_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRAY_TERRACOTTA =
			registerBlock("infested_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GRAY)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GREEN_TERRACOTTA =
			registerBlock("infested_green_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GREEN)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_BLUE_TERRACOTTA =
			registerBlock("infested_light_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_GRAY_TERRACOTTA =
			registerBlock("infested_light_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIME_TERRACOTTA =
			registerBlock("infested_lime_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_MAGENTA_TERRACOTTA =
			registerBlock("infested_magenta_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_MAGENTA)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ORANGE_TERRACOTTA =
			registerBlock("infested_orange_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PINK_TERRACOTTA =
			registerBlock("infested_pink_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PINK)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PURPLE_TERRACOTTA =
			registerBlock("infested_purple_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PURPLE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_RED_TERRACOTTA =
			registerBlock("infested_red_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_RED)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_WHITE_TERRACOTTA =
			registerBlock("infested_white_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_YELLOW_TERRACOTTA =
			registerBlock("infested_yellow_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLESTONE =
			registerBlock("infested_cobblestone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CRYING_OBSIDIAN =
			registerBlock("infested_crying_obsidian", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTATION_WARD_BLOCK =
			registerBlock("infestation_ward_block", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.sound(SoundType.AMETHYST)
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

	public static final RegistryObject<SculkAncientNodeBlock> SCULK_ANCIENT_NODE_BLOCK =
			registerBlock("sculk_ancient_node", () -> new SculkAncientNodeBlock());

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

	public static class BlockTags
	{
		public static final TagKey<Block> SCULK_RAID_TARGET_HIGH_PRIORITY = create("sculk_raid_target/high_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_MEDIUM_PRIORITY = create("sculk_raid_target/medium_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_LOW_PRIORITY = create("sculk_raid_target/low_priority");
		public static final TagKey<Block> SCULK_BEE_HARVESTABLE = create("sculk_bee_harvestable");

		// Helper Function
		private static TagKey<Block> create(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation(SculkHorde.MOD_ID, location));
		}

		// Helper Function
		private static TagKey<Block> createForge(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation("forge", location));
		}

		// Helper Function
		private static TagKey<Block> createMinecraft(String location)
		{
			return net.minecraft.tags.BlockTags.create(new ResourceLocation(location));
		}
	}
}
