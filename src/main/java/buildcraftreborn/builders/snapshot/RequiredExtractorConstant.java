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
import java.util.stream.Collectors;

import org.jetbrains.annotations.Nullable;

import com.google.gson.annotations.SerializedName;

import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.item.ItemStack;
import net.minecraft.nbt.CompoundTag;

import net.neoforged.neoforge.fluids.FluidStack;

@SuppressWarnings("WeakerAccess")
public class RequiredExtractorConstant extends RequiredExtractor {
    @SerializedName("items")
    private List<ItemStackRef> itemRefs = Collections.emptyList();
    @SerializedName("fluids")
    private List<FluidStackRef> fluidRefs = Collections.emptyList();

    
    @Override
    public List<ItemStack> extractItemsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Collections.unmodifiableList(
            itemRefs.stream()
                .map(ref -> ref.get(tileNbt))
                .collect(Collectors.toList())
        );
    }

    
    @Override
    public List<FluidStack> extractFluidsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return Collections.unmodifiableList(
            fluidRefs.stream()
                .map(ref -> ref.get(tileNbt))
                .collect(Collectors.toList())
        );
    }

    
    @Override
    public List<ItemStack> extractItemsFromEntity(CompoundTag entityNbt) {
        return Collections.unmodifiableList(
            itemRefs.stream()
                .map(ref -> ref.get(entityNbt))
                .collect(Collectors.toList())
        );
    }

    
    @Override
    public List<FluidStack> extractFluidsFromEntity(CompoundTag entityNbt) {
        return Collections.unmodifiableList(
            fluidRefs.stream()
                .map(ref -> ref.get(entityNbt))
                .collect(Collectors.toList())
        );
    }
}
