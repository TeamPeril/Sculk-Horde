package com.github.sculkhorde.common.item;

import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.ICursor;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.extensions.IForgeItem;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class DiascitePickaxeItem extends PickaxeItem implements IForgeItem, IHealthRepairable {
    protected static float ATTACK_SPEED = -2.8F;
    protected static int ATTACK_DAMAGE = 1;
    protected static Properties PROPERTIES = new Properties()
            .setNoRepair()
            .rarity(Rarity.EPIC)
            .durability(3000)
            .defaultDurability(3000);

    public DiascitePickaxeItem() {
        super(Tiers.DIAMOND, ATTACK_DAMAGE, ATTACK_SPEED, PROPERTIES);
    }

    @Override
    public boolean isRepairable(ItemStack stack) {
        return false;
    }

    @Override
    public boolean mineBlock(ItemStack itemStack, Level level, BlockState blockState, BlockPos pos, LivingEntity entity) {

        if(entity instanceof Player player && isCorrectToolForDrops(itemStack, blockState) && !blockState.is(Blocks.SCULK_VEIN))
        {
            ICursor cursor = CursorSystem.createOreMinerCursor(level, blockState.getBlock(), player, pos, itemStack);
            cursor.setMaxTransformations(64);
            cursor.setMaxLifeTimeTicks(TickUnits.convertMinutesToTicks(5));
            cursor.setSearchIterationsPerTick(20);
            cursor.setMaxRange(64);
            cursor.setTickIntervalTicks(TickUnits.convertSecondsToTicks(0.2F));
        }

        return super.mineBlock(itemStack, level, blockState, pos, entity);
    }

    @Override
    public void repair(ItemStack stack, int amount) {
        stack.setDamageValue(Math.max(stack.getDamageValue() - amount, 0));
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {
        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.diascite_pickaxe.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.diascite_pickaxe.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }
    }

}
