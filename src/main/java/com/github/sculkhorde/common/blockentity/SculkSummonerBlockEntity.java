package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.block.SculkSummonerBlock;
import com.github.sculkhorde.core.BlockEntityRegistry;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TargetParameters;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.warden.Warden;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BeehiveBlock;
import net.minecraft.world.level.block.SculkShriekerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import software.bernie.geckolib.animatable.GeoBlockEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.core.animation.AnimationController;
import software.bernie.geckolib.core.animation.RawAnimation;
import software.bernie.geckolib.util.GeckoLibUtil;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class SculkSummonerBlockEntity extends BlockEntity implements VibrationListener.VibrationListenerConfig, GeoBlockEntity
{
    private int behavior_state = 0;
    private final int STATE_COOLDOWN = 0;
    private final int STATE_READY_TO_SPAWN = 1;
    private final int STATE_SPAWNING = 2;
    AABB searchArea;
    //ACTIVATION_DISTANCE - The distance at which this is able to detect mobs.
    private final int ACTIVATION_DISTANCE = 32;
    //possibleLivingEntityTargets - A list of nearby targets which are worth infecting.
    private List<LivingEntity> possibleLivingEntityTargets;
    //possibleAggressorTargets - A list of nearby targets which should be considered hostile.
    private List<LivingEntity> possibleAggressorTargets;
    //Used to track the last time this tile was ticked
    private long lastTimeOfTick = System.nanoTime();
    private long timeElapsedSinceTick = 0;
    private long lastTimeOfAlert = 0;
    private int alertPeriodSeconds = 60;
    //How often should this tile tick in seconds if the summoner is alert
    private long tickIntervalAlertSeconds = 30;
    //How often should this tile tick in seconds if the summoner is not alert
    private long tickIntervalUnAlertSeconds = 60;
    private final int MAX_SPAWNED_ENTITIES = 4;
    ReinforcementRequest request;
    private TargetParameters hostileTargetParameters = new TargetParameters().enableTargetHostiles().enableTargetInfected();
    private TargetParameters infectableTargetParameters = new TargetParameters().enableTargetPassives();

    private VibrationListener listener = new VibrationListener(new BlockPositionSource(this.worldPosition), 8, this);

    /**
     * The Constructor that takes in properties
     */
    public SculkSummonerBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(BlockEntityRegistry.SCULK_SUMMONER_BLOCK_ENTITY.get(), blockPos, blockState);
        searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

        if(blockState.getValue(SculkSummonerBlock.STATE) == 0)
        {
            behavior_state = STATE_COOLDOWN;
        }
        else
        {
            behavior_state = STATE_READY_TO_SPAWN;
        }

    }

    /** ~~~~~~~~ Accessors ~~~~~~~~ **/

    private boolean isOnCoolDown()
    {
        return (behavior_state == STATE_COOLDOWN);
    }

    private boolean isReadyToSpawn()
    {
        return (behavior_state == STATE_READY_TO_SPAWN);
    }

    private boolean isSpawning()
    {
        return (behavior_state == STATE_SPAWNING);
    }

    public static int getState(BlockState blockState) {
        return blockState.getValue(SculkSummonerBlock.STATE);
    }

    /** ~~~~~~~~ Modifiers ~~~~~~~~  **/


    /** ~~~~~~~~ Boolean ~~~~~~~~  **/

    /**
     * Returns true if the time elapsed since the last alert is less than the length of the alert period.
     * False otherwise
     * @return True/False
     */
    public boolean isCurrentlyAlert()
    {
        return (TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeOfAlert, TimeUnit.NANOSECONDS) <= alertPeriodSeconds);
    }


    /**
     * Check if all entries are alive
     */
    private boolean areAllReinforcementsDead()
    {
        if(request == null)
        {
            return true;
        }

        // iterate through each request, and for each one, iterate through spawnedEntities and check if they are alive
        for( LivingEntity entity : request.spawnedEntities )
        {
            if( entity == null )
            {
                continue;
            }

            if( entity.isAlive() )
            {
                return false;
            }
        }
        return true;
    }


    /** ~~~~~~~~ Events ~~~~~~~~ **/
    public static void spawnReinforcementsTick(Level level, BlockPos blockPos, BlockState blockState, SculkSummonerBlockEntity blockEntity)
    {
        if(level == null || level.isClientSide) { return;}


        blockEntity.timeElapsedSinceTick = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.lastTimeOfTick, TimeUnit.NANOSECONDS);

        //If ( (currently alert AND enough time has passed) OR (NOT currently alert and enough time has passed) ) AND the cool down for summoning is done
        if(blockEntity.isCurrentlyAlert() && blockEntity.timeElapsedSinceTick < blockEntity.tickIntervalAlertSeconds) { return; }
        if(!blockEntity.isCurrentlyAlert() && blockEntity.timeElapsedSinceTick < blockEntity.tickIntervalUnAlertSeconds) { return; }

        blockEntity.lastTimeOfTick = System.nanoTime();

        if(blockEntity.behavior_state == blockEntity.STATE_COOLDOWN)
        {
            blockEntity.behavior_state = blockEntity.STATE_READY_TO_SPAWN;
            level.setBlockAndUpdate(blockPos, blockState.setValue(SculkSummonerBlock.STATE, 1));
        }
        else if(blockEntity.behavior_state == blockEntity.STATE_READY_TO_SPAWN)
        {
            if(!blockEntity.areAllReinforcementsDead()) { return; }

            //Create bounding box to detect targets
            blockEntity.searchArea = EntityAlgorithms.getSearchAreaRectangle(blockPos.getX(), blockPos.getY(), blockPos.getZ(), blockEntity.ACTIVATION_DISTANCE, 5, blockEntity.ACTIVATION_DISTANCE);

            //Get targets inside bounding box.
            blockEntity.possibleAggressorTargets =
                    blockEntity.level.getEntitiesOfClass(
                            LivingEntity.class,
                            blockEntity.searchArea,
                            blockEntity.hostileTargetParameters.isPossibleNewTargetValid);

            //Get targets inside bounding box.
            blockEntity.possibleLivingEntityTargets =
                    blockEntity.level.getEntitiesOfClass(
                            LivingEntity.class,
                            blockEntity.searchArea,
                            blockEntity.infectableTargetParameters.isPossibleNewTargetValid);

            if (blockEntity.possibleAggressorTargets.size() == 0 && blockEntity.possibleLivingEntityTargets.size() == 0) { return; }

            blockEntity.behavior_state = blockEntity.STATE_SPAWNING;
        }
        else if(blockEntity.behavior_state == blockEntity.STATE_SPAWNING)
        {
            ((ServerLevel)level).sendParticles(ParticleTypes.SCULK_SOUL, blockPos.getX() + 0.5D, blockPos.getY() + 1.15D, blockPos.getZ() + 0.5D, 2, 0.2D, 0.0D, 0.2D, 0.0D);
            ((ServerLevel)level).playSound((Player)null, blockPos, SoundEvents.SCULK_CATALYST_BLOOM, SoundSource.BLOCKS, 2.0F, 0.6F + 1.0F);
            //Choose spawn positions
            ArrayList<BlockPos> possibleSpawnPositions = blockEntity.getSpawnPositionsInCube((ServerLevel) level, blockEntity.getBlockPos(), 5, blockEntity.MAX_SPAWNED_ENTITIES);

            BlockPos[] finalizedSpawnPositions = new BlockPos[blockEntity.MAX_SPAWNED_ENTITIES];

            //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
            for (int iterations = 0; iterations < possibleSpawnPositions.size(); iterations++)
            {
                //If the array is empty, just spawn above block
                if (possibleSpawnPositions.isEmpty()) {
                    finalizedSpawnPositions[iterations] = blockEntity.getBlockPos().above();
                }
                //Else choose the spawn position
                else
                {
                    finalizedSpawnPositions[iterations] = possibleSpawnPositions.get(iterations);
                }
            }

            //Give gravemind context to our request to make more informed situations
            blockEntity.request = new ReinforcementRequest(finalizedSpawnPositions);
            blockEntity.request.sender = ReinforcementRequest.senderType.SculkCocoon;

            if (blockEntity.possibleAggressorTargets.size() != 0) {
                blockEntity.request.is_aggressor_nearby = true;
                blockEntity.lastTimeOfAlert = System.nanoTime();
            }
            if (blockEntity.possibleLivingEntityTargets.size() != 0) {
                blockEntity.request.is_non_sculk_mob_nearby = true;
                blockEntity.lastTimeOfAlert = System.nanoTime();
            }

            //If there is some sort of enemy near by, request reinforcement
            if (blockEntity.request.is_non_sculk_mob_nearby || blockEntity.request.is_aggressor_nearby) {
                //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
                SculkHorde.entityFactory.requestReinforcementAny(level, blockPos, false, blockEntity.request);
            }

            blockEntity.behavior_state = blockEntity.STATE_COOLDOWN;
            level.setBlockAndUpdate(blockPos, blockState.setValue(SculkSummonerBlock.STATE, 0));
        }
    }

    /**
     * Gets a list of all possible spawns, chooses a specified amount of them.
     * @param worldIn The World
     * @param origin The Origin Position
     * @param length The Length of the cube
     * @param amountOfPositions The amount of positions to get
     * @return A list of the spawn positions
     */
    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions)
    {
        //TODO Can potentially be optimized by not getting all the possible positions
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, VALID_SPAWN_BLOCKS, length);
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

    /**
     * Represents a predicate (boolean-valued function) of one argument. <br>
     * Currently determines if a block is a valid flower.
     */
    private final Predicate<BlockPos> VALID_SPAWN_BLOCKS = (blockPos) ->
    {
        return isValidSpawnPosition((ServerLevel) this.level, blockPos) ;
    };

    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
    {
        return SculkHorde.infestationConversionTable.infestationTable.isInfectedVariant(worldIn.getBlockState(pos.below()))  &&
            worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER) &&
            worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

    }

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

    // Save & Load Code

    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("listener", 10))
        {
            VibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((p_222864_) -> {
                this.listener = p_222864_;
            });
        }

    }

    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.listener).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((p_222871_) -> {
            nbt.put("listener", p_222871_);
        });
    }

    // Vibration Code

    public TagKey<GameEvent> getListenableEvents() {
        return GameEventTags.VIBRATIONS;
    }

    public void onSignalReceive(ServerLevel level, GameEventListener gameEventListener, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity1, float strenth) {
        this.spawnReinforcementsTick(level, blockPos, getBlockState(), this);
    }
    
    public VibrationListener getListener() {
        return this.listener;
    }

    public boolean shouldListen(ServerLevel p_222856_, GameEventListener p_222857_, BlockPos p_222858_, GameEvent p_222859_, GameEvent.Context p_222860_) {
        return true;
    }

    public void onSignalSchedule() {
        this.setChanged();
    }


    /* ANIMATION */

    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);

    // We statically instantiate our RawAnimations for efficiency, consistency, and error-proofing
    private static final RawAnimation SCULK_SUMMONER_COOLDOWN_ANIMATION = RawAnimation.begin().thenPlayAndHold("cooldown");
    private static final RawAnimation SCULK_SUMMONER_READY_ANIMATION = RawAnimation.begin().thenPlay("powerup").thenLoop("idle");

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>(this, state ->
        {
                BlockState blockState = state.getAnimatable().getLevel().getBlockState(state.getAnimatable().worldPosition);
                if(blockState.is(BlockRegistry.SCULK_SUMMONER_BLOCK.get()))
                {
                    if(state.getAnimatable().getLevel().getBlockState(state.getAnimatable().worldPosition).getValue(SculkSummonerBlock.STATE) == 0)
                    {
                        return state.setAndContinue(SCULK_SUMMONER_COOLDOWN_ANIMATION);
                    }
                    else
                    {
                        return state.setAndContinue(SCULK_SUMMONER_READY_ANIMATION);
                    }
                }
                return state.setAndContinue(SCULK_SUMMONER_READY_ANIMATION);

        }
        ));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return this.cache;
    }
}
