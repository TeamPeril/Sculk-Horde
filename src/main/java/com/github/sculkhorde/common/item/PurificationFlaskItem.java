package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.PurificationFlaskProjectileEntity;
import com.mojang.blaze3d.platform.InputConstants;
import net.minecraft.client.Minecraft;
import net.minecraft.core.Direction;
import net.minecraft.core.Position;
import net.minecraft.core.dispenser.DefaultDispenseItemBehavior;
import net.minecraft.core.dispenser.BlockSource;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.DispenserBlock;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class PurificationFlaskItem extends CustomItemProjectileItem {

    public PurificationFlaskItem() {
        super();
        DispenserBlock.registerBehavior(this, new DefaultDispenseItemBehavior() {
            @Override
            protected ItemStack execute(BlockSource blockSource, ItemStack itemStack) {
                Level level = blockSource.level();
                Direction direction = blockSource.state().getValue(DispenserBlock.FACING);
                Position position = blockSource.center().add(direction.getStepX() * 0.7D, direction.getStepY() * 0.7D, direction.getStepZ() * 0.7D);
                PurificationFlaskProjectileEntity flask = new PurificationFlaskProjectileEntity(level);
                flask.setPos(position.x(), position.y(), position.z());
                Projectile projectile = flask;
                projectile.shoot(direction.getStepX(), direction.getStepY(), direction.getStepZ(), 1.1F, 6.0F);
                level.addFreshEntity(projectile);
                itemStack.shrink(1);
                return itemStack;
            }
        });
    }

    @Override
    public float getPower()
    {
        return 0.5f;
    }

    @Override
    public float getInaccuracy()
    {
        return 1.0f;
    }

    @Override
    public float getPitchOffset()
    {
        return -20.0f;
    }

    @Override
    public float getDamage()
    {
        return 1f;
    }

    @Override
    public CustomItemProjectileEntity getCustomItemProjectileEntity(Level level, Player player)
    {
        return new PurificationFlaskProjectileEntity(level, player, getDamage());
    }

    //This changes the text you see when hovering over an item
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flagIn) {

        if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_SHIFT))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.purification_flask.functionality"));
        }
        else if(InputConstants.isKeyDown(Minecraft.getInstance().getWindow().getWindow(), GLFW.GLFW_KEY_LEFT_CONTROL))
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.purification_flask.lore"));
        }
        else
        {
            tooltip.add(Component.translatable("tooltip.sculkhorde.default"));
        }

    }

}
