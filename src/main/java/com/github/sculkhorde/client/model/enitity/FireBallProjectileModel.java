package com.github.sculkhorde.client.model.enitity;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.github.sculkhorde.common.entity.projectile.FireBallProjectileEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class FireBallProjectileModel extends DefaultedEntityGeoModel<FireBallProjectileEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public FireBallProjectileModel() {
		super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "fire_ball_projectile_entity"));
	}

	// We want our model to render using the translucent render type
	@Override
	public RenderType getRenderType(FireBallProjectileEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}