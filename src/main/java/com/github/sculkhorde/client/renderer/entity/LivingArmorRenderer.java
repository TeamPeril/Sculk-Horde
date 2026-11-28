package com.github.sculkhorde.client.renderer.entity;

import com.github.sculkhorde.client.model.enitity.LivingArmorModel;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.LivingArmorEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.layer.BlockAndItemGeoLayer;
import software.bernie.geckolib.renderer.layer.ItemArmorGeoLayer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public class LivingArmorRenderer extends GeoEntityRenderer<LivingArmorEntity> {

    private static final String LEFT_HAND = "leftItem";
    private static final String RIGHT_HAND = "rightItem";
    private static final String LEFT_BOOT = "leftBoot";
    private static final String RIGHT_BOOT = "rightBoot";
    private static final String LEFT_ARMOR_LEG = "leftLeg";
    private static final String RIGHT_ARMOR_LEG = "rightLeg";
    private static final String BELT = "belt";
    private static final String CHESTPLATE = "body";
    private static final String LEFT_ARM = "leftArm";
    private static final String RIGHT_ARM = "rightArm";
    private static final String HELMET = "head";

    protected ItemStack mainHandItem;
    protected ItemStack offhandItem;

    public LivingArmorRenderer(EntityRendererProvider.Context renderManager) {
        super(renderManager, new LivingArmorModel());

        this.addRenderLayer(new ItemArmorGeoLayer<LivingArmorEntity>(this) {

            @Nullable
            @Override
            protected ItemStack getArmorItemForBone(GeoBone bone, LivingArmorEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT -> this.bootsStack;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG, BELT -> this.leggingsStack;
                    case CHESTPLATE, LEFT_ARM, RIGHT_ARM -> this.chestplateStack;
                    case HELMET -> this.helmetStack;
                    default -> null;
                };
            }

            @Nonnull
            @Override
            protected EquipmentSlot getEquipmentSlotForBone(GeoBone bone, ItemStack stack, LivingArmorEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_BOOT, RIGHT_BOOT -> EquipmentSlot.FEET;
                    case LEFT_ARMOR_LEG, RIGHT_ARMOR_LEG, BELT -> EquipmentSlot.LEGS;
                    case CHESTPLATE, LEFT_ARM, RIGHT_ARM -> EquipmentSlot.CHEST;
                    case HELMET -> EquipmentSlot.HEAD;
                    default -> super.getEquipmentSlotForBone(bone, stack, animatable);
                };
            }

            @Nonnull
            @Override
            protected ModelPart getModelPartForBone(GeoBone bone, EquipmentSlot slot, ItemStack stack, LivingArmorEntity animatable, HumanoidModel<?> baseModel) {
                // NEVER return null here — GeckoLib unconditionally dereferences modelPart.cubes
                return switch (bone.getName()) {
                    case LEFT_BOOT, LEFT_ARMOR_LEG -> baseModel.leftLeg;
                    case RIGHT_BOOT, RIGHT_ARMOR_LEG -> baseModel.rightLeg;
                    case CHESTPLATE, BELT -> baseModel.body;
                    case RIGHT_ARM -> baseModel.rightArm;
                    case LEFT_ARM -> baseModel.leftArm;
                    case HELMET -> baseModel.head;
                    default -> super.getModelPartForBone(bone, slot, stack, animatable, baseModel);
                };
            }
        });

        this.addRenderLayer(new BlockAndItemGeoLayer<LivingArmorEntity>(this) {
            @Nullable
            @Override
            protected ItemStack getStackForBone(GeoBone bone, LivingArmorEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_HAND -> animatable.isLeftHanded() ? LivingArmorRenderer.this.mainHandItem : LivingArmorRenderer.this.offhandItem;
                    case RIGHT_HAND -> animatable.isLeftHanded() ? LivingArmorRenderer.this.offhandItem : LivingArmorRenderer.this.mainHandItem;
                    default -> null;
                };
            }

            @Override
            protected ItemDisplayContext getTransformTypeForStack(GeoBone bone, ItemStack stack, LivingArmorEntity animatable) {
                return switch (bone.getName()) {
                    case LEFT_HAND -> ItemDisplayContext.THIRD_PERSON_LEFT_HAND;
                    case RIGHT_HAND -> ItemDisplayContext.THIRD_PERSON_RIGHT_HAND;
                    default -> ItemDisplayContext.NONE;
                };
            }

            /*
            @Override
            protected void renderStackForBone(PoseStack poseStack, GeoBone bone, ItemStack stack, LivingArmorEntity animatable, MultiBufferSource bufferSource, float partialTick, int packedLight, int packedOverlay, int renderColor) {
                if (stack == LivingArmorRenderer.this.mainHandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0.0, 0.125, -0.25);
                    }
                } else if (stack == LivingArmorRenderer.this.offhandItem) {
                    poseStack.mulPose(Axis.XP.rotationDegrees(-90.0F));
                    poseStack.scale(0.5F, 0.5F, 0.5F);
                    if (stack.getItem() instanceof ShieldItem) {
                        poseStack.translate(0.0, 0.125, 0.25);
                        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
                    }
                }

                super.renderStackForBone(poseStack, bone, stack, animatable, bufferSource, partialTick, packedLight, packedOverlay, renderColor);
            }

             */
        });


    }

    /*
    @Override
    public void preRender(PoseStack poseStack, LivingArmorEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, int renderColor) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, renderColor);
        this.mainHandItem = animatable.getMainHandItem();
        this.offhandItem = animatable.getOffhandItem();
    }

     */
}