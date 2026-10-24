/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.AABB;

/** Client side: the areas architect tables are scanning, and the blocks they scanned recently (which fade out). Drawn
 * by {@code RenderArchitectTables}. */
public final class ClientArchitectTables {
    public static final int START_BOX_VALUE = 3;
    public static final int START_SCANNED_BLOCK_VALUE = 50;
    public static final Map<AABB, Integer> BOXES = new HashMap<>();
    public static final Map<BlockPos, Integer> SCANNED_BLOCKS = new HashMap<>();

    private ClientArchitectTables() {}

    public static void tick() {
        BOXES.entrySet().forEach(entry -> entry.setValue(entry.getValue() - 1));
        BOXES.values().removeIf(i -> i <= 0);
        SCANNED_BLOCKS.entrySet().forEach(entry -> entry.setValue(entry.getValue() - 1));
        SCANNED_BLOCKS.values().removeIf(i -> i <= 0);
    }

    public static void clear() {
        BOXES.clear();
        SCANNED_BLOCKS.clear();
    }
}
