package com.github.sculkhoard.client.renderer.entity;

import com.github.sculkhoard.client.model.enitity.SculkBeeHarvesterModel;
import com.github.sculkhoard.client.model.enitity.SculkBeeInfectorModel;
import com.github.sculkhoard.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhoard.common.entity.SculkBeeInfectorEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class SculkBeeHarvesterRenderer extends GeoEntityRenderer<SculkBeeHarvesterEntity> {

    public SculkBeeHarvesterRenderer(EntityRendererManager renderManager)
    {
        super(renderManager, new SculkBeeHarvesterModel());
        this.shadowStrength = 0.7F; //change 0.7 to the desired shadow size.
    }

    @Override
    public RenderType getRenderType(SculkBeeHarvesterEntity animatable, float partialTicks, MatrixStack stack, @Nullable IRenderTypeBuffer renderTypeBuffer, @Nullable IVertexBuilder vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
