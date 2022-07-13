package com.github.sculkhoard.common.tileentity;

import com.github.sculkhoard.common.block.SculkBeeNestBlock;
import com.github.sculkhoard.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhoard.core.EntityRegistry;
import com.github.sculkhoard.core.TileEntityRegistry;
import com.google.common.collect.Lists;
import net.minecraft.block.BlockState;
import net.minecraft.block.CampfireBlock;
import net.minecraft.block.FireBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.passive.BeeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.nbt.NBTUtil;
import net.minecraft.network.DebugPacketSender;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;

public class SculkBeeNestTile extends TileEntity implements ITickableTileEntity {

    private final List<SculkBeeNestTile.Bee> stored = Lists.newArrayList();
    @Nullable
    private BlockPos savedFlowerPos = null;

    public SculkBeeNestTile() {
        super(TileEntityRegistry.SCULK_BEE_NEST_TILE.get());
    }

    /**
     * For tile entities, ensures the chunk containing the tile entity is saved to disk later - the game won't think it
     * hasn't changed and skip it.
     */
    public void setChanged()
    {
        if (this.isFireNearby())
        {
            this.emptyAllLivingFromHive((PlayerEntity)null, this.level.getBlockState(this.getBlockPos()), SculkBeeNestTile.State.EMERGENCY);
        }

        super.setChanged();
    }

    public boolean isFireNearby()
    {
        if (this.level == null)
        {
            return false;
        }
        else
        {
            for(BlockPos blockpos : BlockPos.betweenClosed(this.worldPosition.offset(-1, -1, -1), this.worldPosition.offset(1, 1, 1))) {
                if (this.level.getBlockState(blockpos).getBlock() instanceof FireBlock)
                {
                    return true;
                }
            }

            return false;
        }
    }

    public boolean isEmpty() {
        return this.stored.isEmpty();
    }

    public boolean isFull() {
        return this.stored.size() == 3;
    }

    public void emptyAllLivingFromHive(@Nullable PlayerEntity pPlayer, BlockState pState, SculkBeeNestTile.State pReleaseStatus)
    {
        List<Entity> list = this.releaseAllOccupants(pState, pReleaseStatus);
        if (pPlayer != null)
        {
            for(Entity entity : list)
            {
                if (entity instanceof SculkBeeHarvesterEntity)
                {
                    SculkBeeHarvesterEntity beeentity = (SculkBeeHarvesterEntity)entity;
                    if (pPlayer.position().distanceToSqr(entity.position()) <= 16.0D)
                    {
                        if (!this.isSedated())
                        {
                            beeentity.setTarget(pPlayer);
                        }
                        else
                        {
                            beeentity.setStayOutOfHiveCountdown(400);
                        }
                    }
                }
            }
        }

    }

    private List<Entity> releaseAllOccupants(BlockState pState, SculkBeeNestTile.State pReleaseStatus) {
        List<Entity> list = Lists.newArrayList();
        this.stored.removeIf((p_226966_4_) -> {
            return this.releaseOccupant(pState, p_226966_4_, list, pReleaseStatus);
        });
        return list;
    }

    public void addOccupant(Entity p_226961_1_, boolean p_226961_2_) {
        this.addOccupantWithPresetTicks(p_226961_1_, p_226961_2_, 0);
    }

    public int getOccupantCount() {
        return this.stored.size();
    }

    public static int getHoneyLevel(BlockState pState) {
        return pState.getValue(SculkBeeNestBlock.HONEY_LEVEL);
    }

    public boolean isSedated() {
        return CampfireBlock.isSmokeyPos(this.level, this.getBlockPos());
    }


    public void addOccupantWithPresetTicks(Entity entityIn, boolean p_226962_2_, int p_226962_3_)
    {
        if (this.stored.size() < 3)
        {
            entityIn.stopRiding();
            entityIn.ejectPassengers();
            CompoundNBT compoundnbt = new CompoundNBT();
            entityIn.save(compoundnbt);
            this.stored.add(new SculkBeeNestTile.Bee(compoundnbt, p_226962_3_, p_226962_2_ ? 2400 : 600));
            this.stored.get(0);
            if (this.level != null)
            {
                if (entityIn instanceof SculkBeeHarvesterEntity)
                {
                    SculkBeeHarvesterEntity beeentity = (SculkBeeHarvesterEntity)entityIn;
                    if (beeentity.hasSavedFlowerPos() && (!this.hasSavedFlowerPos() || this.level.random.nextBoolean()))
                    {
                        this.savedFlowerPos = beeentity.getSavedFlowerPos();
                    }
                }

                BlockPos blockpos = this.getBlockPos();
                this.level.playSound((PlayerEntity)null, (double)blockpos.getX(), (double)blockpos.getY(), (double)blockpos.getZ(), SoundEvents.BEEHIVE_ENTER, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }

            entityIn.remove();
        }
    }

    private boolean releaseOccupant(BlockState p_235651_1_, SculkBeeNestTile.Bee p_235651_2_, @Nullable List<Entity> p_235651_3_, SculkBeeNestTile.State state) {
        if ((this.level.isNight() || this.level.isRaining()) && state != SculkBeeNestTile.State.EMERGENCY)
        {
            return false;
        }
        else
        {
            BlockPos blockpos = this.getBlockPos();
            CompoundNBT compoundnbt = p_235651_2_.entityData;
            compoundnbt.remove("Passengers");
            compoundnbt.remove("Leash");
            compoundnbt.remove("UUID");
            Direction direction = p_235651_1_.getValue(SculkBeeNestBlock.FACING);
            BlockPos blockpos1 = blockpos.relative(direction);
            boolean flag = !this.level.getBlockState(blockpos1).getCollisionShape(this.level, blockpos1).isEmpty();
            if (flag && state != SculkBeeNestTile.State.EMERGENCY)
            {
                return false;
            }
            else
            {
                Entity entity = EntityType.loadEntityRecursive(compoundnbt, this.level, (p_226960_0_) -> {
                    return p_226960_0_;
                });
                if (entity != null)
                {

                    if (entity instanceof SculkBeeHarvesterEntity)
                    {
                        SculkBeeHarvesterEntity beeentity = (SculkBeeHarvesterEntity)entity;
                        if (this.hasSavedFlowerPos()
                                && !beeentity.hasSavedFlowerPos()
                                && this.level.random.nextFloat() < 0.9F)
                        {
                            beeentity.setSavedFlowerPos(this.savedFlowerPos);
                        }

                        if (state == SculkBeeNestTile.State.HONEY_DELIVERED)
                        {
                            beeentity.dropOffNectar();
                            if (p_235651_1_.getBlock().is(BlockTags.BEEHIVES))
                            {
                                int i = getHoneyLevel(p_235651_1_);
                                if (i < 5) {
                                    int j = this.level.random.nextInt(100) == 0 ? 2 : 1;
                                    if (i + j > 5) {
                                        --j;
                                    }

                                    this.level.setBlockAndUpdate(this.getBlockPos(), p_235651_1_.setValue(SculkBeeNestBlock.HONEY_LEVEL, Integer.valueOf(i + j)));
                                }
                            }
                        }

                        this.setBeeReleaseData(p_235651_2_.ticksInHive, beeentity);

                        if (p_235651_3_ != null)
                        {
                            p_235651_3_.add(beeentity);
                        }

                        float f = entity.getBbWidth();
                        double d3 = flag ? 0.0D : 0.55D + (double)(f / 2.0F);
                        double d0 = (double)blockpos.getX() + 0.5D + d3 * (double)direction.getStepX();
                        double d1 = (double)blockpos.getY() + 0.5D - (double)(entity.getBbHeight() / 2.0F);
                        double d2 = (double)blockpos.getZ() + 0.5D + d3 * (double)direction.getStepZ();
                        entity.moveTo(d0, d1, d2, entity.yRot, entity.xRot);
                    }

                    this.level.playSound((PlayerEntity)null, blockpos, SoundEvents.BEEHIVE_EXIT, SoundCategory.BLOCKS, 1.0F, 1.0F);
                    return this.level.addFreshEntity(entity);

                }
                else
                {
                    return false;
                }
            }
        }
    }

    private void setBeeReleaseData(int p_235650_1_, SculkBeeHarvesterEntity p_235650_2_) {
        p_235650_2_.resetTicksWithoutNectarSinceExitingHive();
    }

    private boolean hasSavedFlowerPos() {
        return this.savedFlowerPos != null;
    }

    private void tickOccupants() {
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

    public void tick()
    {
        if (!this.level.isClientSide)
        {
            this.tickOccupants();
            BlockPos blockpos = this.getBlockPos();
            if (this.stored.size() > 0 && this.level.getRandom().nextDouble() < 0.005D)
            {
                double d0 = (double)blockpos.getX() + 0.5D;
                double d1 = (double)blockpos.getY();
                double d2 = (double)blockpos.getZ() + 0.5D;
                this.level.playSound((PlayerEntity)null, d0, d1, d2, SoundEvents.BEEHIVE_WORK, SoundCategory.BLOCKS, 1.0F, 1.0F);
            }
        }
    }

    public void load(BlockState p_230337_1_, CompoundNBT p_230337_2_) {
        super.load(p_230337_1_, p_230337_2_);
        this.stored.clear();
        ListNBT listnbt = p_230337_2_.getList("Bees", 10);

        for(int i = 0; i < listnbt.size(); ++i) {
            CompoundNBT compoundnbt = listnbt.getCompound(i);
            SculkBeeNestTile.Bee sculkbeenesttile$bee = new SculkBeeNestTile.Bee(compoundnbt.getCompound("EntityData"), compoundnbt.getInt("TicksInHive"), compoundnbt.getInt("MinOccupationTicks"));
            this.stored.add(sculkbeenesttile$bee);
        }

        this.savedFlowerPos = null;
        if (p_230337_2_.contains("FlowerPos")) {
            this.savedFlowerPos = NBTUtil.readBlockPos(p_230337_2_.getCompound("FlowerPos"));
        }

    }

    public CompoundNBT save(CompoundNBT pCompound) {
        super.save(pCompound);
        pCompound.put("Bees", this.writeBees());
        if (this.hasSavedFlowerPos()) {
            pCompound.put("FlowerPos", NBTUtil.writeBlockPos(this.savedFlowerPos));
        }

        return pCompound;
    }

    public ListNBT writeBees() {
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
        EMERGENCY;
    }

}
