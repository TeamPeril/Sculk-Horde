package com.github.sculkhoard.common.block;

import com.github.sculkhoard.common.block.BlockInfestation.InfestationConversionTable;
import com.github.sculkhoard.core.BlockRegistry;
import com.github.sculkhoard.core.SculkHoard;
import net.minecraft.block.Blocks;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.SpawnReason;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import java.util.ArrayList;
import java.util.Random;

import static com.github.sculkhoard.core.SculkHoard.DEBUG_MODE;

public class BlockAlgorithms {

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

    public static ArrayList<BlockPos> getBlockPosInCircle(ServerWorld world, BlockPos Origin, int radius, boolean includeOrigin, boolean includeAir)
    {
        boolean DEBUG_WITH_GLASS = DEBUG_MODE && false;
        ArrayList<BlockPos> positions = new ArrayList<BlockPos>();
        boolean shouldWeAddThisPosition = false;

        //Origin position
        int center_x = Origin.getX();
        int center_y = Origin.getY();
        int center_z = Origin.getZ();

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
                    double delta_x = Math.pow(current_pos_x - Origin.getX(), 2);
                    double delta_y = Math.pow(current_pos_y - Origin.getY(), 2);
                    double delta_z = Math.pow(current_pos_z - Origin.getZ(), 2);
                    BlockPos targetPos = new BlockPos(current_pos_x, current_pos_y, current_pos_z);

                    //If distance between center and current block is less than radius, it is in the circle
                    double distance = Math.sqrt(delta_x + delta_y + delta_z);

                    //If outside of radius, do not include
                    if(distance > radius)
                        shouldWeAddThisPosition = false;
                    //If we are not including origin and this is the origin, do not include
                    if(!includeOrigin && targetPos.getX() == Origin.getX() && targetPos.getY() == Origin.getY() && targetPos.getZ() == Origin.getZ())
                        shouldWeAddThisPosition = false;
                    //If we are not including air, and this block is air, do not include
                    if(!includeAir && world.getBlockState(targetPos).isAir())
                        shouldWeAddThisPosition = false;

                    //If not debugging, function as normal
                    if(shouldWeAddThisPosition && !DEBUG_WITH_GLASS) positions.add(targetPos);
                    //If debug mode, replace with glass
                    else if(shouldWeAddThisPosition && DEBUG_WITH_GLASS) world.setBlockAndUpdate(targetPos, Blocks.GREEN_STAINED_GLASS.defaultBlockState());

                }

            }
        }

        return positions;
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
     * Will only place sculk nodes if sky is visible
     * @param world
     * @param targetPos
     */
    public static void placeSculkNode(ServerWorld world, BlockPos targetPos)
    {
        //If we are too close to another node, do not create one
        if(!SculkHoard.gravemind.isValidPositionForSculkNode(targetPos))
            return;

        //Given random chance and the target location can see the sky, create a sculk node
        if(new Random().nextInt(1000) <= 1 && world.canSeeSky(targetPos))
        {
            world.setBlockAndUpdate(targetPos, BlockRegistry.SCULK_BRAIN.get().defaultBlockState());
            SculkHoard.gravemind.sculkNodePositions.add(targetPos);
            EntityType.LIGHTNING_BOLT.spawn(world, null, null, targetPos, SpawnReason.SPAWNER, true, true);
            if(DEBUG_MODE) System.out.println("New Sculk Node Created at " + targetPos.toString());
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
     * Gets called in {@link InfestationConversionTable#processVictimConversionQueue}
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
