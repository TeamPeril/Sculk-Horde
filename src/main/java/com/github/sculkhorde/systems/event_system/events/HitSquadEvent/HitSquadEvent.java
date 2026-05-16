package com.github.sculkhorde.systems.event_system.events.HitSquadEvent;

import com.github.sculkhorde.common.entity.boss.angel_of_reaping.AngelOfReapingEntity;
import com.github.sculkhorde.core.ModConfig;
import com.github.sculkhorde.core.ModSavedData;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.debugger_system.DebuggerSystem;
import com.github.sculkhorde.systems.event_system.Event;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.ChunkLoading.EntityChunkLoaderHelper;
import com.github.sculkhorde.util.NodeUtil;
import com.github.sculkhorde.util.PlayerProfileHandler;
import com.github.sculkhorde.util.TickUnits;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.Optional;
import java.util.UUID;
import java.util.function.Predicate;

public class HitSquadEvent extends Event {
    protected final int MAX_DISTANCE_FROM_PLAYER = 150;

    protected UUID target;
    protected AngelOfReapingEntity reaper;

    protected static final String targetUUIDIdentifier = "targetUUID";

    protected enum State {
        INITIALIZATION,
        PURSUIT,
        ENGAGING,
        SUCCESS,
        FAILURE
    }

    protected State state;
    protected boolean isEventOver = false;

    protected Optional<HitSquadSpawnFinder> spawnFinder = Optional.empty();

    protected Optional<BlockPos> desiredSpawnPos = Optional.empty();

    public HitSquadEvent(ResourceKey<Level> dimension, UUID target) {
        this(dimension);
        this.target = target;
    }

    public HitSquadEvent(ResourceKey<Level> dimension) {
        super(dimension);
        setEventCost(100);
        setState(State.INITIALIZATION);
        setMinimumDifficulty(Difficulty.NORMAL);
    }

    public Optional<AngelOfReapingEntity> getReaper()
    {
        return Optional.ofNullable(reaper);
    }

    @Override
    public boolean canStart() {

        if(!ModConfig.SERVER.hit_squad_event_enabled.get())
        {
            return false;
        }

        return super.canStart();
    }

    public boolean canContinue()
    {
        return !isEventOver;
    }

    @Override
    public void serverTick() {

        if(state == State.INITIALIZATION)
        {
            initializationTick();
        }
        else if(state == State.PURSUIT)
        {
            pursuitTick();
        }
        else if(state == State.ENGAGING)
        {
            engagingTick();
        }
        else if(state == State.SUCCESS)
        {
            successTick();
        }
        else if(state == State.FAILURE)
        {
            failureTick();
        }

    }

    protected void setState(State state)
    {
        this.state = state;
        DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | " + "State: " + state.toString());
    }


    public Optional<BlockPos> findValidSpawnPosition(int cubeLength) {
        // Calculate the bounds of the cube
        int halfLength = cubeLength / 2;
        int minX = getEventLocation().getX() - halfLength;
        int minY = getEventLocation().getY() - halfLength;
        int minZ = getEventLocation().getZ() - halfLength;
        int maxX = getEventLocation().getX() + halfLength;
        int maxY = getEventLocation().getY() + halfLength;
        int maxZ = getEventLocation().getZ() + halfLength;

        // Create a mutable block position to avoid creating new objects in the loop
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();

        // Iterate through each block position in the cube
        for (int x = minX; x <= maxX; x++)
        {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutablePos.set(x, y, z);

                    // Check if the block is air and not water
                    if (BlockAlgorithms.isReplaceable(getDimension().getBlockState(mutablePos)) && !BlockAlgorithms.isFluid(getDimension(), mutablePos)) {
                        // Check if the position is a valid spawn position
                        if (isValidSpawnPos(mutablePos)) {
                            return Optional.of(mutablePos.immutable());
                        }
                    }
                }
            }
        }

        // If no valid spawn position is found, return an empty Optional
        return Optional.empty();
    }

    public boolean isValidSpawnPos(BlockPos.MutableBlockPos pos)
    {
        if(pos == null) { return false; }

        if(!BlockAlgorithms.isReplaceable(getDimension().getBlockState(pos)))
        {
            return false;
        }

        pos.move(Direction.UP);

        if(!BlockAlgorithms.isReplaceable(getDimension().getBlockState(pos)))
        {
            pos.move(Direction.DOWN);
            return false;
        }

        pos.move(Direction.DOWN);

        return true;
    };

    public final Predicate<BlockPos> isObstructed = (blockPos) ->
    {
        boolean isBlockAir = getDimension().getBlockState(blockPos).is(Blocks.AIR);
        boolean isBlockNotExposedToAir = !BlockAlgorithms.isExposedToAir(getDimension(), blockPos);

        return isBlockAir || isBlockNotExposedToAir;
    };


    protected void initializationTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }

        Player player = getPlayerIfOnline().get();
        Optional<ModSavedData.NodeEntry> entry = NodeUtil.getClosestNode((ServerLevel) player.level(), player.blockPosition());

        if(entry.isEmpty() || !entry.get().isEntryValid()) {
            SculkHorde.LOGGER.error("HitSquadEvent | Error: Could not initialize, no valid nodes nearby.");
            setState(State.FAILURE);
            return;
        }

        setEventLocation(entry.get().getPosition());

        Optional<BlockPos> potentialSpawnPoint = Optional.empty();

        for(int checkLength = 10; checkLength <= 50 && potentialSpawnPoint.isEmpty(); checkLength += 10)
        {
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | Checking for Spawn Pos in cube of length " + checkLength);
            potentialSpawnPoint = findValidSpawnPosition(checkLength);
        }

        if(potentialSpawnPoint.isPresent())
        {
            reaper = AngelOfReapingEntity.spawnWithDifficulty(player.level(), potentialSpawnPoint.get().getCenter(), getTargetProfile().getDifficultyOfNextHit(), true);
            reaper.setHitTarget(player);
            reaper.setParentEventUUID(getEventUUID());
            reaper.addEffect(new MobEffectInstance(MobEffects.GLOWING, Integer.MAX_VALUE));
            setState(State.PURSUIT);
            return;
        }


        if(SculkHorde.isDebugMode()) {
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | FAILURE, Could not find good spawn pos.");
        }
        setState(State.FAILURE);

    }

    protected void pursuitTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }
        Player player = getPlayerIfOnline().get();

        if(player.distanceTo(reaper) <= 64)
        {
            setState(State.ENGAGING);
            return;
        }

        if(player.isDeadOrDying())
        {
            setState(State.SUCCESS);
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | EVENT SUCCESS: Player " + player.getScoreboardName() + " died.");
            return;
        }

        if(reaper.isDeadOrDying())
        {
            setState(State.FAILURE);
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " killed the Soul Reaper.");
            return;
        }

        if(player.distanceTo(reaper) > MAX_DISTANCE_FROM_PLAYER + 50)
        {
            setState(State.FAILURE);
            PlayerProfileHandler.getOrCreatePlayerProfile(player).setTimeOfLastHit(0);
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " moved too far away from Soul Reaper.");
            return;
        }

        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(reaper,3, 3, TickUnits.convertMinutesToTicks(1));

    }

    protected void engagingTick()
    {
        if(getPlayerIfOnline().isEmpty())
        {
            return;
        }
        Player player = getPlayerIfOnline().get();

        if(player.distanceTo(reaper) > 70)
        {
            setState(State.PURSUIT);
            return;
        }

        if(player.isDeadOrDying())
        {
            setState(State.SUCCESS);
            return;
        }

        if(reaper.isDeadOrDying())
        {
            setState(State.FAILURE);
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " killed the Soul Reaper.");
            return;
        }

        if(player.distanceTo(reaper) > MAX_DISTANCE_FROM_PLAYER + 50)
        {
            setState(State.FAILURE);
            PlayerProfileHandler.getOrCreatePlayerProfile(player).setTimeOfLastHit(0);
            DebuggerSystem.eventDebuggerModule.logInfo("HitSquadEvent | EVENT FAILURE: Player " + player.getScoreboardName() + " moved too far away from Soul Reaper.");
            reaper.discard();
            return;
        }

        EntityChunkLoaderHelper.getEntityChunkLoaderHelper().createChunkLoadRequestSquareForEntityIfAbsent(reaper,3, 3, TickUnits.convertMinutesToTicks(1));

    }

    protected void successTick()
    {
        getTargetProfile().decreaseDifficultyOfNextHit();
        isEventOver = true;
    }

    protected void failureTick()
    {
        getTargetProfile().increaseDifficultyOfNextHit();
        isEventOver = true;
    }

    public ModSavedData.PlayerProfileEntry getTargetProfile()
    {
        return PlayerProfileHandler.getOrCreatePlayerProfile(target);
    }

    public Optional<Player> getPlayerIfOnline()
    {
        return getTargetProfile().getPlayer();
    }

    @Override
    public void loadAdditional(CompoundTag tag) {
        if(tag.contains(targetUUIDIdentifier))
        {
            target = tag.getUUID(targetUUIDIdentifier);
        }
    }

    @Override
    public void saveAdditional(CompoundTag tag) {
        tag.putUUID(targetUUIDIdentifier, target);
    }
}
