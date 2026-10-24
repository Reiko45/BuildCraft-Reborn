/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.block;

import net.minecraft.world.level.block.SoundType;

import buildcraftreborn.api.enums.EnumDecoratedBlock;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** A plain decorative block with one of BuildCraft's textures (creative only, as in 1.12). */
public class BlockDecoration extends BlockBCBase_Neptune {
    public final EnumDecoratedBlock type;

    public BlockDecoration(EnumDecoratedBlock type) {
        // 1.12: iron material with BuildCraft's default hardness 5 and resistance 10
        super(Properties.of().strength(5.0F, 10.0F).sound(SoundType.METAL).requiresCorrectToolForDrops()
            .lightLevel(state -> type.lightValue));
        this.type = type;
    }
}
