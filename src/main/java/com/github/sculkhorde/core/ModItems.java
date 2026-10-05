package com.github.sculkhorde.core;


import com.github.sculkhorde.common.item.*;
import com.github.sculkhorde.util.ColorUtil;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.common.DeferredSpawnEggItem;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import org.lwjgl.glfw.GLFW;

import java.util.List;

import static com.github.sculkhorde.util.ColorUtil.hexToInt;

public class ModItems {
    //https://www.mr-pineapple.co.uk/tutorials/items
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, SculkHorde.MOD_ID);

	public static final DeferredHolder<Item, SculkSweeperSword> SCULK_SWEEPER_SWORD = ITEMS.register("sculk_sweeper_sword", () -> new SculkSweeperSword());
	public static final DeferredHolder<Item, Item> SCULK_ENDERMAN_CLEAVER = ITEMS.register("sculk_enderman_cleaver", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

    public static final DeferredHolder<Item, Item> SCULK_MATTER = ITEMS.register("sculk_matter", () -> new Item(new Item.Properties()));

	public static final DeferredHolder<Item, Item> CRYING_SOULS = ITEMS.register("crying_souls", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> PURE_SOULS = ITEMS.register("pure_souls", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> ESSENCE_OF_PURITY = ITEMS.register("essence_of_purity", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, PurificationFlaskItem> PURIFICATION_FLASK = ITEMS.register("purification_flask",
			PurificationFlaskItem::new);

    public static final DeferredHolder<Item, DevWand> DEV_WAND = ITEMS.register("dev_wand",
			() -> new DevWand());

    public static final DeferredHolder<Item, DevConversionWand> DEV_CONVERSION_WAND = ITEMS.register("dev_conversion_wand",
			() -> new DevConversionWand());

    public static final DeferredHolder<Item, InfestationPurifierItem> INFESTATION_PURIFIER = ITEMS.register("infestation_purifier",
			() -> new InfestationPurifierItem());

    public static final DeferredHolder<Item, CustomItemProjectileItem> CUSTOM_ITEM_PROJECTILE = ITEMS.register("custom_item_projectile",
			() -> new CustomItemProjectileItem());

	public static final DeferredHolder<Item, SculkAcidicProjectileItem> SCULK_ACIDIC_PROJECTILE = ITEMS.register("sculk_acidic_projectile",
			SculkAcidicProjectileItem::new);
    public static final DeferredHolder<Item, SculkResinItem> SCULK_RESIN = ITEMS.register("sculk_resin",
			() -> new SculkResinItem());

	public static final DeferredHolder<Item, Item> CALCITE_CLUMP = ITEMS.register("calcite_clump",
			() -> new Item(new Item.Properties()){
				@Override
				@OnlyIn(Dist.CLIENT)
				public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, DevNodeSpawner> DEV_NODE_SPAWNER = ITEMS.register("dev_node_spawner",
			() -> new DevNodeSpawner());

	public static final DeferredHolder<Item, DevRaidWand> DEV_RAID_WAND = ITEMS.register("dev_raid_wand",
			() -> new DevRaidWand());

	public static final DeferredHolder<Item, WardenBeefItem> WARDEN_BEEF = ITEMS.register("warden_beef",
			WardenBeefItem::new);

	public static final DeferredHolder<Item, Item> CHUNK_O_BRAIN = ITEMS.register("chunk_o_brain", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> DORMANT_HEART_OF_THE_HORDE = ITEMS.register("dormant_heart_of_the_horde", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> HEART_OF_THE_HORDE = ITEMS.register("heart_of_the_horde", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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


	public static final DeferredHolder<Item, Item> HEART_OF_PURITY = ITEMS.register("heart_of_purity", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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
	public static final DeferredHolder<Item, EyeOfPurityItem> EYE_OF_PURITY = ITEMS.register("eye_of_purity", () -> new EyeOfPurityItem()
	{
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> COIN_OF_CONTRIBUTION = ITEMS.register("coin_of_contribution", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
			tooltip.add(Component.translatable("tooltip.sculkhorde.coin_of_contribution"));
		}
	});

	public static final DeferredHolder<Item, TomeOfSpinesItem> TOME_OF_SPINES = ITEMS.register("tome_of_spines",
			() -> new TomeOfSpinesItem());

	public static final DeferredHolder<Item, TomeOfReinforcementItem> TOME_OF_REINFORCEMENT = ITEMS.register("tome_of_reinforcement",
			() -> new TomeOfReinforcementItem());

	public static final DeferredHolder<Item, TomeOfVeilItem> TOME_OF_VEIL = ITEMS.register("tome_of_veil",
			() -> new TomeOfVeilItem());

	public static final DeferredHolder<Item, TomeOfSporeItem> TOME_OF_SPORE = ITEMS.register("tome_of_spore",
			() -> new TomeOfSporeItem());

	public static final DeferredHolder<Item, TomeOfSacrificeItem> TOME_OF_SACRIFICE = ITEMS.register("tome_of_sacrifice",
			() -> new TomeOfSacrificeItem());

	public static final DeferredHolder<Item, Item> SOULITE_SHARD = ITEMS.register("soulite_shard", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> FERRISCITE = ITEMS.register("ferriscite", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> DIASCITE = ITEMS.register("diascite", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, Item> ANGEL_OF_REAPING_SOUL = ITEMS.register("angel_of_reaping_soul", () -> new Item(new Item.Properties()){
		@Override
		@OnlyIn(Dist.CLIENT)
		public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
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

	public static final DeferredHolder<Item, FerriscitePickaxeItem> FERRISCITE_PICKAXE = ITEMS.register("ferriscite_pickaxe",
			() -> new FerriscitePickaxeItem());
	public static final DeferredHolder<Item, FerrisciteShovelItem> FERRISCITE_SHOVEL = ITEMS.register("ferriscite_shovel",
			() -> new FerrisciteShovelItem());

	public static final DeferredHolder<Item, FerrisciteAxeItem> FERRISCITE_AXE = ITEMS.register("ferriscite_axe",
			() -> new FerrisciteAxeItem());

	public static final DeferredHolder<Item, FerrisciteHoeItem> FERRISCITE_HOE = ITEMS.register("ferriscite_hoe",
			() -> new FerrisciteHoeItem());

	public static final DeferredHolder<Item, DiascitePickaxeItem> DIASCITE_PICKAXE = ITEMS.register("diascite_pickaxe",
			() -> new DiascitePickaxeItem());
	public static final DeferredHolder<Item, DiasciteShovelItem> DIASCITE_SHOVEL = ITEMS.register("diascite_shovel",
			() -> new DiasciteShovelItem());

	public static final DeferredHolder<Item, DiasciteAxeItem> DIASCITE_AXE = ITEMS.register("diascite_axe",
			() -> new DiasciteAxeItem());

	public static final DeferredHolder<Item, DiasciteHoeItem> DIASCITE_HOE = ITEMS.register("diascite_hoe",
			() -> new DiasciteHoeItem());

	public static final DeferredHolder<Item, BladeOfPurityItem> BLADE_OF_PURITY = ITEMS.register("blade_of_purity",
			() -> new BladeOfPurityItem());

	public static final DeferredHolder<Item, BreadOfPurityItem> BREAD_OF_PURITY = ITEMS.register("bread_of_purity", () -> new BreadOfPurityItem());
	public static final DeferredHolder<Item, BeefOfPurityItem> BEEF_OF_PURITY = ITEMS.register("beef_of_purity", () -> new BeefOfPurityItem());
	public static final DeferredHolder<Item, PorkOfPurityItem> PORK_OF_PURITY = ITEMS.register("pork_of_purity", () -> new PorkOfPurityItem());
	public static final DeferredHolder<Item, ChickenOfPurityItem> CHICKEN_OF_PURITY = ITEMS.register("chicken_of_purity", () -> new ChickenOfPurityItem());
	public static final DeferredHolder<Item, BakedPotatoOfPurityItem> BAKED_POTATO_OF_PURITY = ITEMS.register("baked_potato_of_purity", () -> new BakedPotatoOfPurityItem());

	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_SPORE_SPEWER_SPAWN_EGG = ITEMS.register("sculk_spore_spewer_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_SPORE_SPEWER, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkBaseColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_MITE_SPAWN_EGG = ITEMS.register("sculk_mite_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_MITE, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_MITE_AGGRESSOR_SPAWN_EGG = ITEMS.register("sculk_mite_aggressor_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_MITE_AGGRESSOR, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkBoneColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_ZOMBIE_SPAWN_EGG = ITEMS.register("sculk_zombie_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_ZOMBIE, 0x44975c, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_SPITTER_SPAWN_EGG = ITEMS.register("sculk_spitter_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_SPITTER, 0xD1D6B6, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_CREEPER_SPAWN_EGG = ITEMS.register("sculk_creeper_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_CREEPER, 0x0DA70B, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_HATCHER_SPAWN_EGG = ITEMS.register("sculk_hatcher_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_HATCHER, 0x443626, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_VINDICATOR_SPAWN_EGG = ITEMS.register("sculk_vindicator_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_VINDICATOR, 0x959B9B, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_RAVAGER_SPAWN_EGG = ITEMS.register("sculk_ravager_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_RAVAGER, 0x5B5049, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_ENDERMAN_SPAWN_EGG = ITEMS.register("sculk_enderman_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_ENDERMAN, 0x111B21, 0xE079FA, new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_PHANTOM_SPAWN_EGG = ITEMS.register("sculk_phantom_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_PHANTOM, 0x88FF00, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_SALMON_SPAWN_EGG = ITEMS.register("sculk_salmon_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_SALMON, 0xA93432, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_SQUID_SPAWN_EGG = ITEMS.register("sculk_squid_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_SQUID, 0x1D3241, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_PUFFERFISH_SPAWN_EGG = ITEMS.register("sculk_pufferfish_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_PUFFERFISH, 0xE7A701, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_WITCH_SPAWN_EGG = ITEMS.register("sculk_witch_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_WITCH, 0x310000, hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_GUARDIAN_SPAWN_EGG = ITEMS.register("sculk_guardian_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_GUARDIAN, 0x547A6B, hexToInt(ColorUtil.sculkAcidColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_BROOD_HATCHER_SPAWN_EGG = ITEMS.register("sculk_brood_hatcher_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_BROOD_HATCHER, hexToInt(ColorUtil.sculkBoneColor5), hexToInt(ColorUtil.sculkLightColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_BROOD_SPITTER_SPAWN_EGG = ITEMS.register("sculk_brood_spitter_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_BROOD_SPITTER, hexToInt(ColorUtil.sculkBoneColor6), hexToInt(ColorUtil.sculkLightColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_SHEEP_SPAWN_EGG = ITEMS.register("sculk_sheep_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_SHEEP, 0xFFFFFF, hexToInt(ColorUtil.sculkBoneColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_GHAST_SPAWN_EGG = ITEMS.register("sculk_ghast_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_GHAST, 0xFFFFFF, hexToInt(ColorUtil.sculkAcidColor1), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_LEECH_SPAWN_EGG = ITEMS.register("sculk_leech_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_LEECH, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, DeferredSpawnEggItem> SCULK_STINGER_SPAWN_EGG = ITEMS.register("sculk_stinger_spawn_egg",() ->  new DeferredSpawnEggItem(ModEntities.SCULK_STINGER, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkLightColor6), new Item.Properties()));
	public static final DeferredHolder<Item, AngelOfReapingSpawnEggItem> ANGEL_OF_REAPING_SPAWN_EGG = ITEMS.register("angel_of_reaping_spawn_egg",() ->  new AngelOfReapingSpawnEggItem(ModEntities.ANGEL_OF_REAPING, hexToInt(ColorUtil.sculkBaseColor6), hexToInt(ColorUtil.sculkAcidColor1), new Item.Properties()));

	private static Item.Properties musicDiscProperties(String songId) {
		return new Item.Properties().stacksTo(1).jukeboxPlayable(ResourceKey.create(
				Registries.JUKEBOX_SONG,
				ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, songId)));
	}

	public static final DeferredHolder<Item, Item> DEEP_GREEN_MUSIC_DISC = ITEMS.register("deep_green_music_disc", () -> new Item(musicDiscProperties("deep_green")));
	public static final DeferredHolder<Item, Item> BLIND_AND_ALONE_MUSIC_DISC = ITEMS.register("blind_and_alone_music_disc", () -> new Item(musicDiscProperties("blind_and_alone")));
}
