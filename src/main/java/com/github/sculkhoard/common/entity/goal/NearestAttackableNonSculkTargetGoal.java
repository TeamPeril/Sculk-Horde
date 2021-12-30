package com.github.sculkhoard.common.entity.goal;

import com.github.sculkhoard.common.entity.EntityAlgorithms;
import com.github.sculkhoard.common.entity.SculkLivingEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityPredicate;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.ai.goal.Goal;
import net.minecraft.entity.ai.goal.TargetGoal;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.util.math.AxisAlignedBB;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class NearestAttackableNonSculkTargetGoal<T extends LivingEntity> extends TargetGoal {


    protected final Class<T> targetType;
    protected final int randomInterval;
    protected LivingEntity target;
    protected EntityPredicate targetConditions;
    List<LivingEntity> possibleTargets;

    public NearestAttackableNonSculkTargetGoal(MobEntity p_i50313_1_, Class<T> p_i50313_2_, boolean p_i50313_3_) {
        this(p_i50313_1_, p_i50313_2_, p_i50313_3_, false);
    }

    public NearestAttackableNonSculkTargetGoal(MobEntity p_i50314_1_, Class<T> p_i50314_2_, boolean p_i50314_3_, boolean p_i50314_4_) {
        this(p_i50314_1_, p_i50314_2_, 10, p_i50314_3_, p_i50314_4_, (Predicate<LivingEntity>)null);
    }

    public NearestAttackableNonSculkTargetGoal(MobEntity p_i50315_1_, Class<T> p_i50315_2_, int p_i50315_3_, boolean p_i50315_4_, boolean p_i50315_5_, @Nullable Predicate<LivingEntity> p_i50315_6_) {
        super(p_i50315_1_, p_i50315_4_, p_i50315_5_);
        this.targetType = p_i50315_2_;
        this.randomInterval = p_i50315_3_;
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
        this.targetConditions = (new EntityPredicate()).range(this.getFollowDistance()).selector(p_i50315_6_);
    }

    public boolean canUse() {
        if (this.randomInterval > 0 && this.mob.getRandom().nextInt(this.randomInterval) != 0) {
            return false;
        } else {
            this.findTarget();
            return this.target != null;
        }
    }

    protected AxisAlignedBB getTargetSearchArea(double p_188511_1_) {
        return this.mob.getBoundingBox().inflate(p_188511_1_, 4.0D, p_188511_1_);
    }

    protected void findTarget() {

        //If targetType is not player, Get all possible targets, filter out sculk or infected mobs
        if (this.targetType != PlayerEntity.class && this.targetType != ServerPlayerEntity.class)
        {
            possibleTargets =
                    this.mob.level.getLoadedEntitiesOfClass(
                    this.targetType,
                    this.getTargetSearchArea(this.getFollowDistance()),
                    (Predicate<? super LivingEntity>) null);

            //Remove Any Sculk Entities or entities already infected
            for(int i = 0; i < possibleTargets.size(); i++)
            {
                if(possibleTargets.get(i) instanceof SculkLivingEntity)
                {
                    possibleTargets.remove(i);
                    i--;
                }
                else if(EntityAlgorithms.isLivingEntityInfected(possibleTargets.get(i)))
                {
                    possibleTargets.remove(i);
                    i--;
                }
            }
        }
        else //if targetType is player
        {
            this.target = this.mob.level.getNearestPlayer(
                    this.targetConditions,
                    this.mob,
                    this.mob.getX(),
                    this.mob.getEyeY(),
                    this.mob.getZ()
            );
        }

        //If there is available targets
        if(possibleTargets.size() > 0)
        {
            LivingEntity closestLivingEntity = possibleTargets.get(0);

            //Return nearest Mob
            for(LivingEntity e : possibleTargets)
            {
                if(e.distanceTo(this.mob) < e.distanceTo(closestLivingEntity))
                {
                    closestLivingEntity = e;
                }
            }
            this.target = closestLivingEntity; //Return target
        }
    }

    public void start() {
        this.mob.setTarget(this.target);
        super.start();
    }

    public void setTarget(@Nullable LivingEntity p_234054_1_) {
        this.target = p_234054_1_;
    }

}
