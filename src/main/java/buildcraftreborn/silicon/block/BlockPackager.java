/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.silicon.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.silicon.tile.TilePackager;

/** The recipe packager (7.1's {@code packagerBlock}). */
public class BlockPackager extends BlockBCTile_Neptune {
    public BlockPackager(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TilePackager(pos, state);
    }
}
