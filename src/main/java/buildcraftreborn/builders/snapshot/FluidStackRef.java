/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.snapshot;

import java.util.Optional;

import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidType;

/** A fluid stack in a rule: a fluid id and an amount (a bucket if left out), each written in the rule or read from
 * NBT. */
public class FluidStackRef {
    private NbtRef fluid;
    private NbtRef amount;

    public FluidStack get(Tag nbt) {
        String id = fluid.getString(nbt).orElseThrow(() -> new NullPointerException("No fluid in " + this));
        ResourceLocation location = ResourceLocation.tryParse(id);
        Fluid found = location == null ? Fluids.EMPTY : BuiltInRegistries.FLUID.get(location);
        if (found == Fluids.EMPTY) {
            return FluidStack.EMPTY;
        }
        return new FluidStack(found,
            Optional.ofNullable(amount).flatMap(ref -> ref.getInt(nbt)).orElse(FluidType.BUCKET_VOLUME));
    }

    @Override
    public String toString() {
        return "FluidStackRef{fluid=" + fluid + ", amount=" + amount + "}";
    }
}
