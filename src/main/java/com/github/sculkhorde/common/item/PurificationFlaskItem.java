package com.github.sculkhorde.common.item;

import com.github.sculkhorde.common.entity.projectile.CustomItemProjectileEntity;
import com.github.sculkhorde.common.entity.projectile.PurificationFlaskProjectileEntity;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class PurificationFlaskItem extends CustomItemProjectile{

    public PurificationFlaskItem() {
        super();
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
    public void appendHoverText(ItemStack stack, Level worldIn, List<Component> tooltip, TooltipFlag flagIn) {

        tooltip.add(Component.translatable("tooltip.sculkhorde.purification_flask_item")); //Text that displays if not holding shift

    }

}
