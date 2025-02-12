package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulPoisonProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulPoisonProjectileAttackEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class SoulPoisonProjectileRenderer extends GeoEntityRenderer<SoulPoisonProjectileAttackEntity> {
    public SoulPoisonProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulPoisonProjectileModel());
    }

    @Override
    public void render(SoulPoisonProjectileAttackEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}