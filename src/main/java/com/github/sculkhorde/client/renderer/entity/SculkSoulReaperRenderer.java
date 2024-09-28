package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.SculkSoulReaperModel;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix3f;
import org.joml.Matrix4f;
import software.bernie.geckolib.GeckoLib;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoRenderer;
import software.bernie.geckolib.renderer.layer.GeoRenderLayer;

public class SculkSoulReaperRenderer extends GeoEntityRenderer<SculkSoulReaperEntity> {

    public SculkSoulReaperRenderer(EntityRendererProvider.Context renderManager)
    {
        super(renderManager, new SculkSoulReaperModel());
        addRenderLayer(new BeamRenderLayer(this));
    }


    protected class BeamRenderLayer extends GeoRenderLayer<SculkSoulReaperEntity> {

        private static final ResourceLocation TEXTURE = new ResourceLocation(GeckoLib.MOD_ID, "textures/entity/cool_kid_glasses.png");
        private static final ResourceLocation GUARDIAN_BEAM_LOCATION = new ResourceLocation("textures/entity/guardian_beam.png");
        private static final RenderType BEAM_RENDER_TYPE = RenderType.entityCutoutNoCull(GUARDIAN_BEAM_LOCATION);

        public BeamRenderLayer(GeoRenderer<SculkSoulReaperEntity> entityRendererIn) {
            super(entityRendererIn);
        }

        @Override
        public void render(PoseStack poseStack, SculkSoulReaperEntity animatable, BakedGeoModel bakedModel, RenderType renderType,
                           MultiBufferSource bufferSource, VertexConsumer buffer, float partialTick,
                           int packedLight, int packedOverlay) {

            LivingEntity targetEntity = animatable.getTarget();
            if (targetEntity != null) {
                float attackAnimationScale = 1;
                float clientSideAttackTime = 1;
                float beamAnimationOffset = clientSideAttackTime * 0.5F % 1.0F;
                float guardianEyeHeight = animatable.getEyeHeight();
                poseStack.pushPose();
                poseStack.translate(0.0F, guardianEyeHeight, 0.0F);
                Vec3 targetPosition = getPosition(targetEntity, targetEntity.getBbHeight() * 0.5D, 1);
                Vec3 guardianPosition = getPosition(animatable, guardianEyeHeight, 1);
                Vec3 beamVector = targetPosition.subtract(guardianPosition);
                float beamLength = (float)(beamVector.length() + 1.0D);
                beamVector = beamVector.normalize();
                float beamPitch = (float)Math.acos(beamVector.y);
                float beamYaw = (float)Math.atan2(beamVector.z, beamVector.x);
                poseStack.mulPose(Axis.YP.rotationDegrees((((float)Math.PI / 2F) - beamYaw) * (180F / (float)Math.PI)));
                poseStack.mulPose(Axis.XP.rotationDegrees(beamPitch * (180F / (float)Math.PI)));
                int beamSegments = 1;
                float beamAnimationSpeed = clientSideAttackTime * 0.05F * -1.5F;
                float attackScaleSquared = attackAnimationScale * attackAnimationScale;
                int redColor = 64 + (int)(attackScaleSquared * 191.0F);
                int greenColor = 32 + (int)(attackScaleSquared * 191.0F);
                int blueColor = 128 - (int)(attackScaleSquared * 64.0F);
                float beamRadius = 0.2F;
                float beamOuterRadius = 0.282F;
                float[] beamOffsets = {
                        Mth.cos(beamAnimationSpeed + 2.3561945F) * beamOuterRadius,
                        Mth.sin(beamAnimationSpeed + 2.3561945F) * beamOuterRadius,
                        Mth.cos(beamAnimationSpeed + ((float)Math.PI / 4F)) * beamOuterRadius,
                        Mth.sin(beamAnimationSpeed + ((float)Math.PI / 4F)) * beamOuterRadius,
                        Mth.cos(beamAnimationSpeed + 3.926991F) * beamOuterRadius,
                        Mth.sin(beamAnimationSpeed + 3.926991F) * beamOuterRadius,
                        Mth.cos(beamAnimationSpeed + 5.4977875F) * beamOuterRadius,
                        Mth.sin(beamAnimationSpeed + 5.4977875F) * beamOuterRadius,
                        Mth.cos(beamAnimationSpeed + (float)Math.PI) * beamRadius,
                        Mth.sin(beamAnimationSpeed + (float)Math.PI) * beamRadius,
                        Mth.cos(beamAnimationSpeed + 0.0F) * beamRadius,
                        Mth.sin(beamAnimationSpeed + 0.0F) * beamRadius,
                        Mth.cos(beamAnimationSpeed + ((float)Math.PI / 2F)) * beamRadius,
                        Mth.sin(beamAnimationSpeed + ((float)Math.PI / 2F)) * beamRadius,
                        Mth.cos(beamAnimationSpeed + ((float)Math.PI * 1.5F)) * beamRadius,
                        Mth.sin(beamAnimationSpeed + ((float)Math.PI * 1.5F)) * beamRadius
                };
                float beamStartU = 0.0F;
                float beamEndU = 0.4999F;
                float beamStartV = -1.0F + beamAnimationOffset;
                float beamEndV = beamLength * 2.5F + beamStartV;
                VertexConsumer vertexConsumer = bufferSource.getBuffer(BEAM_RENDER_TYPE);
                PoseStack.Pose pose = poseStack.last();
                Matrix4f poseMatrix = pose.pose();
                Matrix3f normalMatrix = pose.normal();
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[8], beamLength, beamOffsets[9], redColor, greenColor, blueColor, beamEndU, beamEndV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[8], 0.0F, beamOffsets[9], redColor, greenColor, blueColor, beamEndU, beamStartV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[10], 0.0F, beamOffsets[11], redColor, greenColor, blueColor, beamStartU, beamStartV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[10], beamLength, beamOffsets[11], redColor, greenColor, blueColor, beamStartU, beamEndV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[12], beamLength, beamOffsets[13], redColor, greenColor, blueColor, beamEndU, beamEndV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[12], 0.0F, beamOffsets[13], redColor, greenColor, blueColor, beamEndU, beamStartV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[14], 0.0F, beamOffsets[15], redColor, greenColor, blueColor, beamStartU, beamStartV);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[14], beamLength, beamOffsets[15], redColor, greenColor, blueColor, beamStartU, beamEndV);
                float beamPhase = 0.0F;
                if (animatable.tickCount % 2 == 0) {
                    beamPhase = 0.5F;
                }
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[0], beamLength, beamOffsets[1], redColor, greenColor, blueColor, 0.5F, beamPhase + 0.5F);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[2], beamLength, beamOffsets[3], redColor, greenColor, blueColor, 1.0F, beamPhase + 0.5F);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[6], beamLength, beamOffsets[7], redColor, greenColor, blueColor, 1.0F, beamPhase);
                vertex(vertexConsumer, poseMatrix, normalMatrix, beamOffsets[4], beamLength, beamOffsets[5], redColor, greenColor, blueColor, 0.5F, beamPhase);
                poseStack.popPose();
            }

        }

        private static void vertex(VertexConsumer p_253637_, Matrix4f p_253920_, Matrix3f p_253881_, float p_253994_, float p_254492_, float p_254474_, int p_254080_, int p_253655_, int p_254133_, float p_254233_, float p_253939_) {
            p_253637_.vertex(p_253920_, p_253994_, p_254492_, p_254474_).color(p_254080_, p_253655_, p_254133_, 255).uv(p_254233_, p_253939_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(15728880).normal(p_253881_, 0.0F, 1.0F, 0.0F).endVertex();
        }

        private Vec3 getPosition(LivingEntity p_114803_, double p_114804_, float p_114805_) {
            double d0 = Mth.lerp((double)p_114805_, p_114803_.xOld, p_114803_.getX());
            double d1 = Mth.lerp((double)p_114805_, p_114803_.yOld, p_114803_.getY()) + p_114804_;
            double d2 = Mth.lerp((double)p_114805_, p_114803_.zOld, p_114803_.getZ());
            return new Vec3(d0, d1, d2);
        }
    }

}
