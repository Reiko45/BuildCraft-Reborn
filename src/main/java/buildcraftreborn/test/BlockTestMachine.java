/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;

/** The development test machine. Faces the player, like most BuildCraft machines. */
public class BlockTestMachine extends BlockBCTile_Neptune implements IBlockWithFacing {
    public BlockTestMachine(Properties properties) {
        super(properties);
    }

    @Override
    public TileTestMachine newBlockEntity(BlockPos pos, BlockState state) {
        return new TileTestMachine(pos, state);
    }
}
