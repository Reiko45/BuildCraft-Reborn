/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.inventory.InventoryMenu;

import buildcraftreborn.BuildCraftReborn;

/** Block atlas sprite lookups. Client only.
 * <p>
 * 1.21: the block atlas holds every texture under {@code textures/block/} and {@code textures/item/}, so BuildCraft's
 * model and pipe textures live there (1.12 stitched them in by hand). */
public final class SpriteUtil {
    /** A plain white sprite, for LED parts of models (1.12's {@code ModelLoader.White}). */
    public static final ResourceLocation WHITE = BuildCraftReborn.id("block/white");

    private SpriteUtil() {}

    /** @return The block atlas sprite, or the missing sprite if there's no such texture. */
    public static TextureAtlasSprite getBlockSprite(ResourceLocation location) {
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

    public static TextureAtlasSprite getBlockSprite(String location) {
        return getBlockSprite(ResourceLocation.parse(location));
    }

    public static TextureAtlasSprite missingSprite() {
        return getBlockSprite(MissingTextureAtlasSprite.getLocation());
    }

    public static TextureAtlasSprite whiteSprite() {
        return getBlockSprite(WHITE);
    }
}
