package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHorde.MOD_ID);
    
    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties().tab(SculkHorde.SCULK_GROUP)));

    public static final RegistryObject<DevWand> DEV_WAND = ITEMS.register("dev_wand", 
    		() -> new DevWand());

	public static final RegistryObject<DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			() -> new DevConversionWand());

	public static final RegistryObject<AntiSculkMatter> ANTI_SCULK_MATTER = ITEMS.register("anti_sculk_matter",
			() -> new AntiSculkMatter());

	public static final RegistryObject<CustomItemProjectile> CUSTOM_ITEM_PROJECTILE = ITEMS.register("custom_item_projectile",
			() -> new CustomItemProjectile());

	public static final RegistryObject<CustomItemProjectile> SCULK_ACIDIC_PROJECTILE = ITEMS.register("sculk_acidic_projectile",
			() -> new CustomItemProjectile());

	public static final RegistryObject<SculkResinItem> SCULK_RESIN = ITEMS.register("sculk_resin",
			() -> new SculkResinItem());

	public static final RegistryObject<Item> CALCITE_CLUMP = ITEMS.register("calcite_clump",
			() -> new Item(new Item.Properties().tab(SculkHorde.SCULK_GROUP)));

	public static final RegistryObject<DevNodeSpawner> DEVN_NODE_SPAWNER = ITEMS.register("dev_node_spawner",
			() -> new DevNodeSpawner());

	/** HELPER METHODS **/

	private static void registerSpawnEgg(final RegistryEvent.Register<Item> event, final EntityType<?> entity,
										 final String entityName, final int colorBase, final int colorSpots) {
		event.getRegistry().register(new SpawnEggItem(entity, colorBase, colorSpots, new Item.Properties().tab(SculkHorde.SCULK_GROUP))
				.setRegistryName(SculkHorde.MOD_ID, entityName + "_spawn_egg"));
	}
}
