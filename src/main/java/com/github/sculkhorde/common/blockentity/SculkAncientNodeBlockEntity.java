package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.advancement.SculkHordeStartTrigger;
import com.github.sculkhorde.common.block.SculkAncientNodeBlock;
import com.github.sculkhorde.common.entity.SculkPhantomEntity;
import com.github.sculkhorde.common.entity.SculkSporeSpewerEntity;
import com.github.sculkhorde.common.entity.infection.AncientNodePurificationHandler;
import com.github.sculkhorde.common.entity.infection.SculkNodeInfectionHandler;
import com.github.sculkhorde.core.*;
import com.github.sculkhorde.core.gravemind.events.Event;
import com.github.sculkhorde.core.gravemind.events.SpawnPhantomsEvent;
import com.github.sculkhorde.util.*;
import com.github.sculkhorde.util.ChunkLoading.BlockEntityChunkLoaderHelper;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;

/**
 * Chunkloader code created by SuperMartijn642
 */
public class SculkAncientNodeBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem
{


    private long tickedAt = System.nanoTime();
    public static final int tickIntervalSeconds = 5;
    private long heartBeatDelayMillis = TimeUnit.SECONDS.toMillis(5);
    private long lastHeartBeat = System.currentTimeMillis();

    private SculkNodeInfectionHandler infectionHandler;
    private AncientNodePurificationHandler purificationHandler;

    // Vibration Code
    private final VibrationSystem.User vibrationUser = new SculkAncientNodeBlockEntity.VibrationUser(this);
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    public SculkAncientNodeBlockEntity(BlockPos blockPos, BlockState blockState) {
        super(ModBlockEntities.SCULK_ANCIENT_NODE_BLOCK_ENTITY.get(), blockPos, blockState);
    }



    /** Getters **/


    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
    {
        return BlockAlgorithms.isSolid(worldIn, pos.below()) &&
                worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER) &&
                worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER) &&
                worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

    }

    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Currently determines if a block is a valid flower.
     */
    private final Predicate<BlockPos> VALID_SPAWN_BLOCKS = (blockPos) ->
    {
        return isValidSpawnPosition((ServerLevel) this.level, blockPos) ;
    };

    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param predicateIn The predicate that determines if a block is the one were searching for
     * @param pDistance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, Predicate<BlockPos> predicateIn, double pDistance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        //Search area for block
        for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
        {
            for(int j = 0; (double)j < pDistance; ++j)
            {
                for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
                {
                    for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
                    {
                        //blockpos$mutable.setWithOffset(origin, k, i - 1, l);
                        BlockPos temp = new BlockPos(origin.getX() + k, origin.getY() + i-1, origin.getZ() + l);

                        //If the block is close enough and is the right blockstate
                        if (origin.closerThan(temp, pDistance)
                                && predicateIn.test(temp))
                        {
                            list.add(temp); //add position
                        }
                    }
                }
            }
        }
        //else return empty
        return list;
    }

    /**
     * Gets a list of all possible spawns, chooses a specified amount of them.
     * @param worldIn The World
     * @param origin The Origin Position
     * @param amountOfPositions The amount of positions to get
     * @return A list of the spawn positions
     */
    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int amountOfPositions)
    {
        int RADIUS = 20;

        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, VALID_SPAWN_BLOCKS, RADIUS);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && listOfPossibleSpawns.size() > 0; count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            finalList.add(listOfPossibleSpawns.get(randomIndex));
            listOfPossibleSpawns.remove(randomIndex);
        }
        return finalList;
    }

    /** Setters **/


    /** Events **/

    private void initializeInfectionHandler()
    {
        if(infectionHandler == null)
        {
            infectionHandler = new SculkNodeInfectionHandler(this, getBlockPos());
        }
    }
    private void initializePurificationHandler()
    {
        if(purificationHandler == null)
        {
            purificationHandler = new AncientNodePurificationHandler(this, getBlockPos());
        }
    }

    private static void addDarknessEffectToNearbyPlayers(Level level, BlockPos blockPos, int distance)
    {
        level.players().forEach((player) -> {
            if(player.blockPosition().closerThan(blockPos, distance) && !player.isCreative() && !player.isInvulnerable() && !player.isSpectator() && !PlayerProfileHandler.isPlayerVessel(player))
            {
                EntityAlgorithms.applyEffectToTarget(player, MobEffects.DARKNESS, TickUnits.convertMinutesToTicks(1), 0);
            }
        });
    }

    /**
     * Gets called on the client to do heartbeat sounds
     * @param level The level
     * @param blockPos The position
     * @param blockState The blockstate
     * @param blockEntity The block entity
     */
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {
        if(level.isClientSide())
        {
            tickClient(level, blockPos, blockState, blockEntity);
        }
        else if(SculkHorde.savedData.isHordeUnactivated() && ModConfig.SERVER.trigger_ancient_node_automatically.get())
        {
            tickTriggerAutomatically(level, blockPos, blockState, blockEntity);
        }
        else if(SculkHorde.savedData.isHordeDefeated())
        {
            tickDefeated(level, blockPos, blockState, blockEntity);
        }
        else if(SculkHorde.savedData.isHordeUnactivated() && !ModConfig.SERVER.trigger_ancient_node_automatically.get())
        {
            tickUnactivated(level, blockPos, blockState, blockEntity);
        }
        else if(SculkHorde.savedData.isHordeActive())
        {
            tickActive(level, blockPos, blockState, blockEntity);
        }

    }

    /**
     * Gets called on the client to do heartbeat sounds
     * @param level The level
     * @param blockPos The position
     * @param blockState The blockstate
     * @param blockEntity The block entity
     */
    public static void tickClient(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {
        if(System.currentTimeMillis() - blockEntity.lastHeartBeat > blockEntity.heartBeatDelayMillis && !blockState.getValue(SculkAncientNodeBlock.DEFEATED))
        {
            blockEntity.lastHeartBeat = System.currentTimeMillis();
            level.playLocalSound(blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.WARDEN_HEARTBEAT, SoundSource.BLOCKS, 5.0F, 1.0F, false);
        }
    }

    /**
     * Gets called on the client to do heartbeat sounds
     * @param level The level
     * @param blockPos The position
     * @param blockState The blockstate
     * @param blockEntity The block entity
     */
    public static void tickUnactivated(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {
        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();

        if(areAnyPlayersInRange((ServerLevel) level, blockPos, 15))
        {
            tryInitializeHorde(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
        }
    }

    /**
     * Gets called on server when the block is awake
     * @param level The level
     * @param blockPos The position
     * @param blockState The blockstate
     * @param blockEntity The block entity
     */
    public static void tickDefeated(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        // Initialize the infection handler
        if(blockEntity.purificationHandler == null)
        {
            blockEntity.initializePurificationHandler();
        }
        if(blockEntity.purificationHandler.canBeActivated())
        {
            blockEntity.purificationHandler.activate();
        }

        blockEntity.purificationHandler.tick();

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();
    }

    /**
     * Gets called on server when the block is awake
     * @param level The level
     * @param blockPos The position
     * @param blockState The blockstate
     * @param blockEntity The block entity
     */
    public static void tickActive(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {

        long timeElapsed = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.tickedAt, TimeUnit.NANOSECONDS);

        if(!SculkHorde.eventHandler.doesEventExist("ancient_node_spawn_phantom".hashCode()))
        {
            Event phantomEvent = SpawnPhantomsEvent.createEvent(level.dimension());
            phantomEvent.setEventID("ancient_node_spawn_phantom".hashCode());
            phantomEvent.setEventLocation(blockPos);
            phantomEvent.setEventReocurring(true);
            phantomEvent.setEXECUTION_COOLDOWN(TickUnits.convertHoursToTicks(1));
            SculkHorde.eventHandler.addEvent(phantomEvent);
        }


        // Initialize the infection handler
        if(blockEntity.infectionHandler == null)
        {
            blockEntity.initializeInfectionHandler();
        }
        if(blockEntity.infectionHandler.canBeActivated())
        {
            blockEntity.infectionHandler.activate();
        }

        blockEntity.infectionHandler.tick();

        // If the time elapsed is less than the tick interval, return
        if(timeElapsed < tickIntervalSeconds) { return; }

        AdvancementUtil.giveAdvancementToAllPlayers((ServerLevel) level, SculkHordeStartTrigger.INSTANCE);

        // Update the tickedAt time
        blockEntity.tickedAt = System.nanoTime();

        addDarknessEffectToNearbyPlayers(level, blockPos, 25);

        BlockEntityChunkLoaderHelper.getChunkLoaderHelper().createChunkLoadRequestSquare((ServerLevel) level, blockPos, ModConfig.SERVER.sculk_node_chunkload_radius.get(), 1, TickUnits.convertMinutesToTicks(30));
    }

    public static void tickTriggerAutomatically(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {
        // Should never get here but for safety's sake double verify
        if (!ModConfig.SERVER.trigger_ancient_node_automatically.get()) { return; }

        // Check elapsed days
        if (level.getDayTime() < (ModConfig.SERVER.trigger_ancient_node_wait_days.get() * 24000)) { return; }

        // Check time of current day
        if ((level.getDayTime() % 24000) < (long)(ModConfig.SERVER.trigger_ancient_node_time_of_day.get())) { return; }

        tryInitializeHorde(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
    }

    private static boolean areAnyPlayersInRange(ServerLevel level, BlockPos blockPos, int range)
    {
        return level.players().stream().anyMatch((player) ->
                player.blockPosition().closerThan(blockPos, range)
                        && !player.isCreative() && !player.isSpectator() && !player.isInvulnerable()
                );
    }

    private static void spawnSculkPhantomsAtTopOfWorld(SculkAncientNodeBlockEntity blockEntity, int amount)
    {
        ServerLevel level = (ServerLevel) blockEntity.level;
        int spawnRange = 100;
        int minimumSpawnRange = 50;
        Random rng = new Random();
        for(int i = 0; i < amount; i++)
        {
            int x = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int z = minimumSpawnRange + rng.nextInt(spawnRange) - (spawnRange/2);
            int y = level.getMaxBuildHeight();
            BlockPos spawnPosition = new BlockPos(blockEntity.getBlockPos().getX() + x, y, blockEntity.getBlockPos().getZ() + z);

            SculkPhantomEntity.spawnPhantom(level, spawnPosition, true);

        }
    }

    public static void announceToAllPlayers(ServerLevel level, Component message)
    {
        level.players().forEach((player) -> player.displayClientMessage(message, false));
    }

    public static void tryInitializeHorde(Level level, BlockPos blockPos, BlockState blockState, SculkAncientNodeBlockEntity blockEntity)
    {
        if(!SculkHorde.savedData.isHordeUnactivated()) { return; }

        int MAX_SPAWNED_SPORE_SPEWERS = 10;

        // If the horde has no mass, give it some
        if(SculkHorde.savedData.getSculkAccumulatedMass() <= 0)
        {
            SculkHorde.savedData.addSculkAccumulatedMass(1000);
            SculkHorde.statisticsData.addTotalMassFromNodes(1000);
        }

        SculkHorde.savedData.setHordeState(ModSavedData.HordeState.ACTIVE);

        announceToAllPlayers((ServerLevel)level, Component.literal("The Sculk Horde has been awakened!"));

        ArrayList<BlockPos> possibleSpawnPositions = blockEntity.getSpawnPositionsInCube((ServerLevel) level, blockPos, MAX_SPAWNED_SPORE_SPEWERS);

        BlockPos[] finalizedSpawnPositions = new BlockPos[MAX_SPAWNED_SPORE_SPEWERS];

        //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
        for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
        {
            finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
        }

        //If the array is empty, just spawn above block
        if (possibleSpawnPositions.isEmpty()) {
            finalizedSpawnPositions[0] = blockPos.above();
        }

        //Spawn the entities
        for (BlockPos pos : finalizedSpawnPositions)
        {
            if(pos == null) { continue; }

            //Spawn the entity
            SculkSporeSpewerEntity sporeSpewerEntity = new SculkSporeSpewerEntity(level);
            sporeSpewerEntity.setPos(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5);
            level.addFreshEntity(sporeSpewerEntity);
        }

        level.players().forEach((player) -> level.playSound(null, player.blockPosition(), ModSounds.HORDE_START_SOUND.get(), SoundSource.AMBIENT, 1.0F, 1.0F));

        spawnSculkPhantomsAtTopOfWorld(blockEntity, 10);
    }

    // Data

    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("listener", 10)) {
            VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((data) -> {
                this.vibrationData = data;
            });
        }

    }

    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((p_222871_) -> {
            nbt.put("listener", p_222871_);
        });
    }

    // Vibration System
    /** ~~~~~~~~ Vibration Events ~~~~~~~~  **/
    public VibrationSystem.Listener getListener() {
        return this.vibrationListener;
    }

    public VibrationSystem.Data getVibrationData() {
        return this.vibrationData;
    }

    public VibrationSystem.User getVibrationUser() {
        return this.vibrationUser;
    }

    /**
     * The listener for the block entity.
     */
    class VibrationUser implements VibrationSystem.User
    {
        private static final int LISTENER_RADIUS = 24;
        private final PositionSource positionSource = new BlockPositionSource(SculkAncientNodeBlockEntity.this.worldPosition);
        private SculkAncientNodeBlockEntity blockEntity;

        public VibrationUser(SculkAncientNodeBlockEntity ancientNodeBlockEntity) {
            this.blockEntity = ancientNodeBlockEntity;
        }

        public int getListenerRadius() {
            return LISTENER_RADIUS;
        }

        public PositionSource getPositionSource() {
            return this.positionSource;
        }

        public TagKey<GameEvent> getListenableEvents() {
            return GameEventTags.SHRIEKER_CAN_LISTEN;
        }

        public boolean canReceiveVibration(ServerLevel level, BlockPos blockPos, GameEvent gameEvent, GameEvent.Context context) {
            return true;
        }

        public void onReceiveVibration(ServerLevel level, BlockPos sourcePosition, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity1, float power)
        {
            if(areAnyPlayersInRange(level, blockEntity.getBlockPos(), 20))
            {
                tryInitializeHorde(level, blockEntity.getBlockPos(), blockEntity.getBlockState(), blockEntity);
            }
        }

        public void onDataChanged()
        {
            setChanged();
        }

        public boolean requiresAdjacentChunksToBeTicking() {
            return false;
        }
    }

}
