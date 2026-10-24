/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.gui.GuiSpriteScaled;

/** An image, with a border. */
public class GuideImage extends GuidePart {
    final ISprite sprite;
    final GuiSpriteScaled icon;
    final int width, height;

    public GuideImage(GuiGuide gui, ISprite sprite, int width, int height) {
        super(gui);
        this.sprite = sprite;
        this.width = width;
        this.height = height;
        icon = new GuiSpriteScaled(sprite, width, height);
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (height - current.pixel < this.height) {
            current = current.nextPage();
        }
        if (index == current.page) {
            y += current.pixel;
            int w = this.width;
            int h = this.height;
            icon.drawAt(graphics(), x, y);
            GuiGuide.BORDER_TOP_LEFT.drawAt(graphics(), x, y);
            GuiGuide.BORDER_TOP_RIGHT.drawAt(graphics(), x + w - GuiGuide.BORDER_TOP_RIGHT.width, y);
            GuiGuide.BORDER_BOTTOM_LEFT.drawAt(graphics(), x, y + h - GuiGuide.BORDER_BOTTOM_LEFT.height);
            GuiGuide.BORDER_BOTTOM_RIGHT.drawAt(graphics(), x + w - GuiGuide.BORDER_BOTTOM_RIGHT.width,
                y + h - GuiGuide.BORDER_BOTTOM_RIGHT.height);
        }
        return current.nextLine(this.height + 1, height);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (height - current.pixel < this.height) {
            current = current.nextPage();
        }
        return current.nextLine(this.height + 1, height);
    }
}
