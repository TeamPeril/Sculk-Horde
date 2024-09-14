package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.LivingArmorModel;
import com.github.sculkhorde.common.entity.LivingArmorEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;

import javax.annotation.Nullable;


public class LivingArmorRenderer extends GeoEntityRenderer<LivingArmorEntity> {


    public LivingArmorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingArmorModel());

        this.addRenderLayer(new BlockAndItemGeoLayer<>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, LivingArmorEntity animatable) {
                return bone.getName().equalsIgnoreCase("rightItem") ? new ItemStack(Items.AIR) : null;
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, LivingArmorEntity animatable) {
                return ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
            }

            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, LivingArmorEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay) {
                poseStack.mulPose(Axis.XP.rotationDegrees(-110));
                poseStack.mulPose(Axis.YP.rotationDegrees(0));
                poseStack.mulPose(Axis.ZP.rotationDegrees(0));
                poseStack.translate(0.0D, 0.0D, -0.4D);
                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight,
                        packedOverlay);
            }
        });
    }

}
