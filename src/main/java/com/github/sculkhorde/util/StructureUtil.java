package com.github.sculkhorde.util;

import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.mixin.structures.StructureTemplateAccessor;
import com.google.common.collect.Lists;
import com.mojang.brigadier.exceptions.DynamicCommandExceptionType;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.DoubleTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Clearable;
import net.minecraft.world.entity.*;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlockContainer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BitSetDiscreteVoxelShape;
import net.minecraft.world.phys.shapes.DiscreteVoxelShape;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;

import static net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.processBlockInfos;
import static net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate.processEntityInfos;


public class StructureUtil {

    private static final DynamicCommandExceptionType ERROR_TEMPLATE_INVALID = new DynamicCommandExceptionType((p_214582_) -> {
        return Component.translatable("commands.place.template.invalid", p_214582_);
    });

    private static void checkLoaded(ServerLevel level, ChunkPos chunkPos, ChunkPos chunkPos1) {
        if (ChunkPos.rangeClosed(chunkPos, chunkPos1).anyMatch((chunk) -> {
            return !level.isLoaded(chunk.getWorldPosition());
        })) {

            SculkHorde.LOGGER.error("StructureUtil | placeStructureTemplate | Failed to place structure, Area Not Loaded.");
            return;
        }
    }

    public static boolean placeStructureTemplate(ServerLevel level, ResourceLocation structure, BlockPos pos){
        StructureTemplateManager structuretemplatemanager = level.getStructureManager();
        Optional<StructureTemplate> optional;
        optional = structuretemplatemanager.get(structure);


        if (optional.isEmpty()) {
            SculkHorde.LOGGER.error("StructureUtil | placeStructureTemplate | Failed to get structure: " + structure.toString());
            return false;
        }

        StructureTemplate structuretemplate = optional.get();
        checkLoaded(level, new ChunkPos(pos), new ChunkPos(pos.offset(structuretemplate.getSize())));
        StructurePlaceSettings structureplacesettings = (new StructurePlaceSettings());

        boolean wasAbleToPlaceStructure = placeInWorld(structuretemplate, level, pos, pos, structureplacesettings, StructureBlockEntity.createRandom((long)0), 2);

        if(!wasAbleToPlaceStructure)
        {
            SculkHorde.LOGGER.error("StructureUtil | placeStructureTemplate | Failed to place structure: " + structure.toString());
            return false;
        }
        return true;
    }

    private static Optional<Entity> createEntityIgnoreException(ServerLevelAccessor p_74544_, CompoundTag p_74545_) {
        try {
            return EntityType.create(p_74545_, p_74544_.getLevel());
        } catch (Exception exception) {
            return Optional.empty();
        }
    }

    private static void addEntitiesToWorld(StructureTemplate structureTemplate, ServerLevelAccessor p_74524_, BlockPos p_74525_, StructurePlaceSettings placementIn) {
        StructureTemplateAccessor structureTemplateAccessor = ((StructureTemplateAccessor) structureTemplate);

        for(StructureTemplate.StructureEntityInfo structuretemplate$structureentityinfo : processEntityInfos(structureTemplate, p_74524_, p_74525_, placementIn, structureTemplateAccessor.getEntityInfoList())) {
            BlockPos blockpos = structuretemplate$structureentityinfo.blockPos; // FORGE: Position will have already been transformed by processEntityInfos
            if (placementIn.getBoundingBox() == null || placementIn.getBoundingBox().isInside(blockpos)) {
                CompoundTag compoundtag = structuretemplate$structureentityinfo.nbt.copy();
                Vec3 vec31 = structuretemplate$structureentityinfo.pos; // FORGE: Position will have already been transformed by processEntityInfos
                ListTag listtag = new ListTag();
                listtag.add(DoubleTag.valueOf(vec31.x));
                listtag.add(DoubleTag.valueOf(vec31.y));
                listtag.add(DoubleTag.valueOf(vec31.z));
                compoundtag.put("Pos", listtag);
                compoundtag.remove("UUID");
                createEntityIgnoreException(p_74524_, compoundtag).ifPresent((p_275190_) -> {
                    float f = p_275190_.rotate(placementIn.getRotation());
                    f += p_275190_.mirror(placementIn.getMirror()) - p_275190_.getYRot();
                    p_275190_.moveTo(vec31.x, vec31.y, vec31.z, f, p_275190_.getXRot());
                    if (placementIn.shouldFinalizeEntities() && p_275190_ instanceof Mob) {
                        ((Mob)p_275190_).finalizeSpawn(p_74524_, p_74524_.getCurrentDifficultyAt(BlockPos.containing(vec31)), MobSpawnType.STRUCTURE, (SpawnGroupData)null, compoundtag);
                    }

                    p_74524_.addFreshEntityWithPassengers(p_275190_);
                });
            }
        }

    }

    public static boolean placeInWorld(StructureTemplate structureTemplate, ServerLevelAccessor world, BlockPos startPos, BlockPos offsetPos, StructurePlaceSettings settings, RandomSource random, int flags) {
        StructureTemplateAccessor structureTemplateAccessor = ((StructureTemplateAccessor) structureTemplate);
        List<StructureTemplate.Palette> palettes = structureTemplateAccessor.getPalettes();

        // Check if there are any palettes to use
        if (palettes.isEmpty()) {
            return false;
        } else {
            // Get the list of blocks from the selected palette
            List<StructureTemplate.StructureBlockInfo> blockInfoList = settings.getRandomPalette(palettes, startPos).blocks();

            // Ensure there are blocks to place and the structure size is valid
            if ((!blockInfoList.isEmpty() || !settings.isIgnoreEntities() && !structureTemplateAccessor.getEntityInfoList().isEmpty()) && structureTemplateAccessor.getSize().getX() >= 1 && structureTemplateAccessor.getSize().getY() >= 1 && structureTemplateAccessor.getSize().getZ() >= 1) {
                BoundingBox boundingBox = settings.getBoundingBox();
                List<BlockPos> liquidPositions = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? blockInfoList.size() : 0);
                List<BlockPos> sourceLiquidPositions = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? blockInfoList.size() : 0);
                List<Pair<BlockPos, CompoundTag>> blockEntityDataList = Lists.newArrayListWithCapacity(blockInfoList.size());
                int minX = Integer.MAX_VALUE;
                int minY = Integer.MAX_VALUE;
                int minZ = Integer.MAX_VALUE;
                int maxX = Integer.MIN_VALUE;
                int maxY = Integer.MIN_VALUE;
                int maxZ = Integer.MIN_VALUE;

                // Process each block in the structure
                for (StructureTemplate.StructureBlockInfo blockInfo : processBlockInfos(world, startPos, offsetPos, settings, blockInfoList, structureTemplate)) {
                    BlockPos blockPos = blockInfo.pos();
                    if (boundingBox == null || boundingBox.isInside(blockPos)) {
                        FluidState fluidState = settings.shouldKeepLiquids() ? world.getFluidState(blockPos) : null;
                        BlockState blockState = blockInfo.state().mirror(settings.getMirror()).rotate(settings.getRotation());

                        // Handle block entities
                        if (blockInfo.nbt() != null) {
                            BlockEntity blockEntity = world.getBlockEntity(blockPos);
                            Clearable.tryClear(blockEntity);
                            world.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), 20);
                        }

                        // Place the block in the world
                        if (world.setBlock(blockPos, blockState, flags)) {
                            minX = Math.min(minX, blockPos.getX());
                            minY = Math.min(minY, blockPos.getY());
                            minZ = Math.min(minZ, blockPos.getZ());
                            maxX = Math.max(maxX, blockPos.getX());
                            maxY = Math.max(maxY, blockPos.getY());
                            maxZ = Math.max(maxZ, blockPos.getZ());
                            blockEntityDataList.add(Pair.of(blockPos, blockInfo.nbt()));

                            // Load block entity data
                            if (blockInfo.nbt() != null) {
                                BlockEntity blockEntity1 = world.getBlockEntity(blockPos);
                                if (blockEntity1 != null) {
                                    if (blockEntity1 instanceof RandomizableContainerBlockEntity) {
                                        blockInfo.nbt().putLong("LootTableSeed", random.nextLong());
                                    }
                                    blockEntity1.load(blockInfo.nbt());
                                }
                            }

                            // Handle fluid states
                            if (fluidState != null) {
                                if (blockState.getFluidState().isSource()) {
                                    sourceLiquidPositions.add(blockPos);
                                } else if (blockState.getBlock() instanceof LiquidBlockContainer) {
                                    ((LiquidBlockContainer) blockState.getBlock()).placeLiquid(world, blockPos, blockState, fluidState);
                                    if (!fluidState.isSource()) {
                                        liquidPositions.add(blockPos);
                                    }
                                }
                            }
                        }
                    }
                }

                // Ensure all fluids are placed correctly
                boolean flag = true;
                Direction[] directions = new Direction[]{Direction.UP, Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};

                while (flag && !liquidPositions.isEmpty()) {
                    flag = false;
                    Iterator<BlockPos> iterator = liquidPositions.iterator();

                    while (iterator.hasNext()) {
                        BlockPos liquidPos = iterator.next();
                        FluidState currentFluidState = world.getFluidState(liquidPos);

                        for (int i = 0; i < directions.length && !currentFluidState.isSource(); ++i) {
                            BlockPos adjacentPos = liquidPos.relative(directions[i]);
                            FluidState adjacentFluidState = world.getFluidState(adjacentPos);
                            if (adjacentFluidState.isSource() && !sourceLiquidPositions.contains(adjacentPos)) {
                                currentFluidState = adjacentFluidState;
                            }
                        }

                        if (currentFluidState.isSource()) {
                            BlockState currentState = world.getBlockState(liquidPos);
                            Block block = currentState.getBlock();
                            if (block instanceof LiquidBlockContainer) {
                                ((LiquidBlockContainer) block).placeLiquid(world, liquidPos, currentState, currentFluidState);
                                flag = true;
                                iterator.remove();
                            }
                        }
                    }
                }

                // Update voxel shape if necessary
                if (minX <= maxX) {
                    if (!settings.getKnownShape()) {
                        DiscreteVoxelShape voxelShape = new BitSetDiscreteVoxelShape(maxX - minX + 1, maxY - minY + 1, maxZ - minZ + 1);
                        int baseX = minX;
                        int baseY = minY;
                        int baseZ = minZ;

                        for (Pair<BlockPos, CompoundTag> pair : blockEntityDataList) {
                            BlockPos pos = pair.getFirst();
                            voxelShape.fill(pos.getX() - baseX, pos.getY() - baseY, pos.getZ() - baseZ);
                        }

                        StructureTemplate.updateShapeAtEdge(world, flags, voxelShape, baseX, baseY, baseZ);
                    }

                    // Update block states and entities
                    for (Pair<BlockPos, CompoundTag> pair : blockEntityDataList) {
                        BlockPos pos = pair.getFirst();
                        if (!settings.getKnownShape()) {
                            BlockState currentState = world.getBlockState(pos);
                            BlockState updatedState = Block.updateFromNeighbourShapes(currentState, world, pos);
                            if (currentState != updatedState) {
                                world.setBlock(pos, updatedState, flags & -2 | 16);
                            }
                            world.blockUpdated(pos, updatedState.getBlock());
                        }

                        if (pair.getSecond() != null) {
                            BlockEntity blockEntity = world.getBlockEntity(pos);
                            if (blockEntity != null) {
                                blockEntity.setChanged();
                            }
                        }
                    }
                }

                // Add entities to the world if necessary
                if (!settings.isIgnoreEntities()) {
                    addEntitiesToWorld(structureTemplate, world, startPos, settings);
                }

                return true;
            } else {
                return false;
            }
        }
    }


    public static class StructurePlacer
    {
        protected final StructureTemplate structureTemplate;
        protected final ServerLevelAccessor world;
        protected final BlockPos startPos;
        protected final BlockPos offsetPos;
        protected final StructurePlaceSettings settings;
        protected final RandomSource random;
        protected List<StructureTemplate.StructureBlockInfo> rawBlockInfoList;
        protected List<StructureTemplate.StructureBlockInfo> processedBlockInfoList;
        protected int currentIndex = 0;
        protected List<StructureTemplate.Palette> palettes;

        protected int minX = Integer.MAX_VALUE;
        protected int minY = Integer.MAX_VALUE;
        protected int minZ = Integer.MAX_VALUE;
        protected int maxX = Integer.MIN_VALUE;
        protected int maxY = Integer.MIN_VALUE;
        protected int maxZ = Integer.MIN_VALUE;

        protected BoundingBox boundingBox;
        protected List<BlockPos> liquidPositions;
        protected List<BlockPos> sourceLiquidPositions;
        protected List<Pair<BlockPos, CompoundTag>> blockEntityDataList;

        protected List<BlockPos> doNotPlaceBlocksHereList = new ArrayList<>();

        protected BlockPos realOrigin;
        protected BlockPos originOffset;

        public enum State
        {
            INITIALIZATION,
            PLACING,
            FINISHED
        }

        protected State state = State.INITIALIZATION;

        public StructurePlacer(StructureTemplate structureTemplate, ServerLevelAccessor world, BlockPos startPos, BlockPos offsetPos, StructurePlaceSettings settings, RandomSource random) {
            this.structureTemplate = structureTemplate;
            this.world = world;
            this.startPos = startPos;
            this.offsetPos = offsetPos;
            this.settings = settings;
            this.random = random;
        }

        public void appendIgnoreBlockPosList(BlockPos pos)
        {
            doNotPlaceBlocksHereList.add(pos);
        }

        public void setState(State state) {
            SculkHorde.LOGGER.debug("StructurePlacer | State is now: " + state);
            this.state = state;
        }

        public void tick()
        {
            switch(state)
            {
                case INITIALIZATION -> {
                    initializationTick();
                }
                case PLACING -> {
                    placingBlocksTick();
                }
                case FINISHED -> {
                    finishedTick();
                }
            }
        }

        public void initializationTick() {
            StructureTemplateAccessor structureTemplateAccessor = ((StructureTemplateAccessor) structureTemplate);
            palettes = structureTemplateAccessor.getPalettes();

            // Check if there are any palettes to use
            if (palettes.isEmpty()) {
                SculkHorde.LOGGER.debug("StructurePlacer | Failure, Pallet is Empty");
                setState(State.FINISHED);
                return;
            }

                // Get the list of blocks from the selected palette
                rawBlockInfoList = settings.getRandomPalette(palettes, startPos).blocks();

                if (rawBlockInfoList.isEmpty()) {
                    SculkHorde.LOGGER.debug("StructurePlacer | Failure, blockInfoList is Empty");
                    setState(State.FINISHED);
                    return;
                }

                if (settings.isIgnoreEntities() && structureTemplateAccessor.getEntityInfoList().isEmpty()) {
                    SculkHorde.LOGGER.debug("StructurePlacer | Failure, entityInfoList is Empty");
                    setState(State.FINISHED);
                    return;
                }

                if (structureTemplateAccessor.getSize().getX() < 1 || structureTemplateAccessor.getSize().getY() < 1 || structureTemplateAccessor.getSize().getZ() < 1) {
                    SculkHorde.LOGGER.debug("StructurePlacer | Failure, a dimension of structure size is 0.");
                    setState(State.FINISHED);
                    return;
                }


                boundingBox = settings.getBoundingBox();
                liquidPositions = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? rawBlockInfoList.size() : 0);
                sourceLiquidPositions = Lists.newArrayListWithCapacity(settings.shouldKeepLiquids() ? rawBlockInfoList.size() : 0);
                blockEntityDataList = Lists.newArrayListWithCapacity(rawBlockInfoList.size());
                processedBlockInfoList = processBlockInfos(world, startPos, offsetPos, settings, rawBlockInfoList, structureTemplate);
                setState(State.PLACING);

        }

        public void placingBlocksTick()
        {
            if(currentIndex >= processedBlockInfoList.size())
            {
                SculkHorde.LOGGER.debug("StructurePlacer | Successfully Placed Structure.");
                setState(State.FINISHED);
                return;
            }

            StructureTemplate.StructureBlockInfo blockInfo = processedBlockInfoList.get(currentIndex);

            if(doNotPlaceBlocksHereList.contains(blockInfo.pos()))
            {
                currentIndex += 1;
                return;
            }

            BlockPos blockPos = blockInfo.pos();
            if (boundingBox == null || boundingBox.isInside(blockPos))
            {
                FluidState fluidState = settings.shouldKeepLiquids() ? world.getFluidState(blockPos) : null;
                BlockState blockState = blockInfo.state().mirror(settings.getMirror()).rotate(settings.getRotation());

                // Handle block entities
                if (blockInfo.nbt() != null) {
                    BlockEntity blockEntity = world.getBlockEntity(blockPos);
                    Clearable.tryClear(blockEntity);
                    world.setBlock(blockPos, Blocks.BARRIER.defaultBlockState(), 20);
                }

                // Place the block in the world
                if (world.setBlock(blockPos, blockState, 2)) {
                    minX = Math.min(minX, blockPos.getX());
                    minY = Math.min(minY, blockPos.getY());
                    minZ = Math.min(minZ, blockPos.getZ());
                    maxX = Math.max(maxX, blockPos.getX());
                    maxY = Math.max(maxY, blockPos.getY());
                    maxZ = Math.max(maxZ, blockPos.getZ());
                    blockEntityDataList.add(Pair.of(blockPos, blockInfo.nbt()));

                    // Load block entity data
                    if (blockInfo.nbt() != null) {
                        BlockEntity blockEntity1 = world.getBlockEntity(blockPos);
                        if (blockEntity1 != null) {
                            if (blockEntity1 instanceof RandomizableContainerBlockEntity) {
                                blockInfo.nbt().putLong("LootTableSeed", random.nextLong());
                            }
                            blockEntity1.load(blockInfo.nbt());
                        }
                    }

                    // Handle fluid states
                    if (fluidState != null) {
                        if (blockState.getFluidState().isSource()) {
                            sourceLiquidPositions.add(blockPos);
                        } else if (blockState.getBlock() instanceof LiquidBlockContainer) {
                            ((LiquidBlockContainer) blockState.getBlock()).placeLiquid(world, blockPos, blockState, fluidState);
                            if (!fluidState.isSource()) {
                                liquidPositions.add(blockPos);
                            }
                        }
                    }
                }
            }
            currentIndex += 1;
        }

        public void finishedTick()
        {

        }


    }

}
