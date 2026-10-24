/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.builders.tile.TileArchitectTable;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The architect table. {@link #PROP_VALID} is false when it found no area to scan. */
public class BlockArchitectTable extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final BooleanProperty PROP_VALID = BuildCraftProperties.VALID;

    public BlockArchitectTable(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(PROP_VALID, Boolean.TRUE));
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        builder.add(PROP_VALID);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileArchitectTable(pos, state);
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        return false;
    }
}
