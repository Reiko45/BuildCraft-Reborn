/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.shapes.VoxelShape;

import buildcraftreborn.api.transport.pipe.ICustomPipeConnection;

/** Extends a pipe's connection to the edge of the neighbouring block's collision box. */
public enum DefaultPipeConnection implements ICustomPipeConnection {
    INSTANCE;

    @Override
    public float getExtension(Level level, BlockPos pos, Direction face, BlockState state) {
        VoxelShape shape = state.getCollisionShape(level, pos);
        if (shape.isEmpty()) {
            return 0;
        }
        AABB bb = shape.bounds();
        switch (face) {
            case DOWN:
                return (float) bb.minY;
            case UP:
                return 1 - (float) bb.maxY;
            case NORTH:
                return (float) bb.minZ;
            case SOUTH:
                return 1 - (float) bb.maxZ;
            case WEST:
                return (float) bb.minX;
            case EAST:
                return 1 - (float) bb.maxX;
            default:
                return 0;
        }
    }
}
