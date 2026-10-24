/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.sprite;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.render.ISprite;

/** A sprite in Minecraft's GUI sprite atlas ({@code textures/gui/sprites/}). Replaces 1.12's icons that were stitched
 * into the block atlas (ledger backgrounds, the help and engine icons). Animated sprites animate through the atlas. */
public class SpriteGui implements ISprite {
    public final ResourceLocation id;

    /** @param id The sprite name, for example {@code buildcraftreborn:icons/help} for
     *            {@code textures/gui/sprites/icons/help.png}. */
    public SpriteGui(ResourceLocation id) {
        this.id = id;
    }

    private TextureAtlasSprite sprite() {
        return Minecraft.getInstance().getGuiSprites().getSprite(id);
    }

    @Override
    public ResourceLocation getTexture() {
        return sprite().atlasLocation();
    }

    @Override
    public double getInterpU(double u) {
        TextureAtlasSprite s = sprite();
        return s.getU0() + u * (s.getU1() - s.getU0());
    }

    @Override
    public double getInterpV(double v) {
        TextureAtlasSprite s = sprite();
        return s.getV0() + v * (s.getV1() - s.getV0());
    }
}
