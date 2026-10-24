/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.tiles;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.capabilities.BlockCapability;

/** Block capabilities for the tile interfaces. In 1.12 these were Forge {@code Capability} objects registered through
 * {@code CapabilityManager}; in 1.21 they are NeoForge {@link BlockCapability}s, queried with
 * {@code level.getCapability(TilesAPI.CAP_HAS_WORK, pos, side)}. */
public final class TilesAPI {
    public static final BlockCapability<IControllable, @Nullable Direction> CAP_CONTROLLABLE =
        BlockCapability.createSided(id("controllable"), IControllable.class);
    public static final BlockCapability<IHasWork, @Nullable Direction> CAP_HAS_WORK =
        BlockCapability.createSided(id("has_work"), IHasWork.class);
    /** Volume markers (and later anything else that marks out an area for quarries and builders). */
    public static final BlockCapability<ITileAreaProvider, @Nullable Direction> CAP_TILE_AREA_PROVIDER =
        BlockCapability.createSided(id("tile_area_provider"), ITileAreaProvider.class);

    private TilesAPI() {}

    private static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath("buildcraftreborn", path);
    }
}
