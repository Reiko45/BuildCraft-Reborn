/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.sprite.SpriteRaw;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.gui.pos.IGuiPosition;
import buildcraftreborn.lib.misc.GuiUtil;

/** A rectangle of a GUI texture, drawn at its natural size, scaled, or cut. Client only.
 * <p>
 * Port note: 1.12's {@code drawCustomQuad} (perspective-correct arbitrary quads with {@code glTexCoord4d}) and
 * {@code createDynamicTexture} are not ported yet; nothing in phase 1 uses them. */
public class GuiIcon implements ISimpleDrawable {
    public final ISprite sprite;
    public final int textureSize;
    public final int width, height;

    public GuiIcon(ISprite sprite, int textureSize) {
        this.sprite = sprite;
        this.textureSize = textureSize;
        this.width = (int) (Math.abs(sprite.getInterpU(1) - sprite.getInterpU(0)) * textureSize);
        this.height = (int) (Math.abs(sprite.getInterpV(1) - sprite.getInterpV(0)) * textureSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height, int texSize) {
        this(new SpriteRaw(texture, u, v, width, height, texSize), texSize);
    }

    public GuiIcon(ResourceLocation texture, double u, double v, double width, double height) {
        this(texture, u, v, width, height, 256);
    }

    public GuiIcon offset(double u, double v) {
        SpriteRaw raw = (SpriteRaw) sprite;
        double uMin = raw.uMin + u / textureSize;
        double vMin = raw.vMin + v / textureSize;
        return new GuiIcon(new SpriteRaw(raw.location, uMin, vMin, raw.width, raw.height), textureSize);
    }

    public boolean containsGuiPos(double x, double y, IGuiPosition pos) {
        return new GuiRectangle(x, y, width, height).contains(pos);
    }

    @Override
    public void drawAt(GuiGraphics graphics, double x, double y) {
        drawScaledInside(graphics, x, y, this.width, this.height);
    }

    public void drawScaledInside(GuiGraphics graphics, IGuiArea element) {
        drawScaledInside(graphics, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void drawScaledInside(GuiGraphics graphics, double x, double y, double drawnWidth, double drawnHeight) {
        draw(graphics, sprite, x, y, x + drawnWidth, y + drawnHeight);
    }

    public void drawCutInside(GuiGraphics graphics, IGuiArea element) {
        drawCutInside(graphics, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    /** Draws as much of the icon as fits in the given size, without scaling. */
    public void drawCutInside(GuiGraphics graphics, double x, double y, double displayWidth, double displayHeight) {
        displayWidth = Math.min(this.width, displayWidth);
        displayHeight = Math.min(this.height, displayHeight);
        drawCustomScaledAt(graphics, sprite, x, y, x + displayWidth, y + displayHeight, 0, 0, displayWidth / width,
            displayHeight / height);
    }

    public void drawCustomScaledAt(GuiGraphics graphics, IGuiArea area, double texU0, double texV0, double texU1,
        double texV1) {
        drawCustomScaledAt(graphics, sprite, area.getX(), area.getY(), area.getEndX(), area.getEndY(), texU0, texV0,
            texU1, texV1);
    }

    /** @param texU0 Minimum texture U co-ordinate within this sprite, between 0 and 1 (and likewise the others). */
    public static void drawCustomScaledAt(GuiGraphics graphics, ISprite sprite, double xMin, double yMin, double xMax,
        double yMax, double texU0, double texV0, double texU1, double texV1) {
        GuiUtil.drawTexturedQuad(graphics, sprite.getTexture(), xMin, yMin, xMax, yMax, sprite.getInterpU(texU0),
            sprite.getInterpV(texV0), sprite.getInterpU(texU1), sprite.getInterpV(texV1));
    }

    public static void draw(GuiGraphics graphics, ISprite sprite, double xMin, double yMin, double xMax, double yMax) {
        drawCustomScaledAt(graphics, sprite, xMin, yMin, xMax, yMax, 0, 0, 1, 1);
    }
}
