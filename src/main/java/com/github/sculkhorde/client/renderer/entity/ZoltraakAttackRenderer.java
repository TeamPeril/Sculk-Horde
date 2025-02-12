package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.ZoltraakAttackModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ZoltraakAttackEntity;
import mod.azure.azurelib.renderer.GeoEntityRenderer;
import mod.azure.azurelib.renderer.layer.AutoGlowingGeoLayer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class ZoltraakAttackRenderer extends GeoEntityRenderer<ZoltraakAttackEntity> {

    public ZoltraakAttackRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ZoltraakAttackModel());
        this.addRenderLayer(new AutoGlowingGeoLayer(this));
    }
    /*
    @Override
    public void render(ZoltraakAttackEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(Mth.lerp(partialTick, entity.yRotO, entity.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, entity.xRotO, entity.getXRot())));

        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

     */

}
