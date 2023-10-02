package com.github.sculkhorde.client.renderer.entity;


import com.github.sculkhorde.client.model.enitity.ChaosTeleporationRiftModel;
import com.github.sculkhorde.common.entity.boss.sculk_enderman.ChaosTeleporationRiftEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoProjectilesRenderer;

public class ChaosTeleporationRiftRenderer extends GeoProjectilesRenderer<ChaosTeleporationRiftEntity> {

    public ChaosTeleporationRiftRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new ChaosTeleporationRiftModel());
    }

    @Override
    public RenderType getRenderType(ChaosTeleporationRiftEntity animatable, float partialTick, PoseStack poseStack,
                                    MultiBufferSource bufferSource, VertexConsumer buffer, int packedLight,
                                    ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

}
