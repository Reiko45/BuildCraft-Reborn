/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.tile;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;

/** Implemented by {@code TileSpringOil} in the energy module. */
public interface ITileOilSpring {

    /** Pumps should call this when they pump oil from this spring. */
    void onPumpOil(GameProfile pumpOwner, BlockPos oilPos);

}
