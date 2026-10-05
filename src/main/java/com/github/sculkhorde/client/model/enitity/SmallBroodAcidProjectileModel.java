package com.github.sculkhorde.client.model.enitity;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulPoisonProjectileAttackEntity;
import com.github.sculkhorde.common.entity.projectile.SmallBroodAcidProjectileEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SmallBroodAcidProjectileModel extends DefaultedEntityGeoModel<SmallBroodAcidProjectileEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public SmallBroodAcidProjectileModel() {
		super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "soul_poison_projectile"));
	}

	// We want our model to render using the translucent render type
	@Override
	public RenderType getRenderType(SmallBroodAcidProjectileEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

}