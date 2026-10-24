/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;

import buildcraftreborn.lib.gui.elem.ToolTip;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.IGuiArea;

/** Defines an element that can be rendered, that exists inside of a rectangle. Client only.
 * <p>
 * 1.21: the draw methods take the {@link GuiGraphics} that the screen is rendering with. Coordinates are absolute
 * screen coordinates, as in 1.12. */
public interface IGuiElement extends IGuiArea, ITooltipElement, IHelpElement {
    default void drawBackground(GuiGraphics graphics, float partialTicks) {}

    default void drawForeground(GuiGraphics graphics, float partialTicks) {}

    default void tick() {}

    /** {@inheritDoc}
     * <p>
     * This is called EVEN IF the mouse is not inside your width and height! */
    @Override
    default void addToolTips(List<ToolTip> tooltips) {}

    @Override
    default void addHelpElements(List<HelpPosition> elements) {}

    default List<IGuiElement> getThisAndChildrenAt(double x, double y) {
        if (contains(x, y)) {
            return List.of(this);
        } else {
            return List.of();
        }
    }

    /** Add debugging information to the list. Note that a lot of elements will be called for this, so keep the amount
     * of information minimal.
     *
     * @return An identifier for this element (usually a name) */
    default String getDebugInfo(List<String> info) {
        return toString();
    }
}
