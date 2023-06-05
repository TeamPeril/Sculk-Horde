package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.SculkEndermanEntity;
import com.github.sculkhorde.core.EntityRegistry;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.Collections;
import java.util.function.Predicate;

public class EnderArrowSpamAttackGoal extends MeleeAttackGoal
{
    protected int maxAttackDuration = 0;
    protected int elapsedAttackDuration = 0;
    protected final int executionCooldown = TickUnits.convertSecondsToTicks(10);
    protected int ticksElapsed = executionCooldown;

    public EnderArrowSpamAttackGoal(PathfinderMob mob, int durationInTicks) {
        super(mob, 0.0F, true);
        maxAttackDuration = durationInTicks;
    }

    private SculkEndermanEntity getSculkEnderman()
    {
        return (SculkEndermanEntity)this.mob;
    }

    @Override
    public boolean canUse()
    {
        ticksElapsed++;

        if(!getSculkEnderman().isSpecialAttackReady() || mob.getTarget() == null)
        {
            return false;
        }

        if(ticksElapsed < executionCooldown)
        {
            return false;
        }

        if(!mob.closerThan(mob.getTarget(), 10.0F))
        {
            return false;
        }

        return true;
    }

    @Override
    public boolean canContinueToUse()
    {
        return elapsedAttackDuration < maxAttackDuration;
    }

    @Override
    public void start()
    {
        super.start();


        //Disable mob's movement for 10 seconds
        this.mob.getNavigation().stop();
        // Teleport the enderman away from the mob
        getSculkEnderman().teleportAwayFromEntity(mob.getTarget());
        getSculkEnderman().canTeleport = false;


    }

    @Override
    public void tick()
    {
        super.tick();
        elapsedAttackDuration++;
        performRangedAttack(mob.getTarget(), 5.0F);
    }

    @Override
    public void stop()
    {
        super.stop();
        getSculkEnderman().resetSpecialAttackCooldown();
        elapsedAttackDuration = 0;
        ticksElapsed = 0;
        getSculkEnderman().canTeleport = true;
    }

    protected ItemStack getProjectile()
    {
        return new ItemStack(Items.ARROW);
    }

    protected AbstractArrow getArrow(ItemStack itemStack, float power) {
        return getMobArrow(this.mob, itemStack, power);
    }

    public static AbstractArrow getMobArrow(LivingEntity sourceEntity, ItemStack itemStack, float power)
    {
        ArrowItem arrowitem = (ArrowItem)(itemStack.getItem() instanceof ArrowItem ? itemStack.getItem() : Items.ARROW);

        AbstractArrow abstractarrow = arrowitem.createArrow(sourceEntity.level, itemStack, sourceEntity);

        abstractarrow.setPos(sourceEntity.getX(), sourceEntity.getY() + (double)sourceEntity.getBbHeight() + 1, sourceEntity.getZ());

        abstractarrow.setEnchantmentEffectsFromEntity(sourceEntity, power);

        if (itemStack.is(Items.TIPPED_ARROW) && abstractarrow instanceof Arrow)
        {
            ((Arrow)abstractarrow).setEffectsFromItem(itemStack);
        }

        return abstractarrow;
    }

    public void performRangedAttack(LivingEntity targetEntity, float power)
    {
        if(targetEntity == null)
        {
            return;
        }

        ItemStack itemstack = this.getProjectile();
        AbstractArrow abstractarrow = this.getArrow(itemstack, power);

        double xVector = targetEntity.getX() - mob.getX();
        double yVector = targetEntity.getY(0.3333333333333333D) - abstractarrow.getY();
        double zVector = targetEntity.getZ() - mob.getZ();
        double finalVector = Math.sqrt(xVector * xVector + zVector * zVector);
        abstractarrow.shoot(xVector, yVector + finalVector * (double)0.2F, zVector, 1.6F, (float)(14 - mob.level.getDifficulty().getId() * 4));
        mob.playSound(SoundEvents.SKELETON_SHOOT, 1.0F, 1.0F / (mob.getRandom().nextFloat() * 0.4F + 0.8F));
        mob.level.addFreshEntity(abstractarrow);
    }
}
