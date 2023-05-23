package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CursorInfectorRenderer extends EntityRenderer<CursorInfectorEntity> {
    public CursorInfectorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(CursorInfectorEntity pEntity) {
        return null;
    }
}
