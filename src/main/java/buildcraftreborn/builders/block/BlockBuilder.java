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
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;

import buildcraftreborn.api.enums.EnumOptionalSnapshotType;
import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.builders.tile.TileBuilder;
import buildcraftreborn.lib.block.BlockBCTile_Neptune;
import buildcraftreborn.lib.block.IBlockWithFacing;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** The builder. {@link #SNAPSHOT_TYPE} shows what is in its snapshot slot; 1.12 read it from the tile when drawing,
 * here the tile sets it when the slot changes. */
public class BlockBuilder extends BlockBCTile_Neptune implements IBlockWithFacing {
    public static final EnumProperty<EnumOptionalSnapshotType> SNAPSHOT_TYPE = BuildCraftProperties.SNAPSHOT_TYPE;

    public BlockBuilder(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(SNAPSHOT_TYPE, EnumOptionalSnapshotType.NONE));
    }

    @Override
    protected void addProperties(StateDefinition.Builder<Block, BlockState> builder) {
        super.addProperties(builder);
        builder.add(SNAPSHOT_TYPE);
    }

    @Override
    public TileBC_Neptune newBlockEntity(BlockPos pos, BlockState state) {
        return new TileBuilder(pos, state);
    }

    @Override
    public boolean canBeRotated(Level world, BlockPos pos, BlockState state) {
        BlockEntity tile = world.getBlockEntity(pos);
        return !(tile instanceof TileBuilder builder) || builder.getBuilder() == null;
    }
}
