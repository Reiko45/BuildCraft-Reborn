/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.pipe.flow;

import net.minecraft.core.Direction;

public interface IPipeTransportPowerHook {

    /** Override default behavior on receiving energy into the pipe.
     *
     * @return The amount of power used, or -1 for default behavior. */
    int receivePower(Direction from, long val);

    /** Override default requested power. */
    int requestPower(Direction from, long amount);
}
