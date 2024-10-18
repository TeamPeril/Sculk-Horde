package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulFlySwatterProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulFlySwatterProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.util.RenderUtils;

public class SoulFlySwatterProjectileRenderer extends GeoEntityRenderer<SoulFlySwatterProjectileEntity> {
    public SoulFlySwatterProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulFlySwatterProjectileModel());
    }

    public void preRender(PoseStack poseStack, SoulFlySwatterProjectileEntity e, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        RenderUtils.faceRotation(poseStack, e, partialTick);
        super.preRender(poseStack, e, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}