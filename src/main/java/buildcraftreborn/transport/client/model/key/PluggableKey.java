/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model.key;

import com.google.common.collect.ImmutableSet;

import net.minecraft.core.Direction;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.api.transport.pipe.IPipeHolder;
import buildcraftreborn.api.transport.pluggable.PipePluggable;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;

/** The static models of every pluggable on a pipe, for one render layer. */
public final class PluggableKey {
    private final ImmutableSet<PluggableModelKey> pluggables;
    private final int hash;

    public PluggableKey(EnumRenderLayer layer, IPipeHolder holder) {
        ImmutableSet.Builder<PluggableModelKey> builder = ImmutableSet.builder();
        for (Direction side : Direction.values()) {
            PipePluggable pluggable = holder.getPluggable(side);
            if (pluggable == null) continue;
            PluggableModelKey key = pluggable.getModelRenderKey(layer);
            if (key == null) continue;
            builder.add(key);
        }
        this.pluggables = builder.build();
        this.hash = pluggables.hashCode();
    }

    public ImmutableSet<PluggableModelKey> getKeys() {
        return pluggables;
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return pluggables.equals(((PluggableKey) obj).pluggables);
    }
}
