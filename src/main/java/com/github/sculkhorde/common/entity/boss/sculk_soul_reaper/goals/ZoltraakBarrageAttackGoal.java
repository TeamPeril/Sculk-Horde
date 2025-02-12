package com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.goals;

import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.SculkSoulReaperEntity;
import com.github.sculkhorde.common.entity.boss.sculk_soul_reaper.ZoltraakAttackEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Collections;

public class ZoltraakBarrageAttackGoal extends ReaperCastSpellGoal
{
    protected int maxAttackDuration = TickUnits.convertSecondsToTicks(10);
    protected int elapsedAttackDuration = 0;
    protected int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.5F);
    protected int attackkIntervalCooldown = 0;
    ArrayList<LivingEntity> targets = new ArrayList<>();


    public ZoltraakBarrageAttackGoal(SculkSoulReaperEntity mob) {
        super(mob);
    }

    protected BlockPos getRandomBlockPosAboveEntity()
    {
        int xOffset = mob.getRandom().nextInt(-2, 2);
        int yOffset = mob.getRandom().nextInt(-2, 2);
        int zOffset = mob.getRandom().nextInt(-2, 2);

        return new BlockPos(mob.getBlockX() + xOffset, mob.getBlockY() + 5 + yOffset, mob.getBlockZ() + zOffset);
    }

    @Override
    protected void doAttackTick() {
        elapsedAttackDuration++;

        if(elapsedAttackDuration >= maxAttackDuration)
        {
            setSpellCompleted();
            return;
        }

        if(targets.isEmpty())
        {
            populateTargetList();
            return;
        }
        mob.setTarget(targets.get(0));
        targets.remove(0);

        shootZoltraakAtRandomTarget(5);
    }

    protected void populateTargetList()
    {
        int maxTargets = 5;

        AABB targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 10);
        targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 20);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));
            //SculkHorde.LOGGER.debug("ZoltraakBarrageAttackGoal | Expanding hitbox to length of 20");
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 30);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));
            //SculkHorde.LOGGER.debug("ZoltraakBarrageAttackGoal | Expanding hitbox to length of 30");
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 40);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));
            //SculkHorde.LOGGER.debug("ZoltraakBarrageAttackGoal | Expanding hitbox to length of 40");
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 50);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));
            //SculkHorde.LOGGER.debug("ZoltraakBarrageAttackGoal | Expanding hitbox to length of 50");
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = EntityAlgorithms.createBoundingBoxCubeAtBlockPos(mob.position(), 60);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level, targetHitBox));
            //SculkHorde.LOGGER.debug("ZoltraakBarrageAttackGoal | Expanding hitbox to length of 60");
        }

        if(targets.size() < maxTargets && targets.isEmpty())
        {
            elapsedAttackDuration = maxAttackDuration;
        }


        Collections.shuffle(targets);


        // Trim the list to the desired size
        while (targets.size() > maxTargets) {
            targets.remove(targets.size() - 1);
        }
    }


    @Override
    public void stop()
    {
        super.stop();
        elapsedAttackDuration = 0;
    }


    public void shootZoltraakAtRandomTarget(int range)
    {
        attackkIntervalCooldown--;

        if(attackkIntervalCooldown > 0)
        {
            return;
        }

        if(mob.getTarget() == null)
        {
            return;
        }

        //SculkSoulReaperEntity.performTargetedZoltraakAttack(mob, getRandomBlockPosAboveEntity().getCenter(), mob.getTarget(), DAMAGE);
        ZoltraakAttackEntity.castZoltraakOnEntity(mob, mob.getTarget(), Vec3.atCenterOf(getRandomBlockPosAboveEntity()));

        attackkIntervalCooldown = attackIntervalTicks;
    }

}