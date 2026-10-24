/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.core.render;

import net.minecraft.resources.ResourceLocation;

/** Holds information on some sort of sprite. These might not be part of the main texture atlas (they might be from a
 * GUI texture).
 * <p>
 * <b> IMPORTANT: Unlike Minecraft's {@code TextureAtlasSprite} this uses co-ordinates between 0 and 1, rather than 0
 * and 16! </b>
 * <p>
 * 1.21 change: {@code bindTexture()} is gone because rendering no longer binds textures globally. Instead a sprite
 * names its backing texture with {@link #getTexture()}, and renderers pass that to {@code GuiGraphics} or a
 * {@code RenderType}. */
public interface ISprite {
    /** @return The texture (a full texture file path, or an atlas location) that the UV coordinates refer to. */
    ResourceLocation getTexture();

    /** @param u A value between 0 and 1
     * @return The interpolated U coordinate within {@link #getTexture()}, between 0 and 1. */
    double getInterpU(double u);

    /** @param v A value between 0 and 1
     * @return The interpolated V coordinate within {@link #getTexture()}, between 0 and 1. */
    double getInterpV(double v);
}
