/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.sprite;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.render.ISprite;

/** Defines a sprite that is taken directly from the given texture file. */
public class SpriteRaw implements ISprite {
    public final ResourceLocation location;
    public final double uMin, vMin, width, height;

    /** @param xMin, yMin, width, height In pixels, within a texture of {@code textureSize} pixels square. */
    public SpriteRaw(ResourceLocation location, double xMin, double yMin, double width, double height,
        double textureSize) {
        this.location = location;
        this.uMin = xMin / textureSize;
        this.vMin = yMin / textureSize;
        this.width = width / textureSize;
        this.height = height / textureSize;
    }

    /** @param uMin, vMin, width, height Between 0 and 1. */
    public SpriteRaw(ResourceLocation location, double uMin, double vMin, double width, double height) {
        this.location = location;
        this.uMin = uMin;
        this.vMin = vMin;
        this.width = width;
        this.height = height;
    }

    @Override
    public ResourceLocation getTexture() {
        return location;
    }

    @Override
    public double getInterpU(double u) {
        return uMin + u * width;
    }

    @Override
    public double getInterpV(double v) {
        return vMin + v * height;
    }
}
