package com.github.sculkhorde.common.entity.goal;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.RaidHandler;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.entity.ai.util.LandRandomPos;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import javax.swing.text.html.Option;
import java.util.EnumSet;
import java.util.Optional;

public class ImprovedRandomStrollGoal extends Goal{
    public static final int DEFAULT_INTERVAL = 120;
    protected final PathfinderMob mob;
    protected double wantedX;
    protected double wantedY;
    protected double wantedZ;
    protected final double speedModifier;
    protected int interval;
    protected boolean forceTrigger;
    public static final float PROBABILITY = 0.001F;
    protected float probability;
    protected boolean avoidWater;

    public ImprovedRandomStrollGoal(PathfinderMob mob, double speedModifier) {
        this(mob, speedModifier, DEFAULT_INTERVAL);
    }

    public ImprovedRandomStrollGoal(PathfinderMob mob, double speedModifier, int interval) {
        this.mob = mob;
        this.speedModifier = speedModifier;
        this.interval = interval;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        probability = PROBABILITY;
    }

    public ImprovedRandomStrollGoal setProbabilty(float probability) {
        this.probability = probability;
        return this;
    }

    public ImprovedRandomStrollGoal setToAvoidWater(boolean value)
    {
        avoidWater = value;
        return this;
    }

    public boolean canUse()
    {
        if (this.mob.isVehicle() || ((ISculkSmartEntity)mob).isParticipatingInRaid())
        {
            return false;
        }
        else
        {
            if (!this.forceTrigger)
            {
                if (this.mob.getRandom().nextInt(reducedTickDelay(this.interval)) != 0) {
                    return false;
                }
            }

            Optional<Vec3> position = avoidWater ? getPositionAvoidWater() : getPosition();

            if (position.isEmpty())
            {
                return false;
            }
            else
            {
                this.wantedX = position.get().x;
                this.wantedY = position.get().y;
                this.wantedZ = position.get().z;
                this.forceTrigger = false;
                return true;
            }
        }
    }

    @Nullable
    protected Optional<Vec3> getPosition()
    {
        Optional<Vec3> optional = Optional.ofNullable(DefaultRandomPos.getPos(this.mob, 10, 7));
        return optional;
    }

    @Nullable
    protected Optional<Vec3> getPositionAvoidWater()
    {
        Optional<Vec3> output = Optional.empty();

        if (this.mob.isInWaterOrBubble())
        {
            output = Optional.ofNullable(LandRandomPos.getPos(this.mob, 15, 7));
            if(output.isPresent())
            {
                return output;
            }
            else
            {
                return getPosition();
            }
        }
        else
        {
            if(this.mob.getRandom().nextFloat() >= this.probability)
            {
                output = Optional.ofNullable(LandRandomPos.getPos(this.mob, 10, 7));
            }
            else
            {
                return getPosition();
            }
        }
        return output;
    }

    public boolean canContinueToUse() {
        return !this.mob.getNavigation().isDone() && !this.mob.isVehicle();
    }

    public void start() {
        this.mob.getNavigation().moveTo(this.wantedX, this.wantedY, this.wantedZ, this.speedModifier);
    }

    public void stop() {
        this.mob.getNavigation().stop();
        super.stop();
    }

    public void trigger() {
        this.forceTrigger = true;
    }

    public void setInterval(int p_25747_) {
        this.interval = p_25747_;
    }
}
