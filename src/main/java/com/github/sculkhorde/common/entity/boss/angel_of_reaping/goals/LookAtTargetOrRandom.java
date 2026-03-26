package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class LookAtTargetOrRandom extends Goal {
    public static final float DEFAULT_PROBABILITY = 0.02F;
    protected final Mob mob;
    @Nullable
    protected Entity lookAt;
    protected final float lookDistance;
    private int lookTime;
    protected final float probability;
    private final boolean onlyHorizontal;
    protected final TargetingConditions lookAtContext;

    public LookAtTargetOrRandom(Mob mob, float lookDistanceIn, float probabilityIn, boolean onlyHorizontal) {
        this.mob = mob;
        this.lookDistance = lookDistanceIn;
        this.probability = probabilityIn;
        this.onlyHorizontal = onlyHorizontal;
        this.setFlags(EnumSet.of(Flag.LOOK));
        this.lookAtContext = TargetingConditions.forNonCombat().range((double)lookDistanceIn);


    }

    public boolean canUse()
    {
        if (this.mob.getRandom().nextFloat() >= this.probability && mob.getTarget() == null)
        {
            return false;
        }

        if (this.mob.getTarget() != null)
        {
            this.lookAt = this.mob.getTarget();
            return true;
        }

        this.lookAt = this.mob.level().getNearestEntity(this.mob.level().getEntitiesOfClass(LivingEntity.class, this.mob.getBoundingBox().inflate((double)this.lookDistance, 3.0, (double)this.lookDistance), (p_148124_) -> {
            return true;
        }), this.lookAtContext, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());

        return this.lookAt != null;

    }

    public boolean canContinueToUse() {
        if (!this.lookAt.isAlive()) {
            return false;
        } else if (this.mob.distanceToSqr(this.lookAt) > (double)(this.lookDistance * this.lookDistance)) {
            return false;
        } else {
            return this.lookTime > 0;
        }
    }

    public void start() {
        this.lookTime = this.adjustedTickDelay(40 + this.mob.getRandom().nextInt(40));
    }

    public void stop() {
        this.lookAt = null;
    }

    public void tick() {
        if (this.lookAt.isAlive()) {
            double $$0 = this.onlyHorizontal ? this.mob.getEyeY() : this.lookAt.getEyeY();
            this.mob.getLookControl().setLookAt(this.lookAt.getX(), $$0, this.lookAt.getZ());
            --this.lookTime;
        }
    }
}
