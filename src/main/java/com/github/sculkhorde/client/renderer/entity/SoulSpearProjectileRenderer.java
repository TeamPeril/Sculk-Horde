package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulSpearProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearProjectileAttackEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;

public class SoulSpearProjectileRenderer extends GeoEntityRenderer<SoulSpearProjectileAttackEntity> {
    public SoulSpearProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulSpearProjectileModel());
    }

    @Override
    public void render(SoulSpearProjectileAttackEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}