/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.parts.contents.PageLink;

/** A link to another page. */
public class GuidePartLink extends GuidePart {
    public final PageLink link;

    public GuidePartLink(GuiGuide gui, PageLink link) {
        super(gui);
        this.link = link;
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        return renderLine(current, link.text, x, y, width, height, index);
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        PagePosition pos = renderLine(current, link.text, x, y, width, height, -1);
        if (pos.page == index && wasHovered()) {
            GuidePageBase page = link.getFactoryLink().createNew(gui);
            if (page != null) {
                gui.openPage(page);
            }
        }
        return pos;
    }
}
