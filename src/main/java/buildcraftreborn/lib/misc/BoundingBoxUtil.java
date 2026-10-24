/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

/** Bounding box helpers. Only the ones used so far are ported. */
public final class BoundingBoxUtil {
    private BoundingBoxUtil() {}

    /** @return A box reaching {@code radius} out from the centre in every direction. */
    public static AABB makeAround(Vec3 centre, double radius) {
        return new AABB(centre.x - radius, centre.y - radius, centre.z - radius, centre.x + radius, centre.y + radius,
            centre.z + radius);
    }

    public static AABB makeFrom(Vec3 from, Vec3 to) {
        return new AABB(from, to);
    }

    /** @return A box covering the given depth just outside the given face of the block. */
    public static AABB extrudeFace(BlockPos pos, Direction face, double depth) {
        Vec3 from = Vec3.atLowerCornerOf(pos);
        Vec3 to = Vec3.atLowerCornerOf(pos).add(1, 1, 1);

        Axis axis = face.getAxis();
        if (face.getAxisDirection() == AxisDirection.POSITIVE) {
            from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) + 1);
            to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) + depth);
        } else {
            to = VecUtil.replaceValue(to, axis, VecUtil.getValue(to, axis) - 1);
            from = VecUtil.replaceValue(from, axis, VecUtil.getValue(from, axis) - depth);
        }
        return makeFrom(from, to);
    }
}
