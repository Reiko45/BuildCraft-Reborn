/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.util;

import java.util.Iterator;

import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

/** Random offsets within a sphere. */
public class BlockScannerRandom implements Iterable<BlockPos> {

    private final RandomSource rand;
    private final int maxDistance;

    class BlockIt implements Iterator<BlockPos> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BlockPos next() {
            double radius = rand.nextFloat() * maxDistance;
            float polarAngle = rand.nextFloat() * 2.0F * (float) Math.PI;
            float azimuthAngle = rand.nextFloat() * (float) Math.PI;

            int searchX = (int) (radius * Mth.cos(polarAngle) * Mth.sin(azimuthAngle));
            int searchY = (int) (radius * Mth.cos(azimuthAngle));
            int searchZ = (int) (radius * Mth.sin(polarAngle) * Mth.sin(azimuthAngle));

            return new BlockPos(searchX, searchY, searchZ);
        }
    }

    public BlockScannerRandom(RandomSource iRand, int iMaxDistance) {
        rand = iRand;
        maxDistance = iMaxDistance;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockIt();
    }
}
