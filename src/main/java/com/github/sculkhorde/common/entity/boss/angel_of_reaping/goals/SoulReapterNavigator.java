package com.github.sculkhorde.common.entity.boss.angel_of_reaping.goals;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class SoulReapterNavigator extends Goal {
    private final AngelOfReapingEntity mob;
    @Nullable
    private LivingEntity target;
    private double wantedX;
    private double wantedY;
    private double wantedZ;
    private final double speedModifier;
    private final float maxDistance;
    private final float minDistance;

    public SoulReapterNavigator(AngelOfReapingEntity reaper, float maxDistance, float minDistance) {
        this.mob = reaper;
        this.speedModifier = 1;
        this.maxDistance = maxDistance;
        this.minDistance = minDistance;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
    }

    public boolean canUse()
    {
        this.target = mob.getHitTarget().isEmpty() ? mob.getTarget() : mob.getHitTarget().get();

        if (this.target == null)
        {
            return false;
        }
        else if (this.target.distanceTo(this.mob) < this.minDistance)
        {
            Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 16, 7, this.target.position());
            if (vec3 == null)
            {
                return false;
            }
            else
            {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                return true;
            }
        }
        else if (this.target.distanceTo(this.mob) > this.maxDistance || !mob.getSensing().hasLineOfSight(this.target))
        {
            Vec3 vec3 = DefaultRandomPos.getPosTowards(this.mob, 16, 7, this.target.position(), (double)((float)Math.PI / 2F));
            if (vec3 == null)
            {
                return false;
            }
            else
            {
                this.wantedX = vec3.x;
                this.wantedY = vec3.y;
                this.wantedZ = vec3.z;
                return true;
            }
        }



        return false;
    }

    public boolean canContinueToUse() {

        return !this.mob.getNavigation().isDone() && this.target.isAlive() && this.target.distanceTo(this.mob) <= this.maxDistance && this.target.distanceTo(this.mob) >= this.minDistance;
    }

    public void stop() {
        this.target = null;
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }
}
