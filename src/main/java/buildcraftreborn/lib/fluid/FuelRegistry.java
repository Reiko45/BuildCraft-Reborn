/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.fluid;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.IFuelManager;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.FuelData;

/** Combustion engine fuels: the {@link BCFluidDataMaps#FUELS} data map first, then fuels added in code through
 * {@link IFuelManager}. */
public enum FuelRegistry implements IFuelManager {
    INSTANCE;

    private final List<IFuel> fuels = new ArrayList<>();

    @Override
    public <F extends IFuel> F addFuel(F fuel) {
        fuels.add(fuel);
        return fuel;
    }

    /** Removes a fuel added with {@link #addFuel(IFuel)} (so scripts can undo what they added). Data map fuels can
     * only be changed by data packs.
     *
     * @return True if it was there. */
    public boolean removeFuel(IFuel fuel) {
        return fuels.remove(fuel);
    }

    @Override
    public IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
        return addFuel(new Fuel(fluid, powerPerCycle, totalBurningTime));
    }

    @Override
    public IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        return addFuel(new DirtyFuel(fuel, powerPerCycle, totalBurningTime, residue));
    }

    @Override
    public Collection<IFuel> getFuels() {
        List<IFuel> all = new ArrayList<>();
        for (Map.Entry<ResourceKey<Fluid>, FuelData> entry : BuiltInRegistries.FLUID.getDataMap(BCFluidDataMaps.FUELS)
            .entrySet()) {
            BuiltInRegistries.FLUID.getHolder(entry.getKey())
                .ifPresent(fluid -> all.add(fromData(new FluidStack(fluid, 1), entry.getValue())));
        }
        all.addAll(fuels);
        return all;
    }

    @Override
    @Nullable
    public IFuel getFuel(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return null;
        }
        FuelData data = fluid.getFluidHolder().getData(BCFluidDataMaps.FUELS);
        if (data != null) {
            return fromData(fluid.copyWithAmount(1), data);
        }
        for (IFuel fuel : fuels) {
            if (FluidStack.isSameFluidSameComponents(fuel.getFluid(), fluid)) {
                return fuel;
            }
        }
        return null;
    }

    private static IFuel fromData(FluidStack fluid, FuelData data) {
        long powerPerCycle = Math.round(data.powerPerTick() * MjAPI.MJ);
        if (data.residue().isPresent()) {
            return new DirtyFuel(fluid, powerPerCycle, data.burnTime(), data.residue().get());
        }
        return new Fuel(fluid, powerPerCycle, data.burnTime());
    }

    public static class Fuel implements IFuel {
        private final FluidStack fluid;
        private final long powerPerCycle;
        private final int totalBurningTime;

        public Fuel(FluidStack fluid, long powerPerCycle, int totalBurningTime) {
            this.fluid = fluid;
            this.powerPerCycle = powerPerCycle;
            this.totalBurningTime = totalBurningTime;
        }

        @Override
        public FluidStack getFluid() {
            return fluid;
        }

        @Override
        public long getPowerPerCycle() {
            return powerPerCycle;
        }

        @Override
        public int getTotalBurningTime() {
            return totalBurningTime;
        }
    }

    public static class DirtyFuel extends Fuel implements IDirtyFuel {
        private final FluidStack residue;

        public DirtyFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime, FluidStack residue) {
            super(fluid, powerPerCycle, totalBurningTime);
            this.residue = residue;
        }

        @Override
        public FluidStack getResidue() {
            return residue;
        }
    }
}
