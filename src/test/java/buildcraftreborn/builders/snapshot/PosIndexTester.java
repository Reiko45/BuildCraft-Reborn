/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.List;

import com.google.common.collect.ImmutableList;

import org.junit.Assert;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.core.BlockPos;

/** Checks that a snapshot's position-to-index mapping round trips for every position in a box. */
@RunWith(Theories.class)
public class PosIndexTester {
    private static final BlockPos SIZE = new BlockPos(6, 4, 8);

    @DataPoints
    public static final List<BlockPos> POSITIONS;

    static {
        ImmutableList.Builder<BlockPos> builder = new ImmutableList.Builder<>();
        for (int z = 0; z < SIZE.getZ(); z++) {
            for (int y = 0; y < SIZE.getY(); y++) {
                for (int x = 0; x < SIZE.getX(); x++) {
                    builder.add(new BlockPos(x, y, z));
                }
            }
        }
        POSITIONS = builder.build();
    }

    @Theory
    public void test(BlockPos pos) {
        Assert.assertEquals(
            Integer.toString(Snapshot.posToIndex(SIZE, pos)),
            pos,
            Snapshot.indexToPos(SIZE, Snapshot.posToIndex(SIZE, pos))
        );
    }
}
