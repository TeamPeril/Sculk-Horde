package com.github.sculkhorde.util;

import com.github.sculkhorde.common.entity.ISculkSmartEntity;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.ArrayList;
import java.util.Optional;

public class SquadHandler {

    private ISculkSmartEntity sculkSmartEntity;
    public  Optional<ISculkSmartEntity> squadLeader = Optional.empty();

    public final ArrayList<ISculkSmartEntity> squadMembers = new ArrayList<>();

    private final int squadSize = 5;

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
        return squad.isSquadLeaderDead();
    }

    public boolean isSquadLeader() {
        if(squadLeader.isEmpty())
        {
            return false;
        }

        return ((Mob) squadLeader.get()).getUUID().equals(getMob().getUUID());
    }

    public boolean isSquadMember() {
        return squadMembers.contains(getMob());
    }

    public boolean isSquadJoinable() {
        return squadMembers.size() < squadSize;
    }

    public boolean ableToJoinASquad() {
        return !doesSquadExist(this);
    }

    public boolean canJoinMobSquad(ISculkSmartEntity entity) {
        return entity.getSquad().isSquadLeader() && entity.getSquad().isSquadJoinable() && ableToJoinASquad();
    }

    public boolean isSquadLeaderDead()
    {
        return !squadLeader.isPresent() || ((Mob) squadLeader.get()).isDeadOrDying();
    }

    public boolean tryToAcceptMemberIntoSquad(ISculkSmartEntity joiningMob) {
        if (isSquadJoinable()) {
            squadMembers.add(joiningMob);
            joiningMob.getSquad().squadLeader = Optional.ofNullable(sculkSmartEntity);
            return true;
        }
        return false;
    }

    public void createSquad() {
        squadLeader = Optional.ofNullable(sculkSmartEntity);
        MobEffectInstance effect = new MobEffectInstance(MobEffects.GLOWING, 1000000, 0, false, false);
        getMob().addEffect(effect);
    }

    public void disbandSquad() {
        for (ISculkSmartEntity member : squadMembers) {
            member.getSquad().squadLeader = Optional.empty();
        }
        squadMembers.clear();
        squadLeader = Optional.empty();
    }

    public LivingEntity getSquadTarget()
    {
        return squadLeader.map(iSculkSmartEntity -> ((Mob) iSculkSmartEntity).getTarget()).orElse(null);
    }
}
