/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.builders.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.builders.tile.TileConstructionMarker;
import buildcraftreborn.lib.block.BlockMarkerBase;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The construction marker: a marker that holds a blueprint for builder robots to build (7.1). */
public class BlockConstructionMarker extends BlockMarkerBase {
    public BlockConstructionMarker(Properties properties) {
        super(properties);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileConstructionMarker(pos, state);
    }
}
