/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.lib.gui.pos.IGuiPosition;

/** Something that can draw itself at a position. 1.21: takes the {@link GuiGraphics} to draw with. Client only. */
@FunctionalInterface
public interface ISimpleDrawable {
    void drawAt(GuiGraphics graphics, double x, double y);

    default void drawAt(GuiGraphics graphics, IGuiPosition element) {
        drawAt(graphics, element.getX(), element.getY());
    }

    default ISimpleDrawable andThen(ISimpleDrawable after) {
        ISimpleDrawable t = this;
        return (g, x, y) -> {
            t.drawAt(g, x, y);
            after.drawAt(g, x, y);
        };
    }
}
