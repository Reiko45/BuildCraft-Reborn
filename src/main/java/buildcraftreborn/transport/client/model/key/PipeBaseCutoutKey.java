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
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.transport.pipe.PipeFaceTex;

/** What the solid (cutout) part of a pipe looks like: its textures, connections and (for border colouring) its
 * paint. Plain data, so the client builds it from the tile on the main thread. */
public final class PipeBaseCutoutKey {
    public final PipeDefinition definition;
    public final PipeFaceTex centerSprite;
    public final PipeFaceTex[] sideSprites;
    public final float[] connections;
    public final DyeColor colour;
    public final EnumPipeColourType colourType;
    private final int hashCode;

    public PipeBaseCutoutKey(PipeModelKey key) {
        definition = key.definition;
        centerSprite = key.center;
        sideSprites = key.sides;
        connections = key.connected;
        EnumPipeColourType defColourType = key.getColourType();
        if (key.colour != null && canBakeCutoutColour(defColourType)) {
            this.colour = key.colour;
            this.colourType = defColourType;
        } else {
            this.colour = null;
            this.colourType = null;
        }
        hashCode = Objects.hash(System.identityHashCode(definition), centerSprite, Arrays.hashCode(sideSprites),
            Arrays.hashCode(connections), colour, colourType);
    }

    private static boolean canBakeCutoutColour(EnumPipeColourType type) {
        return type == EnumPipeColourType.BORDER_OUTER || type == EnumPipeColourType.BORDER_INNER;
    }

    @Override
    public int hashCode() {
        return hashCode;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PipeBaseCutoutKey other = (PipeBaseCutoutKey) obj;
        return definition == other.definition && Objects.equals(centerSprite, other.centerSprite)
            && colour == other.colour && colourType == other.colourType
            && Arrays.equals(connections, other.connections) && Arrays.equals(sideSprites, other.sideSprites);
    }

    @Override
    public String toString() {
        return "PipeBaseCutoutKey [center=" + centerSprite + ", sides=" + Arrays.toString(sideSprites)
            + ", connections=" + Arrays.toString(connections) + "]";
    }
}
