/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.gui.pos.GuiRectangle;
import buildcraftreborn.lib.gui.pos.IGuiArea;

/** An {@link ISimpleDrawable} that draws the specified {@link ISprite} as-is into the given width and height. */
public class GuiSpriteScaled implements ISimpleDrawable {
    public final ISprite sprite;
    public final IGuiArea area;

    public GuiSpriteScaled(ISprite sprite, IGuiArea area) {
        this.sprite = sprite;
        this.area = area;
    }

    public GuiSpriteScaled(ISprite sprite, double width, double height) {
        this(sprite, new GuiRectangle(width, height));
    }

    @Override
    public void drawAt(GuiGraphics graphics, double x, double y) {
        x += area.getX();
        y += area.getY();
        GuiIcon.draw(graphics, sprite, x, y, x + area.getWidth(), y + area.getHeight());
    }
}
