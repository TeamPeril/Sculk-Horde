package com.github.sculkhorde.util;

import com.github.sculkhorde.common.block.BlockInfestation.InfestationConversionHandler;
import com.github.sculkhorde.common.block.SculkFloraBlock;
import com.github.sculkhorde.common.block.TendrilsBlock;
import com.github.sculkhorde.common.blockentity.SculkBeeNestBlockEntity;
import com.github.sculkhorde.common.procedural.structures.PlannedBlock;
import com.github.sculkhorde.core.BlockRegistry;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.gravemind.Gravemind;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import net.minecraft.server.level.ServerLevel;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Predicate;

public class BlockAlgorithms {

    public static float getSudoRNGFromPosition(BlockPos position, int min, int max)
    {
        int range = max - min;
        long seed = (long) position.getX() * position.getY() * position.getZ() + (position.getX() + position.getY() + position.getZ());
        int rng = (int) (seed % (range + 1.0)); //Get output between 0 and range
        rng += min;
        return rng;
    }

    /**
     * Will return an array list that represents a 3x3x3 cube of all block
     * positions with the origin being the centroid. Does not include origin
     * in this list.
     * @return A list of Neighbors in a cube
     */
    public static ArrayList<BlockPos> getNeighborsCube(BlockPos pos, boolean includeOrigin) {
        ArrayList<BlockPos> neighbors = new ArrayList<>();
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    if (i == 0 && j == 0 && k == 0 && !includeOrigin) {
                        continue;
                    }
                    neighbors.add(pos.offset(i, j, k));
                }
            }
        }
        return neighbors;
    }


    /**
     * Will return an array list that represents the neighbors that are directly
     * touching any face of the a block
     * @param origin The target position
     * @return A list of all adjacent neighbors
     */
    public static ArrayList<BlockPos> getAdjacentNeighbors(BlockPos origin)
    {
        ArrayList<BlockPos> list = new ArrayList<>();
        list.addAll(getNeighborsXZPlane(origin, false));
        list.addAll(getNeighborsXZPlane(origin.above(), true));
        list.addAll(getNeighborsXZPlane(origin.below(), true));
        //list.addAll(getNeighborsXZPlane(origin.north(), true));
        //list.addAll(getNeighborsXZPlane(origin.east(), true));
        //list.addAll(getNeighborsXZPlane(origin.south(), true));
        //list.addAll(getNeighborsXZPlane(origin.west(), true));
        return list;
    }

    /**
     * Will return an array list representing a 2D layer
     * @param origin The origin position
     * @return A list of block positions
     */
    public static ArrayList<BlockPos> getNeighborsXZPlane(BlockPos origin, boolean includeOrigin)
    {
        ArrayList<BlockPos> list = new ArrayList<>();
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
     * Returns an ArrayList of BlockPos that represent a cube
     * @param origin the center BlockPos of the cube
     * @param length the length of all sides of the cube
     * @param includeOrigin if true, the origin position will be included in the final output
     * @return an ArrayList of BlockPos that represent a cube
     */
    public static ArrayList<BlockPos> getBlockPosInCube(BlockPos origin, int length, boolean includeOrigin) {
        ArrayList<BlockPos> positions = new ArrayList<>();
        boolean shouldWeAddThisPosition;

        //Origin position
        int center_x = origin.getX();
        int center_y = origin.getY();
        int center_z = origin.getZ();

        //Initial position is in corner
        int start_pos_x = center_x - (length/2) - 1;
        int start_pos_y = center_y - (length/2) - 1;
        int start_pos_z = center_z - (length/2) - 1;

        //Iterate over y positions
        for(int y_offset = 0; y_offset < length; y_offset++)
        {
            //Iterate over z positions
            int current_pos_y = start_pos_y + y_offset;
            for(int z_offset = 0; z_offset < length; z_offset++)
            {
                //Iterate over x positions
                int current_pos_z = start_pos_z + z_offset;
                for(int x_offset = 0; x_offset < length; x_offset++)
                {
                    int current_pos_x = start_pos_x + x_offset;
                    shouldWeAddThisPosition = true;

                    BlockPos targetPos = new BlockPos(current_pos_x, current_pos_y, current_pos_z);

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
     * Gets all blocks in a circle and retuns it in a list
     * NOTE: Something is wrong with this algorithm, the size is too small
     * @param radius The radius
     * @param includeOrigin Whether to include the origin
     * @return A list of all the block positions
     */
    public static ArrayList<BlockPos> getBlockPosInCircle(BlockPos origin, int radius, boolean includeOrigin)
    {
        if(radius <= 0) { throw new IllegalArgumentException("Radius must be greater than 0. Radius given was " + radius + ".");}

        ArrayList<BlockPos> positions = new ArrayList<>();
        boolean shouldWeAddThisPosition;

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
     * Finds the location of the nearest block given a block state predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static ArrayList<BlockPos> getBlocksInArea(ServerLevel level, BlockPos origin, Predicate<BlockState> predicate, int distance)
    {
        ArrayList<BlockPos> list = new ArrayList<>();

        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(level.getBlockState(mutableBlockPos))) {
                        list.add(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return list;
    }


    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param level The world
     * @param origin The origin of the search location
     * @param predicate The predicate that determines if a block is the one were searching for
     * @param distance The search distance
     * @return The position of the block
     */
    public static Optional<BlockPos> findBlockInCube(ServerLevel level, BlockPos origin, Predicate<BlockState> predicate, int distance) {
        BlockPos blockPos = origin;

        // Define the bounds of the cube
        int minX = blockPos.getX() - distance;
        int minY = blockPos.getY() - distance;
        int minZ = blockPos.getZ() - distance;
        int maxX = blockPos.getX() + distance;
        int maxY = blockPos.getY() + distance;
        int maxZ = blockPos.getZ() + distance;

        BlockPos.MutableBlockPos mutableBlockPos = new BlockPos.MutableBlockPos();

        for (int x = minX; x <= maxX; x++) {
            for (int y = minY; y <= maxY; y++) {
                for (int z = minZ; z <= maxZ; z++) {
                    mutableBlockPos.set(x, y, z);
                    if (level.getBlockState(mutableBlockPos).is(Blocks.AIR)) {
                        continue;
                    }
                    if (predicate.test(level.getBlockState(mutableBlockPos))) {
                        return Optional.of(mutableBlockPos.immutable());
                    }
                }
            }
        }
        return Optional.empty();
    }

    /**
     * Checks immediate blocks to see if any of them are air
     * @param serverWorld The world
     * @param targetPos The position to check
     * @return true if any air found, false otherwise
     */
    public static boolean isExposedToAir(ServerLevel serverWorld, BlockPos targetPos)
    {
        ArrayList<BlockPos> list = getAdjacentNeighbors(targetPos);

        for(BlockPos position : list)
        {
            if(!serverWorld.getBlockState(position).isSolidRender(serverWorld, position))
            {
                return true;
            }
        }

        return false;
    }


    /**
     * Will only place Sculk Bee Hives
     * @param world The World to place it in
     * @param targetPos The position to place it in
     */
    public static void placeSculkBeeHive(ServerLevel world, BlockPos targetPos)
    {
        //Given random chance and the target location can see the sky, create a sculk hive
        if(new Random().nextInt(4000) <= 1 && world.canSeeSky(targetPos))
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_BEE_NEST_BLOCK.get().defaultBlockState());
            SculkBeeNestBlockEntity nest = (SculkBeeNestBlockEntity) world.getBlockEntity(targetPos);

            //Add bees
            nest.addFreshInfectorOccupant();
            nest.addFreshInfectorOccupant();
            nest.addFreshHarvesterOccupant();
            nest.addFreshHarvesterOccupant();
        }

    }

    /**
     * Checks if near sculk node, if true, spawns structure.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void tryPlaceLivingRockRoot(BlockPos targetPos, ServerLevel world)
    {
        //If block below target is valid and the target can be replaced by water
        if(Gravemind.getGravemindMemory().isInRangeOfNode(targetPos, 100) && world.getBlockState(targetPos).isAir())
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_LIVING_ROCK_ROOT_BLOCK.get().defaultBlockState());
        }
    }

    /**
     * A Jank solution to spawning flora. Given a random chance, spawn flora.
     * @param targetPos The BlockPos to spawn it at
     * @param world The world to spawn it in.
     */
    public static void tryPlaceSculkFlora(BlockPos targetPos, ServerLevel world)
    {
        BlockState blockState = SculkHorde.randomSculkFlora.getRandomEntry().defaultBlockState();

        //If block below target is valid and the target can be replaced by water
        if(blockState.canSurvive(world, targetPos)
                && world.getBlockState(targetPos).canBeReplaced(Fluids.WATER))
        {
            world.setBlockAndUpdate(targetPos, blockState);
        }
    }

    /**
     * Will place random flora attached to a given position.
     * @param serverWorld the world
     * @param origin the position
     */
    public static void placeFloraAroundLog(ServerLevel serverWorld, BlockPos origin) {
        TendrilsBlock vein = BlockRegistry.TENDRILS.get();

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
    public static void placePatchesOfVeinAbove(ServerLevel serverWorld, BlockPos origin)
    {
        int OFFSET_MAX = 3;
        int LENGTH_MAX = 5;
        int LENGTH_MIN = 3;

        Random rng = new Random();
        int offset = rng.nextInt(OFFSET_MAX);
        int length = rng.nextInt(LENGTH_MAX - LENGTH_MIN) + LENGTH_MIN;
        TendrilsBlock vein = BlockRegistry.TENDRILS.get();

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
     * Gets called in {@link InfestationConversionHandler#processDeInfectionQueue}
     * @param serverWorld the world
     * @param targetPos the position
     */
    public static void replaceSculkFlora(ServerLevel serverWorld, BlockPos targetPos)
    {
        if(serverWorld.getBlockState(targetPos).getBlock() instanceof SculkFloraBlock)
        {
            serverWorld.setBlockAndUpdate(targetPos, Blocks.GRASS.defaultBlockState());
        }
        else if(serverWorld.getBlockState(targetPos).getBlock() instanceof TendrilsBlock)
        {
            serverWorld.removeBlock(targetPos, false);
        }
    }

    /**
     * Generates a list of PlannedBlocks in the shape of a 2D circle.
     *
     * @param centerPos The center of the circle
     * @param diameter The diameter of the circle
     * @param world The world where the blocks will be placed
     * @param plannedBlock The type of block that will be placed in the circle
     * @return A list of PlannedBlocks in the shape of a 2D circle
     */
    public static ArrayList<PlannedBlock> generate2DCirclePlan(BlockPos centerPos, int diameter, ServerLevel world, BlockState plannedBlock) {
        ArrayList<PlannedBlock> circleBlocks = new ArrayList<>();
        // The radius of the circle is half the diameter
        int radius = diameter / 2;
        // Iterate through all blocks in a square that surrounds the circle
        for (int x = centerPos.getX() - radius; x <= centerPos.getX() + radius; x++) {
            for (int z = centerPos.getZ() - radius; z <= centerPos.getZ() + radius; z++) {
                // Calculate the distance between the current block and the center of the circle
                double distance = Math.sqrt((x - centerPos.getX()) * (x - centerPos.getX()) + (z - centerPos.getZ()) * (z - centerPos.getZ()));
                // If the distance is less than or equal to the radius, add a PlannedBlock for that block
                if (distance <= radius) {
                    BlockPos pos = new BlockPos(x, centerPos.getY(), z);
                    circleBlocks.add(new PlannedBlock(world, plannedBlock, pos));
                }
            }
        }
        return circleBlocks;
    }


    public static ArrayList<BlockPos> getPointsOnCircumference(BlockPos origin, int numPoints, int radius) {
        ArrayList<BlockPos> points = new ArrayList<>();
        double angleIncrement = (2 * Math.PI) / numPoints;
        for (int i = 0; i < numPoints; i++) {
            double angle = i * angleIncrement;
            int x = (int) (origin.getX() + radius * Math.cos(angle));
            int z = (int) (origin.getZ() + radius * Math.sin(angle));
            points.add(new BlockPos(x, origin.getY(), z));
        }
        return points;
    }

    public static Vec3 scalarMultiply(Vec3 vector, double t) {
        return new Vec3(vector.x + t, vector.y * t, vector.z + t);
    }
}
