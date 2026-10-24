/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntity;

import net.neoforged.neoforge.capabilities.BlockCapability;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.inventory.IItemTransactor;

/** BuildCraft's own capabilities that aren't part of the public API. (1.12's {@code CAP_ITEMS} and
 * {@code CAP_FLUIDS} are NeoForge's {@code Capabilities.ItemHandler.BLOCK} and {@code Capabilities.FluidHandler.BLOCK}.)
 * Every BuildCraft tile forwards these through {@code BCCapabilities}. */
public final class CapUtil {
    /** An {@link IItemTransactor} for a side of a block. Blocks that only offer an item handler get a wrapper (see
     * {@code ItemTransactorHelper}). */
    public static final BlockCapability<IItemTransactor, @Nullable Direction> CAP_ITEM_TRANSACTOR =
        BlockCapability.createSided(BuildCraftReborn.id("item_transactor"), IItemTransactor.class);

    private CapUtil() {}

    /** Gets a block capability of a block entity (1.12's {@code tile.getCapability(cap, side)}). 1.21 capabilities
     * belong to the block at a position, so this asks the block entity's level.
     *
     * @return The capability, or null if the tile is not in a level or does not offer it. */
    @Nullable
    public static <T> T getCapability(@Nullable BlockEntity tile, BlockCapability<T, @Nullable Direction> cap,
        @Nullable Direction side) {
        if (tile == null || tile.getLevel() == null || tile.isRemoved()) {
            return null;
        }
        return tile.getLevel().getCapability(cap, tile.getBlockPos(), tile.getBlockState(), tile, side);
    }

    /** @return True if the block entity offers the capability on the given side. */
    public static boolean hasCapability(@Nullable BlockEntity tile, BlockCapability<?, @Nullable Direction> cap,
        @Nullable Direction side) {
        return getCapability(tile, cap, side) != null;
    }
}
