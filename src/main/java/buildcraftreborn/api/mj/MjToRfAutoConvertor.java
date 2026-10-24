/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.mj;

import org.jetbrains.annotations.Nullable;

import net.neoforged.neoforge.energy.IEnergyStorage;

/** Wraps a Forge Energy ({@link IEnergyStorage}) handler so MJ machines can talk to it, when auto-conversion is on. */
public class MjToRfAutoConvertor implements IMjReadable {
    final IEnergyStorage rf;

    /** @return An {@link MjToRfAutoConvertor} that may implement {@link IMjPassiveProvider} and/or {@link IMjReceiver}
     *         if the given storage can provide/receive energy, or null if the given storage is null, or if
     *         RF&lt;-&gt;MJ autoconversion is not enabled ( {@link MjAPI#isRfAutoConversionEnabled()} ) */
    @Nullable
    public static MjToRfAutoConvertor create(@Nullable IEnergyStorage rf) {
        if (rf == null) {
            return null;
        }
        if (!MjAPI.isRfAutoConversionEnabled()) {
            return null;
        }
        if (rf.canReceive()) {
            if (rf.canExtract()) {
                return new OfBoth(rf);
            } else {
                return new OfReceiver(rf);
            }
        } else {
            if (rf.canExtract()) {
                return new OfProvider(rf);
            } else {
                return new MjToRfAutoConvertor(rf);
            }
        }
    }

    @Nullable
    public static IMjReceiver createReceiver(@Nullable IEnergyStorage rf) {
        return create(rf) instanceof IMjReceiver receiver ? receiver : null;
    }

    @Nullable
    public static IMjPassiveProvider createProvider(@Nullable IEnergyStorage rf) {
        return create(rf) instanceof IMjPassiveProvider provider ? provider : null;
    }

    MjToRfAutoConvertor(IEnergyStorage storage) {
        this.rf = storage;
    }

    /** @return true. (Redstone-like engines are expected to not connect due to this class never implementing
     *         {@link IMjRedstoneReceiver}) */
    @Override
    public boolean canConnect(IMjConnector other) {
        return true;
    }

    @Override
    public long getStored() {
        return rf.getEnergyStored() * MjAPI.getRfConversion().mjPerRf;
    }

    @Override
    public long getCapacity() {
        return rf.getMaxEnergyStored() * MjAPI.getRfConversion().mjPerRf;
    }

    long implGetPowerRequested() {
        int accepted = rf.receiveEnergy(Integer.MAX_VALUE, true);
        if (accepted > 0) {
            return accepted * MjAPI.getRfConversion().mjPerRf;
        }
        return 0;
    }

    /** @return excess */
    long implReceivePower(long microJoules, boolean simulate) {
        if (!rf.canReceive()) {
            return microJoules;
        }
        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
        int maxRf = (int) (microJoules / mjPerRf);
        if (maxRf <= 0) {
            return microJoules;
        }
        int received = rf.receiveEnergy(maxRf, simulate);
        return microJoules - received * mjPerRf;
    }

    long implExtractPower(long min, long max, boolean simulate) {
        if (!rf.canExtract()) {
            return 0;
        }
        long mjPerRf = MjAPI.getRfConversion().mjPerRf;
        int maxRf = (int) (max / mjPerRf);
        if (maxRf <= 0) {
            return 0;
        }
        int extractedRF = rf.extractEnergy(maxRf, true);
        long extractedMJ = extractedRF * mjPerRf;
        if (extractedMJ < min) {
            return 0;
        }
        if (!simulate) {
            rf.extractEnergy(maxRf, false);
        }
        return extractedMJ;
    }
}

final class OfReceiver extends MjToRfAutoConvertor implements IMjReceiver {
    OfReceiver(IEnergyStorage storage) {
        super(storage);
    }

    @Override
    public boolean canReceive() {
        return rf.canReceive();
    }

    @Override
    public long getPowerRequested() {
        return implGetPowerRequested();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return implReceivePower(microJoules, simulate);
    }
}

final class OfProvider extends MjToRfAutoConvertor implements IMjPassiveProvider {
    OfProvider(IEnergyStorage storage) {
        super(storage);
    }

    @Override
    public long extractPower(long min, long max, boolean simulate) {
        return implExtractPower(min, max, simulate);
    }
}

final class OfBoth extends MjToRfAutoConvertor implements IMjReceiver, IMjPassiveProvider {
    OfBoth(IEnergyStorage storage) {
        super(storage);
    }

    @Override
    public boolean canReceive() {
        return rf.canReceive();
    }

    @Override
    public long getPowerRequested() {
        return implGetPowerRequested();
    }

    @Override
    public long receivePower(long microJoules, boolean simulate) {
        return implReceivePower(microJoules, simulate);
    }

    @Override
    public long extractPower(long min, long max, boolean simulate) {
        return implExtractPower(min, max, simulate);
    }
}
