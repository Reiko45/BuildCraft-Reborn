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
import net.minecraft.data.tags.ItemTagsProvider;
import net.minecraft.data.tags.TagsProvider;
import net.minecraft.world.level.block.Block;

import net.neoforged.neoforge.common.data.ExistingFileHelper;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.BCCoreTags;

/** Item tags. Replaces the 1.12 ore dictionary registrations ({@code oreDict("gearWood")} in {@code BCCore}). */
public final class BCItemTags extends ItemTagsProvider {
    public BCItemTags(PackOutput output, CompletableFuture<HolderLookup.Provider> lookup,
        CompletableFuture<TagsProvider.TagLookup<Block>> blockTags, ExistingFileHelper files) {
        super(output, lookup, blockTags, BuildCraftReborn.MODID, files);
    }

    @Override
    protected void addTags(HolderLookup.Provider provider) {
        tag(BCCoreTags.GEARS_WOOD).add(BCCoreItems.GEAR_WOOD.get());
        tag(BCCoreTags.GEARS_STONE).add(BCCoreItems.GEAR_STONE.get());
        tag(BCCoreTags.GEARS_IRON).add(BCCoreItems.GEAR_IRON.get());
        tag(BCCoreTags.GEARS_GOLD).add(BCCoreItems.GEAR_GOLD.get());
        tag(BCCoreTags.GEARS_DIAMOND).add(BCCoreItems.GEAR_DIAMOND.get());
        tag(BCCoreTags.GEARS)
            .addTag(BCCoreTags.GEARS_WOOD)
            .addTag(BCCoreTags.GEARS_STONE)
            .addTag(BCCoreTags.GEARS_IRON)
            .addTag(BCCoreTags.GEARS_GOLD)
            .addTag(BCCoreTags.GEARS_DIAMOND);
    }
}
