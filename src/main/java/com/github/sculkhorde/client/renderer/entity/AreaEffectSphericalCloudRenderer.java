package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.AreaEffectSphericalCloudEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class AreaEffectSphericalCloudRenderer extends EntityRenderer<AreaEffectSphericalCloudEntity> {
    public AreaEffectSphericalCloudRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(AreaEffectSphericalCloudEntity pEntity) {
        return null;
    }
}
