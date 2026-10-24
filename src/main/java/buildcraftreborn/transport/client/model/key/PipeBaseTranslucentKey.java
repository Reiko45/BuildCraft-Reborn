/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model.key;

import java.util.Arrays;
import java.util.Objects;

import net.minecraft.world.item.DyeColor;

import buildcraftreborn.api.transport.pipe.EnumPipeColourType;

/** The translucent paint over a pipe painted with {@link EnumPipeColourType#TRANSLUCENT} colouring (if any). */
public final class PipeBaseTranslucentKey {
    public final DyeColor colour;
    public final float[] connections;
    private final int hashCode;

    public PipeBaseTranslucentKey(PipeModelKey key) {
        this.colour = key.getColourType() == EnumPipeColourType.TRANSLUCENT ? key.colour : null;
        if (colour == null) {
            connections = null;
            hashCode = 0;
        } else {
            connections = key.connected;
            hashCode = Objects.hash(colour, Arrays.hashCode(connections));
        }
    }

    public boolean shouldRender() {
        return colour != null;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof PipeBaseTranslucentKey other)) return false;
        // Nothing to draw on either: equal, whatever the other fields are
        if (!shouldRender() && !other.shouldRender()) return true;
        return Arrays.equals(connections, other.connections) && colour == other.colour;
    }

    @Override
    public String toString() {
        return "PipeBaseTranslucentKey [colour=" + colour + ", connections=" + Arrays.toString(connections) + "]";
    }
}
