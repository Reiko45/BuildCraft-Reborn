/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.transport.pluggable;

import java.util.Objects;

import net.minecraft.core.Direction;

import buildcraftreborn.api.core.render.EnumRenderLayer;

public abstract class PluggableModelKey {
    public final EnumRenderLayer layer;
    public final Direction side;
    private final int hash;

    public PluggableModelKey(EnumRenderLayer layer, Direction side) {
        if (layer != EnumRenderLayer.CUTOUT && layer != EnumRenderLayer.TRANSLUCENT) {
            throw new IllegalArgumentException("Can only use CUTOUT or TRANSLUCENT at the moment (was " + layer + ")");
        }
        if (side == null) throw new NullPointerException("side");
        this.layer = layer;
        this.side = side;
        this.hash = Objects.hash(layer, side);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null) return false;
        if (getClass() != obj.getClass()) return false;
        PluggableModelKey other = (PluggableModelKey) obj;
        if (layer != other.layer) return false;
        if (side != other.side) return false;
        return true;
    }

    @Override
    public int hashCode() {
        return hash;
    }
}
