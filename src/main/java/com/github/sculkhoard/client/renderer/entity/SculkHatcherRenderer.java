package com.github.sculkhoard.client.renderer.entity;

import com.github.sculkhoard.client.model.enitity.SculkHatcherModel;
import com.github.sculkhoard.client.model.enitity.SculkZombieModel;
import com.github.sculkhoard.common.entity.SculkHatcherEntity;
import com.github.sculkhoard.common.entity.SculkZombieEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class SculkHatcherRenderer extends GeoEntityRenderer<SculkHatcherEntity> {

    public SculkHatcherRenderer(EntityRendererManager renderManager)
    {
        super(renderManager, new SculkHatcherModel());
        this.shadowStrength = 0.7F; //change 0.7 to the desired shadow size.
    }

    @Override
    public RenderType getRenderType(SculkHatcherEntity animatable, float partialTicks, MatrixStack stack, @Nullable IRenderTypeBuffer renderTypeBuffer, @Nullable IVertexBuilder vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
