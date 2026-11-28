package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ZoltraakAttackEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;

public class ZoltraakAttackModel extends DefaultedEntityGeoModel<ZoltraakAttackEntity> {
    public ZoltraakAttackModel() {
        super(ResourceLocation.fromNamespaceAndPath(SculkHorde.MOD_ID, "zoltraak_attack_entity"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(ZoltraakAttackEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }
}
