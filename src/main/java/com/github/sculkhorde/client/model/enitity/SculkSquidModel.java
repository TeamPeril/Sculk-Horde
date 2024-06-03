package com.github.sculkhorde.client.model.enitity;

import com.github.sculkhorde.common.entity.SculkCreeperEntity;
import com.github.sculkhorde.common.entity.SculkSquidEntity;
import com.github.sculkhorde.core.SculkHorde;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.DefaultedEntityGeoModel;
import software.bernie.geckolib.model.data.EntityModelData;

public class SculkSquidModel extends DefaultedEntityGeoModel<SculkSquidEntity> {


    public SculkSquidModel() {
        super(new ResourceLocation(SculkHorde.MOD_ID, "sculk_squid"));
    }

    // We want our model to render using the translucent render type
    @Override
    public RenderType getRenderType(SculkSquidEntity animatable, ResourceLocation texture) {
        return RenderType.entityTranslucent(getTextureResource(animatable));
    }

    @Override
    public void setCustomAnimations(SculkSquidEntity entity, long instanceId, AnimationState<SculkSquidEntity> animationState) {

        CoreGeoBone wholeBody = getAnimationProcessor().getBone("body");

        EntityModelData entityData = animationState.getData(DataTickets.ENTITY_MODEL_DATA);

        if(wholeBody != null) {

            //Swimming

            if (entity.isInWaterOrBubble()) {
                wholeBody.setRotX(80);
            }
            else
            {
                wholeBody.setRotX(0);
            }

            /*
            float currentRotX = wholeBody.getRotX();
            float currentRotY = wholeBody.getRotY();

            // Determine the target rotation angles based on movement direction
            float targetRotX = entity.isInWaterOrBubble() ? 80 : 0; // For swimming, as an example
            float targetRotY = -entityData.netHeadYaw(); // Assuming netHeadYaw is the horizontal rotation angle

            // Interpolate the rotation for smoothness
            float smoothFactor = 0.01F; // Adjust this value for smoother or quicker rotations
            wholeBody.setRotX(currentRotX + (targetRotX - currentRotX) * smoothFactor);
            wholeBody.setRotY(currentRotY + (targetRotY - currentRotY) * smoothFactor);



            // Get the current rotation angles
            float currentRotX = wholeBody.getRotX();
            float currentRotY = wholeBody.getRotY();

            // Determine the target rotation angles based on movement direction
            float targetRotX = entity.isInWaterOrBubble() ? 80 : 0; // For swimming, as an example
            float targetRotY = -entityData.netHeadYaw(); // Assuming netHeadYaw is the horizontal rotation angle

            // Calculate the time since the last update
            float deltaTime = Minecraft.getInstance().getDeltaFrameTime();

            // Apply an easing function for smoothness
            float easeFactor = easeInOutCubic(deltaTime);

            // Interpolate the rotation using the easing factor
            wholeBody.setRotX(currentRotX + (targetRotX - currentRotX) * easeFactor);
            wholeBody.setRotY(currentRotY + (targetRotY - currentRotY) * easeFactor);


            float currentRotX = wholeBody.getRotX();
            float currentRotY = wholeBody.getRotY();


            // Determine the target rotation angles based on movement direction
            float targetRotX = entity.isInWaterOrBubble() ? 80 : -90; // Adjust pitch for swimming or not
            Vec3 vec3 = entity.getDeltaMovement();
            double horizontalDistance = vec3.horizontalDistance();
            float targetRotY = -((float) Mth.atan2(vec3.x, vec3.z)) * (180F / (float)Math.PI);
            float targetRotZ = (float)Math.PI * entity.rotateSpeed * 1.5F;

            // Interpolate the rotation for smoothness
            float smoothFactor = 0.1F; // Adjust this value for smoother or quicker rotations
            wholeBody.setRotX(currentRotX + (targetRotX - currentRotX) * smoothFactor);
            wholeBody.setRotY(currentRotY + (targetRotY - currentRotY) * smoothFactor);
            // Calculate the new Z rotation, ensuring it doesn't exceed 360 degrees
            float newRotZ = (wholeBody.getRotZ() + targetRotZ * smoothFactor) % 360;
            wholeBody.setRotZ(newRotZ);
            */

        }
    }

    private float easeInOutCubic(float t) {
        return t < 0.5 ? 4 * t * t * t : 1 - (float)Math.pow(-2 * t + 2, 3) / 2;
    }
}
