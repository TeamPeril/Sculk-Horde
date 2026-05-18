package com.github.sculkhorde.common.blockentity;

import com.github.sculkhorde.common.entity.InfestationPurifierEntity;
import com.github.sculkhorde.common.entity.SculkBroodlingEntity;
import com.github.sculkhorde.common.entity.SculkBroodHatcherEntity;
import com.github.sculkhorde.common.entity.infection.CursorSurfacePurifierEntity;
import com.github.sculkhorde.core.ModBlockEntities;
import com.github.sculkhorde.core.ModBlocks;
import com.github.sculkhorde.core.ModEntities;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.systems.cursor_system.CursorSystem;
import com.github.sculkhorde.systems.cursor_system.VirtualWebSpreadCursor;
import com.github.sculkhorde.systems.gravemind_system.Gravemind;
import com.github.sculkhorde.systems.infestation_systems.block_infestation_system.BlockInfestationSystem;
import com.github.sculkhorde.util.BlockAlgorithms;
import com.github.sculkhorde.util.EntityAlgorithms;
import com.github.sculkhorde.util.TickUnits;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.GameEventTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.BlockPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.PositionSource;
import net.minecraft.world.level.gameevent.vibrations.VibrationSystem;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Random;
import java.util.UUID;

public class BroodNestBlockEntity extends BlockEntity implements GameEventListener.Holder<VibrationSystem.Listener>, VibrationSystem{
    protected long lastTickTime = 0;
    protected int minTickInterval = TickUnits.convertSecondsToTicks(15);

    public ArrayList<LivingEntity> spawnedEntities = new ArrayList<>();
    public final int MAX_ENTITIES = 6;

    private boolean isBroodHatcherInside = true;
    private UUID nestUUID = UUID.randomUUID();
    private UUID hatcherUUID = null;
    

    // Vibration Code
    private final VibrationSystem.User vibrationUser = new BroodNestBlockEntity.VibrationUser(this);
    private VibrationSystem.Data vibrationData = new VibrationSystem.Data();
    private final VibrationSystem.Listener vibrationListener = new VibrationSystem.Listener(this);

    /**
     * The Constructor that takes in properties
     */
    public BroodNestBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.BROOD_NEST_BLOCK_ENTITY.get(), pos, state);
    }


    public void spawnBroodlings(int amount)
    {
        ArrayList<BlockPos> spawnPos = getSpawnPositionsInCube((ServerLevel) level, getBlockPos(), 2, amount);

        if(spawnPos.isEmpty()) { return; }

        for(BlockPos pos : spawnPos)
        {
            if(spawnedEntities.size() >= MAX_ENTITIES)
            {
                break;
            }

            SculkBroodlingEntity broodling = new SculkBroodlingEntity(level, pos);
            level.addFreshEntity(broodling);
            spawnedEntities.add(broodling);
        }
    }

    public void summonBroodHatcher()
    {
        if(!isBroodHatcherInside || level.isClientSide)
        {
            return;
        }

        BlockPos spawnPos = getBlockPos().above();
        SculkBroodHatcherEntity hatcher = ModEntities.SCULK_BROOD_HATCHER.get().create(level);
        if(hatcher != null)
        {
            hatcher.setPos(spawnPos.getX() + 0.5D, spawnPos.getY(), spawnPos.getZ() + 0.5D);
            CompoundTag entityData = hatcher.getPersistentData();
            entityData.putUUID("nestID", nestUUID);
            entityData.putInt("nestX", getBlockPos().getX());
            entityData.putInt("nestY", getBlockPos().getY());
            entityData.putInt("nestZ", getBlockPos().getZ());

            level.addFreshEntity(hatcher);
            isBroodHatcherInside = false;
            hatcherUUID = hatcher.getUUID();
            setChanged();
        }
    }

    public void occupyNest(SculkBroodHatcherEntity hatcher)
    {
        if(isBroodHatcherInside || level.isClientSide)
        {
            return;
        }

        isBroodHatcherInside = true;
        hatcherUUID = null;
        hatcher.discard();
        setChanged();
    }

    public void tick()
    {
        if(level.isClientSide || !isBroodHatcherInside)
        {
            return;
        }

        if(level.getGameTime() % 20 == 0)
        {
            AABB searchArea = new AABB(getBlockPos()).inflate(10);
            boolean tntDetected = !level.getEntitiesOfClass(PrimedTnt.class, searchArea).isEmpty();
            boolean purificationCursorDetected = !level.getEntitiesOfClass(CursorSurfacePurifierEntity.class, searchArea).isEmpty();
            boolean infestationPurifierDetected = !level.getEntitiesOfClass(InfestationPurifierEntity.class, searchArea).isEmpty();

            if(tntDetected || purificationCursorDetected || infestationPurifierDetected)
            {
                summonBroodHatcher();
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
    public ArrayList<BlockPos> getSpawnPositionsInCube(ServerLevel worldIn, BlockPos origin, int length, int amountOfPositions)
    {
        ArrayList<BlockPos> listOfPossibleSpawns = getSpawnPositions(worldIn, origin, length);
        ArrayList<BlockPos> finalList = new ArrayList<>();
        Random rng = new Random();
        for(int count = 0; count < amountOfPositions && !listOfPossibleSpawns.isEmpty(); count++)
        {
            int randomIndex = rng.nextInt(listOfPossibleSpawns.size());
            //Get random position between 0 and size of list
            finalList.add(listOfPossibleSpawns.get(randomIndex));
            listOfPossibleSpawns.remove(randomIndex);
        }
        return finalList;
    }

    /**
     * Returns true if the block below is a sculk block,
     * and if the two blocks above it are free.
     * @param worldIn The World
     * @param pos The Position to spawn the entity
     * @return True/False
     */
    public boolean isValidSpawnPosition(ServerLevel worldIn, BlockPos pos)
    {
        BlockState belowBlock = worldIn.getBlockState(pos.below());

        boolean isBlockBelowCurableOrNest = BlockInfestationSystem.isCurable(worldIn, pos.below()) || belowBlock.is(ModBlocks.BROOD_NEST_BLOCK.get());
        boolean isBaseBlockNotSolid = BlockAlgorithms.isNotSolid(worldIn, pos);
        boolean isBlockAboveNotSolid = BlockAlgorithms.isNotSolid(worldIn, pos.above());

        return isBlockBelowCurableOrNest && isBaseBlockNotSolid && isBlockAboveNotSolid;

    }

    /**
     * Finds the location of the nearest block given a BlockPos predicate.
     * @param worldIn The world
     * @param origin The origin of the search location
     * @param pDistance The search distance
     * @return The position of the block
     */
    public ArrayList<BlockPos> getSpawnPositions(ServerLevel worldIn, BlockPos origin, double pDistance)
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
                        if (origin.closerThan(temp, pDistance) && isValidSpawnPosition(worldIn, temp))
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

    /* ~~~~~~~~ Save/Load Events ~~~~~~~~  */

    public void load(CompoundTag nbt) {
        super.load(nbt);

        if (nbt.contains("listener", 10)) {
            VibrationSystem.Data.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, nbt.getCompound("listener"))).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((data) -> {
                this.vibrationData = data;
            });
        }

        isBroodHatcherInside = nbt.getBoolean("isBroodHatcherInside");
        if(nbt.hasUUID("nestUUID"))
        {
            nestUUID = nbt.getUUID("nestUUID");
        }

        if(nbt.hasUUID("hatcherUUID"))
        {
            hatcherUUID = nbt.getUUID("hatcherUUID");
        }

    }

    protected void saveAdditional(CompoundTag nbt)
    {
        super.saveAdditional(nbt);
        VibrationSystem.Data.CODEC.encodeStart(NbtOps.INSTANCE, this.vibrationData).resultOrPartial(SculkHorde.LOGGER::error).ifPresent((p_222871_) -> {
            nbt.put("listener", p_222871_);
        });

        nbt.putBoolean("isBroodHatcherInside", isBroodHatcherInside);
        nbt.putUUID("nestUUID", nestUUID);
        if(hatcherUUID != null)
        {
            nbt.putUUID("hatcherUUID", hatcherUUID);
        }
    }

    /* ~~~~~~~~ Vibration Events ~~~~~~~~  */

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
     * The listener for the sculk summoner block entity.
     */
    class VibrationUser implements VibrationSystem.User
    {
        private static final int LISTENER_RADIUS = 24;
        private final PositionSource positionSource = new BlockPositionSource(BroodNestBlockEntity.this.worldPosition);
        private final BroodNestBlockEntity broodNest;

        public VibrationUser(BroodNestBlockEntity broodNestIn) {
            this.broodNest = broodNestIn;
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

        public boolean canReceiveVibration(ServerLevel level, BlockPos pos, GameEvent event, GameEvent.Context context) {

            // If world is not a server world, return
            if(level.isClientSide)
            {
                return false;
            }

            if(!Gravemind.isGravemindActive())
            {
                return false;
            }

            if(broodNest.spawnedEntities.size() >= broodNest.MAX_ENTITIES)
            {
                return false;
            }

            if(SculkHorde.populationHandler.isPopulationAtMax())
            {
                return false;
            }


            // Delay the spawning of mobs for balance
            if(broodNest.lastTickTime == 0)
            {
                broodNest.lastTickTime = level.getGameTime();
            }

            if(!TickUnits.hasTicksPassed(broodNest.lastTickTime, level, broodNest.minTickInterval))
            {
                return false;
            }

            return true;
        }

        public void onReceiveVibration(ServerLevel level, BlockPos blockPos, GameEvent gameEvent, @Nullable Entity entity, @Nullable Entity entity1, float power)
        {
            broodNest.lastTickTime = level.getGameTime();
            VirtualWebSpreadCursor cursor = CursorSystem.createWebSpreadCursor(level, broodNest.worldPosition);
            cursor.setMaxTransformations(10);
            cursor.setMaxRange(30);
            cursor.setMaxLifeTimeTicks(TickUnits.convertMinutesToTicks(2));
            // Spawn Spiders
            if(EntityAlgorithms.getNonSculkEntitiesAtBlockPos((ServerLevel) level, blockPos, 16).isEmpty())
            {
                return;
            }

            broodNest.spawnBroodlings(3);
        }

        public void onDataChanged()
        {
            setChanged();
        }

        public boolean requiresAdjacentChunksToBeTicking() {
            return true;
        }
    }
}
