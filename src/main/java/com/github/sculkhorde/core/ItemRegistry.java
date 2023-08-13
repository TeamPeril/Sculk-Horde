package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ItemRegistry {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHorde.MOD_ID);
    
    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> CRYING_SOULS = ITEMS.register("crying_souls", () -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> PURE_SOULS = ITEMS.register("pure_souls", () -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> ESSENCE_OF_PURITY = ITEMS.register("essence_of_purity", () -> new Item(new Item.Properties()));

    public static final RegistryObject<DevWand> DEV_WAND = ITEMS.register("dev_wand",
			DevWand::new);

	public static final RegistryObject<DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			DevConversionWand::new);

	public static final RegistryObject<InfestationPurifier> INFESTATION_PURIFIER = ITEMS.register("infestation_purifier",
			InfestationPurifier::new);

	public static final RegistryObject<CustomItemProjectile> CUSTOM_ITEM_PROJECTILE = ITEMS.register("custom_item_projectile",
			CustomItemProjectile::new);

	public static final RegistryObject<CustomItemProjectile> SCULK_ACIDIC_PROJECTILE = ITEMS.register("sculk_acidic_projectile",
			() -> new CustomItemProjectile()
			{
				@Override
				public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
					tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_acidic_projectile"));
				}
			});

	public static final RegistryObject<CustomItemProjectile> PURIFICATION_FLASK_ITEM = ITEMS.register("purification_flask_item",
			PurificationFlaskItem::new);

	public static final RegistryObject<SculkResinItem> SCULK_RESIN = ITEMS.register("sculk_resin",
			SculkResinItem::new);

	public static final RegistryObject<Item> CALCITE_CLUMP = ITEMS.register("calcite_clump",
			() -> new Item(new Item.Properties()){
				@Override
				public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
					tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_clump"));
				}
			});

	public static final RegistryObject<DevNodeSpawner> DEV_NODE_SPAWNER = ITEMS.register("dev_node_spawner",
			() -> new DevNodeSpawner());

	public static final RegistryObject<DevRaidWand> DEV_RAID_WAND = ITEMS.register("dev_raid_wand",
			DevRaidWand::new);

	public static final RegistryObject<WardenBeefItem> WARDEN_BEEF = ITEMS.register("warden_beef",
			WardenBeefItem::new);

}
