/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.robotics.zone;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.util.RandomSource;

/** Which of the 16x16 columns of one chunk are in a zone.
 * <p>
 * 1.21 fixes: clearing a column of a full chunk no longer also clears the last column, and a chunk with nothing set
 * counts as empty instead of crashing. */
public class ZoneChunk {
    @Nullable
    public BitSet property;
    private boolean fullSet = false;

    public ZoneChunk() {}

    public ZoneChunk(ZoneChunk old) {
        if (old.property != null) {
            property = BitSet.valueOf(old.property.toLongArray());
        }
        fullSet = old.fullSet;
    }

    public boolean get(int xChunk, int zChunk) {
        return fullSet || property != null && property.get(xChunk + zChunk * 16);
    }

    public void set(int xChunk, int zChunk, boolean value) {
        if (value) {
            if (fullSet) {
                return;
            }
            if (property == null) {
                property = new BitSet(16 * 16);
            }
            property.set(xChunk + zChunk * 16, true);
            if (property.cardinality() >= 16 * 16) {
                property = null;
                fullSet = true;
            }
        } else {
            if (fullSet) {
                property = new BitSet(16 * 16);
                property.set(0, 16 * 16);
                fullSet = false;
            } else if (property == null) {
                // Note - ZonePlan should usually destroy such chunks
                property = new BitSet(16 * 16);
            }
            property.set(xChunk + zChunk * 16, false);
        }
    }

    /** @return The set columns, as chunk-local x and z. */
    public List<ZonePlan.Point> getAll() {
        List<ZonePlan.Point> list = new ArrayList<>();
        for (int zChunk = 0; zChunk < 16; zChunk++) {
            for (int xChunk = 0; xChunk < 16; xChunk++) {
                if (get(xChunk, zChunk)) {
                    list.add(new ZonePlan.Point(xChunk, zChunk));
                }
            }
        }
        return list;
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.putBoolean("fullSet", fullSet);
        if (property != null) {
            nbt.putByteArray("bits", property.toByteArray());
        }
    }

    public void readFromNBT(CompoundTag nbt) {
        fullSet = nbt.getBoolean("fullSet");
        if (nbt.contains("bits")) {
            property = BitSet.valueOf(nbt.getByteArray("bits"));
        }
    }

    /** @return A random set column (chunk-local x and z) at a random height. */
    public BlockPos getRandomBlockPos(RandomSource rand) {
        int x, z;
        if (fullSet || property == null) {
            x = rand.nextInt(16);
            z = rand.nextInt(16);
        } else {
            int bitId = rand.nextInt(property.cardinality());
            int bitPosition = property.nextSetBit(0);
            while (bitId > 0) {
                bitId--;
                bitPosition = property.nextSetBit(bitPosition + 1);
            }
            z = bitPosition / 16;
            x = bitPosition - 16 * z;
        }
        int y = rand.nextInt(255);
        return new BlockPos(x, y, z);
    }

    public boolean isEmpty() {
        return !fullSet && (property == null || property.isEmpty());
    }

    public ZoneChunk readFromByteBuf(FriendlyByteBuf buf) {
        int flags = buf.readUnsignedByte();
        if ((flags & 1) != 0) {
            property = BitSet.valueOf(buf.readByteArray(16 * 16 / 8 + 8));
        } else {
            property = null;
        }
        fullSet = (flags & 2) != 0;
        return this;
    }

    public void writeToByteBuf(FriendlyByteBuf buf) {
        int flags = (fullSet ? 2 : 0) | (property != null ? 1 : 0);
        buf.writeByte(flags);
        if (property != null) {
            buf.writeByteArray(property.toByteArray());
        }
    }
}
