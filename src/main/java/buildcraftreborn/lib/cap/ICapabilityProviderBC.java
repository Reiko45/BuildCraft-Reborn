/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.cap;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;

import net.neoforged.neoforge.capabilities.BlockCapability;

/** Something that can answer a sided block capability query. 1.12's {@code ICapabilityProvider}, reduced to the one
 * method BuildCraft needs. */
@FunctionalInterface
public interface ICapabilityProviderBC {
    @Nullable
    <T> T getCapability(BlockCapability<T, @Nullable Direction> capability, @Nullable Direction side);
}
