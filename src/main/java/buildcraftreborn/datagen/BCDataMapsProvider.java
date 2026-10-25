/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.common.data.DataMapProvider;
import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.energy.BCRefinedFuels;
import buildcraftreborn.lib.fluid.BCFluid;
import buildcraftreborn.lib.fluid.BCFluidDataMaps;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.CoolantData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.DistillationData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.FuelData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.HeatExchangeData;
import buildcraftreborn.lib.fluid.BCFluidDataMaps.SolidCoolantData;
import buildcraftreborn.lib.misc.MathUtil;

/** Combustion engine fuels and coolants, and the distiller and heat exchanger recipes, with 1.12's values
 * ({@code BCEnergyRecipes}). Only the cool version of each fluid is a fuel, as in 1.12.
 * <p>
 * Other mods' oil and fuel get BuildCraft's values through the common tags in {@link BCEnergyTags}: anything in
 * {@code c:oil} or {@code c:crude_oil} burns, heats and distills like BuildCraft's oil, and anything in {@code c:fuel}
 * or {@code c:light_fuel} burns like light fuel. BuildCraft's own fluids are in those tags too, and their tag entries
 * hold exactly the same values as their own entries, so it doesn't matter which of the two a data pack load sees
 * last. Other mods' refined fuels (diesel, gasoline and the rest, in {@link BCRefinedFuels}) burn with their own
 * values, even when those mods put them in {@code c:fuel} too. */
public final class BCDataMapsProvider extends DataMapProvider {
    // Relative amounts of the fluid -- the amount of oil used in refining will return X amount of fluid
    // single
    private static final int _oil = 8;
    private static final int _gas = 16;
    private static final int _light = 4;
    private static final int _dense = 2;
    private static final int _residue = 1;
    // double
    private static final int _gas_light = 10;
    private static final int _light_dense = 5;
    private static final int _dense_residue = 2;
    // triple
    private static final int _light_dense_residue = 3;
    private static final int _gas_light_dense = 8;

    private static final int TIME_BASE = 240_000; // 240_000 - multiple of 3, 5, 16, 1000

    private static final List<TagKey<Fluid>> OIL_TAGS = List.of(BCEnergyTags.OIL, BCEnergyTags.CRUDE_OIL);
    private static final List<TagKey<Fluid>> FUEL_TAGS = List.of(BCEnergyTags.FUEL, BCEnergyTags.LIGHT_FUEL);

    private Builder<FuelData, Fluid> fuels;
    private Builder<DistillationData, Fluid> distillation;
    private Builder<HeatExchangeData, Fluid> heatable;
    private Builder<HeatExchangeData, Fluid> coolable;

    public BCDataMapsProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup) {
        super(output, lookup);
    }

    @Override
    protected void gather() {
        fuels = builder(BCFluidDataMaps.FUELS);
        addFuel(BCEnergyFluids.fuelGaseous, _gas, 8, 4);
        FuelData light = addFuel(BCEnergyFluids.fuelLight, _light, 6, 6);
        FUEL_TAGS.forEach(tag -> fuels.add(tag, light, false));
        addFuel(BCEnergyFluids.fuelDense, _dense, 4, 12);
        addFuel(BCEnergyFluids.fuelMixedLight, _gas_light, 3, 5);
        addFuel(BCEnergyFluids.fuelMixedHeavy, _light_dense, 5, 8);
        addDirtyFuel(BCEnergyFluids.oilDense, _dense_residue, 4, 4);
        addFuel(BCEnergyFluids.oilDistilled, _gas_light_dense, 1, 5);
        addDirtyFuel(BCEnergyFluids.oilHeavy, _light_dense_residue, 2, 4);
        FuelData oil = addDirtyFuel(BCEnergyFluids.crudeOil, _oil, 3, 4);
        OIL_TAGS.forEach(tag -> fuels.add(tag, oil, false));
        // Other mods' diesel, gasoline and so on (these win over #c:fuel, see BCFluidDataMaps.FUEL_MERGER)
        for (BCRefinedFuels.RefinedFuel fuel : BCRefinedFuels.ALL) {
            fuels.add(fuel.tag(), new FuelData(fuel.powerPerTick(), fuel.burnTime(), Optional.empty()), false);
        }

        builder(BCFluidDataMaps.COOLANTS)
            .add(Fluids.WATER.builtInRegistryHolder(), new CoolantData(0.0023f), false);

        builder(BCFluidDataMaps.SOLID_COOLANTS)
            .add(Items.ICE.builtInRegistryHolder(), new SolidCoolantData(new FluidStack(Fluids.WATER, 1500)), false)
            .add(Items.PACKED_ICE.builtInRegistryHolder(), new SolidCoolantData(new FluidStack(Fluids.WATER, 2000)),
                false);

        refining();
    }

    /** 1.12's distillation and heat exchange recipes, registered there only when the factory module was loaded. */
    private void refining() {
        distillation = builder(BCFluidDataMaps.DISTILLATION);
        heatable = builder(BCFluidDataMaps.HEATABLE);
        coolable = builder(BCFluidDataMaps.COOLABLE);

        FluidStack[] gas_light_dense_residue = createFluidStack(BCEnergyFluids.crudeOil, _oil);
        FluidStack[] gas_light_dense = createFluidStack(BCEnergyFluids.oilDistilled, _gas_light_dense);
        FluidStack[] gas_light = createFluidStack(BCEnergyFluids.fuelMixedLight, _gas_light);
        FluidStack[] gas = createFluidStack(BCEnergyFluids.fuelGaseous, _gas);
        FluidStack[] light_dense_residue = createFluidStack(BCEnergyFluids.oilHeavy, _light_dense_residue);
        FluidStack[] light_dense = createFluidStack(BCEnergyFluids.fuelMixedHeavy, _light_dense);
        FluidStack[] light = createFluidStack(BCEnergyFluids.fuelLight, _light);
        FluidStack[] dense_residue = createFluidStack(BCEnergyFluids.oilDense, _dense_residue);
        FluidStack[] dense = createFluidStack(BCEnergyFluids.fuelDense, _dense);
        FluidStack[] residue = createFluidStack(BCEnergyFluids.oilResidue, _residue);

        DistillationData oil = addDistillation(gas_light_dense_residue, gas, light_dense_residue, 0, 32);
        OIL_TAGS.forEach(tag -> distillation.add(tag, oil, false));
        addDistillation(gas_light_dense_residue, gas_light, dense_residue, 1, 16);
        addDistillation(gas_light_dense_residue, gas_light_dense, residue, 2, 12);

        addDistillation(gas_light_dense, gas, light_dense, 0, 24);
        addDistillation(gas_light_dense, gas_light, dense, 1, 16);

        addDistillation(gas_light, gas, light, 0, 24);

        addDistillation(light_dense_residue, light, dense_residue, 1, 16);
        addDistillation(light_dense_residue, light_dense, residue, 2, 12);

        addDistillation(light_dense, light, dense, 1, 16);

        addDistillation(dense_residue, dense, residue, 2, 12);

        HeatExchangeData heatOil = addHeatExchange(BCEnergyFluids.crudeOil);
        OIL_TAGS.forEach(tag -> heatable.add(tag, heatOil, false));
        addHeatExchange(BCEnergyFluids.oilDistilled);
        addHeatExchange(BCEnergyFluids.oilHeavy);
        addHeatExchange(BCEnergyFluids.fuelMixedLight);
        addHeatExchange(BCEnergyFluids.fuelMixedHeavy);
        addHeatExchange(BCEnergyFluids.oilDense);
        addHeatExchange(BCEnergyFluids.fuelGaseous);
        addHeatExchange(BCEnergyFluids.fuelLight);
        addHeatExchange(BCEnergyFluids.fuelDense);
        addHeatExchange(BCEnergyFluids.oilResidue);

        heatable.add(Fluids.WATER.builtInRegistryHolder(), new HeatExchangeData(10, Optional.empty(), 0, 1), false);
        coolable.add(Fluids.LAVA.builtInRegistryHolder(), new HeatExchangeData(5, Optional.empty(), 4, 2), false);
    }

    private static FluidStack[] createFluidStack(BCFluid[] fluid, int amount) {
        FluidStack[] arr = new FluidStack[fluid.length];
        for (int i = 0; i < arr.length; i++) {
            arr[i] = new FluidStack(fluid[i].getSource(), amount);
        }
        return arr;
    }

    private DistillationData addDistillation(FluidStack[] in, FluidStack[] outGas, FluidStack[] outLiquid, int heat,
        long mjCost) {
        FluidStack _in = in[heat];
        FluidStack _outGas = outGas[heat];
        FluidStack _outLiquid = outLiquid[heat];
        int hcf = MathUtil.findHighestCommonFactor(_in.getAmount(), _outGas.getAmount());
        hcf = MathUtil.findHighestCommonFactor(hcf, _outLiquid.getAmount());
        if (hcf > 1) {
            _in = _in.copyWithAmount(_in.getAmount() / hcf);
            _outGas = _outGas.copyWithAmount(_outGas.getAmount() / hcf);
            _outLiquid = _outLiquid.copyWithAmount(_outLiquid.getAmount() / hcf);
            // 1.12 divided the cost in micro-MJ (a long), so keep the integer division on that scale.
            mjCost = mjCost * MjAPI.MJ / hcf;
        } else {
            mjCost = mjCost * MjAPI.MJ;
        }
        DistillationData data = new DistillationData(_in.getAmount(), _outGas, _outLiquid, mjCost / (double) MjAPI.MJ);
        distillation.add(_in.getFluidHolder(), data, false);
        return data;
    }

    /** Adds heating each temperature of the fluid to the next, and cooling it back, and returns the heating of the
     * cool fluid. */
    private HeatExchangeData addHeatExchange(BCFluid[] fluid) {
        HeatExchangeData heatCool = null;
        for (int i = 0; i < fluid.length - 1; i++) {
            BCFluid cool = fluid[i];
            BCFluid hot = fluid[i + 1];
            FluidStack hot_f = new FluidStack(hot.getSource(), 10);
            FluidStack cool_f = new FluidStack(cool.getSource(), 10);
            HeatExchangeData heat = new HeatExchangeData(10, Optional.of(hot_f), cool.heat, hot.heat);
            heatable.add(cool.source.getKey(), heat, false);
            coolable.add(hot.source.getKey(), new HeatExchangeData(10, Optional.of(cool_f), hot.heat, cool.heat),
                false);
            if (i == 0) {
                heatCool = heat;
            }
        }
        return heatCool;
    }

    private FuelData addFuel(BCFluid[] in, int amountDiff, int multiplier, int boostOver4) {
        int totalTime = TIME_BASE * boostOver4 / 4 / multiplier / amountDiff;
        FuelData data = new FuelData(multiplier, totalTime, Optional.empty());
        fuels.add(in[0].source.getKey(), data, false);
        return data;
    }

    private FuelData addDirtyFuel(BCFluid[] in, int amountDiff, int multiplier, int boostOver4) {
        int totalTime = TIME_BASE * boostOver4 / 4 / multiplier / amountDiff;
        FluidStack residue = new FluidStack(BCEnergyFluids.oilResidue[0].getSource(), 1000 / amountDiff);
        FuelData data = new FuelData(multiplier, totalTime, Optional.of(residue));
        fuels.add(in[0].source.getKey(), data, false);
        return data;
    }
}
