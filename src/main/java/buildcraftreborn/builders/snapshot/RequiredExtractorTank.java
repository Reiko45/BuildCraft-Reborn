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
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.lib.misc.RegistryAccessUtil;

/** The fluid stack at a path in the NBT (a tank's contents). */
public class RequiredExtractorTank extends RequiredExtractor {
    private NbtPath path = null;

    @Override
    public List<FluidStack> extractFluidsFromBlock(BlockState blockState, @Nullable CompoundTag tileNbt) {
        return read(path.get(tileNbt));
    }

    @Override
    public List<FluidStack> extractFluidsFromEntity(CompoundTag entityNbt) {
        return read(path.get(entityNbt));
    }

    private static List<FluidStack> read(Tag tag) {
        if (tag instanceof CompoundTag compound && !compound.isEmpty() && !compound.contains("Empty")) {
            FluidStack fluid = FluidStack.parseOptional(RegistryAccessUtil.current(), compound);
            return fluid.isEmpty() ? Collections.emptyList() : Collections.singletonList(fluid);
        }
        return Collections.emptyList();
    }
}
