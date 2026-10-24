/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import java.util.Map.Entry;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

import org.junit.Assert;
import org.junit.Test;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.FromDataPoints;
import org.junit.experimental.theories.Theories;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

/** 1.12's {@code buildcraft.test.core.BoxTester} (the box moved to {@code lib.misc.data} in BuildCraft 8). */
@RunWith(Theories.class)
public class BoxTester {
    private static final BlockPos MIN = new BlockPos(1, 2, 3), MAX = new BlockPos(4, 5, 6);
    private static final BlockPos SIZE = new BlockPos(4, 4, 4);
    private static final BlockPos CENTER = new BlockPos(3, 4, 5);
    private static final Vec3 CENTER_EXACT = new Vec3(3, 4, 5);

    @DataPoints("testContainsVec3")
    public static Set<Entry<Vec3, Boolean>> dataContainsVec3() {
        return ImmutableMap.<Vec3, Boolean> builder()
            .put(new Vec3(0, 0, 0), false)
            .put(new Vec3(1, 2, 3), true)
            .put(new Vec3(1.3, 2.4, 3.5), true)
            .put(new Vec3(4.9, 5.9, 6.9), true)
            .put(new Vec3(5, 5, 6), false)
            .build().entrySet();
    }

    @Theory
    public void testContainsVec3(@FromDataPoints("testContainsVec3") Entry<Vec3, Boolean> entry) {
        Box box = new Box(MIN, MAX);
        Assert.assertEquals(entry.getKey().toString(), entry.getValue(), box.contains(entry.getKey()));
    }

    @Test
    public void testMin() {
        Assert.assertEquals(MIN, new Box(MIN, MAX).min());
    }

    @Test
    public void testMax() {
        Assert.assertEquals(MAX, new Box(MIN, MAX).max());
    }

    @Test
    public void testSize() {
        Assert.assertEquals(SIZE, new Box(MIN, MAX).size());
    }

    @Test
    public void testCenter() {
        Assert.assertEquals(CENTER, new Box(MIN, MAX).center());
    }

    @Test
    public void testCenterExact() {
        Vec3 center = new Box(MIN, MAX).centerExact();
        Assert.assertEquals(CENTER_EXACT.x, center.x, 0.001);
        Assert.assertEquals(CENTER_EXACT.y, center.y, 0.001);
        Assert.assertEquals(CENTER_EXACT.z, center.z, 0.001);
    }

    @Test
    public void testIntersection1() {
        Box box1 = new Box(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        Box box2 = new Box(new BlockPos(1, 1, 1), new BlockPos(3, 3, 3));
        Box inter = new Box(new BlockPos(1, 1, 1), new BlockPos(2, 2, 2));
        Assert.assertEquals(inter, box1.getIntersect(box2));
        Assert.assertEquals(inter, box2.getIntersect(box1));
    }

    @Test
    public void testIntersection2() {
        Box box1 = new Box(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        Box box2 = new Box(new BlockPos(0, 0, 0), new BlockPos(3, 3, 3));
        Box inter = new Box(new BlockPos(0, 0, 0), new BlockPos(2, 2, 2));
        Assert.assertEquals(inter, box1.getIntersect(box2));
        Assert.assertEquals(inter, box2.getIntersect(box1));
    }

    @Test
    public void testIntersection3() {
        Box box1 = new Box(new BlockPos(1, 1, 1), new BlockPos(2, 2, 2));
        Box box2 = new Box(new BlockPos(0, 0, 0), new BlockPos(1, 1, 1));
        Box inter = new Box(new BlockPos(1, 1, 1), new BlockPos(1, 1, 1));
        Assert.assertEquals(inter, box1.getIntersect(box2));
        Assert.assertEquals(inter, box2.getIntersect(box1));
    }
}
