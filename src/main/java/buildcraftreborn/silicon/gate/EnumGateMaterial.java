/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.gate;

import java.util.Locale;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum EnumGateMaterial {
    CLAY_BRICK(Blocks.BRICKS, 1, false),
    IRON(Blocks.IRON_BLOCK, 2, true),
    NETHER_BRICK(Blocks.NETHER_BRICKS, 4, true),
    GOLD(Blocks.GOLD_BLOCK, 8, true);

    public static final EnumGateMaterial[] VALUES = values();

    public final Block block;
    public final int numSlots;
    public final boolean canBeModified;
    public final String tag = name().toLowerCase(Locale.ROOT);

    EnumGateMaterial(Block block, int numSlots, boolean canBeModified) {
        this.block = block;
        this.numSlots = numSlots;
        this.canBeModified = canBeModified;
    }

    /** @return The value with the given {@link #tag}, or CLAY_BRICK if there's none. */
    public static EnumGateMaterial byTag(String tag) {
        for (EnumGateMaterial value : VALUES) {
            if (value.tag.equals(tag)) {
                return value;
            }
        }
        return CLAY_BRICK;
    }

    public static EnumGateMaterial getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateMaterial.CLAY_BRICK;
        }
        return VALUES[ord];
    }
}
