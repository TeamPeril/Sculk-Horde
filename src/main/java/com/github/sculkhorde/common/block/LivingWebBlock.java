package com.github.sculkhorde.common.block;

import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkVeinBlock;
import net.minecraft.world.level.block.state.BlockState;

public class LivingWebBlock extends SculkVeinBlock {
    public LivingWebBlock(Properties properties) {
        super(properties);
    }

    public LivingWebBlock()
    {
        this(getProperties());
    }

    public static Properties getProperties()
    {
        return Properties.copy(Blocks.SCULK_VEIN);
    }

    @Override
    public boolean canBeReplaced(BlockState p_222381_, BlockPlaceContext p_222382_) {
        return true;
    }
}
