package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.*;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ElementalMagicCircleAttackGoal extends ReaperCastSpellGoal
{

    protected int elementType = 0;

    public ElementalMagicCircleAttackGoal(AngelOfReapingEntity mob) {
        super(mob);
    }

    @Override
    public void start()
    {
        super.start();
        elementType = mob.level().getRandom().nextInt(4);
    }

    @Override
    public boolean canUse()
    {
        if(!super.canUse())
        {
            return false;
        }
        if(!mob.getTarget().onGround())
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

        int innerCircleRadius = 2;
        int innerCircleAmount = 3;
        int middleCircleRadius = 6;
        int middleCircleAmount = 8;
        int outerCircleRadius = 10;
        int outerCircleAmount = 10;

        // If the target is within a close range
        if (mob.distanceToSqr(targetEntity) < 9.0D) {

            BlockAlgorithms.getPointsOnCircumferenceVec3(mob.position(), innerCircleRadius, innerCircleAmount).forEach((blockPos) -> {
                float angleOffset = (float) Mth.atan2(blockPos.z - mob.getZ(), blockPos.x - mob.getX());
                this.createSpellEntity(blockPos.x, blockPos.z, minY, maxY, angleOffset, 0);
            });

            BlockAlgorithms.getPointsOnCircumferenceVec3(mob.position(), middleCircleRadius, middleCircleAmount).forEach((blockPos) -> {
                float angleOffset = (float) Mth.atan2(blockPos.z - mob.getZ(), blockPos.x - mob.getX());
                this.createSpellEntity(blockPos.x, blockPos.z, minY, maxY, angleOffset, 0);
            });

            BlockAlgorithms.getPointsOnCircumferenceVec3(mob.position(), outerCircleRadius, outerCircleAmount).forEach((blockPos) -> {
                float angleOffset = (float) Mth.atan2(blockPos.z - mob.getZ(), blockPos.x - mob.getX());
                this.createSpellEntity(blockPos.x, blockPos.z, minY, maxY, angleOffset, 0);
            });

        } else {
            // Create spell entities in a line up to target
            for (int l = 0; l < EntityAlgorithms.getDistanceBetweenEntities(mob, targetEntity); l += 4) {
                double distanceMultiplier = l * 1.25D;
                int delay = 1 * l;
                this.createSpellEntity(mob.getX() + (double)Mth.cos(angleToTarget) * distanceMultiplier, mob.getZ() + (double)Mth.sin(angleToTarget) * distanceMultiplier, minY, maxY, angleToTarget, delay);
            }
        }
    }

    // Creates a spell entity at the specified coordinates
    private void createSpellEntity(double x, double z, double minY, double maxY, float angle, int delay) {
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
            ElementalFireMagicCircleAttackEntity spell = getElementalSpellEntity(x, (double)blockPos.getY() + yOffset, z, angle, mob);
            spell.setDelay(delay);
            mob.level().addFreshEntity(spell);
        }
    }

    public ElementalFireMagicCircleAttackEntity getElementalSpellEntity(double x, double y, double z, float angle, LivingEntity owner)
    {
        return switch (elementType) {
            case 0 -> new ElementalFireMagicCircleAttackEntity(mob.level(), x, y, z, angle, owner);
            case 1 -> new ElementalPoisonMagicCircleAttackEntity(mob.level(), x, y, z, angle, owner);
            case 2 -> new ElementalIceMagicCircleAttackEntity(mob.level(), x, y, z, angle, owner);
            case 3 -> new ElementalBreezeMagicCircleAttackEntity(mob.level(), x, y, z, angle, owner);
            default -> new ElementalFireMagicCircleAttackEntity(mob.level(), x, y, z, angle, owner);
        };
    }

    @Override
    protected int getPreAttackDelay() {
        return TickUnits.convertSecondsToTicks(0.96F);
    }

    @Override
    protected void playPreAttackAnimation()
    {
        getReaper().triggerAnim(AngelOfReapingEntity.COMBAT_ATTACK_ANIMATION_CONTROLLER_ID, AngelOfReapingEntity.MAGIC_CIRCLE_SPELL_USE_ID);
    }
}
