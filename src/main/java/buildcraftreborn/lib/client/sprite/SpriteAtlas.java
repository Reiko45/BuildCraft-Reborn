/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.sprite;

import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.render.ISprite;

/** An {@link ISprite} for a sprite of a texture atlas (such as the block atlas, which holds item textures too). */
public class SpriteAtlas implements ISprite {
    public final TextureAtlasSprite sprite;

    public SpriteAtlas(TextureAtlasSprite sprite) {
        this.sprite = sprite;
    }

    @Override
    public ResourceLocation getTexture() {
        return sprite.atlasLocation();
    }

    @Override
    public double getInterpU(double u) {
        return sprite.getU0() + (sprite.getU1() - sprite.getU0()) * u;
    }

    @Override
    public double getInterpV(double v) {
        return sprite.getV0() + (sprite.getV1() - sprite.getV0()) * v;
    }
}
