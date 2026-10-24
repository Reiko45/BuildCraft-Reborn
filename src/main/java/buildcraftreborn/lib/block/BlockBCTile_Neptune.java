/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;

import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** A BuildCraft block with a {@link TileBC_Neptune}. Forwards placement, removal, use and neighbour changes to the
 * tile, and ticks tiles that implement {@link ITickable}. */
public abstract class BlockBCTile_Neptune extends BlockBCBase_Neptune implements EntityBlock {
    /** Whether this block's tiles implement {@link ITickable}; found out from the first tile created. */
    @Nullable
    private Boolean tileTicks;

    public BlockBCTile_Neptune(Properties properties) {
        super(properties);
    }

    @Override
    public abstract TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state);

    @Override
    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state,
        BlockEntityType<T> type) {
        if (tileTicks == null) {
            tileTicks = newBlockEntity(BlockPos.ZERO, state) instanceof ITickable;
        }
        if (!tileTicks) {
            return null;
        }
        return (lvl, pos, st, tile) -> {
            if (tile instanceof ITickable tickable) {
                tickable.update();
            }
        };
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof TileBC_Neptune tile) {
            tile.onRemove();
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer,
        ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.getBlockEntity(pos) instanceof TileBC_Neptune tile) {
            tile.onPlacedBy(placer, stack);
        }
    }

    /** 1.12 semantics: the tile decides everything in {@link TileBC_Neptune#onActivated}. If it returns false the
     * item gets its turn (so a wrench can rotate the block), and nothing else happens: tiles with a GUI open it
     * themselves (see {@link TileBC_Neptune#openGui(Player)}). */
    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos,
        Player player, InteractionHand hand, BlockHitResult hit) {
        if (level.getBlockEntity(pos) instanceof TileBC_Neptune tile) {
            if (tile.onActivated(player, hand, stack, hit)) {
                return ItemInteractionResult.sidedSuccess(level.isClientSide);
            }
            return ItemInteractionResult.SKIP_DEFAULT_BLOCK_INTERACTION;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block block, BlockPos fromPos,
        boolean isMoving) {
        super.neighborChanged(state, level, pos, block, fromPos, isMoving);
        if (level.getBlockEntity(pos) instanceof TileBC_Neptune tile) {
            tile.onNeighbourBlockChanged(block, fromPos);
        }
    }
}
