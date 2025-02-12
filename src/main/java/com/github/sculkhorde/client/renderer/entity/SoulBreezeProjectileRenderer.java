package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulBreezeProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulBreezeProjectileAttackEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class SoulBreezeProjectileRenderer extends GeoEntityRenderer<SoulBreezeProjectileAttackEntity> {
    public SoulBreezeProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulBreezeProjectileModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }

    @Override
    public void render(SoulBreezeProjectileAttackEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}
