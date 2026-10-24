/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import com.mojang.blaze3d.platform.NativeImage;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.MissingTextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.sprite.SpriteAtlas;
import buildcraftreborn.lib.client.sprite.SpriteRaw;

/** An image: either a sprite of the block atlas (such as {@code buildcraftreborn:item/wrench}) or a whole texture
 * file (such as {@code buildcraftreborn:textures/gui/filler.png}). Its size is only found when a page is made, as
 * the atlas may not be ready when pages load. */
public class GuideImageFactory implements GuidePartFactory {
    private final String location;
    private final int width, height;

    public GuideImageFactory(String location) {
        this(location, -1, -1);
    }

    public GuideImageFactory(String location, int width, int height) {
        this.location = location;
        this.width = width;
        this.height = height;
    }

    @Override
    public GuideImage createNew(GuiGuide gui) {
        Minecraft mc = Minecraft.getInstance();
        TextureAtlas atlas = mc.getModelManager().getAtlas(TextureAtlas.LOCATION_BLOCKS);
        ResourceLocation resLoc = ResourceLocation.parse(location);
        TextureAtlasSprite stitched = atlas.getSprite(resLoc);
        ISprite sprite;
        int srcWidth, srcHeight;
        if (!stitched.contents().name().equals(MissingTextureAtlasSprite.getLocation())) {
            sprite = new SpriteAtlas(stitched);
            srcWidth = stitched.contents().width();
            srcHeight = stitched.contents().height();
        } else {
            int[] size = readSize(resLoc);
            if (size != null) {
                sprite = new SpriteRaw(resLoc, 0, 0, 1, 1);
                srcWidth = size[0];
                srcHeight = size[1];
            } else {
                sprite = new SpriteAtlas(stitched);
                srcWidth = stitched.contents().width();
                srcHeight = stitched.contents().height();
            }
        }
        return new GuideImage(gui, sprite, width <= 0 ? srcWidth : width, height <= 0 ? srcHeight : height);
    }

    private static int[] readSize(ResourceLocation location) {
        Optional<Resource> resource = Minecraft.getInstance().getResourceManager().getResource(location);
        if (resource.isEmpty()) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.image] Couldn't find image '{}'", location);
            return null;
        }
        try (InputStream in = resource.get().open(); NativeImage image = NativeImage.read(in)) {
            return new int[] { image.getWidth(), image.getHeight() };
        } catch (IOException io) {
            BuildCraftReborn.LOGGER.warn("[lib.guide.loader.image] Couldn't load image '{}' because {}", location,
                io.getMessage());
            return null;
        }
    }
}
