/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.block;

/** Marker interface for {@link BlockTank} to determine if the block below should be considered a tank block when
 * visually connecting below. This should only really be implemented when the block creates an instance of
 * {@link buildcraftreborn.factory.tile.TileTank}. */
public interface ITankBlockConnector {}
