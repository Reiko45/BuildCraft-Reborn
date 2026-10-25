/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.fuels;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

/** Combustion engine fuels. In BuildCraft Reborn the built-in fuels come from the {@code buildcraftreborn:fuels}
 * fluid data map, so data packs can add, change or remove them; fuels added through this API are checked after the
 * data map. */
public interface IFuelManager {
    <F extends IFuel> F addFuel(F fuel);

    IFuel addFuel(FluidStack fluid, long powerPerCycle, int totalBurningTime);

    default IFuel addFuel(Fluid fluid, long powerPerCycle, int totalBurningTime) {
        return addFuel(new FluidStack(fluid, 1), powerPerCycle, totalBurningTime);
    }

    /** @param residue The residue fluidstack, per bucket of the original fuel. */
    IDirtyFuel addDirtyFuel(FluidStack fuel, long powerPerCycle, int totalBurningTime, FluidStack residue);

    /** @param residue The residue fluidstack, per bucket of the original fuel. */
    default IDirtyFuel addDirtyFuel(Fluid fuel, long powerPerCycle, int totalBurningTime, FluidStack residue) {
        return addDirtyFuel(new FluidStack(fuel, 1), powerPerCycle, totalBurningTime, residue);
    }

    /** @return Every fuel: the data map entries (source fluids only) followed by fuels added through this API. */
    Collection<IFuel> getFuels();

    @Nullable
    IFuel getFuel(FluidStack fluid);

    interface IDirtyFuel extends IFuel {
        /** @return The residue fluidstack, per bucket of original fuel. */
        FluidStack getResidue();
    }
}
