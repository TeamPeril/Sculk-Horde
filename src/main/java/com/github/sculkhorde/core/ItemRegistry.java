package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TranslatableComponent;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

public class ItemRegistry {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHorde.MOD_ID);
    
    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties().tab(SculkHorde.SCULK_GROUP)));

    public static final RegistryObject<DevWand> DEV_WAND = ITEMS.register("dev_wand", 
    		() -> new DevWand());

	public static final RegistryObject<DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			() -> new DevConversionWand());

	public static final RegistryObject<InfestationPurifier> INFESTATION_PURIFIER = ITEMS.register("infestation_purifier",
			() -> new InfestationPurifier());

	public static final RegistryObject<CustomItemProjectile> CUSTOM_ITEM_PROJECTILE = ITEMS.register("custom_item_projectile",
			() -> new CustomItemProjectile());

	public static final RegistryObject<CustomItemProjectile> SCULK_ACIDIC_PROJECTILE = ITEMS.register("sculk_acidic_projectile",
			() -> new CustomItemProjectile()
			{
				@Override
				public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
					tooltip.add(new TranslatableComponent("tooltip.sculkhorde.sculk_acidic_projectile"));
				}
			});

	public static final RegistryObject<SculkResinItem> SCULK_RESIN = ITEMS.register("sculk_resin",
			() -> new SculkResinItem());

	public static final RegistryObject<Item> CALCITE_CLUMP = ITEMS.register("calcite_clump",
			() -> new Item(new Item.Properties().tab(SculkHorde.SCULK_GROUP)){
				@Override
				public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
					tooltip.add(new TranslatableComponent("tooltip.sculkhorde.calcite_clump"));
				}
			});

	public static final RegistryObject<DevNodeSpawner> DEV_NODE_SPAWNER = ITEMS.register("dev_node_spawner",
			() -> new DevNodeSpawner());

	/** HELPER METHODS **/

	private static void registerSpawnEgg(final RegistryEvent.Register<Item> event, final EntityType<?> entity,
										 final String entityName, final int colorBase, final int colorSpots) {
		event.getRegistry().register(new SpawnEggItem(entity, colorBase, colorSpots, new Item.Properties().tab(SculkHorde.SCULK_GROUP))
				.setRegistryName(SculkHorde.MOD_ID, entityName + "_spawn_egg"));
	}
}
