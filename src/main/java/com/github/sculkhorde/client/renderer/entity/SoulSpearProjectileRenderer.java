package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulSpearProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulSpearProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulSpearProjectileRenderer extends GeoEntityRenderer<SoulSpearProjectileEntity> {
    public SoulSpearProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulSpearProjectileModel());
    }

    /*
    public void preRender(PoseStack poseStack, SoulSpearProjectileEntity e, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //poseStack.translate(0, e.getBoundingBox().getYsize() * .5f, 0);

        //Vec3 motion = e.getDeltaMovement();
        //float xRot = -((float) (Mth.atan2(motion.horizontalDistance(), motion.y) * (double) (180F / (float) Math.PI)) - 90.0F);
        //float yRot = -((float) (Mth.atan2(motion.z, motion.x) * (double) (180F / (float) Math.PI)) + 90.0F);
        //poseStack.mulPose(Axis.YP.rotationDegrees(yRot));
        //poseStack.mulPose(Axis.XP.rotationDegrees(xRot));
        //RenderUtils.faceRotation(poseStack, e, partialTick);
        super.preRender(poseStack, e, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }

     */

    public void preRender(PoseStack poseStack, SoulSpearProjectileEntity e, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        //RenderUtils.faceRotation(poseStack, e, partialTick);
    }

    @Override
    public void render(SoulSpearProjectileEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }
}