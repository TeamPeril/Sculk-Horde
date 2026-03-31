package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.ZoltraakAttackEntity;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.github.sculkhorde.util.hitboxes.HitboxUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.Collections;

public class ZoltraakBarrageAttackGoal extends ReaperCastSpellGoal
{
    protected int maxAttackDuration = TickUnits.convertSecondsToTicks(10);
    protected int elapsedAttackDuration = 0;
    protected int attackIntervalTicks = TickUnits.convertSecondsToTicks(0.5F);
    protected int attackkIntervalCooldown = 0;
    ArrayList<LivingEntity> targets = new ArrayList<>();


    public ZoltraakBarrageAttackGoal(AngelOfReapingEntity mob) {
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
            setPostAttack(true);
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

        AABB targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 10);
        targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 20);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 30);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 40);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 50);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));
        }

        if(targets.size() < maxTargets)
        {
            targets.clear();
            targetHitBox = HitboxUtil.createBoundingBoxCubeAtBlockPos(mob.position(), 60);
            targets.addAll(EntityAlgorithms.getHostileEntitiesInBoundingBox((ServerLevel) mob.level(), targetHitBox));
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

        //AngelOfReapingEntity.performTargetedZoltraakAttack(mob, getRandomBlockPosAboveEntity().getCenter(), mob.getTarget(), DAMAGE);
        ZoltraakAttackEntity.castZoltraakOnEntity(mob, mob.getTarget(), getRandomBlockPosAboveEntity().getCenter());

        attackkIntervalCooldown = attackIntervalTicks;
    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(1);
    }

    @Override
    protected void playPreAttackAnimation() {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ATTACK_SPELL_USE_ID);
    }
}