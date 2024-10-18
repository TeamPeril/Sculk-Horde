package com.github.sculkhorde.client.model.enitity;// Made with Blockbench 4.10.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearProjectileEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class SoulSpearProjectileModel extends DefaultedEntityGeoModel<SoulSpearProjectileEntity> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public SoulSpearProjectileModel() {
		super(new ResourceLocation(SculkHorde.MOD_ID, "soul_spear_projectile"));
	}

	// We want our model to render using the translucent render type
	@Override
	public RenderType getRenderType(SoulSpearProjectileEntity animatable, ResourceLocation texture) {
		return RenderType.entityTranslucent(getTextureResource(animatable));
	}

	@Override
	public void setCustomAnimations(SoulSpearProjectileEntity animatable, long instanceId, AnimationState<SoulSpearProjectileEntity> animationState) {
		super.setCustomAnimations(animatable, instanceId, animationState);
		//float yRot = Mth.rotLerp(animationState.getPartialTick(), animatable.yRotO, animatable.getYRot()) - 90;
		//float xRot = Mth.lerp(animationState.getPartialTick(), animatable.xRotO, animatable.getXRot());
		//this.rotateProjectile(yRot, xRot);
	}

	private void rotateProjectile(float yRot, float xRot) {
		CoreGeoBone arm = getAnimationProcessor().getBone("root");
		if(arm != null){
			arm.setRotY(-yRot * Mth.DEG_TO_RAD);
			arm.setRotX(-xRot * Mth.DEG_TO_RAD);
		}
	}



}