package com.github.sculkhorde.client.model.enitity;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulIceProjectileAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;

public class SoulIceProjectileModel extends DefaultedEntityGeoModel<SoulIceProjectileAttackEntity> { {

}
	public SoulIceProjectileModel() {
		super(new ResourceLocation(SculkHorde.MOD_ID, "soul_ice_projectile"));
	}

	// We want our model to render using the translucent render type
	@Override
	public RenderType getRenderType(SoulIceProjectileAttackEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}