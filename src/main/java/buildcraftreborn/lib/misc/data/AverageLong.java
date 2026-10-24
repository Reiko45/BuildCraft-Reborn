/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import net.minecraft.nbt.CompoundTag;

/** A rolling average of the values pushed in each tick, over the last {@code precise} ticks. */
public class AverageLong {
    private long[] data;
    private final int precise;
    private int pos;
    private long averageRaw, tickValue;

    public AverageLong(int precise) {
        this.precise = precise;
        clear();
    }

    public void clear() {
        this.data = new long[precise];
        this.pos = 0;
    }

    public double getAverage() {
        return (double) averageRaw / precise;
    }

    public long getAverageLong() {
        return averageRaw / precise;
    }

    public void tick(long value) {
        internalTick(tickValue + value);
        tickValue = 0;
    }

    public void tick() {
        internalTick(tickValue);
        tickValue = 0;
    }

    private void internalTick(long value) {
        pos = ++pos % precise;
        long oldValue = data[pos];
        data[pos] = value;
        if (pos == 0) {
            averageRaw = 0;
            for (long iValue : data) {
                averageRaw += iValue;
            }
        } else {
            averageRaw = averageRaw - oldValue + value;
        }
    }

    public void push(long value) {
        tickValue += value;
    }

    public void writeToNbt(CompoundTag nbt, String subTag) {
        nbt.putLongArray(subTag, data.clone());
    }

    public void readFromNbt(CompoundTag nbt, String subTag) {
        long[] values = nbt.getLongArray(subTag);
        if (values.length >= precise) {
            averageRaw = 0;
            pos = 0;
            tickValue = 0;
            for (int i = 0; i < precise; i++) {
                long val = values[i];
                averageRaw += val;
                data[i] = val;
            }
        }
    }
}
