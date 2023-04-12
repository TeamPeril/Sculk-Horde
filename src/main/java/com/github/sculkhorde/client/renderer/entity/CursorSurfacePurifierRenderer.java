package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.resources.ResourceLocation;

public class CursorSurfacePurifierRenderer extends EntityRenderer<CursorSurfacePurifierEntity> {
    public CursorSurfacePurifierRenderer(EntityRenderDispatcher p_i46179_1_) {
        super(p_i46179_1_);
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
