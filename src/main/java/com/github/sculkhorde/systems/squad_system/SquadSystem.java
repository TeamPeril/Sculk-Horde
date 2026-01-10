package com.github.sculkhorde.systems.squad_system;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.LivingEntity;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SquadSystem {

    // Map of squad UUID -> Squad
    private final Map<UUID, Squad> SQUADS = new ConcurrentHashMap<>();

    public SquadSystem()
    {

    }

    // Create a squad with the given leader, return the squad's UUID
    public static UUID createSquad(LivingEntity leader) {
        Squad squad = new Squad(leader);
        SculkHorde.squadSystem.SQUADS.put(squad.uuid, squad);
        return squad.uuid;
    }

    // Disband the squad with the given UUID if it exists
    public static void disbandSquad(UUID squadId) {
        Squad squad = SculkHorde.squadSystem.SQUADS.remove(squadId);
        if (squad != null) {
            squad.disband();
        }
    }

    public static Optional<Squad> getSquadOfLivingEntity(LivingEntity entity)
    {
        Optional<Squad> result = Optional.empty();

        Optional<UUID> squadUUID = SquadSystem.getSquadIdForMember(entity);

        if(squadUUID.isPresent())
        {
            return SquadSystem.getSquad(squadUUID.get());
        }

        return result;
    }

    // Try to add a member to the squad. Returns true if successful.
    public static boolean addMember(UUID squadId, LivingEntity member) {
        Squad squad = SculkHorde.squadSystem.SQUADS.get(squadId);
        if (squad == null) return false;
        return squad.attemptAddMember(member);
    }

    // Force add a member (bypass limits)
    public static boolean forceAddMember(UUID squadId, LivingEntity member) {
        Squad squad = SculkHorde.squadSystem.SQUADS.get(squadId);
        if (squad == null) return false;
        squad.forceAcceptMemberIntoSquad(member);
        return true;
    }

    // Remove a member by entity UUID from the squad. Returns true if removed.
    public static boolean removeMember(UUID squadId, UUID memberUUID) {
        Squad squad = SculkHorde.squadSystem.SQUADS.get(squadId);
        if (squad == null) return false;
        squad.removeMember(memberUUID);
        return true;
    }

    // Get a squad by its UUID
    public static Optional<Squad> getSquad(UUID squadId) {
        return Optional.ofNullable(SculkHorde.squadSystem.SQUADS.get(squadId));
    }

    // Find the squad UUID that contains the given member (if any)
    public static Optional<UUID> getSquadIdForMember(LivingEntity member) {
        for (Map.Entry<UUID, Squad> e : SculkHorde.squadSystem.SQUADS.entrySet()) {
            Squad s = e.getValue();
            if (s.squadMembers.contains(member)) {
                return Optional.of(e.getKey());
            }
        }
        return Optional.empty();
    }

    // Return an unmodifiable view of current squads
    public static Map<UUID, Squad> getAllSquads() {
        return Collections.unmodifiableMap(SculkHorde.squadSystem.SQUADS);
    }

    // Return an unmodifiable view of current squads
    public static Collection<Squad> getAllSquadsAsList() {
        return getAllSquads().values();
    }

    public static Optional<Squad> getSquadNearPos(BlockPos pos)
    {
        Optional<Squad> result = Optional.empty();

        for(Squad squad : getAllSquadsAsList())
        {
            if(squad.isAllMembersDead() || squad.getLeader().isEmpty())
            {
                continue;
            }

            if(BlockAlgorithms.getBlockDistance(pos, squad.getLeader().get().blockPosition()) > 20)
            {
                continue;
            }

            result = Optional.of(squad);
        }

        return result;
    }

    // Instance method called each server tick to update squads
    public void serverTick() {
        // Iterate over a snapshot to avoid concurrent-modification surprises
        List<UUID> toRemove = new ArrayList<>();
        for (Map.Entry<UUID, Squad> entry : SQUADS.entrySet()) {
            UUID id = entry.getKey();
            Squad squad = entry.getValue();

            // Clean up dead members and check if squad is empty
            squad.removeDeadMembers();
            if (squad.toBeRemoved) {
                toRemove.add(id);
                continue;
            }
            squad.serverTick();
        }

        // Remove disbanded/empty squads from the map
        for (UUID id : toRemove) {
            SQUADS.remove(id);
        }
    }
}
