package com.github.sculkhorde.core;

import java.util.List;
import java.util.function.Supplier;

import com.github.sculkhorde.common.block.*;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.StairBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;

import javax.annotation.Nullable;

public class ModBlocks {
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
		ModItems.ITEMS.register(name, () -> new BlockItem(block.get(),
				new Item.Properties()));
	}

    //NOTE: Learned from https://www.youtube.com/watch?v=4igJ_nsFAZs "Creating a Block - Minecraft Forge 1.16.4 Modding Tutorial"
    
    //Register Ancient Large Bricks
    public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS =
			registerBlock("ancient_large_bricks", () -> new Block(BlockBehaviour.Properties.of()
							.mapColor(MapColor.TERRACOTTA_BLUE)
							.strength(4f, 30f)
							.requiresCorrectToolForDrops()
							.destroyTime(10f)
							.sound(SoundType.ANCIENT_DEBRIS)
    					));

    //Ancient Large Tile
    public static final RegistryObject<Block> ANCIENT_LARGE_TILE =
			registerBlock("ancient_large_tile", () -> new Block(BlockBehaviour.Properties.of()
							.mapColor(MapColor.TERRACOTTA_BLUE)
							.strength(4f, 30f)//Hardness & Resistance
							.requiresCorrectToolForDrops()
							.destroyTime(10f)
							.sound(SoundType.ANCIENT_DEBRIS)
    					));
	public static final RegistryObject<SculkArachnoidBlock> SCULK_ARACHNOID =
			registerBlock("sculk_arachnoid", SculkArachnoidBlock::new);
	public static final RegistryObject<SculkDuraMatterBlock> SCULK_DURA_MATTER =
			registerBlock("sculk_dura_matter", SculkDuraMatterBlock::new);


	public static final RegistryObject<Block> CALCITE_ORE =
			registerBlock("calcite_ore", () -> new Block(BlockBehaviour.Properties.of()
						.mapColor(MapColor.QUARTZ)
						.strength(4f, 30f)//Hardness & Resistance
						.destroyTime(5f)
						.requiresCorrectToolForDrops()
						.sound(SoundType.ANCIENT_DEBRIS)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_ore.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_ore.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<Block> INFESTED_STONE =
			registerBlock("infested_stone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));
	
	public static final RegistryObject<StairBlock> INFESTED_STONE_STAIRS =
			registerBlock("infested_stone_stairs", () -> new StairBlock(() -> StairBlock.stateById(0), BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.ANCIENT_DEBRIS)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_LOG =
			registerBlock("infested_log", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infested_log.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infested_log.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<Block> INFESTED_SAND =
			registerBlock("infested_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_RED_SAND =
			registerBlock("infested_red_sand", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SAND)
			));

	public static final RegistryObject<Block> INFESTED_DEEPSLATE =
			registerBlock("infested_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.DEEPSLATE)
			));

	public static final RegistryObject<Block> INFESTED_SANDSTONE =
			registerBlock("infested_sandstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));
	public static final RegistryObject<Block> INFESTED_DIORITE =
			registerBlock("infested_diorite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRANITE =
			registerBlock("infested_granite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ANDESITE =
			registerBlock("infested_andesite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_TUFF =
			registerBlock("infested_tuff", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CALCITE =
			registerBlock("infested_calcite", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.CALCITE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLED_DEEPSLATE =
			registerBlock("infested_cobbled_deepslate", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRAVEL =
			registerBlock("infested_gravel", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<Block> INFESTED_MOSS =
			registerBlock("infested_moss", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.GRASS)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MOSS)
			));

	public static final RegistryObject<Block> INFESTED_SNOW =
			registerBlock("infested_snow", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.SNOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.SNOW)
			));

	public static final RegistryObject<Block> INFESTED_TERRACOTTA =
			registerBlock("infested_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLACK_TERRACOTTA =
			registerBlock("infested_black_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BLUE_TERRACOTTA =
			registerBlock("infested_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BROWN_TERRACOTTA =
			registerBlock("infested_brown_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BROWN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CYAN_TERRACOTTA =
			registerBlock("infested_cyan_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_CYAN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GRAY_TERRACOTTA =
			registerBlock("infested_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_GREEN_TERRACOTTA =
			registerBlock("infested_green_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_GREEN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_BLUE_TERRACOTTA =
			registerBlock("infested_light_blue_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIGHT_GRAY_TERRACOTTA =
			registerBlock("infested_light_gray_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_LIME_TERRACOTTA =
			registerBlock("infested_lime_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_LIGHT_GREEN)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_MAGENTA_TERRACOTTA =
			registerBlock("infested_magenta_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_MAGENTA)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ORANGE_TERRACOTTA =
			registerBlock("infested_orange_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_ORANGE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PINK_TERRACOTTA =
			registerBlock("infested_pink_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PINK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_PURPLE_TERRACOTTA =
			registerBlock("infested_purple_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_PURPLE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_RED_TERRACOTTA =
			registerBlock("infested_red_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_RED)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_WHITE_TERRACOTTA =
			registerBlock("infested_white_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_WHITE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_YELLOW_TERRACOTTA =
			registerBlock("infested_yellow_terracotta", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_YELLOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_COBBLESTONE =
			registerBlock("infested_cobblestone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));
	
	public static final RegistryObject<StairBlock> INFESTED_COBBLESTONE_STAIRS =
			registerBlock("infested_cobblestone_stairs", () -> new StairBlock(() -> StairBlock.stateById(0), BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLUE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CRYING_OBSIDIAN =
			registerBlock("infested_crying_obsidian", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_PURPLE)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
					.explosionResistance(1200f)
					.destroyTime(50f)
			));

	public static final RegistryObject<Block> INFESTED_MUD =
			registerBlock("infested_mud", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD)
			));

	public static final RegistryObject<Block> INFESTED_PACKED_MUD =
			registerBlock("infested_packed_mud", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.DIRT)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.PACKED_MUD)
			));

	public static final RegistryObject<Block> INFESTED_MUD_BRICKS =
			registerBlock("infested_mud_bricks", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.DIRT)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD_BRICKS)
			));

	public static final RegistryObject<Block> INFESTED_BLACKSTONE =
			registerBlock("infested_blackstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_BASALT =
			registerBlock("infested_basalt", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_SMOOTH_BASALT =
			registerBlock("infested_smooth_basalt", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_GRAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_ENDSTONE =
			registerBlock("infested_endstone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_NETHERRACK =
			registerBlock("infested_netherrack", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CRIMSON_NYLIUM =
			registerBlock("infested_crimson_nylium", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_WARPED_NYLIUM =
			registerBlock("infested_warped_nylium", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.NETHER)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_MOSSY_COBBLESTONE =
			registerBlock("infested_mossy_cobblestone", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor. STONE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));
	
	public static final RegistryObject<StairBlock> INFESTED_MOSSY_COBBLESTONE_STAIRS =
			registerBlock("infested_mossy_cobblestone_stairs", () -> new StairBlock(() -> StairBlock.stateById(0), BlockBehaviour.Properties.of()
					.mapColor(MapColor. STONE)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<Block> INFESTED_CLAY =
			registerBlock("infested_clay", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor. CLAY)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MUD)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_WOOD_MASS =
			registerBlock("infested_wood_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));
	
	public static final RegistryObject<InfestedStairBlock> INFESTED_WOOD_STAIRS =
			registerBlock("infested_wood_stairs", () -> new InfestedStairBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.QUARTZ)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.WOOD)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_STURDY_MASS =
			registerBlock("infested_sturdy_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.STONE)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_CRUMPLED_MASS =
			registerBlock("infested_crumpled_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.GRAVEL)
			));

	public static final RegistryObject<InfestedTagBlock> INFESTED_COMPOST_MASS =
			registerBlock("infested_compost_mass", () -> new InfestedTagBlock(BlockBehaviour.Properties.of()
					.mapColor(MapColor.TERRACOTTA_BLACK)
					.strength(4f, 30f)//Hardness & Resistance
					.destroyTime(5f)
					.requiresCorrectToolForDrops()
					.sound(SoundType.MOSS)
			));


	public static final RegistryObject<Block> INFESTATION_WARD_BLOCK =
			registerBlock("infestation_ward_block", () -> new Block(BlockBehaviour.Properties.of()
					.mapColor(MapColor.COLOR_YELLOW)
					.sound(SoundType.AMETHYST)
			)
			{
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, @Nullable BlockGetter iBlockReader, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_ward_block.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.infestation_ward_block.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<SpikeBlock> SPIKE =
			registerBlock("spike", SpikeBlock::new);

	public static final RegistryObject<SmallShroomBlock> SMALL_SHROOM =
			registerBlock("small_shroom", SmallShroomBlock::new);

	public static final RegistryObject<SculkFloraBlock> GRASS =
			registerBlock("grass", SculkFloraBlock::new);

	public static final RegistryObject<SculkFloraBlock> GRASS_SHORT =
			registerBlock("grass_short", SculkFloraBlock::new);

	public static final RegistryObject<SculkShroomCultureBlock> SCULK_SHROOM_CULTURE =
			registerBlock("sculk_shroom_culture", SculkShroomCultureBlock::new);

	public static final RegistryObject<SculkMassBlock> SCULK_MASS =
			registerBlock("sculk_mass", SculkMassBlock::new);

	public static final RegistryObject<TendrilsBlock> TENDRILS =
			registerBlock("tendrils", TendrilsBlock::new);

	public static final RegistryObject<SculkNodeBlock> SCULK_NODE_BLOCK =
			registerBlock("sculk_node", SculkNodeBlock::new);

	public static final RegistryObject<SculkAncientNodeBlock> SCULK_ANCIENT_NODE_BLOCK =
			registerBlock("sculk_ancient_node", SculkAncientNodeBlock::new);

	public static final RegistryObject<SculkBeeNestBlock> SCULK_BEE_NEST_BLOCK =
			registerBlock("sculk_bee_nest", SculkBeeNestBlock::new);

	public static final RegistryObject<SculkBeeNestCellBlock> SCULK_BEE_NEST_CELL_BLOCK =
			registerBlock("sculk_bee_nest_cell", SculkBeeNestCellBlock::new);

	public static final RegistryObject<SculkSummonerBlock> SCULK_SUMMONER_BLOCK =
			registerBlock("sculk_summoner", SculkSummonerBlock::new);

	public static final RegistryObject<SculkLivingRockBlock> SCULK_LIVING_ROCK_BLOCK =
			registerBlock("sculk_living_rock", SculkLivingRockBlock::new);

	public static final RegistryObject<SculkLivingRockRootBlock> SCULK_LIVING_ROCK_ROOT_BLOCK =
			registerBlock("sculk_living_rock_root", SculkLivingRockRootBlock::new);

	public static final RegistryObject<DevStructureTesterBlock> DEV_STRUCTURE_TESTER_BLOCK =
			registerBlock("dev_structure_tester", DevStructureTesterBlock::new);

	public static final RegistryObject<DevMassInfectinator3000Block> DEV_MASS_INFECTINATOR_3000_BLOCK =
			registerBlock("dev_mass_infectinator_3000", DevMassInfectinator3000Block::new);

	public static final RegistryObject<SoulHarvesterBlock> SOUL_HARVESTER_BLOCK =
			registerBlock("soul_harvester", SoulHarvesterBlock::new);

	public static final RegistryObject<FleshyCompostBlock> PASTY_ORGANIC_MASS =
			registerBlock("fleshy_compost_block", FleshyCompostBlock::new);

	public static final RegistryObject<DiseasedKelpBlock> DISEASED_KELP_BLOCK =
			registerBlock("diseased_kelp_block", DiseasedKelpBlock::new);

	public static class BlockTags
	{
		public static final TagKey<Block> SCULK_RAID_TARGET_HIGH_PRIORITY = create("sculk_raid_target/high_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_MEDIUM_PRIORITY = create("sculk_raid_target/medium_priority");
		public static final TagKey<Block> SCULK_RAID_TARGET_LOW_PRIORITY = create("sculk_raid_target/low_priority");
		public static final TagKey<Block> SCULK_BEE_HARVESTABLE = create("sculk_bee_harvestable");
		public static final TagKey<Block> INFESTED_BLOCK = create("infested_block");

		public static final TagKey<Block> NOT_INFESTABLE = create("not_infestable");

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
