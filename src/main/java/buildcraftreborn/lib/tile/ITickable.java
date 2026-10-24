/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.tile;

/** Implemented by tiles that run code every tick, on both sides. {@code BlockBCTile_Neptune} gives such tiles a
 * {@code BlockEntityTicker}; this keeps 1.12's {@code ITickable.update()} shape so ported tiles change little. */
public interface ITickable {
    void update();
}
