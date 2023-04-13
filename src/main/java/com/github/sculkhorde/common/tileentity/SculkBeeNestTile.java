package com.github.sculkhorde.common.tileentity;

import com.github.sculkhorde.common.block.SculkBeeNestBlock;
import com.github.sculkhorde.common.entity.SculkBeeHarvesterEntity;
import com.github.sculkhorde.common.entity.infection.CursorInfectorEntity;
import com.github.sculkhorde.common.procedural.structures.SculkBeeNestProceduralStructure;
import com.github.sculkhorde.core.SculkHorde;
import com.github.sculkhorde.core.TileEntityRegistry;
import com.google.common.collect.Lists;
import net.minecraft.world.level.block.entity.BeehiveBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.CampfireBlock;
import net.minecraft.world.level.block.FireBlock;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import javax.annotation.Nullable;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SculkBeeNestTile extends BeehiveBlockEntity
{
    public SculkBeeNestTile(BlockPos p_155134_, BlockState p_155135_) {
        super(p_155134_, p_155135_);
    }
}
