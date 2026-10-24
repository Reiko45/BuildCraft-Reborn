/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.help;

import java.util.List;

import buildcraftreborn.lib.gui.IGuiElement;
import buildcraftreborn.lib.gui.help.ElementHelpInfo.HelpPosition;
import buildcraftreborn.lib.gui.pos.IGuiArea;

/** A simple, non-drawing, gui element that holds an {@link ElementHelpInfo}. */
public class DummyHelpElement implements IGuiElement {
    public final IGuiArea area;
    public final ElementHelpInfo help;

    public DummyHelpElement(IGuiArea area, ElementHelpInfo help) {
        this.area = area;
        this.help = help;
    }

    @Override
    public double getX() {
        return area.getX();
    }

    @Override
    public double getY() {
        return area.getY();
    }

    @Override
    public double getWidth() {
        return area.getWidth();
    }

    @Override
    public double getHeight() {
        return area.getHeight();
    }

    @Override
    public void addHelpElements(List<HelpPosition> elements) {
        elements.add(help.target(area));
    }
}
