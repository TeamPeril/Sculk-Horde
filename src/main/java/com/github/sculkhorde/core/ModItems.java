package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import com.github.sculkhorde.util.ColorUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.RecordItem;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.github.sculkhorde.util.ColorUtil.hexToInt;

public class ModItems {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, SculkHorde.MOD_ID);

	public static final RegistryObject<SculkSweeperSword> SCULK_SWEEPER_SWORD = ITEMS.register("sculk_sweeper_sword", SculkSweeperSword::new);
	public static final RegistryObject<Item> SCULK_ENDERMAN_CLEAVER = ITEMS.register("sculk_enderman_cleaver", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_enderman_cleaver.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.sculk_enderman_cleaver.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

    public static final RegistryObject<Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties()));

	public static final RegistryObject<Item> CRYING_SOULS = ITEMS.register("crying_souls", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.crying_souls.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.crying_souls.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> PURE_SOULS = ITEMS.register("pure_souls", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.pure_souls.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.pure_souls.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> ESSENCE_OF_PURITY = ITEMS.register("essence_of_purity", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.essence_of_purity.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.essence_of_purity.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<PurificationFlaskItem> PURIFICATION_FLASK = ITEMS.register("purification_flask",
			PurificationFlaskItem::new);

    public static final RegistryObject<DevWand> DEV_WAND = ITEMS.register("dev_wand",
			DevWand::new);

	public static final RegistryObject<DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			DevConversionWand::new);

	public static final RegistryObject<InfestationPurifierItem> INFESTATION_PURIFIER = ITEMS.register("infestation_purifier",
			InfestationPurifierItem::new);

	public static final RegistryObject<CustomItemProjectileItem> CUSTOM_ITEM_PROJECTILE = ITEMS.register("custom_item_projectile",
			CustomItemProjectileItem::new);

	public static final RegistryObject<SculkAcidicProjectileItem> SCULK_ACIDIC_PROJECTILE = ITEMS.register("sculk_acidic_projectile",
			SculkAcidicProjectileItem::new);
	public static final RegistryObject<SculkResinItem> SCULK_RESIN = ITEMS.register("sculk_resin",
			SculkResinItem::new);

	public static final RegistryObject<Item> CALCITE_CLUMP = ITEMS.register("calcite_clump",
			() -> new Item(new Item.Properties()){
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
					if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_clump.functionality"));
					}
					else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.calcite_clump.lore"));
					}
					else
					{
						tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
					}
				}
			});

	public static final RegistryObject<DevNodeSpawner> DEV_NODE_SPAWNER = ITEMS.register("dev_node_spawner",
			() -> new DevNodeSpawner());

	public static final RegistryObject<DevRaidWand> DEV_RAID_WAND = ITEMS.register("dev_raid_wand",
			DevRaidWand::new);

	public static final RegistryObject<WardenBeefItem> WARDEN_BEEF = ITEMS.register("warden_beef",
			WardenBeefItem::new);

	public static final RegistryObject<Item> CHUNK_O_BRAIN = ITEMS.register("chunk_o_brain", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.chunk_o_brain.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.chunk_o_brain.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> DORMANT_HEART_OF_THE_HORDE = ITEMS.register("dormant_heart_of_the_horde", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.dormant_heart_of_the_horde.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.dormant_heart_of_the_horde.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> HEART_OF_THE_HORDE = ITEMS.register("heart_of_the_horde", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.heart_of_the_horde.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.heart_of_the_horde.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});


	public static final RegistryObject<Item> HEART_OF_PURITY = ITEMS.register("heart_of_purity", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.heart_of_purity.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.heart_of_purity.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});
	public static final RegistryObject<EyeOfPurityItem> EYE_OF_PURITY = ITEMS.register("eye_of_purity", () -> new EyeOfPurityItem()
	{
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.eye_of_purity.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.eye_of_purity.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> COIN_OF_CONTRIBUTION = ITEMS.register("coin_of_contribution", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.coin_of_contribution"));
		}
	});

	public static final RegistryObject<TomeOfSpinesItem> TOME_OF_SPINES = ITEMS.register("tome_of_spines",
			TomeOfSpinesItem::new);

	public static final RegistryObject<TomeOfReinforcementItem> TOME_OF_REINFORCEMENT = ITEMS.register("tome_of_reinforcement",
			TomeOfReinforcementItem::new);

	public static final RegistryObject<TomeOfVeilItem> TOME_OF_VEIL = ITEMS.register("tome_of_veil",
			TomeOfVeilItem::new);

	public static final RegistryObject<TomeOfSporeItem> TOME_OF_SPORE = ITEMS.register("tome_of_spore",
			TomeOfSporeItem::new);

	public static final RegistryObject<TomeOfSacrificeItem> TOME_OF_SACRIFICE = ITEMS.register("tome_of_sacrifice",
			TomeOfSacrificeItem::new);

	public static final RegistryObject<Item> SOULITE_SHARD = ITEMS.register("soulite_shard", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.soulite_shard.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.soulite_shard.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<SoulDisrupterItem> SOUL_DISRUPTER = ITEMS.register("soul_disrupter",
			SoulDisrupterItem::new);

	public static final RegistryObject<Item> FERRISCITE = ITEMS.register("ferriscite", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.ferriscite.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.ferriscite.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> DIASCITE = ITEMS.register("diascite", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.diascite.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.diascite.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> SOUL_ANIMATOR = ITEMS.register("soul_animator", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.soul_animator.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.soul_animator.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<Item> ANGEL_OF_REAPING_SOUL = ITEMS.register("angel_of_reaping_soul", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
			if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.angel_of_reaping_soul.functionality"));
			}
			else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.angel_of_reaping_soul.lore"));
			}
			else
			{
				tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
			}
		}
	});

	public static final RegistryObject<FerriscitePickaxeItem> FERRISCITE_PICKAXE = ITEMS.register("ferriscite_pickaxe",
			FerriscitePickaxeItem::new);
	public static final RegistryObject<FerrisciteShovelItem> FERRISCITE_SHOVEL = ITEMS.register("ferriscite_shovel",
			FerrisciteShovelItem::new);

	public static final RegistryObject<FerrisciteAxeItem> FERRISCITE_AXE = ITEMS.register("ferriscite_axe",
			FerrisciteAxeItem::new);

	public static final RegistryObject<FerrisciteHoeItem> FERRISCITE_HOE = ITEMS.register("ferriscite_hoe",
			FerrisciteHoeItem::new);

	public static final RegistryObject<DiascitePickaxeItem> DIASCITE_PICKAXE = ITEMS.register("diascite_pickaxe",
			DiascitePickaxeItem::new);
	public static final RegistryObject<DiasciteShovelItem> DIASCITE_SHOVEL = ITEMS.register("diascite_shovel",
			DiasciteShovelItem::new);

	public static final RegistryObject<DiasciteAxeItem> DIASCITE_AXE = ITEMS.register("diascite_axe",
			DiasciteAxeItem::new);

	public static final RegistryObject<DiasciteHoeItem> DIASCITE_HOE = ITEMS.register("diascite_hoe",
			DiasciteHoeItem::new);

	public static final RegistryObject<BladeOfPurityItem> BLADE_OF_PURITY = ITEMS.register("blade_of_purity",
			BladeOfPurityItem::new);

	public static final RegistryObject<BreadOfPurityItem> BREAD_OF_PURITY = ITEMS.register("bread_of_purity", BreadOfPurityItem::new);
	public static final RegistryObject<BeefOfPurityItem> BEEF_OF_PURITY = ITEMS.register("beef_of_purity", BeefOfPurityItem::new);
	public static final RegistryObject<PorkOfPurityItem> PORK_OF_PURITY = ITEMS.register("pork_of_purity", PorkOfPurityItem::new);
	public static final RegistryObject<ChickenOfPurityItem> CHICKEN_OF_PURITY = ITEMS.register("chicken_of_purity", ChickenOfPurityItem::new);
	public static final RegistryObject<BakedPotatoOfPurityItem> BAKED_POTATO_OF_PURITY = ITEMS.register("baked_potato_of_purity", BakedPotatoOfPurityItem::new);

	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SPORE_SPEWER_SPAWN_EGG = ITEMS.register("sculk_spore_spewer_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SPORE_SPEWER, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkBaseColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_MITE_SPAWN_EGG = ITEMS.register("sculk_mite_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_MITE, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_MITE_AGGRESSOR_SPAWN_EGG = ITEMS.register("sculk_mite_aggressor_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_MITE_AGGRESSOR, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkBoneColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_ZOMBIE_SPAWN_EGG = ITEMS.register("sculk_zombie_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_ZOMBIE, 0x44975c, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SPITTER_SPAWN_EGG = ITEMS.register("sculk_spitter_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SPITTER, 0xD1D6B6, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_CREEPER_SPAWN_EGG = ITEMS.register("sculk_creeper_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_CREEPER, 0x0DA70B, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_HATCHER_SPAWN_EGG = ITEMS.register("sculk_hatcher_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_HATCHER, 0x443626, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_VINDICATOR_SPAWN_EGG = ITEMS.register("sculk_vindicator_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_VINDICATOR, 0x959B9B, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_RAVAGER_SPAWN_EGG = ITEMS.register("sculk_ravager_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_RAVAGER, 0x5B5049, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_ENDERMAN_SPAWN_EGG = ITEMS.register("sculk_enderman_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_ENDERMAN, 0x111B21, 0xE079FA, new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_PHANTOM_SPAWN_EGG = ITEMS.register("sculk_phantom_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_PHANTOM, 0x88FF00, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SALMON_SPAWN_EGG = ITEMS.register("sculk_salmon_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SALMON, 0xA93432, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SQUID_SPAWN_EGG = ITEMS.register("sculk_squid_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SQUID, 0x1D3241, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_PUFFERFISH_SPAWN_EGG = ITEMS.register("sculk_pufferfish_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_PUFFERFISH, 0xE7A701, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_WITCH_SPAWN_EGG = ITEMS.register("sculk_witch_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_WITCH, 0x310000, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_GUARDIAN_SPAWN_EGG = ITEMS.register("sculk_guardian_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_GUARDIAN, 0x547A6B, hexToInt(ColorUtil.sculkAcidColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_BROOD_HATCHER_SPAWN_EGG = ITEMS.register("sculk_brood_hatcher_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_BROOD_HATCHER, hexToInt(ColorUtil.sculkBoneColor5), hexToInt(ColorUtil.sculkLightColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_BROODLING_SPAWN_EGG = ITEMS.register("sculk_broodling_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_BROODLING, hexToInt(ColorUtil.sculkBoneColor6), hexToInt(ColorUtil.sculkLightColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_SHEEP_SPAWN_EGG = ITEMS.register("sculk_sheep_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_SHEEP, 0xFFFFFF, hexToInt(ColorUtil.sculkBoneColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_GHAST_SPAWN_EGG = ITEMS.register("sculk_ghast_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_GHAST, 0xFFFFFF, hexToInt(ColorUtil.sculkAcidColor1), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_LEECH_SPAWN_EGG = ITEMS.register("sculk_leech_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_LEECH, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final RegistryObject<ForgeSpawnEggItem> SCULK_STINGER_SPAWN_EGG = ITEMS.register("sculk_stinger_spawn_egg",() ->  new ForgeSpawnEggItem(ModEntities.SCULK_STINGER, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));

	public static final RegistryObject<Item> DEEP_GREEN_MUSIC_DISC = ITEMS.register("deep_green_music_disc", () -> new RecordItem(6, ModSounds.DEEP_GREEN, new Item.Properties().stacksTo(1), 5120));
	public static final RegistryObject<Item> BLIND_AND_ALONE_MUSIC_DISC = ITEMS.register("blind_and_alone_music_disc", () -> new RecordItem(6, ModSounds.BLIND_AND_ALONE, new Item.Properties().stacksTo(1), 4920));
}
