package com.github.sculkhorde.common.structures.procedural;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.function.Consumer;

public class Line3D {
    private Vec3 start; // The starting point of the line
    private Vec3 end; // The end point of the line
    private Vec3 direction; // The direction of the line

    // Constructor taking in two Vector3D points
    public Line3D(Vec3 start, Vec3 end) {
        this.start = start;
        this.end = end;
        this.direction = end.subtract(start); // Calculate the direction of the line
    }

    // Optional constructor taking in a BlockPos
    public Line3D(BlockPos start, BlockPos end) {
        this(new Vec3(start.getX(), start.getY(), start.getZ()), new Vec3(end.getX(), end.getY(), end.getZ()));
    }

    // Method to get the length of the line
    public double getLength() {
        return direction.length(); // The length of the line is the magnitude of the direction vector
    }



    // Method to iterate over the length of the line with a given step size
    /* Example of using the iterateOverLine method

       // This method will iterate over the line with a step size of 1 and set the block at each point to air
       line.iterateOverLine(1, (point) -> {
            BlockPos pos = new BlockPos(point.x, point.y, point.z);
            world.setBlockState(pos, Blocks.AIR.getDefaultState(), 2);
        });

     */
    /**
     * Iterate over the length of the line with a given step size
     * @param stepSize The step size to use when iterating over the line
     * @param consumer The consumer to use when iterating over the line
     */
    public void iterateOverLine(double stepSize, Consumer<Vec3> consumer) {
        double length = getLength();
        double offset = 0.0;
        while (offset < length) {
            // Get the point on the line from the start plus the offset
            Vec3 point = start.add(direction.normalize().scale(offset));
            consumer.accept(point); // Use the consumer to do something with the point
            offset += stepSize; // Increment the parameter value
        }
    }

    /**
     * Get a list of block positions within a sphere along the line
     * @param radius The radius of the sphere
     * @return A list of block positions within a sphere along the line
     */
    public ArrayList<BlockPos> getBlockPositionsOnLineWithSphere(double radius) {
        ArrayList<BlockPos> blockPosList = new ArrayList<>();

        // Iterate over the line with a step size of 1
        iterateOverLine(1.0, point -> {
            int x = Mth.floor(point.x);
            int y = Mth.floor(point.y);
            int z = Mth.floor(point.z);

            for (int i = -Mth.floor(radius); i <= Mth.floor(radius); i++) {
                for (int j = -Mth.floor(radius); j <= Mth.floor(radius); j++) {
                    for (int k = -Mth.floor(radius); k <= Mth.floor(radius); k++) {
                        double distanceSq = (double)(i * i + j * j + k * k);
                        if (distanceSq <= radius * radius) {
                            blockPosList.add(new BlockPos(x + i, y + j, z + k));
                        }
                    }
                }
            }
        });

        return blockPosList;
    }
}
