package com.github.sculkhorde.client.model.enitity;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.github.sculkhorde.common.entity.projectile.AcidBlobProjectileEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class AcidBlobProjectileModel extends DefaultedEntityGeoModel<AcidBlobProjectileEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public AcidBlobProjectileModel() {
		super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "acid_blob_projectile"));
	}

	// We want our model to render using the translucent render type
	@Override
	public RenderType getRenderType(AcidBlobProjectileEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}