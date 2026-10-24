/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.cap;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.world.level.block.entity.BlockEntityType;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;
import net.neoforged.neoforge.fluids.capability.wrappers.FluidBucketWrapper;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.item.ItemFragileFluidContainer;
import buildcraftreborn.lib.BCRegistries;
import buildcraftreborn.lib.fluid.BCBucketItem;
import buildcraftreborn.lib.misc.CapUtil;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Registers, for every BuildCraft block entity type, a provider for each capability a BuildCraft tile may expose.
 * Each provider forwards to the tile's own {@link TileBC_Neptune#getCapability(BlockCapability, Direction)}, so a
 * tile that doesn't offer a capability simply returns null. */
public final class BCCapabilities {
    /** Capabilities forwarded to BuildCraft tiles. Add here when a new capability type is ported. */
    public static final List<BlockCapability<?, @Nullable Direction>> FORWARDED = List.of(
        Capabilities.ItemHandler.BLOCK,
        Capabilities.FluidHandler.BLOCK,
        Capabilities.EnergyStorage.BLOCK,
        MjAPI.CAP_CONNECTOR,
        MjAPI.CAP_RECEIVER,
        MjAPI.CAP_REDSTONE_RECEIVER,
        MjAPI.CAP_READABLE,
        MjAPI.CAP_PASSIVE_PROVIDER,
        TilesAPI.CAP_CONTROLLABLE,
        TilesAPI.CAP_HAS_WORK,
        TilesAPI.CAP_TILE_AREA_PROVIDER,
        CapUtil.CAP_ITEM_TRANSACTOR,
        PipeApi.CAP_PIPE_HOLDER,
        PipeApi.CAP_PIPE,
        PipeApi.CAP_PLUG,
        PipeApi.CAP_INJECTABLE);

    private BCCapabilities() {}

    public static void register(RegisterCapabilitiesEvent event) {
        for (var holder : BCRegistries.TILE_TYPES) {
            BlockEntityType<? extends TileBC_Neptune> type = holder.get();
            for (BlockCapability<?, @Nullable Direction> cap : FORWARDED) {
                registerOne(event, cap, type);
            }
        }
        // NeoForge only gives buckets of exactly the BucketItem class a fluid handler, so BuildCraft's own bucket
        // class needs one registered here (otherwise they couldn't fill tanks).
        for (var item : BCRegistries.ITEMS.getEntries()) {
            if (item.get() instanceof BCBucketItem bucket) {
                event.registerItem(Capabilities.FluidHandler.ITEM, (stack, ctx) -> new FluidBucketWrapper(stack),
                    bucket);
            }
        }
        event.registerItem(Capabilities.FluidHandler.ITEM,
            (stack, ctx) -> new ItemFragileFluidContainer.FragileFluidHandler(stack),
            BCCoreItems.FRAGILE_FLUID_SHARD.get());
    }

    private static <T> void registerOne(RegisterCapabilitiesEvent event, BlockCapability<T, @Nullable Direction> cap,
        BlockEntityType<? extends TileBC_Neptune> type) {
        event.registerBlockEntity(cap, type, (tile, side) -> tile.getCapability(cap, side));
    }
}
