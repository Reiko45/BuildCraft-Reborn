/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.energy;

import java.util.List;

import net.minecraft.tags.TagKey;
import net.minecraft.world.level.material.Fluid;

/** Other mods' refined fuels, which the combustion engine burns through their common tags (they are tag entries in
 * the fuel data map, so data packs can change them). BuildCraft makes none of them.
 * <p>
 * The values follow what those mods agree on (diesel holds the most energy, gasoline burns hottest, kerosene is in
 * between, biodiesel is a little weaker than diesel and ethanol is the weakest) on BuildCraft's own scale: all of them
 * are near light fuel (6 MJ/t for 750 seconds, 90,000 MJ a bucket) and none comes close to dense fuel (360,000 MJ), so
 * BuildCraft's refinery stays worth building. LPG is like a longer-lasting gaseous fuel, and the premium diesels sit
 * above diesel. */
public final class BCRefinedFuels {
    private BCRefinedFuels() {}

    /** @param powerPerTick MJ a tick.
     * @param burnTime Ticks a bucket lasts. */
    public record RefinedFuel(TagKey<Fluid> tag, double powerPerTick, int burnTime) {
        /** MJ from a bucket. */
        public double totalPower() {
            return powerPerTick * burnTime;
        }
    }

    public static final List<RefinedFuel> ALL = List.of(
        new RefinedFuel(BCEnergyTags.GASOLINE, 7, 12_000), // 600 s, 84,000 MJ
        new RefinedFuel(BCEnergyTags.KEROSENE, 6, 15_000), // 750 s, 90,000 MJ (light fuel)
        new RefinedFuel(BCEnergyTags.DIESEL, 5, 19_200), // 960 s, 96,000 MJ (heavy mixed fuel)
        new RefinedFuel(BCEnergyTags.BIODIESEL, 5, 16_000), // 800 s, 80,000 MJ
        new RefinedFuel(BCEnergyTags.ETHANOL, 4, 12_500), // 625 s, 50,000 MJ
        new RefinedFuel(BCEnergyTags.BIOETHANOL, 4, 12_500), // as ethanol
        new RefinedFuel(BCEnergyTags.LPG, 8, 10_000), // 500 s, 80,000 MJ
        new RefinedFuel(BCEnergyTags.HIGH_POWER_BIODIESEL, 6, 20_000), // 1,000 s, 120,000 MJ
        new RefinedFuel(BCEnergyTags.BOOSTED_DIESEL, 6, 20_000)); // as high power biodiesel
}
