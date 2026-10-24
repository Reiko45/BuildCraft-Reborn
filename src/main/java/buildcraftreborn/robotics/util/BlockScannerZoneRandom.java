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
import net.minecraft.util.RandomSource;

import buildcraftreborn.api.core.IZone;

/** Random positions in a zone, as offsets from a position. */
public class BlockScannerZoneRandom implements Iterable<BlockPos> {

    private final RandomSource rand;
    private final IZone zone;
    private final BlockPos pos;

    class BlockIt implements Iterator<BlockPos> {

        @Override
        public boolean hasNext() {
            return true;
        }

        @Override
        public BlockPos next() {
            BlockPos found = zone.getRandomBlockPos(rand);
            return found.subtract(BlockScannerZoneRandom.this.pos);
        }
    }

    public BlockScannerZoneRandom(BlockPos pos, RandomSource iRand, IZone iZone) {
        this.pos = pos;
        rand = iRand;
        zone = iZone;
    }

    @Override
    public Iterator<BlockPos> iterator() {
        return new BlockIt();
    }
}
