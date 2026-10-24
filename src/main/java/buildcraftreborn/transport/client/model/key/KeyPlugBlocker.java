/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model.key;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;

import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

public final class KeyPlugBlocker extends PluggableModelKey {
    public KeyPlugBlocker(Direction side) {
        super(EnumRenderLayer.CUTOUT, side);
    }

    /** Factory for {@link #KeyPlugBlocker(Direction)} that avoids class verification errors. */
    public static PluggableModelKey create(Direction side) {
        return new KeyPlugBlocker(side);
    }
}
