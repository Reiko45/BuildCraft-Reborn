/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.energy;

import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.material.Fluid;

import buildcraftreborn.BuildCraftReborn;

/** Biome tags that replace 1.12's oil biome lists, and the common fluid tags BuildCraft's oil and fuel are shared
 * with other mods through. */
public final class BCEnergyTags {
    // Biome tags: these replace 1.12's excessiveBiomes, surfaceDepositBiomes and excludedBiomes config lists. The
    // biome checked is the one at the centre of each oil well.

    /** Oil wells are 30 times more likely here. 1.12 used this for its Oil Desert and Oil Ocean biomes; this port has
     * no oil biomes, so it holds deserts and oceans instead. */
    public static final TagKey<Biome> OIL_EXCESSIVE = biome("oil/excessive");
    /** Oil wells are 3 times more likely here, and small surface oil lakes can form. Empty by default, as in 1.12. */
    public static final TagKey<Biome> OIL_SURFACE_DEPOSIT = biome("oil/surface_deposit");
    /** No oil wells here (the Nether and The End by default). This is a whitelist instead if
     * {@code excludedBiomesIsBlacklist} is false in the energy config. */
    public static final TagKey<Biome> OIL_EXCLUDED = biome("oil/excluded");

    // Common ("c") fluid tags. There's no NeoForge convention for oil or fuel, so these are the names other 1.21.1 mods
    // already use. Cool oil goes in both oil tags and cool light fuel in both fuel tags (the source fluids only, like
    // those mods do). Anything another mod puts in them works in BuildCraft's machines too, through tag entries in the
    // fuel, distillation and heat exchange data maps (see BCDataMapsProvider).

    /** Crude oil, as Oritech and Ad Astra name it (Ad Astra's fuel refinery takes it). */
    public static final TagKey<Fluid> OIL = common("oil");
    /** Crude oil, as PneumaticCraft and Modern Industrialization name it (PneumaticCraft's refinery takes it). */
    public static final TagKey<Fluid> CRUDE_OIL = common("crude_oil");
    /** Fuel, as Ad Astra names it (its rockets and rovers burn it). BuildCraft's main fuel is light fuel. */
    public static final TagKey<Fluid> FUEL = common("fuel");
    /** Light fuel, as Modern Industrialization names it. */
    public static final TagKey<Fluid> LIGHT_FUEL = common("light_fuel");

    private BCEnergyTags() {}

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, BuildCraftReborn.id(path));
    }

    private static TagKey<Fluid> common(String path) {
        return FluidTags.create(ResourceLocation.fromNamespaceAndPath("c", path));
    }
}
