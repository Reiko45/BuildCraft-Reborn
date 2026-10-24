/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.sprite;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.gui.pos.IGuiArea;
import buildcraftreborn.lib.misc.GuiUtil;

/** Defines and draws a 9-sliced sprite. */
public class SpriteNineSliced {
    public final ISprite sprite;
    public final double xMin, yMin, xMax, yMax;
    public final double xScale, yScale;

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int textureSize) {
        this(sprite, xMin, yMin, xMax, yMax, textureSize, textureSize);
    }

    public SpriteNineSliced(ISprite sprite, int xMin, int yMin, int xMax, int yMax, int xScale, int yScale) {
        this.sprite = sprite;
        this.xMin = xMin / (double) xScale;
        this.yMin = yMin / (double) yScale;
        this.xMax = xMax / (double) xScale;
        this.yMax = yMax / (double) yScale;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double scale) {
        this(sprite, xMin, yMin, xMax, yMax, scale, scale);
    }

    public SpriteNineSliced(ISprite sprite, double xMin, double yMin, double xMax, double yMax, double xScale,
        double yScale) {
        this.sprite = sprite;
        this.xMin = xMin;
        this.yMin = yMin;
        this.xMax = xMax;
        this.yMax = yMax;
        this.xScale = xScale;
        this.yScale = yScale;
    }

    public void draw(GuiGraphics graphics, IGuiArea element) {
        draw(graphics, element.getX(), element.getY(), element.getWidth(), element.getHeight());
    }

    public void draw(GuiGraphics graphics, double x, double y, double width, double height) {
        double sx = xScale;
        double sy = yScale;
        double[] xa = { x, x + xMin * sx, x + width + (xMax - 1) * sx, x + width };
        double[] ya = { y, y + yMin * sy, y + height + (yMax - 1) * sy, y + height };
        double[] ua = { 0, xMin, xMax, 1 };
        double[] va = { 0, yMin, yMax, 1 };
        for (int xi = 0; xi < 3; xi++) {
            for (int yi = 0; yi < 3; yi++) {
                if (xa[xi + 1] <= xa[xi] || ya[yi + 1] <= ya[yi]) {
                    continue;
                }
                GuiUtil.drawTexturedQuad(graphics, sprite.getTexture(), xa[xi], ya[yi], xa[xi + 1], ya[yi + 1],
                    sprite.getInterpU(ua[xi]), sprite.getInterpV(va[yi]), sprite.getInterpU(ua[xi + 1]),
                    sprite.getInterpV(va[yi + 1]));
            }
        }
    }
}
