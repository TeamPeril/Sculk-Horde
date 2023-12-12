package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkRavagerEntity;
import com.github.sculkhorde.core.SculkHorde;

import mod.azure.azurelib.model.DefaultedEntityGeoModel;
import net.minecraft.resources.ResourceLocation;

public class SculkRavagerModel extends DefaultedEntityGeoModel<SculkRavagerEntity> {

    public SculkRavagerModel()
    {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_ravager"));
    }

    /*
    @Override
    public void setLivingAnimations(SculkRavagerEntity entity, Integer uniqueID, AnimationEvent customPredicate)
    {
        super.setLivingAnimations(entity, uniqueID, customPredicate);

        // Rotate the legs based on the movement of the entity
        float walkSpeed = entity.animationSpeed * 0.5F;
        float swingAngle = Mth.cos((float) (customPredicate.getAnimationTick() * 0.5F)) * walkSpeed * 0.5F;

        getModel(getModelLocation(entity)).getBone("leg0").ifPresent(leg0 -> {
            leg0.setRotationX(swingAngle);
        });
        getModel(getModelLocation(entity)).getBone("leg1").ifPresent(leg1 -> {
            leg1.setRotationX(-swingAngle);
        });
        getModel(getModelLocation(entity)).getBone("leg2").ifPresent(leg2 -> {
            leg2.setRotationX(-swingAngle);
        });
        getModel(getModelLocation(entity)).getBone("leg3").ifPresent(leg3 -> {
            leg3.setRotationX(swingAngle);
        });

    }

     */


}
