package com.github.sculkhorde.core;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModCreativeModeTab {
	
	public static final CreativeModeTab SCULK_HORDE_TAB = new CreativeModeTab("sculkhorde_tab") 
	{
		@Override
		public ItemStack makeIcon()
		{
			return new ItemStack(ModBlocks.SCULK_ANCIENT_NODE_BLOCK.get());
		}
	};
}
