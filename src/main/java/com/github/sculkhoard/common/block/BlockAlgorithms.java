package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhoard.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhoard.common.entity.SculkBeeInfectorEntity;
import com.github.sculkhoard.common.tileentity.SculkBeeNestTile;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class BlockAlgorithms {

    public static float getSudoRNGFromPosition(BlockPos position, int min, int max)
    {
        int range = max - min;
        long seed = position.getX() * position.getY() * position.getZ();
        int rng = (int) (seed % (range + 1.0)); //Get output between 0 and range
        rng += min;
        return rng;
    }

    /**
     * Will return an array list that represents a 3x3x3 cube of all block
     * positions with the origin being the centroid. Does not include origin
     * in this list.
     * @param origin
     * @return
     */
    public static ArrayList<BlockPos> getNeighborsCube(BlockPos origin)
    {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        list.addAll(getNeighborsXZPlane(origin, false));
        list.addAll(getNeighborsXZPlane(origin.above(), true));
        list.addAll(getNeighborsXZPlane(origin.below(), true));
        return list;
    }


    /**
     * Will return an array list that represents the neighbors that are directly
     * touching any face of the a block
     * @param origin The target position
     * @return A list of all adjacent neighbors
     */
    public static ArrayList<BlockPos> getAdjacentNeighbors(BlockPos origin)
    {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        list.addAll(getNeighborsXZPlane(origin, false));
        list.addAll(getNeighborsXZPlane(origin.above(), true));
        list.addAll(getNeighborsXZPlane(origin.below(), true));
        list.addAll(getNeighborsXZPlane(origin.north(), true));
        list.addAll(getNeighborsXZPlane(origin.east(), true));
        list.addAll(getNeighborsXZPlane(origin.south(), true));
        list.addAll(getNeighborsXZPlane(origin.west(), true));
        return list;
    }

    /**
     * Will return an array list representing a 2D layer
     * @param origin
     * @return
     */
    public static ArrayList<BlockPos> getNeighborsXZPlane(BlockPos origin, boolean includeOrigin)
    {
        ArrayList<BlockPos> list = new ArrayList<BlockPos>();
        list.add(origin.north());
        list.add(origin.north().east());
        list.add(origin.north().west());
        list.add(origin.east());
        list.add(origin.south());
        list.add(origin.south().east());
        list.add(origin.south().west());
        list.add(origin.west());
        if(includeOrigin) list.add(origin);

        return list;
    }

    public static float getBlockDistance(BlockPos pos1, BlockPos pos2)
    {
        return (float) Math.sqrt( Math.pow(pos2.getX() - pos1.getX(), 2) + Math.pow(pos2.getY() - pos1.getY(), 2) + Math.pow(pos2.getZ() - pos1.getZ(), 2));
    }

    /**
     * Gets all blocks in a circle and retuns it in a list
     * NOTE: Something is wrong with this algorithm, the size is too small
     * @param Origin The center
     * @param radius The radius
     * @param includeOrigin Whether to include the origin
     * @return A list of all the block positions
     */
    public static ArrayList<BlockPos> getBlockPosInCircle(BlockPos origin, int radius, boolean includeOrigin)
    {
        ArrayList<BlockPos> positions = new ArrayList<>();
        boolean shouldWeAddThisPosition = false;

        //Origin position
        int center_x = origin.getX();
        int center_y = origin.getY();
        int center_z = origin.getZ();

        //Initial position is in corner
        int start_pos_x = center_x - radius - 1;
        int start_pos_y = center_y - radius - 1;
        int start_pos_z = center_z - radius - 1;

        //Iterate over y positions
        for(int y_offset = 0; y_offset < (radius*2) + 2; y_offset++)
        {
            //Iterate over z positions
            int current_pos_y = start_pos_y + y_offset;
            for(int z_offset = 0; z_offset < (radius*2) + 2; z_offset++)
            {
                //Iterate over x positions
                int current_pos_z = start_pos_z + z_offset;
                for(int x_offset = 0; x_offset < (radius*2) + 2; x_offset++)
                {
                    int current_pos_x = start_pos_x + x_offset;
                    shouldWeAddThisPosition = true;

                    BlockPos targetPos = new BlockPos(current_pos_x, current_pos_y, current_pos_z);

                    //If distance between center and current block is less than radius, it is in the circle
                    double distance = getBlockDistance(origin, targetPos);

                    //If outside of radius, do not include
                    if(distance > radius)
                        shouldWeAddThisPosition = false;
                    //If we are not including origin and this is the origin, do not include
                    if(!includeOrigin && targetPos.getX() == origin.getX() && targetPos.getY() == origin.getY() && targetPos.getZ() == origin.getZ())
                        shouldWeAddThisPosition = false;

                    //If not debugging, function as normal
                    if(shouldWeAddThisPosition) positions.add(targetPos);
                }
            }
        }

        return positions;
    }


    /**
     * Finds the location of the nearest block given a predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param predicateIn The predicate that determines if a block is the one were searching for
     * @param pDistance The search distance
     * @return The position of the block
     */
    public Optional<BlockPos> findNearestBlock(ServerWorld worldIn, BlockPos origin, Predicate<BlockState> predicateIn, double pDistance)
    {
;
        //?
        BlockPos.Mutable blockpos$mutable = new BlockPos.Mutable();

        //Search area for block
        for(int i = 0; (double)i <= pDistance; i = i > 0 ? -i : 1 - i)
        {
            for(int j = 0; (double)j < pDistance; ++j)
            {
                for(int k = 0; k <= j; k = k > 0 ? -k : 1 - k)
                {
                    for(int l = k < j && k > -j ? j : 0; l <= j; l = l > 0 ? -l : 1 - l)
                    {
                        blockpos$mutable.setWithOffset(origin, k, i - 1, l);

                        //If the block is close enough and is the right blockstate
                        if (origin.closerThan(blockpos$mutable, pDistance)
                                && predicateIn.test(worldIn.getBlockState(blockpos$mutable)))
                        {
                            return Optional.of(blockpos$mutable); //Return position
                        }
                    }
                }
            }
        }
        //else return empty
        return Optional.empty();
    }


    /**
     * Finds the location of the nearest block given a block state predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param predicateIn The predicate that determines if a block is the one were searching for
     * @param pDistance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getBlocksInArea(ServerWorld worldIn, BlockPos origin, Predicate<BlockState> predicateIn, double pDistance)
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
                                && predicateIn.test(worldIn.getBlockState(temp)))
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
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param predicateIn The predicate that determines if a block is the one were searching for
     * @param pDistance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getBlocksInCube(ServerWorld worldIn, BlockPos origin, Predicate<BlockPos> predicateIn, double pDistance)
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
     * Chooses a random neighbor position
     * @param origin The origin Block Position
     * @param serverWorld The ServerWorld of the block
     * @return The target Block Position
     */
    public static BlockPos getRandomNeighbor(ServerWorld serverWorld, BlockPos origin)
    {
        ArrayList<BlockPos> positions = getNeighborsCube(origin);
        return positions.get(serverWorld.random.nextInt(positions.size()));
    }

    /**
     * Checks immediate blocks to see if any of them are air
     * @param serverWorld The world
     * @param targetPos The position to check
     * @return true if any air found, false otherwise
     */
    public static boolean isExposedToAir(ServerWorld serverWorld, BlockPos targetPos)
    {
        ArrayList<BlockPos> list = getAdjacentNeighbors(targetPos);

        for(BlockPos position : list)
        {
            if(serverWorld.getBlockState(position).isAir())
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Will only place sculk nodes if sky is visible
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void placeSculkBeeHive(ServerWorld world, BlockPos targetPos)
    {
        //Given random chance and the target location can see the sky, create a sculk node
        if(new Random().nextInt(4000) <= 1 && world.canSeeSky(targetPos))
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_BEE_NEST_BLOCK.get().defaultBlockState());
            SculkBeeNestTile nest = (SculkBeeNestTile) world.getBlockEntity(targetPos);

            //Add 10 bees
            nest.addOccupant(new SculkBeeHarvesterEntity(world), false);
            nest.addOccupant(new SculkBeeHarvesterEntity(world), false);
            nest.addOccupant(new SculkBeeInfectorEntity(world), false);
            nest.addOccupant(new SculkBeeInfectorEntity(world), false);
        }

    }

    /**
     * A Jank solution to spawning flora. Given a random chance, spawn flora.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void placeSculkFlora(BlockPos targetPos, ServerWorld world)
    {

        ((SculkFloraBlock) SculkHoard.randomSculkFlora.getRandomEntry()).placeBlockHere(world, targetPos);

    }

    /**
     * Will place random flora attached to a given position.
     * @param serverWorld the world
     * @param origin the position
     */
    public static void placeFloraAroundLog(ServerWorld serverWorld, BlockPos origin) {
        boolean DEBUG_THIS = false;
        VeinBlock vein = BlockRegistry.VEIN.get();

        BlockPos[] possiblePositions = {
                origin.north(),
                origin.east(),
                origin.south(),
                origin.west()
        };

        //50% chance to place sculk vein for each face
        for(BlockPos pos : possiblePositions)
        {
            if(serverWorld.random.nextInt(10) < 3 &&
                    serverWorld.getBlockState(pos).isAir())
            {
                vein.placeBlock(serverWorld, pos);
            }
        }
    }

    /**
     * Places a line of sculk vein above a block. Length and height of line is random.
     * @param serverWorld the world
     * @param origin the block we want to place these above
     */
    public static void placePatchesOfVeinAbove(ServerWorld serverWorld, BlockPos origin)
    {
        int OFFSET_MAX = 3;
        int LENGTH_MAX = 5;
        int LENGTH_MIN = 3;

        Random rng = new Random();
        int offset = rng.nextInt(OFFSET_MAX);
        int length = rng.nextInt(LENGTH_MAX - LENGTH_MIN) + LENGTH_MIN;
        VeinBlock vein = BlockRegistry.VEIN.get();

        //Attempt to place sculk vein in a straight line above origin
        BlockPos indexPos = origin.above(offset);
        for(int i = 0; i < length; i++)
        {
            indexPos = indexPos.above();

            //75% chance to place vein
            if(serverWorld.random.nextInt(4) <= 2)
            {
                vein.placeBlock(serverWorld, indexPos);
            }
        }
    }


    /**
     * Will replace sculk flora with grass.
     * Gets called in {@link InfestationConversionHandler#processVictimConversionQueue}
     * @param serverWorld the world
     * @param targetPos the position
     */
    public static void replaceSculkFlora(ServerWorld serverWorld, BlockPos targetPos)
    {
        if(serverWorld.getBlockState(targetPos).getBlock() instanceof SculkFloraBlock)
        {
            serverWorld.setBlockAndUpdate(targetPos, Blocks.GRASS.defaultBlockState());
        }
        else if(serverWorld.getBlockState(targetPos).getBlock() instanceof VeinBlock)
        {
            serverWorld.removeBlock(targetPos, false);
        }
    }
}
