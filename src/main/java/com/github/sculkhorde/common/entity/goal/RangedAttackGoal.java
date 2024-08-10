package com.github.sculkhorde.common.entity.goal;
import com.github.sculkhorde.common.entity.SculkSpitterEntity;
import com.github.sculkhorde.common.entity.attack.RangedAttack;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class RangedAttackGoal extends Goal {

    private final Mob entity; //The Entity Calling this function
    private double moveSpeedAmp = 1; //Movement Speed
    private int attackCooldown; //Chase Speed
    private int visibleTicksDelay = 3;
    private float maxAttackDistance = 20;
    private int strafeTicksThreshold = 20; //The maximum amount of ticks this entity can strafe
    private int attackTime = -1;
    private int seeTime; //How many ticks our target has been visible
    private boolean strafingClockwise; //Are we strafing clockwise?
    private boolean strafingBackwards; //Are we strafing backwards?
    private int strafingTime = -1;
    private int statecheck;

    private RangedAttack attack;

    /**
     * Constructor
     * @param mob The Shooter Mob
     * @param attack The Type of Attack
     * @param moveSpeedAmpIn Chase Speed
     * @param attackCooldownIn The cooldown
     * @param visibleTicksDelay ???
     * @param strafeTicksThreshold The maximum amount of ticks this entity can strafe
     * @param maxAttackDistanceIn The max distance to target we can use ranged attack
     * @param state ???
     */
    public RangedAttackGoal(Mob mob, RangedAttack attack, double moveSpeedAmpIn,
                            int attackCooldownIn, int visibleTicksDelay, int strafeTicksThreshold, float maxAttackDistanceIn, int state) {
        this.entity = mob;
        this.moveSpeedAmp = moveSpeedAmpIn;
        this.attackCooldown = attackCooldownIn;
        this.maxAttackDistance = maxAttackDistanceIn * maxAttackDistanceIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.attack = attack;
        this.visibleTicksDelay = visibleTicksDelay;
        this.strafeTicksThreshold = strafeTicksThreshold;
        this.statecheck = state;
    }

    // use defaults
    public RangedAttackGoal(Mob mob, RangedAttack attack, int attackCooldownIn) {
        this.entity = mob;
        this.attackCooldown = attackCooldownIn;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
        this.attack = attack;
    }

    private boolean multiShot = false;
    private int multiShotCount = 0;
    private int multiShotTickDelay = 0;

    private boolean multiShooting = false;
    private int multiShotsLeft = 0;
    private int multiShotTicker = 0;

    /**
     *
     * @param count
     * @param tickDelay
     * @return
     */
    public RangedAttackGoal setMultiShot(int count, int tickDelay) {
        multiShot = true;
        multiShotCount = count;
        multiShotTickDelay = tickDelay;
        return this;
    }

    /**
     *
     * @return
     */
    public boolean tickMultiShot()
    {
        //If we have multi shots left and it is time to shoot
        if (multiShotsLeft > 0 && multiShotTicker == 0)
        {
            multiShotsLeft--; //deincrement how many shots  we have left
            //If it is the last shot, stop attack.
            if (multiShotsLeft == 0)
                finishMultiShot();
            //Reset timer for shooting
            multiShotTicker = multiShotTickDelay;
            return true;
        }
        //If it is not time to shoot yet, deincrement tick tracker
        multiShotTicker--;
        return false;
    }

    /**
     *
     */
    public void beginMultiShooting() {
        multiShooting = true;
        multiShotsLeft = multiShotCount - 1;
        multiShotTicker = multiShotTickDelay;
    }

    /**
     *
     */
    public void finishMultiShot() {
        multiShooting = false;
        multiShotsLeft = 0;
    }

    /**
     *
     * @param attackCooldownIn
     */
    public void setAttackCooldown(int attackCooldownIn) {
        this.attackCooldown = attackCooldownIn;
    }

    /**
     * Returns whether execution should begin. You can also read and cache any state
     * necessary for execution in this method as well.
     *
     * NOTE: I SHOULD NOT NEED TO CHECK IF THE ENTITY IS DEAD
     */
    public boolean canUse()
    {
        if(this.entity.getTarget() == null) {return false;}
        return this.entity.getTarget().isAlive();
    }

    /**
     * Returns whether an in-progress EntityAIBase should continue executing
     */
    public boolean canContinueToUse() {
        return (this.canUse() || !this.entity.getNavigation().isDone());
    }

    /**
     * Execute a one shot task or start executing a continuous task
     */
    public void start() {
        super.start();
        this.entity.setAggressive(true);
        this.entity.swinging = true;
    }

    /**
     * Reset the task's internal state. Called when this task is interrupted by
     * another one
     */
    public void stop() {
        super.stop();
        this.entity.setAggressive(false);
        //TODO: PORT
        //this.entity.setAttackingState(0);
        this.seeTime = 0;
        this.attackTime = attackCooldown;
        this.entity.stopUsingItem();
        this.entity.swinging = false;
    }

    /**
     * Keep ticking a continuous task that has already been started
     */
    public void tick()
    {
        if(!this.canUse()) {return;}

        LivingEntity targetEntity = this.entity.getTarget();

        double distanceToTargetSq = this.entity.distanceToSqr(targetEntity.getX(), targetEntity.getY(), targetEntity.getZ());
        boolean inLineOfSight = this.entity.getSensing().hasLineOfSight(targetEntity);

        //If target is in light
        if (inLineOfSight != this.seeTime > 0)
            this.seeTime = 0;

        //NOTE: ++Variable returns the value after incrementing. Variable++ returns the value before this.
        //If target can be seen
        if (inLineOfSight)
        {
            ++this.seeTime; //Keep track of the ticks this target has been visible
        }
        else //If the target is not visible
        {
            //Finish a multishot attack in case we are in the middle of one
            if (multiShot)
                finishMultiShot();

            //De-increment the tick tracker for target being visible
            --this.seeTime;
        }

        //If the distance to target is in range and we have seen the target long enough
        if (distanceToTargetSq <= (double) this.maxAttackDistance && this.seeTime >= 20)
        {
            this.entity.getNavigation().stop();//Tell entity to stop
            ++this.strafingTime; //Track how many ticks were strafing
        }
        else //If target not in range, move to it
        {
            //Go to the target mob
            this.entity.getNavigation().moveTo(targetEntity, this.moveSpeedAmp);
            this.strafingTime = -1; //Stop Strafing
        }

        //If we have reached the max threshold for strafing
        if (this.strafingTime >= strafeTicksThreshold)
        {
            //Given a random chance, change strafe rotation
            if ((double) this.entity.getRandom().nextFloat() < 0.3D)
            {
                this.strafingClockwise = !this.strafingClockwise; //Change strafe direction
            }
            //Given random chance, change to strafe forward/backwards
            if ((double) this.entity.getRandom().nextFloat() < 0.3D)
            {
                this.strafingBackwards = !this.strafingBackwards;
            }

            this.strafingTime = 0; //Reset Strafing Time
            if(this.entity instanceof SculkSpitterEntity spitter)
            {
                spitter.setStrafing(true);
            }
        }

        //If directed to stop strafing
        if (this.strafingTime > -1)
        {
            //If distance to target is 75% to 100% of maxAttackDistance
            if (distanceToTargetSq > (double) (this.maxAttackDistance * 0.75F))
            {
                this.strafingBackwards = false; //Stop strafing backwards
            }
            //If distance to target is between 0% to 25% of maxAttackDistance
            else if (distanceToTargetSq < (double) (this.maxAttackDistance * 0.25F))
            {
                this.strafingBackwards = true;
            }

            //Tell entity how to strafe given the booleans strafingBackwards and strafingClockwise
            this.entity.getMoveControl().strafe(this.strafingBackwards ? -0.5F : 0.5F,
                    this.strafingClockwise ? 0.5F : -0.5F);
            //Tell Entity to look at target
            this.entity.lookAt(targetEntity, 30.0F, 30.0F);

            if(this.entity instanceof SculkSpitterEntity spitter)
            {
                spitter.setStrafing(false);
            }
        }
        else //If in process of strafing, just look at target entity
        {
            this.entity.getLookControl().setLookAt(targetEntity, 30.0F, 30.0F);
            if(this.entity instanceof SculkSpitterEntity spitter)
            {
                spitter.setStrafing(true);
            }
        }


        //If multishooting is enabled, do a multishot tick instead of a single one
        if (multiShooting)
        {
            if (tickMultiShot())
                this.attack.shoot();
            return;
        }
        //If multishooting is not enabled
        else if (this.seeTime >= this.visibleTicksDelay)
        {
            if (this.attackTime >= this.attackCooldown)
            {
                this.attack.triggerAttackAnimation();
                this.attack.shoot();
                this.attackTime = 0;
            }
            else
                this.attackTime++;
        }
        //TODO: PORT
        //this.entity.setAttackingState(attackTime >= attackCooldown * 0.75 ? this.statecheck : 0);

    }
}
