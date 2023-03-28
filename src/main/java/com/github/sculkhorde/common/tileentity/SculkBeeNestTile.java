package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.block.SculkBeeNestBlock;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.common.procedural.structures.SculkBeeNestProceduralStructure;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.TileEntityRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.server.ServerWorld;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SculkBeeNestTile extends TileEntity implements ITickableTileEntity
{
    //This is a list of all the bees in the structure
    private final List<SculkBeeNestTile.Bee> stored = Lists.newArrayList();

    //The procedural structure that will be built
    private SculkBeeNestProceduralStructure beeNestStructure;

    //Used for ticking this block at an interval
    private int tickTracker = 0;

    //The Maximum amount of honey this block can store
    protected final int MAX_HONEY_LEVEL = 5;

    protected final int MIN_TICKS_IN_HIVE = 20 * 60 * 5; //5 Minutes

    //Keep track of last time since repair so we know when to restart
    private long lastTimeSinceRepair = -1;

    /**
     * Default Constructor
     */
    public SculkBeeNestTile()
    {
        super(TileEntityRegistry.SCULK_BEE_NEST_TILE.get());
    }

    /* ### Setter Methods ### */

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void setChanged()
    {
        if (this.isFireNearby())
        {
            this.emptyAllLivingFromHive(null, this.level.getBlockState(this.getBlockPos()), SculkBeeNestTile.State.EMERGENCY);
        }

        super.setChanged();
    }

    public static int getHoneyLevel(BlockState pState) {
        return pState.getValue(SculkBeeNestBlock.HONEY_LEVEL);
    }


    public boolean isSedated()
    {
        assert this.level != null;
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }

    public boolean isFireNearby()
    {
        if (this.level == null) { return false; }

        for(BlockPos blockpos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1)))
        {
            if (this.level.getBlockState(blockpos).getBlock() instanceof FireBlock)
            {
                return true;
            }
        }
        return false;
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 10;
    }

    /** ### Functionality Methods ### */

    public void emptyAllLivingFromHive(@Nullable PlayerEntity pPlayer, BlockState pState, SculkBeeNestTile.State pReleaseStatus)
    {
        List<Entity> list = this.releaseAllOccupants(pState, pReleaseStatus);
        if (pPlayer != null) { return; }

        for(Entity entity : list)
        {
            if (! (entity instanceof SculkBeeHarvesterEntity)) { continue; }
            if (pPlayer.position().distanceToSqr(entity.position()) > 16.0D) { continue; }

            SculkBeeHarvesterEntity beeentity = (SculkBeeHarvesterEntity)entity;

            if (!this.isSedated())
            {
                beeentity.setTarget(pPlayer);
                continue;
            }
            beeentity.setStayOutOfHiveCountdown(400);
        }
    }

    private List<Entity> releaseAllOccupants(BlockState blockState, SculkBeeNestTile.State releaseStatus) {
        // Declare a list to store the released entities
        List<Entity> releasedEntities = Lists.newArrayList();

        // Iterate through the stored occupants and release them
        this.stored.removeIf((occupant) -> this.releaseOccupant(blockState, occupant, releasedEntities, releaseStatus));

        // Return the list of released entities
        return releasedEntities;
    }

    public void addOccupant(Entity entityIn) {
        this.addOccupantWithPresetTicks(entityIn, 0);
    }

    public void addOccupantWithPresetTicks(Entity entityIn, int ticksInHive)
    {
        if (this.level == null) { return; }
        if (this.stored.size() >= 10) { return; }

        entityIn.stopRiding();
        entityIn.ejectPassengers();
        CompoundNBT compoundnbt = new CompoundNBT();
        entityIn.save(compoundnbt);
        this.stored.add(new SculkBeeNestTile.Bee(compoundnbt, ticksInHive, MIN_TICKS_IN_HIVE));
        this.stored.get(0);

        BlockPos blockpos = this.getBlockPos();
        this.level.playSound(null, blockpos.getX(), blockpos.getY(), blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
        entityIn.remove();

    }

    private boolean releaseOccupant(BlockState blockStateIn, SculkBeeNestTile.Bee entityIn, @Nullable List<Entity> entityListIn, SculkBeeNestTile.State state)
    {
        // If the gravemind has disabled this hive, do not release bees.
        if (SculkBeeNestBlock.isNestClosed(blockStateIn)) { return false; }

        //Check if front of hive is blocked
        Direction direction = blockStateIn.getValue(SculkBeeNestBlock.FACING);
        BlockPos frontOfHive = this.getBlockPos().relative(direction);

        boolean isEnteranceBlocked = !this.level.getBlockState(frontOfHive).getCollisionShape(this.level, frontOfHive).isEmpty();

        //IF front of hive is blocked and it is not an emergency, do not release occupant
        if (isEnteranceBlocked && state != SculkBeeNestTile.State.EMERGENCY) { return false; }

        CompoundNBT compoundnbt = entityIn.entityData;
        compoundnbt.remove("Passengers");
        compoundnbt.remove("Leash");
        compoundnbt.remove("UUID");

        //Load entity
        Entity entity = EntityType.loadEntityRecursive(compoundnbt, this.level, (p_226960_0_) -> {
            return p_226960_0_;
        });

        //If the entity isnt null
        if (entity == null) { return false; }
        //If the entity is not an instance of sculk bee
        if (!(entity instanceof SculkBeeHarvesterEntity)) { return false; }

        //Create entity
        SculkBeeHarvesterEntity beeentity = (SculkBeeHarvesterEntity)entity;

        //If honey is being delivered to nest
        if (state == SculkBeeNestTile.State.HONEY_DELIVERED)
        {
            beeentity.dropOffNectar(); //give hive nector

            int currentHoneyLevel = getHoneyLevel(blockStateIn); //Get Current Honey Level

            if(beeNestStructure != null)
            {
                beeNestStructure.makeRandomBlockMature();
            }

            // Spawn Infector Cursors
            for(int i = 0; i < getHoneyLevel(blockStateIn); i++)
            {
                CursorInfectorEntity cursor = new CursorInfectorEntity(this.level);
                cursor.setPos(getBlockPos().getX(), getBlockPos().getY(), getBlockPos().getZ());
                cursor.setMaxRange(100);
                cursor.setMaxInfections(100);
                level.addFreshEntity(cursor);
            }

            //If we have not reached max level
            if (currentHoneyLevel < MAX_HONEY_LEVEL)
            {
                //Increment honey level
                this.level.setBlockAndUpdate(this.getBlockPos(), blockStateIn.setValue(SculkBeeNestBlock.HONEY_LEVEL, Integer.valueOf(currentHoneyLevel + 1)));
            }
        }
        //Give bee appropriate data on release
        this.setBeeReleaseData(entityIn.ticksInHive, beeentity);

        //Keep track of released bee
        if (entityListIn != null)
        {
            entityListIn.add(beeentity);
        }

        //Set bee to be at front of hive and rotate it appropriately
        float f = entity.getBbWidth();
        double d3 = isEnteranceBlocked ? 0.0D : 0.55D + (double)(f / 2.0F);
        double d0 = (double)this.getBlockPos().getX() + 0.5D + d3 * (double)direction.getStepX();
        double d1 = (double)this.getBlockPos().getY() + 0.5D - (double)(entity.getBbHeight() / 2.0F);
        double d2 = (double)this.getBlockPos().getZ() + 0.5D + d3 * (double)direction.getStepZ();
        entity.moveTo(d0, d1, d2, entity.yRot, entity.xRot);


        //PlaySound
        this.level.playSound(null, this.getBlockPos(), SoundEvents.BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
        //Create bee
        return this.level.addFreshEntity(entity);

    }

    private void setBeeReleaseData(int p_235650_1_, SculkBeeHarvesterEntity p_235650_2_)
    {
        p_235650_2_.resetTicksWithoutNectarSinceExitingHive();
    }

    private void tickOccupants()
    {
        Iterator<SculkBeeNestTile.Bee> iterator = this.stored.iterator();

        SculkBeeNestTile.Bee sculkbeenesttile$bee;
        for(BlockState blockstate = this.getBlockState(); iterator.hasNext(); sculkbeenesttile$bee.ticksInHive++)
        {
            sculkbeenesttile$bee = iterator.next();
            if (sculkbeenesttile$bee.ticksInHive > sculkbeenesttile$bee.minOccupationTicks)
            {
                SculkBeeNestTile.State sculkbeenesttile$state = sculkbeenesttile$bee.entityData.getBoolean("HasNectar") ? SculkBeeNestTile.State.HONEY_DELIVERED : SculkBeeNestTile.State.BEE_RELEASED;
                if (this.releaseOccupant(blockstate, sculkbeenesttile$bee, (List<Entity>)null, sculkbeenesttile$state))
                {
                    iterator.remove();
                }
            }
        }

    }

    @Override
    public void tick()
    {
        if (this.level.isClientSide) { return; }

        this.tickOccupants();

        //Make Random Noises if there are bees inside
        BlockPos blockpos = this.getBlockPos();
        if (this.stored.size() > 0 && this.level.getRandom().nextDouble() < 0.005D)
        {
            double d0 = blockpos.getX() + 0.5D;
            double d1 = blockpos.getY();
            double d2 = blockpos.getZ() + 0.5D;
            this.level.playSound(null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
        }

        /** Check if full of honey**/
        //If full of honey, reset to 0 and add sculk mass to gravemind
        //TODO Fix lazy solution of -1. figure out why this never got triggered normally
        if(getHoneyLevel(this.getBlockState()) >= MAX_HONEY_LEVEL - 1)
        {
            this.level.setBlockAndUpdate(this.getBlockPos(), this.getBlockState().setValue(SculkBeeNestBlock.HONEY_LEVEL, 0));
            SculkHorde.gravemind.getGravemindMemory().addSculkAccumulatedMass(10);
        }

        /** Structure Building Process **/
        tickTracker++;
        //Tick Every 60 Seconds
        if(tickTracker < 20 * 60) { return; }

        tickTracker = 0;
        long timeElapsed = TimeUnit.MINUTES.convert(System.nanoTime() - lastTimeSinceRepair, TimeUnit.NANOSECONDS);

        //If the Bee Nest Structure hasnt been initialized yet, do it
        if(beeNestStructure == null)
        {
            //Create Structure
            beeNestStructure = new SculkBeeNestProceduralStructure((ServerWorld) this.level, this.getBlockPos());
            beeNestStructure.generatePlan();
        }

        //If currently building, call build tick.
        //Repair routine will restart after an hour
        long repairIntervalInMinutes = 30;
        if(beeNestStructure.isCurrentlyBuilding())
        {
            beeNestStructure.buildTick();
            lastTimeSinceRepair = System.nanoTime();
        }
        //If enough time has passed, or we havent built yet, start build
        else if((timeElapsed >= repairIntervalInMinutes || lastTimeSinceRepair == -1) && beeNestStructure.canStartToBuild())
        {
            beeNestStructure.startBuildProcedure();
        }
    }

    public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_)
    {
        super.load(p_230337_1_, p_230337_2_);
        this.stored.clear();
        ListNBT listnbt = p_230337_2_.getList("Bees", 10);

        for(int i = 0; i < listnbt.size(); ++i)
        {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            SculkBeeNestTile.Bee sculkbeenesttile$bee = new SculkBeeNestTile.Bee(compoundnbt.getCompound("EntityData"), compoundnbt.getInt("TicksInHive"), compoundnbt.getInt("MinOccupationTicks"));
            this.stored.add(sculkbeenesttile$bee);
        }
    }

    public CompoundNBT save(CompoundNBT pCompound)
    {
        super.save(pCompound);
        pCompound.put("Bees", this.writeBees());
        return pCompound;
    }

    public ListNBT writeBees()
    {
        ListNBT listnbt = new ListNBT();

        for(SculkBeeNestTile.Bee sculkbeenesttile$bee : this.stored) {
            sculkbeenesttile$bee.entityData.remove("UUID");
            CompoundNBT compoundnbt = new CompoundNBT();
            compoundnbt.put("EntityData", sculkbeenesttile$bee.entityData);
            compoundnbt.putInt("TicksInHive", sculkbeenesttile$bee.ticksInHive);
            compoundnbt.putInt("MinOccupationTicks", sculkbeenesttile$bee.minOccupationTicks);
            listnbt.add(compoundnbt);
        }

        return listnbt;
    }

    static class Bee {
        private final CompoundNBT entityData;
        private int ticksInHive;
        private final int minOccupationTicks;

        private Bee(CompoundNBT pEntityData, int pTicksInHive, int pMinOccupationTicks) {
            pEntityData.remove("UUID");
            this.entityData = pEntityData;
            this.ticksInHive = pTicksInHive;
            this.minOccupationTicks = pMinOccupationTicks;
        }
    }

    public static enum State {
        HONEY_DELIVERED,
        BEE_RELEASED,
        EMERGENCY,
        DISABLED;
    }

}
