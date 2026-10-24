/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class MjBattery {
    private final long capacity;
    private long microJoules = 0;

    public MjBattery(long capacity) {
        this.capacity = capacity;
    }

    public CompoundTag serializeNBT() {
        CompoundTag nbt = new CompoundTag();
        nbt.putLong("stored", microJoules);
        return nbt;
    }

    public void deserializeNBT(CompoundTag nbt) {
        microJoules = nbt.getLong("stored");
    }

    public void writeToBuffer(FriendlyByteBuf buffer) {
        buffer.writeLong(microJoules);
    }

    public void readFromBuffer(FriendlyByteBuf buffer) {
        microJoules = buffer.readLong();
    }

    public long addPower(long microJoulesToAdd, boolean simulate) {
        if (!simulate) {
            this.microJoules += microJoulesToAdd;
        }
        return 0;
    }

    /** Attempts to add power, but only if this is not already full.
     *
     * @param microJoulesToAdd The power to add.
     * @return The excess power. */
    public long addPowerChecking(long microJoulesToAdd, boolean simulate) {
        if (isFull()) {
            return microJoulesToAdd;
        } else {
            return addPower(microJoulesToAdd, simulate);
        }
    }

    public long extractAll() {
        return extractPower(0, microJoules);
    }

    /** Attempts to extract exactly the given amount of power.
     *
     * @param power The amount of power to extract.
     * @return True if the power was removed, false if not. */
    public boolean extractPower(long power) {
        return extractPower(power, power) > 0;
    }

    public long extractPower(long min, long max) {
        if (microJoules < min) return 0;
        long extracting = Math.min(microJoules, max);
        microJoules -= extracting;
        return extracting;
    }

    public boolean isFull() {
        return microJoules >= capacity;
    }

    public long getStored() {
        return microJoules;
    }

    public long getCapacity() {
        return capacity;
    }

    public void tick(Level world, BlockPos position) {
        tick(world, Vec3.atCenterOf(position));
    }

    public void tick(Level world, Vec3 position) {
        if (microJoules > capacity * 2) {
            losePower(world, position);
        }
    }

    protected void losePower(Level world, Vec3 position) {
        long diff = microJoules - capacity * 2;
        long lost = ceilDivide(diff, 32);
        microJoules -= lost;
        MjAPI.EFFECT_MANAGER.createPowerLossEffect(world, position, lost);
    }

    private static long ceilDivide(long val, long by) {
        return (val / by) + (val % by == 0 ? 0 : 1);
    }

    public String getDebugString() {
        return MjAPI.formatMj(microJoules) + " / " + MjAPI.formatMj(capacity) + " MJ";
    }
}
