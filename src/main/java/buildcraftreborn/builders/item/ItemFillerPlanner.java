/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.item;

import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.ItemAddon;

/** Puts a filler planner addon on the corner of a volume box. */
public class ItemFillerPlanner extends ItemAddon {
    public ItemFillerPlanner(Properties properties) {
        super(properties);
    }

    @Override
    public Addon createAddon() {
        return new AddonFillerPlanner();
    }
}
