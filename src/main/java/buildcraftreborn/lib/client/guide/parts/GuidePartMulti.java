/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.List;
import java.util.function.BooleanSupplier;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;

/** Several parts that are only shown when something is true (such as "show lore"). */
public class GuidePartMulti extends GuidePart {
    public final List<GuidePart> parts;
    public final BooleanSupplier visibleFunction;

    public GuidePartMulti(GuiGuide gui, List<GuidePart> subParts, BooleanSupplier isVisible) {
        super(gui);
        this.parts = subParts;
        this.visibleFunction = isVisible;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuidePart part : parts) {
            part.setFontRenderer(fontRenderer);
        }
    }

    protected boolean isVisible() {
        return visibleFunction.getAsBoolean();
    }

    @Override
    public void updateScreen() {
        super.updateScreen();
        for (GuidePart part : parts) {
            part.updateScreen();
        }
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.renderIntoArea(x, y, width, height, current, index);
            }
        }
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        if (isVisible()) {
            for (GuidePart part : parts) {
                current = part.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
            }
        }
        return current;
    }
}
