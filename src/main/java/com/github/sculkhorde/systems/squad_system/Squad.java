package com.github.sculkhorde.systems.squad_system;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.neoforged.neoforge.server.ServerLifecycleHooks;

import java.util.ArrayList;
import java.util.Optional;
import java.util.UUID;

public class Squad {

    public final UUID uuid = UUID.randomUUID();

    public  Optional<UUID> leaderUUID = Optional.empty();

    public final ArrayList<LivingEntity> squadMembers = new ArrayList<>();

    private final int MAX_SQUAD_SIZE = 10;

    public boolean toBeRemoved = false;

    public Squad(LivingEntity leader) {
        squadMembers.add(leader);
        leaderUUID = Optional.of((leader).getUUID());
    }

    public void serverTick()
    {
        if(isAllMembersDead())
        {
            toBeRemoved = true;
        }

        // If leader is dead, promote mob with most max health
        if(isLeaderDead())
        {
            Optional<LivingEntity> livingEntity = getMemberWithMostMaxHealth();
            if(livingEntity.isEmpty())
            {
                toBeRemoved = true;
                return;
            }
            promoteToLeaderOfSquad(livingEntity.get());
        }
    }

    public ServerLevel getLevel()
    {
        if(getLeader().isEmpty())
        {
            return ServerLifecycleHooks.getCurrentServer().overworld();
        }
        return (ServerLevel) getLeader().get().level();
    }

    public Optional<LivingEntity> getLeader()
    {
        if(leaderUUID.isEmpty())
        {
            return Optional.empty();
        }

        return getMember(leaderUUID.get());
    }

    public Optional<LivingEntity> getMember(UUID uuid)
    {
        Optional<LivingEntity> result = Optional.empty();

        for(LivingEntity e : squadMembers)
        {
            if(e.getUUID().equals(uuid))
            {
                result = Optional.of(e);
                break;
            }
        }

        return result;
    }

    public boolean isLeader(UUID comparisonMob) {
        if(leaderUUID.isEmpty())
        {
            return false;
        }

        return leaderUUID.get().equals(comparisonMob);
    }

    public boolean isMember(UUID uuid) {
        return getMember(uuid).isPresent();
    }

    public boolean isLeaderDead()
    {
        if(leaderUUID.isEmpty())
        {
            return true;
        }

        Optional<LivingEntity> leader = getLeader();

        return leader.isPresent() && leader.get().isDeadOrDying();
    }


    public boolean canJoinSquad() {
        return squadMembers.size() < MAX_SQUAD_SIZE;
    }


    public boolean isAllMembersDead()
    {
        removeDeadMembers();
        if(leaderUUID.isEmpty() && squadMembers.isEmpty())
        {
            return true;
        }
        return false;
    }

    public void removeDeadMembers()
    {
        if(isLeaderDead())
        {
            leaderUUID = Optional.empty();
        }

        for(int i = 0; i < squadMembers.size(); i++)
        {
            LivingEntity member = squadMembers.get(i);
            if(member.isAlive())
            {
                continue;
            }

            squadMembers.remove(i);
            i--;
        }
    }

    public boolean attemptAddMember(LivingEntity entity) {
        if (canJoinSquad()) {
            squadMembers.add(entity);
            return true;
        }
        return false;
    }



    public void forceAcceptMemberIntoSquad(LivingEntity entity)
    {
        squadMembers.add(entity);
    }

    public void removeMember(UUID entityUUID)
    {
        for(int index = 0; index < squadMembers.size(); index++)
        {
            if(squadMembers.get(index).getUUID().equals(entityUUID))
            {
                squadMembers.remove(index);
                break;
            }
        }
    }


    public void disband() {
        SquadSystem.disbandSquad(uuid);
    }

    public Optional<LivingEntity> getMemberWithMostMaxHealth()
    {
        if(isAllMembersDead())
        {
            return Optional.empty();
        }

        Optional<LivingEntity> result = Optional.empty();

        double squadLeaderMaxHealth = 0;

        for (LivingEntity squadMember : squadMembers) {
            LivingEntity compareEntity = squadMember;
            if (compareEntity.getMaxHealth() > squadLeaderMaxHealth) {
                result = Optional.of(squadMember);
            }
        }
        return result;
    }

    public void promoteToLeaderOfSquad(LivingEntity entity)
    {
        if(!isMember(entity.getUUID()))
        {
            forceAcceptMemberIntoSquad(entity);
        }

        leaderUUID = Optional.of(entity.getUUID());

        //TODO Add leader goal to entity.
    }

    public LivingEntity getSquadTarget()
    {
        if(isAllMembersDead() || getLeader().isEmpty())
        {
            return null;
        }

        if(getLeader().get() instanceof Mob mob)
        {
            return mob.getTarget();
        }
        return null;
    }
}
