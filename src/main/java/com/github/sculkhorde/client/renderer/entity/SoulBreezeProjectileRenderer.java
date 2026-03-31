package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SoulBreezeProjectileModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.SoulBreezeProjectileAttackEntity;
import com.github.sculkhorde.core.ModConfig;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.constant.DataTickets;
import software.bernie.geckolib.core.animatable.GeoAnimatable;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

import java.util.Objects;

public class SoulBreezeProjectileRenderer extends GeoEntityRenderer<SoulBreezeProjectileAttackEntity> {
    public SoulBreezeProjectileRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new SoulBreezeProjectileModel());
        if(!ModConfig.SERVER.enable_gpu_compatibility_mode.get()) {this.addRenderLayer(new AutoGlowingGeoLayer(this));}
    }

    @Override
    public void actuallyRender(PoseStack poseStack, SoulBreezeProjectileAttackEntity animatable, BakedGeoModel model, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        poseStack.pushPose();

        if (!isReRender) {
            float motionThreshold = this.getMotionAnimThreshold(animatable);
            Vec3 velocity = animatable.getDeltaMovement();
            float avgVelocity = (float)((Math.abs(velocity.x) + Math.abs(velocity.z)) / 2.0);
            AnimationState<SoulBreezeProjectileAttackEntity> animationState = new AnimationState(animatable, 0, 0, partialTick, avgVelocity >= motionThreshold);
            long instanceId = this.getInstanceId(animatable);
            GeoModel<SoulBreezeProjectileAttackEntity> currentModel = this.getGeoModel();
            animationState.setData(DataTickets.TICK, ((GeoAnimatable)animatable).getTick(animatable));
            animationState.setData(DataTickets.ENTITY, animatable);
            Objects.requireNonNull(animationState);
            currentModel.addAdditionalStateData(animatable, instanceId, animationState::setData);
            currentModel.handleAnimations(animatable, instanceId, animationState);
        }

        // Apply entity rotation
        poseStack.mulPose(Axis.YN.rotationDegrees(Mth.lerp(partialTick, animatable.yRotO, animatable.getYRot())));
        poseStack.mulPose(Axis.XP.rotationDegrees(Mth.lerp(partialTick, animatable.xRotO, animatable.getXRot())));

        poseStack.translate(0.0F, 0.01F, 0.0F);
        this.modelRenderTranslations = new Matrix4f(poseStack.last().pose());
        if (animatable.isInvisibleTo(Minecraft.getInstance().player)) {
            if (Minecraft.getInstance().shouldEntityAppearGlowing(animatable)) {
                buffer = bufferSource.getBuffer(renderType = RenderType.outline(this.getTextureLocation(animatable)));
            } else {
                renderType = null;
            }
        }

        if (renderType != null) {
            super.actuallyRender(poseStack, animatable, model, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        }

        poseStack.popPose();
    }
}
