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

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

/** Combustion engine coolants. In BuildCraft Reborn the built-in coolants come from the {@code buildcraftreborn:coolants}
 * fluid data map and the {@code buildcraftreborn:solid_coolants} item data map; ones added through this API are
 * checked after the data maps. */
public interface ICoolantManager {
    ICoolant addCoolant(ICoolant coolant);

    ICoolant addCoolant(FluidStack fluid, float degreesCoolingPerMb);

    default ICoolant addCoolant(Fluid fluid, float degreesCoolingPerMb) {
        return addCoolant(new FluidStack(fluid, 1), degreesCoolingPerMb);
    }

    ISolidCoolant addSolidCoolant(ISolidCoolant solidCoolant);

    ISolidCoolant addSolidCoolant(ItemStack solid, FluidStack fluid, float multiplier);

    Collection<ICoolant> getCoolants();

    Collection<ISolidCoolant> getSolidCoolants();

    @Nullable
    ICoolant getCoolant(FluidStack fluid);

    float getDegreesPerMb(FluidStack fluid, float heat);

    @Nullable
    ISolidCoolant getSolidCoolant(ItemStack solid);
}
