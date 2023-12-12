package com.github.sculkhorde.util;

import java.util.ArrayList;
import java.util.Optional;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

public class SquadHandler {

    private ISculkSmartEntity sculkSmartEntity;
    public  Optional<ISculkSmartEntity> squadLeader = Optional.empty();

    public final ArrayList<ISculkSmartEntity> squadMembers = new ArrayList<>();

    private final int squadSize = 10;

    public SquadHandler(ISculkSmartEntity entity) {
        this.sculkSmartEntity = entity;
    }

    public Mob getMob() {
        return (Mob) sculkSmartEntity;
    }

    public static boolean doesSquadExist(SquadHandler squad) {
        if(squad == null)
        {
            return false;
        }
        return !squad.isSquadLeaderDead();
    }

    public boolean isSquadLeader() {
        if(squadLeader.isEmpty())
        {
            return false;
        }

        return ((Mob) squadLeader.get()).getUUID().equals(getMob().getUUID());
    }

    public boolean isSquadJoinable() {
        return squadMembers.size() < squadSize;
    }

    public boolean isSquadLeaderDead()
    {
        return !squadLeader.isPresent() || ((Mob) squadLeader.get()).isDeadOrDying();
    }

    public boolean isEntireSquadDead()
    {
        removeDeadMembersFromSquad();
        if(squadLeader.isEmpty() && squadMembers.isEmpty())
        {
            return true;
        }
        return false;
    }

    public void removeDeadMembersFromSquad()
    {
        if(squadLeader.isPresent())
        {
            if(((Mob) squadLeader.get()).isDeadOrDying())
            {
                squadLeader = Optional.empty();
            }
        }

        for(int i = 0; i < squadMembers.size(); i++)
        {
            Mob member = (Mob) squadMembers.get(i);
            if(member.isAlive())
            {
                continue;
            }

            squadMembers.remove(i);
            i--;
        }
    }

    public boolean tryToAcceptMemberIntoSquad(ISculkSmartEntity joiningMob) {
        if (isSquadJoinable()) {
            squadMembers.add(joiningMob);
            joiningMob.getSquad().squadLeader = Optional.ofNullable(sculkSmartEntity);
            return true;
        }
        return false;
    }

    public void forceAcceptMemberIntoSquad(ISculkSmartEntity joiningMob)
    {
        squadMembers.add(joiningMob);
        joiningMob.getSquad().squadLeader = Optional.of(sculkSmartEntity);
    }

    public void createSquad() {
        squadLeader = Optional.ofNullable(sculkSmartEntity);
    }

    public void disbandSquad() {
        for (ISculkSmartEntity member : squadMembers) {
            member.getSquad().squadLeader = Optional.empty();
        }
        squadMembers.clear();
        squadLeader = Optional.empty();
    }

    public ISculkSmartEntity getMobMemberWithMostMaxHealth()
    {
        if(isEntireSquadDead())
        {
            return null;
        }

        Mob squadLeaderAsMob = ((Mob) squadLeader.get());
        ISculkSmartEntity result = squadLeader.get();

        double squadLeaderMaxHealth = squadLeaderAsMob.getMaxHealth();

        for(int i = 0; i < squadMembers.size(); i++)
        {
            Mob compareMob = (Mob) squadMembers.get(i);
            if(compareMob.getMaxHealth() > squadLeaderMaxHealth)
            {
                result = squadMembers.get(i);
            }
        }
        return result;
    }

    public static void promoteToLeaderOfSquad(ISculkSmartEntity entity, SquadHandler oldSquad)
    {
        entity.getSquad().createSquad();
        if(oldSquad.squadLeader.isPresent())
        {
            entity.getSquad().forceAcceptMemberIntoSquad(oldSquad.squadLeader.get());
        }

        // Tell each mob who their new leader is.
        for(ISculkSmartEntity member : oldSquad.squadMembers)
        {
            member.getSquad().squadLeader = Optional.of(entity);
            entity.getSquad().forceAcceptMemberIntoSquad(member);
        }

    }

    public LivingEntity getSquadTarget()
    {
        return squadLeader.map(iSculkSmartEntity -> ((Mob) iSculkSmartEntity).getTarget()).orElse(null);
    }
}
