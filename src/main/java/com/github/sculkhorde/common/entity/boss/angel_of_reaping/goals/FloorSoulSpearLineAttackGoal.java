package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.FloorSoulSpearsAttackEntity;
import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class FloorSoulSpearLineAttackGoal extends ReaperCastSpellGoal
{
    public FloorSoulSpearLineAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public boolean canUse()
    {
        if(!super.canUse())
        {
            return false;
        }
        else if(!mob.getTarget().onGround())
        {
            return false;
        }

        return true;
    }

    @Override
    protected void doAttackTick() {
        performSpellCasting();
        setPostAttack(true);
    }

    // Performs the spell casting action
    protected void performSpellCasting() {
        LivingEntity targetEntity = mob.getTarget();
        double minY = Math.min(targetEntity.getY(), mob.getY());
        double maxY = Math.max(targetEntity.getY(), mob.getY()) + 1.0D;
        float angleToTarget = (float) Mth.atan2(targetEntity.getZ() - mob.getZ(), targetEntity.getX() - mob.getX());

        // If the target is within a close range
        if (mob.distanceToSqr(targetEntity) < 9.0D) {
            // Create 5 spell entities in a circular pattern
            for (int i = 0; i < 5; ++i) {
                float angleOffset = angleToTarget + (float)i * (float)Math.PI * 0.4F;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleOffset) * 1.5D, mob.getZ() + (double)Mth.sin(angleOffset) * 1.5D, minY, maxY, 0);
            }

            // Create 8 spell entities in a larger circular pattern
            for (int k = 0; k < 8; ++k) {
                float angleOffset = angleToTarget + (float)k * (float)Math.PI * 2.0F / 8.0F + 1.2566371F;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleOffset) * 2.5D, mob.getZ() + (double)Mth.sin(angleOffset) * 2.5D, minY, maxY, 3);
            }
        } else {

            // Create 16 spell entities in a straight line
            for (int length = 0; length < Math.min(mob.distanceToSqr(targetEntity), 64); ++length) {
                double distanceMultiplier = 1.25D * (double)(length + 1);
                int delay = 1 * length;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleToTarget) * distanceMultiplier, mob.getZ() + (double)Mth.sin(angleToTarget) * distanceMultiplier, minY, maxY, delay);
            }

            float angleToLeftOfTarget = angleToTarget + 0.07F;
            for (int length = 0; length < Math.min(mob.distanceToSqr(targetEntity), 64); ++length) {
                double distanceMultiplier = 1.25D * (double)(length + 1);
                int delay = 1 * length;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleToLeftOfTarget) * distanceMultiplier, mob.getZ() + (double)Mth.sin(angleToLeftOfTarget) * distanceMultiplier, minY, maxY, delay);
            }

            float angleToRightOfTarget = angleToTarget - 0.07F;
            for (int length = 0; length < Math.min(mob.distanceToSqr(targetEntity), 64); ++length) {
                double distanceMultiplier = 1.25D * (double)(length + 1);
                int delay = 1 * length;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleToRightOfTarget) * distanceMultiplier, mob.getZ() + (double)Mth.sin(angleToRightOfTarget) * distanceMultiplier, minY, maxY, delay);
            }

        }
    }

    // Creates a spell entity at the specified coordinates
    private void createSpellEntity(double x, double z, double minY, double maxY, int delay) {
        BlockPos blockPos = BlockPos.containing(x, maxY, z);
        boolean foundSuitablePosition = false;
        double yOffset = 0.0D;

        // Find a suitable position for the spell entity
        do {
            BlockPos belowBlockPos = blockPos.below();
            BlockState belowBlockState = mob.level().getBlockState(belowBlockPos);
            if (belowBlockState.isFaceSturdy(mob.level(), belowBlockPos, Direction.UP)) {
                if (!mob.level().isEmptyBlock(blockPos)) {
                    BlockState blockState = mob.level().getBlockState(blockPos);
                    VoxelShape voxelShape = blockState.getCollisionShape(mob.level(), blockPos);
                    if (!voxelShape.isEmpty()) {
                        yOffset = voxelShape.max(Direction.Axis.Y);
                    }
                }

                foundSuitablePosition = true;
                break;
            }

            blockPos = blockPos.below();
        } while (blockPos.getY() >= Mth.floor(minY) - 1);

        // Add the spell entity to the world if a suitable position is found
        if (foundSuitablePosition) {
            FloorSoulSpearsAttackEntity entity = new FloorSoulSpearsAttackEntity(mob, x, blockPos.getY(), z, delay);
            entity.setOwner(mob);
            mob.level().addFreshEntity(entity);
        }
    }

    @Override
    protected void playAttackAnimation() {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.ATTACK_SPELL_USE_ID);
    }
}
