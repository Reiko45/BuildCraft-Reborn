/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.datagen;

import java.util.concurrent.CompletableFuture;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.FluidTagsProvider;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.lib.fluid.BCFluid;

/** Fluid tags: the common tags that share BuildCraft's oil and fuel with other mods (see {@link BCEnergyTags}). 1.12
 * had no equivalent; other mods registered their fuels with BuildCraft's API instead. */
public final class BCFluidTags extends FluidTagsProvider {
    public BCFluidTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        // Only the cool fluids: a hot or searing fluid is part way through refining, and other mods' machines would
        // treat it as the cool one.
        tag(BCEnergyTags.OIL).add(BCEnergyFluids.crudeOil[0].getSource());
        tag(BCEnergyTags.CRUDE_OIL).add(BCEnergyFluids.crudeOil[0].getSource());
        tag(BCEnergyTags.FUEL).add(BCEnergyFluids.fuelLight[0].getSource());
        tag(BCEnergyTags.LIGHT_FUEL).add(BCEnergyFluids.fuelLight[0].getSource());
        // NeoForge's tag for fluids that are gaseous at room temperature. Gaseous fuel is a gas even when cool (the
        // other fluids only boil once heated), so all three of its temperatures go in.
        for (BCFluid gas : BCEnergyFluids.fuelGaseous) {
            tag(Tags.Fluids.GASEOUS).add(gas.getSource());
        }
    }
}
