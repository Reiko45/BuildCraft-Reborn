/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.factory.tile.TileAutoWorkbenchItems;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;

public class BlockAutoWorkbenchItems extends BlockBCTile_Neptune {
    public BlockAutoWorkbenchItems(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileAutoWorkbenchItems(pos, state);
    }
}
