package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import com.github.sculkhorde.common.potion.PuritySplashPotionItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.List;

public class ModItems {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHorde.MOD_ID);

	public static final RegistryObject<SculkSweeperSword> SCULK_SWEEPER_SWORD = ITEMS.register("sculk_sweeper_sword", SculkSweeperSword::new);
	public static final RegistryObject<Item> SCULK_ENDERMAN_CLEAVER = ITEMS.register("sculk_enderman_cleaver", () -> new Item(new Item.Properties()){
		@Override
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_enderman_cleaver"));
		}
	});

    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> CRYING_SOULS = ITEMS.register("crying_souls", () -> new Item(new Item.Properties()){
		@Override
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.crying_souls"));
		}
	});

	public static final RegistryObject<Item> PURE_SOULS = ITEMS.register("pure_souls", () -> new Item(new Item.Properties()){
		@Override
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.pure_souls"));
		}
	});

	public static final RegistryObject<Item> ESSENCE_OF_PURITY = ITEMS.register("essence_of_purity", () -> new Item(new Item.Properties()){
		@Override
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.essence_of_purity"));
		}
	});

	public static final RegistryObject<PuritySplashPotionItem> PURITY_SPLASH_POTION = ITEMS.register("purity_splash_potion",
			PuritySplashPotionItem::new);

    public static final RegistryObject<DevWand> DEV_WAND = ITEMS.register("dev_wand",
			DevWand::new);

	public static final RegistryObject<DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			DevConversionWand::new);

	public static final RegistryObject<InfestationPurifierItem> INFESTATION_PURIFIER = ITEMS.register("infestation_purifier",
			InfestationPurifierItem::new);

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

	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SPORE_SPEWER_SPAWN_EGG = ITEMS.register("sculk_spore_spewer_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SPORE_SPEWER, 0x111B21, 0xD1D6B6, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_MITE_SPAWN_EGG = ITEMS.register("sculk_mite_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_MITE, 0x062E37, 0x034150, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_MITE_AGGRESSOR_SPAWN_EGG = ITEMS.register("sculk_mite_aggressor_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_MITE_AGGRESSOR, 0x062E37, 0xA2AF86, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_ZOMBIE_SPAWN_EGG = ITEMS.register("sculk_zombie_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_ZOMBIE, 0x44975c, 0x062E37, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SPITTER_SPAWN_EGG = ITEMS.register("sculk_spitter_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SPITTER, 0xD1D6B6, 0x0BB4AA, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_CREEPER_SPAWN_EGG = ITEMS.register("sculk_creeper_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_CREEPER, 0x0DA70B, 0x062E37, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_HATCHER_SPAWN_EGG = ITEMS.register("sculk_hatcher_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_HATCHER, 0x443626, 0x062E37, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_VINDICATOR_SPAWN_EGG = ITEMS.register("sculk_vindicator_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_VINDICATOR, 0x959B9B, 0x062E37, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_RAVAGER_SPAWN_EGG = ITEMS.register("sculk_ravager_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_RAVAGER, 0x5B5049, 0x062E37, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_ENDERMAN_SPAWN_EGG = ITEMS.register("sculk_enderman_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_ENDERMAN, 0x111B21, 0xE079FA, new Item.Properties()));
}
