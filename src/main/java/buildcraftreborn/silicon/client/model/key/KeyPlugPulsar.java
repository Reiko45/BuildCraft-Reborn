/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.model.key;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import net.minecraft.core.Direction;

import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

public class KeyPlugPulsar extends PluggableModelKey {
    public KeyPlugPulsar(Direction side) {
        super(EnumRenderLayer.CUTOUT, side);
    }
}
