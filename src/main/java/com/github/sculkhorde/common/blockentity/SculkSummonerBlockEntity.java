package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.core.TileEntityRegistry;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import com.github.sculkhorde.util.TargetParameters;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.AABB;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class SculkSummonerBlockEntity extends BlockEntity
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
    //Records the last time this block summoned a mob
    private long lastTimeOfSummon = 0;
    private final int MAX_SPAWNED_ENTITIES = 4;
    ReinforcementRequest request;
    private TargetParameters hostileTargetParameters = new TargetParameters().enableTargetHostiles().enableTargetInfected();
    private TargetParameters infectableTargetParameters = new TargetParameters().enableTargetPassives();

    /**
     * The Constructor that takes in properties
     */
    public SculkSummonerBlockEntity(BlockPos blockPos, BlockState blockState)
    {
        super(TileEntityRegistry.SCULK_SUMMONER_TILE.get(), blockPos, blockState);
        searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

    }

    /** ~~~~~~~~ Accessors ~~~~~~~~ **/


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
        if(SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0) { return; }

        blockEntity.timeElapsedSinceTick = TimeUnit.SECONDS.convert(System.nanoTime() - blockEntity.lastTimeOfTick, TimeUnit.NANOSECONDS);

        //If ( (currently alert AND enough time has passed) OR (NOT currently alert and enough time has passed) ) AND the cool down for summoning is done
        if(blockEntity.isCurrentlyAlert() && blockEntity.timeElapsedSinceTick < blockEntity.tickIntervalAlertSeconds) { return; }
        if(!blockEntity.isCurrentlyAlert() && blockEntity.timeElapsedSinceTick < blockEntity.tickIntervalUnAlertSeconds) { return; }

        blockEntity.lastTimeOfTick = System.nanoTime();

        if(blockEntity.behavior_state == blockEntity.STATE_COOLDOWN)
        {
            blockEntity.behavior_state = blockEntity.STATE_READY_TO_SPAWN;
        }
        if(blockEntity.behavior_state == blockEntity.STATE_READY_TO_SPAWN)
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
        if(blockEntity.behavior_state == blockEntity.STATE_SPAWNING)
        {
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
        ArrayList<BlockPos> listOfPossibleSpawns = BlockAlgorithms.getBlocksInCube(worldIn, origin, VALID_SPAWN_BLOCKS, length);
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
}
