/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.slot;

public interface IPhantomSlot {
    /** @return True if this slot can hold a count other than 0 (empty) or 1 (filled). */
    boolean canAdjustCount();

    /** @return True if a click puts a copy of the whole held stack in (the robotics requester), rather than one item
     *         that further clicks add to. */
    default boolean copiesWholeStack() {
        return false;
    }
}
