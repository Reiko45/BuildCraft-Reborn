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
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;

import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyTags;

/** Biome tags for oil generation. These replace 1.12's biome lists in the config, and deserts and oceans take the
 * place of 1.12's Oil Desert and Oil Ocean biomes. */
public final class BCBiomeTags extends BiomeTagsProvider {
    public BCBiomeTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup, ExistingFileHelper files) {
        super(output, lookup, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BCEnergyTags.OIL_EXCESSIVE).addTag(Tags.Biomes.IS_DESERT).addTag(BiomeTags.IS_OCEAN);
        tag(BCEnergyTags.OIL_SURFACE_DEPOSIT);
        // 1.12 excluded "hell" (the Nether) and "sky" (The End's main island).
        tag(BCEnergyTags.OIL_EXCLUDED).addTag(BiomeTags.IS_NETHER).add(Biomes.THE_END);
    }
}
