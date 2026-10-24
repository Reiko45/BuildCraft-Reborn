/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.energy.block;

import javax.annotation.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.enums.EnumSpring;
import buildcraftreborn.core.block.BlockSpring;
import buildcraftreborn.energy.tile.TileSpringOil;

/** The oil spring: a {@link BlockSpring} with a {@link TileSpringOil} (1.12 set the tile through
 * {@code EnumSpring.OIL.tileConstructor}). */
public class BlockSpringOil extends BlockSpring implements EntityBlock {
    public BlockSpringOil(Properties properties) {
        super(EnumSpring.OIL, properties);
    }

    @Override
    @Nullable
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TileSpringOil(pos, state);
    }
}
