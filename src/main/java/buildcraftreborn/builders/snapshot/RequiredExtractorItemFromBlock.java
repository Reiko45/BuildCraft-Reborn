/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** The block's own item. 1.21: there is no metadata, so one item per block (1.12 used {@code damageDropped}). */
public class RequiredExtractorItemFromBlock extends RequiredExtractor {
    /** 1.21 addition: how many of the block's item it takes (for double slabs, snow layers, candles...). */
    private int amount = 1;

    @Override
    public List<ItemStack> extractItemsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Collections.singletonList(new ItemStack(blockState.getBlock().asItem(), Math.max(1, amount)));
    }
}
