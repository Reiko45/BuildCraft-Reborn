/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

/** Every item stack in a list read from the NBT at a path (a chest's {@code Items}, say). */
public class RequiredExtractorItemsList extends RequiredExtractor {
    private NbtPath path = null;

    @Override
    public List<ItemStack> extractItemsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return read(path.get(tileNbt));
    }

    @Override
    public List<ItemStack> extractItemsFromEntity(CompoundTag entityNbt) {
        return read(path.get(entityNbt));
    }

    private static List<ItemStack> read(Tag tag) {
        if (!(tag instanceof ListTag list)) {
            return Collections.emptyList();
        }
        List<ItemStack> stacks = new ArrayList<>();
        for (Tag element : list) {
            stacks.addAll(RequiredExtractorItem.read(element));
        }
        return Collections.unmodifiableList(stacks);
    }
}
