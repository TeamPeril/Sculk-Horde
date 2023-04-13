package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CursorSurfacePurifierRenderer extends EntityRenderer<CursorSurfacePurifierEntity> {
    public CursorSurfacePurifierRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(CursorSurfacePurifierEntity pEntity) {
        return null;
    }
}
