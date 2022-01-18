package com.github.sculkhoard.core;

import com.github.sculkhoard.common.block.*;
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

	public static final RegistryObject<CrustBlock> CRUST =
			registerBlock("crust", () -> new CrustBlock());

	public static final RegistryObject<InfectedDirtBlock> INFECTED_DIRT =
			registerBlock("infected_dirt", () -> new InfectedDirtBlock());

	public static final RegistryObject<InfestedStoneActiveBlock> INFESTED_STONE_ACTIVE =
			registerBlock("infested_stone_active", () -> new InfestedStoneActiveBlock());

	public static final RegistryObject<InfestedStoneBlock> INFESTED_STONE =
			registerBlock("infested_stone", () -> new InfestedStoneBlock());

	public static final RegistryObject<InfestedLogBlock> INFESTED_LOG =
			registerBlock("infested_log", () -> new InfestedLogBlock());

	public static final RegistryObject<SpikeBlock> SPIKE =
			registerBlock("spike", () -> new SpikeBlock());

	public static final RegistryObject<SmallShroomBlock> SMALL_SHROOM =
			registerBlock("small_shroom", () -> new SmallShroomBlock());

	public static final RegistryObject<SculkFloraBlock> GRASS =
			registerBlock("grass", () -> new SculkFloraBlock());

	public static final RegistryObject<SculkFloraBlock> GRASS_SHORT =
			registerBlock("grass_short", () -> new SculkFloraBlock());

	public static final RegistryObject<CocoonRootBlock> COCOON_ROOT =
			registerBlock("cocoon_root", () -> new CocoonRootBlock());

	public static final RegistryObject<CocoonBlock> COCOON =
			registerBlock("cocoon", () -> new CocoonBlock());

	public static final RegistryObject<SculkMassBlock> SCULK_MASS =
			registerBlock("sculk_mass", () -> new SculkMassBlock());

	public static final RegistryObject<VeinBlock> VEIN =
			registerBlock("vein", () -> new VeinBlock());
}
