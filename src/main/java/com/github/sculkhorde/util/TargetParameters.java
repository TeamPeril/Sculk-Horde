package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MobEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.pathfinding.Path;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.server.ServerWorld;

import java.util.HashMap;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

import static com.github.sculkhorde.util.EntityAlgorithms.*;

public class TargetParameters
{
    private MobEntity mob;

    private boolean targetHostiles = false; //Should we attack hostiles?
    private boolean targetPassives = false; //Should we target passives?
    private boolean targetInfected = false;//If a passive or hostile is infected, should we attack it?
    private boolean targetBelow50PercentHealth = true; //Should we target entities below 50% health?
    private boolean targetSwimmers = false; //Should we target entities that can swim?
    private boolean mustSeeTarget = false; //Should we only target entities we can see?
    private long lastTargetSeenTime = System.currentTimeMillis(); //The last time we saw the target
    private long MAX_TARGET_UNSEEN_TIME_MILLIS = TimeUnit.SECONDS.toMillis(30); //The max time we can go without seeing the target
    private boolean mustReachTarget = false; //Should we only target entities we can reach?
    //A hash map which we store a blacklist of mobs we should not attack. Should use UUIDs of mobs to identify
    private HashMap<UUID, Long> blacklist = new HashMap<>();




    public TargetParameters()
    {
        this.mob = null;
    }

    public TargetParameters(MobEntity mob)
    {
        this.mob = mob;
    }


    // Predicate to test if valid target
    public final Predicate<LivingEntity> isPossibleNewTargetValid = (e) -> {
        return isEntityValidTarget(e, false);
    };


    public boolean isEntityValidTarget(LivingEntity e, boolean validatingExistingTarget)
    {
        if(e == null)
        {
            return false;
        }

        if(!(e instanceof MobEntity) && !(e instanceof PlayerEntity))
        {
            return false;
        }

        //If passes sculk predicate
        if(isSculkLivingEntity.test(e))
        {
            return false;
        }

        //Do not attack creepers
        if(e instanceof CreeperEntity)
        {
            return false;
        }

        if(e instanceof InfestationPurifierEntity)
        {
            return true;
        }

        //If not attackable or invulnerable or is dead/dying
        if(!e.isAttackable() || e.isInvulnerable() || !e.isAlive() || e.isSpectator())
        {
            return false;
        }

        //If player is in creative or spectator
        if(e instanceof PlayerEntity && (((PlayerEntity) e).isCreative() || ((PlayerEntity) e).isSpectator()))
        {
            return false;
        }

        // If Blacklisted
        if(blacklist.containsKey(e.getUUID()))
        {
            return false;
        }

        //If we do not attack infected and entity is infected
        if(!targetInfected && isLivingEntityInfected(e))
        {
            return false;
        }

        //If we do not attack passives and entity is non-hostile
        if(!targetPassives && !isLivingEntityHostile(e)) //NOTE: horde assumes everything is passive until provoked
        {
            return false;
        }

        //If we do not attack hostiles and target is hostile
        if(!targetHostiles && isLivingEntityHostile(e))
        {
            return false;
        }

        //If we do not attack swimmers and target is a swimmer
        if(!targetSwimmers && isLivingEntitySwimmer(e))
        {
            return false;
        }

        //If we do not attack swimmers and target is a swimmer
        if(!targetBelow50PercentHealth && (e.getHealth() < e.getMaxHealth() / 2))
        {
            return false;
        }

        //If we must reach target and cannot reach target
        // NOTE: validating existing targets gets called significantly more often.
        // When we do this, we disable reach check because it lags to all hell.
        if(!validatingExistingTarget && mustReachTarget() && !canReach(e))
        {
            return false;
        }

        //Entity is Valid
        return true;
    }

    public TargetParameters enableTargetHostiles()
    {
        targetHostiles = true;
        return this;
    }

    public boolean isTargetingHostiles()
    {
        return targetHostiles;
    }

    public TargetParameters enableTargetPassives()
    {
        targetPassives = true;
        return this;
    }

    public boolean isTargetingPassives()
    {
        return targetPassives;
    }

    public TargetParameters enableTargetInfected()
    {
        targetInfected = true;
        return this;
    }

    public boolean isTargetingInfected()
    {
        return targetInfected;
    }

    public TargetParameters ignoreTargetBelow50PercentHealth()
    {
        targetBelow50PercentHealth = false;
        return this;
    }


    public boolean isIgnoringTargetBelow50PercentHealth()
    {
        return targetBelow50PercentHealth;
    }

    public TargetParameters enableTargetSwimmers()
    {
        targetSwimmers = true;
        return this;
    }

    public boolean isTargetingSwimmers()
    {
        return targetSwimmers;
    }

    public TargetParameters enableMustReachTarget()
    {
        if(this.mob == null)
        {
            throw new IllegalStateException("Cannot enable must reach target without a mob");
        }
        mustReachTarget = true;
        return this;
    }

    public boolean mustReachTarget()
    {
        return mustReachTarget;
    }

    private boolean canReach(LivingEntity pTarget)
    {
        Path path = this.mob.getNavigation().createPath(pTarget, 0);
        if (path == null)
        {
            return false;
        }
        else
        {
            PathPoint pathpoint = path.getEndNode();
            if (pathpoint == null)
            {
                return false;
            }
            else
            {
                int i = pathpoint.x - MathHelper.floor(pTarget.getX());
                int j = pathpoint.z - MathHelper.floor(pTarget.getZ());
                return (double)(i * i + j * j) <= 50;
            }
        }
    }


    public void addToBlackList(MobEntity entity)
    {
        blacklist.put(entity.getUUID(), System.currentTimeMillis());
    }

    // Is mob on blacklist
    public boolean isOnBlackList(MobEntity entity)
    {
        return blacklist.containsKey(entity.getUUID());
    }
}
