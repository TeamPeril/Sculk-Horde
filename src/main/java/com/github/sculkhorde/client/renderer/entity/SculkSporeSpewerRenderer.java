package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSporeSpewerModel;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.util.ResourceLocation;
import software.bernie.geckolib3.renderers.geo.GeoEntityRenderer;

import javax.annotation.Nullable;

public class SculkSporeSpewerRenderer extends GeoEntityRenderer<SculkSporeSpewerEntity> {

    public SculkSporeSpewerRenderer(EntityRendererManager renderManager)
    {
        super(renderManager, new SculkSporeSpewerModel());
        this.shadowStrength = 0.7F; //change 0.7 to the desired shadow size.
    }

    @Override
    public RenderType getRenderType(SculkSporeSpewerEntity animatable, float partialTicks, MatrixStack stack, @Nullable IRenderTypeBuffer renderTypeBuffer, @Nullable IVertexBuilder vertexBuilder, int packedLightIn, ResourceLocation textureLocation) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }
}
