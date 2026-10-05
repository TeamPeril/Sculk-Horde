package com.github.sculkhorde.common.item;

import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class FerrisciteShovelItem extends ShovelItem implements IHealthRepairable {
    protected static float ATTACK_SPEED = -3.0F;
    protected static int ATTACK_DAMAGE = 2;
    public static String blocksBrokenTagID = "blocks_broken";
    public static String lastBlockBrokenID = "last_block_broken";

    protected static Properties PROPERTIES = new Properties()
            .setNoRepair()
            .rarity(Rarity.EPIC)
            .durability(3000);

    public FerrisciteShovelItem() {
        super(Tiers.IRON, PROPERTIES.attributes(DiggerItem.createAttributes(Tiers.IRON, ATTACK_DAMAGE, ATTACK_SPEED)));
    }

    @Override
    public float getDestroySpeed(ItemStack itemStack, BlockState blockState) {
        if(isCorrectToolForDrops(itemStack, blockState))
        {
            float baseMiningSpeed = 10F;
            float additionalMiningSpeed = Math.abs(Math.min(getBlocksBroken(itemStack) / 10F, 100F));
            return baseMiningSpeed + additionalMiningSpeed;
        }
        else
        {
            return super.getDestroySpeed(itemStack, blockState);
        }

    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack pickaxe, Level level, BlockState blockState, BlockPos pos, LivingEntity entity) {

        if(!level.isClientSide())
        {
            if(!isBlockEqualToLastBlockBroken(pickaxe, blockState.getBlock()))
            {
                updateLastBlockBroken(pickaxe, blockState.getBlock());
                resetBlocksBroken(pickaxe);
                super.mineBlock(pickaxe, level, blockState, pos, entity);
            }

            incrementBlocksBroken(pickaxe);
        }

        return super.mineBlock(pickaxe, level, blockState, pos, entity);
    }

    private static CompoundTag getCustomData(ItemStack stack) {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        return data == null ? new CompoundTag() : data.copyTag();
    }

    private static void setCustomData(ItemStack stack, CompoundTag data) {
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(data));
    }

    public static int getBlocksBroken(ItemStack stack)
    {
        CompoundTag nbt = getCustomData(stack);
        return nbt.contains(blocksBrokenTagID) ? nbt.getInt(blocksBrokenTagID) : 0;
    }

    public static void incrementBlocksBroken(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(blocksBrokenTagID)) return;
        CompoundTag nbt = data.copyTag();
        nbt.putInt(blocksBrokenTagID, Math.min(nbt.getInt(blocksBrokenTagID) + 1, 1000));
        setCustomData(stack, nbt);
    }

    public static void resetBlocksBroken(ItemStack stack)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(blocksBrokenTagID)) return;
        CompoundTag nbt = data.copyTag();
        nbt.putInt(blocksBrokenTagID, Math.max(0, nbt.getInt(blocksBrokenTagID) - 20));
        setCustomData(stack, nbt);
    }

    public static boolean isBlockEqualToLastBlockBroken(ItemStack stack, Block block)
    {
        CompoundTag nbt = getCustomData(stack);
        return nbt.contains(lastBlockBrokenID) && nbt.getString(lastBlockBrokenID).equals(block.toString());
    }

    public static void updateLastBlockBroken(ItemStack stack, Block block)
    {
        CustomData data = stack.get(DataComponents.CUSTOM_DATA);
        if (data == null || !data.contains(lastBlockBrokenID)) return;
        CompoundTag nbt = data.copyTag();
        nbt.putString(lastBlockBrokenID, block.toString());
        setCustomData(stack, nbt);
    }



    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slot, boolean selected) {
        if(!level.isClientSide())
        {
            CompoundTag nbt = getCustomData(itemStack);
            if(!nbt.contains(blocksBrokenTagID))
            {
                nbt.putInt(blocksBrokenTagID, 0);
            }

            if(!nbt.contains(lastBlockBrokenID))
            {
                nbt.putString(lastBlockBrokenID, "");
            }
            setCustomData(itemStack, nbt);

        }

        super.inventoryTick(itemStack, level, entity, slot, selected);
    }

    @Override
    public void repair(ItemStack stack, int amount) {
        stack.setDamageValue(Math.max(stack.getDamageValue() - amount, 0));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.ferriscite_shovel.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.ferriscite_shovel.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }
}
