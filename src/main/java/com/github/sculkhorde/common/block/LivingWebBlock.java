package com.github.sculkhorde.common.block;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SculkVeinBlock;

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
}
