package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.LivingArmorEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class LivingArmorModel extends DefaultedEntityGeoModel<LivingArmorEntity> {


    public LivingArmorModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "living_armor"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(LivingArmorEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
