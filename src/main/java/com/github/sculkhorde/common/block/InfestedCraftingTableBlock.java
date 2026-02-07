package com.github.sculkhorde.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.CraftingMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.CraftingTableBlock;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;

public class InfestedCraftingTableBlock extends CraftingTableBlock {

    private static final Component CONTAINER_TITLE = Component.translatable("container.crafting");

    public InfestedCraftingTableBlock()
    {
        this(getProperties());
    }

    public InfestedCraftingTableBlock(Properties properties) {
        super(properties);
    }

    /**
     * Determines the properties of a block.<br>
     * I made this in order to be able to establish a block's properties from within the block class and not in the BlockRegistry.java
     * @return The Properties of the block
     */
    public static Properties getProperties()
    {
        return Properties.of()
                .mapColor(MapColor.TERRACOTTA_BLUE)
                .strength(4f, 30f)//Hardness & Resistance
                .destroyTime(5f)
                .requiresCorrectToolForDrops()
                .sound(SoundType.WOOD);
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand interactionHand, BlockHitResult blockHitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
            player.awardStat(Stats.INTERACT_WITH_CRAFTING_TABLE);
            return InteractionResult.CONSUME;
        }
    }

    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        return new SimpleMenuProvider((id, inventory, player) -> {
            return new CraftingMenu(id, inventory, ContainerLevelAccess.create(level, blockPos)) {
                @Override
                public boolean stillValid(Player playerEntity) {
                    // This ensures the menu stays open when interacting with your specific block
                    return stillValid(ContainerLevelAccess.create(level, blockPos), playerEntity, InfestedCraftingTableBlock.this);
                }
            };
        }, CONTAINER_TITLE);
    }
}
