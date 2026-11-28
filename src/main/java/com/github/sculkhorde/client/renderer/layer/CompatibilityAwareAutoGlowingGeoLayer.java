package com.github.sculkhorde.client.renderer.layer;

import com.github.sculkhorde.core.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.animatable.GeoAnimatable;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class CompatibilityAwareAutoGlowingGeoLayer<T extends GeoAnimatable> extends AutoGlowingGeoLayer<T> {
    public CompatibilityAwareAutoGlowingGeoLayer(GeoRenderer<T> renderer) {
        super(renderer);
    }

    @Override
    public void render(PoseStack poseStack, T animatable, BakedGeoModel bakedModel, @Nullable RenderType renderType, MultiBufferSource bufferSource, @Nullable VertexConsumer buffer, float partialTick, int packedLight, int packedOverlay) {
        if (ModConfig.SERVER != null && ModConfig.SERVER.enable_gpu_compatibility_mode != null && ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {
            return;
        }
        super.render(poseStack, animatable, bakedModel, renderType, bufferSource, buffer, partialTick, packedLight, packedOverlay);
    }
}
