/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker;

import net.minecraft.world.level.Level;

import buildcraftreborn.lib.marker.MarkerCache;

public class PathCache extends MarkerCache<PathSubCache> {
    public static final PathCache INSTANCE = new PathCache();

    private PathCache() {
        super("path");
    }

    @Override
    protected PathSubCache createSubCache(Level world) {
        return new PathSubCache(world);
    }
}
