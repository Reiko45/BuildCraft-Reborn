/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.color.block.BlockColor;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Lets pluggables tint their quads (facades take the colour of the block they show). Tint index
 * {@code side + 6 * n} asks the pluggable on that side for its tint {@code n}. */
public enum PipeBlockColours implements BlockColor {
    INSTANCE;

    @Override
    public int getColor(BlockState state, @Nullable BlockAndTintGetter level, @Nullable BlockPos pos, int tintIndex) {
        if (level != null && pos != null && tintIndex >= 0) {
            BlockEntity tile = level.getBlockEntity(pos);
            if (tile instanceof TilePipeHolder holder) {
                Direction side = Direction.from3DDataValue(tintIndex % 6);
                PipePluggable pluggable = holder.getPluggable(side);
                if (pluggable != null) {
                    return pluggable.getBlockColor(tintIndex / 6);
                }
            }
        }
        return -1;
    }
}
