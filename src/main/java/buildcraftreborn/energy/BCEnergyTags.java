/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.energy;

import net.minecraft.core.registries.Registries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

import buildcraftreborn.BuildCraftReborn;

/** Biome tags that replace 1.12's oil biome lists ({@code excessiveBiomes}, {@code surfaceDepositBiomes} and
 * {@code excludedBiomes} in the config). The biome checked is the one at the centre of each oil well. */
public final class BCEnergyTags {
    /** Oil wells are 30 times more likely here. 1.12 used this for its Oil Desert and Oil Ocean biomes; this port has
     * no oil biomes, so it holds deserts and oceans instead. */
    public static final TagKey<Biome> OIL_EXCESSIVE = biome("oil/excessive");
    /** Oil wells are 3 times more likely here, and small surface oil lakes can form. Empty by default, as in 1.12. */
    public static final TagKey<Biome> OIL_SURFACE_DEPOSIT = biome("oil/surface_deposit");
    /** No oil wells here (the Nether and The End by default). This is a whitelist instead if
     * {@code excludedBiomesIsBlacklist} is false in the energy config. */
    public static final TagKey<Biome> OIL_EXCLUDED = biome("oil/excluded");

    private BCEnergyTags() {}

    private static TagKey<Biome> biome(String path) {
        return TagKey.create(Registries.BIOME, BuildCraftReborn.id(path));
    }
}
