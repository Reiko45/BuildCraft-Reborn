/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.energy.IEnergyStorage;

/** Exposes a single {@link IMjConnector} through every MJ capability it implements, plus a Forge Energy view when
 * RF auto-conversion is enabled. Block entities hand one of these to their capability provider and answer
 * {@link #getCapability(BlockCapability)} for each MJ capability (and {@link Capabilities.EnergyStorage#BLOCK}). */
public class MjCapabilityHelper {
    private final IMjConnector connector;
    @Nullable
    private final IMjReceiver receiver;
    @Nullable
    private final IMjRedstoneReceiver rsReceiver;
    @Nullable
    private final IMjReadable readable;
    @Nullable
    private final IMjPassiveProvider provider;
    @Nullable
    private final IEnergyStorage rfAutoConvert;

    public MjCapabilityHelper(IMjConnector mj) {
        this.connector = mj;
        this.receiver = mj instanceof IMjReceiver r ? r : null;
        this.rsReceiver = mj instanceof IMjRedstoneReceiver r ? r : null;
        this.readable = mj instanceof IMjReadable r ? r : null;
        this.provider = mj instanceof IMjPassiveProvider p ? p : null;
        this.rfAutoConvert = MjAPI.isRfAutoConversionEnabled() ? new RfView() : null;
    }

    /** @return The value for the given capability, or null if the connector doesn't provide it. */
    @Nullable
    @SuppressWarnings("unchecked")
    public <T> T getCapability(BlockCapability<T, @Nullable Direction> capability) {
        Object value = null;
        if (capability == MjAPI.CAP_CONNECTOR) value = connector;
        else if (capability == MjAPI.CAP_RECEIVER) value = receiver;
        else if (capability == MjAPI.CAP_REDSTONE_RECEIVER) value = rsReceiver;
        else if (capability == MjAPI.CAP_READABLE) value = readable;
        else if (capability == MjAPI.CAP_PASSIVE_PROVIDER) value = provider;
        else if (capability == Capabilities.EnergyStorage.BLOCK) value = rfAutoConvert;
        return (T) value;
    }

    /** Forge Energy view of the connector, used only when RF auto-conversion is enabled. */
    private final class RfView implements IEnergyStorage {
        @Override
        public int getEnergyStored() {
            IMjReadable read = readable;
            if (read == null) return 0;
            return (int) (read.getStored() / MjAPI.getRfConversion().mjPerRf);
        }

        @Override
        public int getMaxEnergyStored() {
            IMjReadable read = readable;
            if (read == null) return 0;
            return (int) (read.getCapacity() / MjAPI.getRfConversion().mjPerRf);
        }

        @Override
        public boolean canReceive() {
            return receiver != null && receiver.canReceive();
        }

        /** @return Amount of energy that was (or would have been, if simulated) accepted by the storage. */
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            if (maxReceive <= 0) {
                return 0;
            }
            IMjReceiver recv = receiver;
            if (recv == null || !recv.canReceive()) {
                return 0;
            }
            long mjPerRf = MjAPI.getRfConversion().mjPerRf;
            long maxReceiveMj = maxReceive * mjPerRf;
            long excess = recv.receivePower(maxReceiveMj, true);
            // Actual MJ that was accepted
            long acceptedMj = maxReceiveMj - excess;
            if (acceptedMj < mjPerRf) {
                return 0;
            }
            // Only accept an exact multiple of mjPerRf so the conversion back to RF is lossless
            long exactAcceptableMj = acceptedMj - (acceptedMj % mjPerRf);
            if (exactAcceptableMj <= 0) {
                return 0;
            }
            int rf = (int) (exactAcceptableMj / mjPerRf);
            if (recv.receivePower(exactAcceptableMj, true) != 0) {
                // Odd. This means we can't actually accept the exact amount (not actually a crash)
                return 0;
            }
            if (!simulate) {
                long excess3 = recv.receivePower(exactAcceptableMj, false);
                if (excess3 != 0) {
                    throw new IllegalStateException("Bad impl: " + recv.getClass() + " of receivePower");
                }
            }
            return rf;
        }

        @Override
        public boolean canExtract() {
            return provider != null;
        }

        /** Nothing in BuildCraft supports extracting RF from MJ machines (same as 1.12). */
        @Override
        public int extractEnergy(int maxExtract, boolean simulate) {
            return 0;
        }
    }
}
