/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.engine;

import buildcraftreborn.api.mj.IMjConnector;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.IMjRedstoneReceiver;

public class EngineConnector implements IMjConnector {
    public final boolean redstoneOnly;

    public EngineConnector(boolean redstoneOnly) {
        this.redstoneOnly = redstoneOnly;
    }

    @Override
    public boolean canConnect(IMjConnector other) {
        if (other instanceof IMjReceiver receiver && receiver.canReceive()) {
            if (redstoneOnly) {
                return other instanceof IMjRedstoneReceiver;
            }
            return true;
        }
        return false;
    }
}
