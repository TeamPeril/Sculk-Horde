package com.github.sculkhoard.core;


import com.github.sculkhoard.common.item.AntiSculkMatter;
import com.github.sculkhoard.common.item.DevConversionWand;
import com.github.sculkhoard.common.item.DevWand;
import com.github.sculkhoard.common.item.CustomItemProjectile;
import net.minecraft.entity.EntityType;
import net.minecraft.item.Item;
import net.minecraft.item.SpawnEggItem;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHoard.MOD_ID);
    
    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties().tab(SculkHoard.SCULK_GROUP)));

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

	/** HELPER METHODS **/

	private static void registerSpawnEgg(final RegistryEvent.Register<Item> event, final EntityType<?> entity,
										 final String entityName, final int colorBase, final int colorSpots) {
		event.getRegistry().register(new SpawnEggItem(entity, colorBase, colorSpots, new Item.Properties().tab(SculkHoard.SCULK_GROUP))
				.setRegistryName(SculkHoard.MOD_ID, entityName + "_spawn_egg"));
	}
}
