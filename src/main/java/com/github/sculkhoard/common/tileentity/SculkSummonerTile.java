package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.common.block.BlockAlgorithms;
import com.github.sculkhoard.common.entity.EntityAlgorithms;
import com.github.sculkhoard.core.SculkHoard;
import com.github.sculkhoard.core.TileEntityRegistry;
import com.github.sculkhoard.core.gravemind.entity_factory.ReinforcementContext;
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
    //ACTIVATION_DISTANCE - The distance at which this is able to detect mobs.
    private final int ACTIVATION_DISTANCE = 32;
    //possibleLivingEntityTargets - A list of nearby targets which are worth infecting.
    private List<LivingEntity> possibleLivingEntityTargets;
    //possibleAggressorTargets - A list of nearby targets which should be considered hostile.
    private List<LivingEntity> possibleAggressorTargets;
    //Used to track the last time this tile was ticked
    private long lastTimeOfTick = System.nanoTime();
    private long lastTimeOfAlert = 0;
    private int alertPeriodSeconds = 60;
    //How often should this tile tick in seconds if the summoner is alert
    private long tickIntervalAlertSeconds = 10;
    //How often should this tile tick in seconds if the summoner is alert
    private long tickIntervalUnAlertSeconds = 60 * 5;
    //Records the last time this block summoned a mob
    private long lastTimeOfSummon = 0;
    //How long this block should wait between summons
    private long summonCooldownSeconds = 20;

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


    /** ~~~~~~~~ Events ~~~~~~~~ **/

    @Override
    public void tick()
    {
        if(this.level != null && !this.level.isClientSide)
        {

            long timeElapsedSinceTick = TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeOfTick, TimeUnit.NANOSECONDS);
            long timeElapsedSinceSummon = TimeUnit.SECONDS.convert(System.nanoTime() - lastTimeOfSummon, TimeUnit.NANOSECONDS);

            //If ( (currently alert AND enough time has passed) OR (NOT currently alert and enough time has passed) ) AND the cool down for summoning is done
            if(( (isCurrentlyAlert() && timeElapsedSinceTick >= tickIntervalAlertSeconds) || (!isCurrentlyAlert() && timeElapsedSinceTick >= tickIntervalUnAlertSeconds)  )
                            && timeElapsedSinceSummon >= summonCooldownSeconds)
            {
                lastTimeOfTick = System.nanoTime();
                //Create bounding box to detect targets
                AxisAlignedBB searchArea = EntityAlgorithms.getSearchAreaRectangle(this.getBlockPos().getX(), this.getBlockPos().getY(), this.getBlockPos().getZ(), ACTIVATION_DISTANCE, 5, ACTIVATION_DISTANCE);

                //Get targets inside bounding box.
                possibleAggressorTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
                EntityAlgorithms.filterOutNonHostiles(possibleAggressorTargets);

                possibleLivingEntityTargets = EntityAlgorithms.getLivingEntitiesInBoundingBox((ServerWorld) this.level, searchArea);
                EntityAlgorithms.filterOutHostiles(possibleLivingEntityTargets);

                //Choose a spawn position
                BlockPos spawnPosition;
                ArrayList<BlockPos> spawnPositions = getSpawnPositionsInCube((ServerWorld) this.level, this.getBlockPos(), 5, 1);
                //If the array is empty, just spawn above block
                if(spawnPositions.isEmpty())
                {
                    spawnPosition = this.getBlockPos().above();
                }
                //Else choose the spawn position
                else
                {
                    spawnPosition = spawnPositions.get(0);
                }


                //Give gravemind context to our request to make more informed situations
                ReinforcementContext context = new ReinforcementContext(spawnPosition);
                context.sender = ReinforcementContext.senderType.SculkCocoon;

                if (!possibleAggressorTargets.isEmpty())
                {
                    context.is_aggressor_nearby = true;
                    lastTimeOfAlert = System.nanoTime();
                }
                if (!possibleLivingEntityTargets.isEmpty())
                {
                    context.is_non_sculk_mob_nearby = true;
                    lastTimeOfAlert = System.nanoTime();
                }

                //If there is some sort of enemy near by, request reinforcement
                if (context.is_non_sculk_mob_nearby || context.is_aggressor_nearby)
                {
                    //Request reinforcement from entity factory (this request gets approved or denied by gravemind)
                    SculkHoard.entityFactory.requestReinforcementAny(this.level, spawnPosition, false, context);

                    //If the gravemind has viewed
                    if (context.isRequestViewed)
                    {
                        //If our request is approved, keep track
                        if (context.isRequestApproved)
                        {
                            lastTimeOfSummon = System.nanoTime();
                        }
                    }
                }
            }
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
        return SculkHoard.infestationConversionTable.isConsideredDormantSpreader(worldIn.getBlockState(pos.below()))  &&
            worldIn.getBlockState(pos).canBeReplaced(Fluids.WATER) &&
            worldIn.getBlockState(pos.above()).canBeReplaced(Fluids.WATER);

    }
}
