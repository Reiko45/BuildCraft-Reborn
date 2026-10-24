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
import net.minecraft.nbt.Tag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.lib.misc.RegistryAccessUtil;

/** One item stack read from the NBT at a path. */
public class RequiredExtractorItem extends RequiredExtractor {
    private NbtPath path = null;

    @Override
    public List<ItemStack> extractItemsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return read(path.get(tileNbt));
    }

    @Override
    public List<ItemStack> extractItemsFromEntity(CompoundTag entityNbt) {
        return read(path.get(entityNbt));
    }

    static List<ItemStack> read(Tag tag) {
        if (tag instanceof CompoundTag compound && !compound.isEmpty()) {
            ItemStack stack = ItemStack.parseOptional(RegistryAccessUtil.current(), compound);
            return stack.isEmpty() ? Collections.emptyList() : Collections.singletonList(stack);
        }
        return Collections.emptyList();
    }
}
