package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorProberEntity;
import com.github.sculkhorde.common.entity.infection.CursorPurifierProberEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CursorPurifierProberRenderer extends EntityRenderer<CursorPurifierProberEntity> {
    public CursorPurifierProberRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager);
    }

    /**
     * Returns the location of an entity's texture.
     *
     * @param pEntity
     */
    @Override
    public ResourceLocation getTextureLocation(CursorPurifierProberEntity pEntity) {
        return null;
    }
}
