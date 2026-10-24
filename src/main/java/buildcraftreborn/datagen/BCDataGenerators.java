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
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;

import net.neoforged.neoforge.common.data.ExistingFileHelper;
import net.neoforged.neoforge.data.event.GatherDataEvent;

/**
 * Data generation entry point ({@code gradlew runData}). Output goes to {@code src/generated/resources} and is
 * committed. 1.12 BuildCraft hand-wrote most JSON and coded most recipes in Java; in this port all models, lang,
 * tags, recipes, loot tables and advancements are generated here instead.
 */
public final class BCDataGenerators {
    private BCDataGenerators() {}

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();
        ExistingFileHelper files = event.getExistingFileHelper();

        // Client assets (fluid textures first: the models refer to them)
        generator.addProvider(event.includeClient(), new BCFluidTextures(output, files));
        generator.addProvider(event.includeClient(), new BCItemModels(output, files));
        generator.addProvider(event.includeClient(), new BCBlockStates(output, files));
        generator.addProvider(event.includeClient(), new BCLanguage(output));

        // Server data
        BCBlockTags blockTags = generator.addProvider(event.includeServer(), new BCBlockTags(output, lookup, files));
        generator.addProvider(event.includeServer(),
            new BCItemTags(output, lookup, blockTags.contentsGetter(), files));
        generator.addProvider(event.includeServer(), new BCBiomeTags(output, lookup, files));
        generator.addProvider(event.includeServer(), new BCRecipes(output, lookup));
        generator.addProvider(event.includeServer(), BCLootTables.create(output, lookup));
        generator.addProvider(event.includeServer(), new BCDataMapsProvider(output, lookup));
        generator.addProvider(event.includeServer(), BCAdvancements.create(output, lookup, files));
    }
}
