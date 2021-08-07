package com.github.sculkhoard.core;

import net.minecraft.block.AbstractBlock;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.block.SoundType;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialColor;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraftforge.common.ToolType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Main.MOD_ID);

    //Method to Register Blocks & Register them as items
    private static RegistryObject<Block> createBlock(String name, AbstractBlock.Properties properties) {
        RegistryObject<Block> block = BLOCKS.register(name, () -> new Block(properties));
        ItemRegistry.ITEMS.register(name, () -> new BlockItem(block.get(), new Item.Properties().tab(Main.SCULK_GROUP)));
        return block;
    }

    //Register Ancient Large Bricks
    //public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS = createBlock("ancient_large_bricks", AbstractBlock.Properties.copy(Blocks.STONE_BRICKS));
    public static final RegistryObject<Block> ANCIENT_LARGE_BRICKS = 
    		createBlock("ancient_large_bricks", AbstractBlock.Properties.of(Material.STONE, MaterialColor.TERRACOTTA_BLUE)
    				.strength(15f, 30f)//Hardness & Resistance
    				.harvestTool(ToolType.PICKAXE) //Block Prefered Harvest Tool
    				.harvestLevel(3) //-1 = All Levels; 0 = Wood; 1 = Stone & Gold; 2 = Iron; 3 = Diamond; 4 = Netherite
    				.sound(SoundType.ANCIENT_DEBRIS)
    				);

}
