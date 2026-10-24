/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.tile;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.event.level.BlockEvent;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.core.SafeTimeTracker;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.inventory.AutomaticProvidingTransactor;
import buildcraftreborn.lib.misc.BlockUtil;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.misc.InventoryUtil;
import buildcraftreborn.lib.mj.MjBatteryReceiver;

/** Digs straight down, one block at a time, and pushes what it mines into neighbouring inventories (or drops it).
 * <p>
 * 1.21: 1.12 listened for block changes with a world event listener, to start digging again as soon as something
 * changes in the column below. Here a {@link BlockEvent.NeighborNotifyEvent} listener does the same job (see
 * {@link #onNeighbourNotify}). */
public class TileMiningWell extends TileMiner {
    /** Loaded mining wells (server side), per level and per column. */
    private static final Map<LevelAccessor, Long2ObjectMap<List<TileMiningWell>>> LOADED = new WeakHashMap<>();

    private boolean shouldCheck = true;
    private final SafeTimeTracker tracker = new SafeTimeTracker(256);

    public TileMiningWell(BlockPos pos, BlockState state) {
        super(BCFactoryBlocks.MINING_WELL_TILE.get(), pos, state);
        caps.addCapabilityInstance(CapUtil.CAP_ITEM_TRANSACTOR, AutomaticProvidingTransactor.INSTANCE,
            EnumPipePart.VALUES);
    }

    @Override
    protected void mine() {
        if (currentPos != null && canBreak()) {
            shouldCheck = true;
            long target = BlockUtil.computeBlockBreakPower(level, currentPos);
            progress += battery.extractPower(0, target - progress);
            if (progress >= target) {
                progress = 0;
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
                BlockUtil.breakBlockAndGetDrops((ServerLevel) level, currentPos, new ItemStack(Items.DIAMOND_PICKAXE),
                    getOwner()).ifPresent(stacks ->
                        stacks.forEach(stack -> InventoryUtil.addToBestAcceptor(level, worldPosition, null, stack)));
                nextPos();
            } else {
                if (!level.isEmptyBlock(currentPos)) {
                    level.destroyBlockProgress(currentPos.hashCode(), currentPos, (int) ((progress * 9) / target));
                }
            }
        } else if (shouldCheck || tracker.markTimeIfDelay(level)) {
            nextPos();
            if (currentPos == null) {
                shouldCheck = false;
            }
        }
    }

    private boolean canBreak() {
        if (level.isEmptyBlock(currentPos) || BlockUtil.isUnbreakableBlock(level, currentPos, getOwner())) {
            return false;
        }
        Fluid fluid = BlockUtil.getFluidWithFlowing(level, currentPos);
        return fluid == null || fluid.getFluidType().getViscosity() <= 1000;
    }

    private void nextPos() {
        currentPos = worldPosition;
        while (true) {
            currentPos = currentPos.below();
            if (level.isOutsideBuildHeight(currentPos)) {
                break;
            }
            if (worldPosition.getY() - currentPos.getY() > BCLibConfig.miningMaxDepth()) {
                break;
            }
            if (canBreak()) {
                updateLength();
                return;
            } else if (!level.isEmptyBlock(currentPos)
                && level.getBlockState(currentPos).getBlock() != BCFactoryBlocks.TUBE.get()) {
                break;
            }
        }
        currentPos = null;
        updateLength();
    }

    // Tracking changes in the column

    @Override
    public void onLoad() {
        super.onLoad();
        if (!level.isClientSide) {
            LOADED.computeIfAbsent(level, l -> new Long2ObjectOpenHashMap<>())
                .computeIfAbsent(columnKey(worldPosition), k -> new ArrayList<>()).add(this);
        }
    }

    @Override
    public void setRemoved() {
        super.setRemoved();
        if (level != null && !level.isClientSide) {
            untrack();
            if (currentPos != null) {
                level.destroyBlockProgress(currentPos.hashCode(), currentPos, -1);
            }
        }
    }

    @Override
    public void onChunkUnloaded() {
        super.onChunkUnloaded();
        if (level != null && !level.isClientSide) {
            untrack();
        }
    }

    private void untrack() {
        Long2ObjectMap<List<TileMiningWell>> columns = LOADED.get(level);
        if (columns != null) {
            long key = columnKey(worldPosition);
            List<TileMiningWell> wells = columns.get(key);
            if (wells != null) {
                wells.remove(this);
                if (wells.isEmpty()) {
                    columns.remove(key);
                }
            }
        }
    }

    private static long columnKey(BlockPos pos) {
        return ChunkPos.asLong(pos.getX(), pos.getZ());
    }

    /** A block changed: wake up any mining well above it in the same column. */
    public static void onNeighbourNotify(BlockEvent.NeighborNotifyEvent event) {
        if (event.getLevel().isClientSide()) {
            return;
        }
        Long2ObjectMap<List<TileMiningWell>> columns = LOADED.get(event.getLevel());
        if (columns == null) {
            return;
        }
        BlockPos pos = event.getPos();
        List<TileMiningWell> wells = columns.get(columnKey(pos));
        if (wells != null) {
            for (TileMiningWell well : wells) {
                if (pos.getY() <= well.worldPosition.getY()) {
                    well.shouldCheck = true;
                }
            }
        }
    }

    @Override
    protected IMjReceiver createMjReceiver() {
        return new MjBatteryReceiver(battery);
    }
}
