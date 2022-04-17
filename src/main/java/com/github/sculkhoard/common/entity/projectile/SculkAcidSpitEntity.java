package com.github.sculkhoard.common.entity.projectile;

import com.github.sculkhoard.common.entity.SculkLivingEntity;
import com.github.sculkhoard.core.EntityRegistry;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.projectile.AbstractArrowEntity;
import net.minecraft.entity.projectile.ProjectileEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.IPacket;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;

public class SculkAcidSpitEntity extends AbstractArrowEntity {

    public SculkAcidSpitEntity(EntityType<? extends AbstractArrowEntity> entityTypeIn, World worldIn) {
        super(entityTypeIn, worldIn);
    }

    public SculkAcidSpitEntity(World pLevel, SculkLivingEntity pSpitter) {
        this(EntityRegistry.SCULK_ACID_SPIT.get(), pLevel);
        super.setOwner(pSpitter);
        this.setPos(pSpitter.getX() - (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double) MathHelper.sin(pSpitter.yBodyRot * ((float)Math.PI / 180F)), pSpitter.getEyeY() - (double)0.1F, pSpitter.getZ() + (double)(pSpitter.getBbWidth() + 1.0F) * 0.5D * (double)MathHelper.cos(pSpitter.yBodyRot * ((float)Math.PI / 180F)));
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

}
