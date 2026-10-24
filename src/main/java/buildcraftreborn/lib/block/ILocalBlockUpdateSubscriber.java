/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.block;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** Something that wants to hear about block changes near it. See {@link LocalBlockUpdateNotifier}. */
public interface ILocalBlockUpdateSubscriber {
    /** @return The position used with {@link #getUpdateRange()} to decide whether a change is close enough. */
    BlockPos getSubscriberPos();

    /** @return How far (on each axis) from {@link #getSubscriberPos()} a change may be and still be reported. */
    int getUpdateRange();

    /** Called when a block within range changed. */
    void setWorldUpdated(Level level, BlockPos eventPos, BlockState newState);
}
