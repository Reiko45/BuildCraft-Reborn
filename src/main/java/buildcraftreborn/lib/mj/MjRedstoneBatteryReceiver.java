/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.mj;

import buildcraftreborn.api.mj.IMjRedstoneReceiver;
import buildcraftreborn.api.mj.MjBattery;

public class MjRedstoneBatteryReceiver extends MjBatteryReceiver implements IMjRedstoneReceiver {
    public MjRedstoneBatteryReceiver(MjBattery battery) {
        super(battery);
    }
}
