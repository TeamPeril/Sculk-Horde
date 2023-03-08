package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.core.TileEntityRegistry;
import com.github.sculkhorde.core.gravemind.entity_factory.ReinforcementRequest;
import net.minecraft.entity.LivingEntity;
import net.minecraft.fluid.Fluids;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Predicate;


public class SculkSummonerTile extends TileEntity implements ITickableTileEntity
{
    private int behavior_state = 0;
    private final int STATE_COOLDOWN = 0;
    private final int STATE_READY_TO_SPAWN = 1;
    private final int STATE_SPAWNING = 2;
    AxisAlignedBB searchArea;
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
    private long tickIntervalUnAlertSeconds = 60 * 5;
    //Records the last time this block summoned a mob
    private long lastTimeOfSummon = 0;
    private final int MAX_SPAWNED_ENTITIES = 8;
    ReinforcementRequest[] requests = new ReinforcementRequest[MAX_SPAWNED_ENTITIES];

    /**
     * The Constructor that takes in properties
     * @param type The Tile Entity Type
     */
    public SculkSummonerTile(TileEntityType<?> type)
    {
        super(type);
    }

    /**
     * A simpler constructor that does not take in entity type.<br>
     * I made this so that registering tile entities can look cleaner
     */
    public SculkSummonerTile()
    {
        this(TileEntityRegistry.SCULK_SUMMONER_TILE.get());
        //Create bounding box to detect targets
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
     * Find oldest entry and replace it.
     * @param request The new request to input
     */
    private void addReinforcementToList(ReinforcementRequest request)
    {
        if(request == null) {return;}

        int index_oldest = 0;
        for(int i = 0; i < requests.length; i++)
        {
            if(requests[i] == null)
            {
                requests[index_oldest] = request;
                return;
            }
            else if(requests[i].creationTime < requests[index_oldest].creationTime)
            {
                index_oldest = i;
            }
        }

        requests[index_oldest] = request;
    }

    /**
     * Check if all entries are alive
     */
    private boolean areAllReinforcementsDead()
    {
        for(int i = 0; i < requests.length; i++)
        {
            if(requests[i] == null || requests[i].spawnedEntity == null) { continue; }
            else if(requests[i].spawnedEntity.isAlive())
            {
                return false;
            }
        }
        return true;
    }

    /** ~~~~~~~~ Events ~~~~~~~~ **/

    @Override
    public void tick()
    {
        if(this.level == null || this.level.isClientSide) { return;}
        if(SculkHorde.gravemind.getGravemindMemory().getSculkAccumulatedMass() <= 0) { return; }

        timeElapsedSinceTick = TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeOfTick, TimeUnit.NANOSECONDS);

        //If ( (currently alert AND enough time has passed) OR (NOT currently alert and enough time has passed) ) AND the cool down for summoning is done
        if(isCurrentlyAlert() && timeElapsedSinceTick < tickIntervalAlertSeconds) { return; }
        if(!isCurrentlyAlert() && timeElapsedSinceTick < tickIntervalUnAlertSeconds) { return; }

        lastTimeOfTick = System.nanoTime();

        if(behavior_state == STATE_COOLDOWN)
        {
            behavior_state = STATE_READY_TO_SPAWN;
        }
        else if(behavior_state == STATE_READY_TO_SPAWN)
        {
            if(!areAllReinforcementsDead()) { return; }

            //Create bounding box to detect targets
            searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

            //Get targets inside bounding box.
            possibleAggressorTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
            EntityAlgorithms.filterOutNonTargets(possibleAggressorTargets, true, false, true);

            possibleLivingEntityTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
            EntityAlgorithms.filterOutNonTargets(possibleLivingEntityTargets, false, true, false);

            if (possibleAggressorTargets.size() == 0 && possibleLivingEntityTargets.size() == 0) { return; }

            behavior_state = STATE_SPAWNING;
        }
        else if(behavior_state == STATE_SPAWNING)
        {

            //Create MAX_SPAWNED_ENTITIES amount of Reinforcement Requests
            for (int iterations = 0; iterations < MAX_SPAWNED_ENTITIES; iterations++) {

                //Create bounding box to detect targets
                searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

                //Get targets inside bounding box.
                possibleAggressorTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
                EntityAlgorithms.filterOutNonTargets(possibleAggressorTargets, true, false, true);

                possibleLivingEntityTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
                EntityAlgorithms.filterOutNonTargets(possibleLivingEntityTargets, false, true, false);

                //Choose a spawn position
                BlockPos spawnPosition;
                ArrayList<BlockPos> spawnPositions = getSpawnPositionsInCube((ServerWorld) this.level, this.getBlockPos(), 5, 1);
                //If the array is empty, just spawn above block
                if (spawnPositions.isEmpty()) {
                    spawnPosition = this.getBlockPos().above();
                }
                //Else choose the spawn position
                else {
                    spawnPosition = spawnPositions.get(0);
                }

                //Give gravemind context to our request to make more informed situations
                ReinforcementRequest request = new ReinforcementRequest(spawnPosition);
                request.sender = ReinforcementRequest.senderType.SculkCocoon;
                requests[iterations] = request;

                if (possibleAggressorTargets.size() != 0) {
                    request.is_aggressor_nearby = true;
                    lastTimeOfAlert = System.nanoTime();
                }
                if (possibleLivingEntityTargets.size() != 0) {
                    request.is_non_sculk_mob_nearby = true;
                    lastTimeOfAlert = System.nanoTime();
                }

                //If there is some sort of enemy near by, request reinforcement
                if (request.is_non_sculk_mob_nearby || request.is_aggressor_nearby) {
                    //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
                    SculkHorde.entityFactory.requestReinforcementAny(this.level, spawnPosition, false, request);
                }
            }
            behavior_state = STATE_COOLDOWN;
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
    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerWorld worldIn, BlockPos origin, int length, int amountOfPositions)
    {
        //TODO Can potentially be optimized by not getting all the possible positions
        ArrayList<BlockPos> listOfPossibleSpawns = BlockAlgorithms.getBlocksInCube(worldIn, origin, VALID_SPAWN_BLOCKS, length);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && listOfPossibleSpawns.size() > 0; count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size()-1);
            //Get random position between 0 and size of list - 1
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
        return isValidSpawnPosition((ServerWorld) this.level, blockPos) ;
    };

    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerWorld worldIn, BlockPos pos)
    {
        return SculkHorde.infestationConversionTable.isConsideredDormantSpreader(worldIn.getBlockState(pos.below()))  &&
            worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER) &&
            worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

    }
}
