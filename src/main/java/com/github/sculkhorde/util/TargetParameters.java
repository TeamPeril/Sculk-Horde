package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.CreeperEntity;
import net.minecraft.entity.player.PlayerEntity;

import static com.github.sculkhorde.util.EntityAlgorithms.*;

public class TargetParameters {
    private boolean targetHostiles = false; //Should we attack hostiles?
    private boolean targetPassives = false; //Should we target passives?
    private boolean targetInfected = false;//If a passive or hostile is infected, should we attack it?
    private boolean targetBelow50PercentHealth = true; //Should we target entities below 50% health?
    private boolean targetSwimmers = false; //Should we target entities that can swim?

    public TargetParameters enableTargetHostiles()
    {
        targetHostiles = true;
        return this;
    }

    public boolean isEntityValidTarget(LivingEntity e)
    {
        if(e == null)
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

        //If we do not attack below 50% health and target is below 50% health
        return true;
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
}
