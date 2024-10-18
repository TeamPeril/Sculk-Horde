package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulFireProjectileModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SoulFireProjectileEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SoulFireProjectileRenderer extends GeoEntityRenderer<SoulFireProjectileEntity> {
    public SoulFireProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulFireProjectileModel());
    }

    public void preRender(PoseStack poseStack, SoulFireProjectileEntity e, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {

        //RenderUtils.faceRotation(poseStack, e, partialTick);
        super.preRender(poseStack, e, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
