/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts;

import java.util.List;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;
import buildcraftreborn.lib.client.guide.ref.GuideGroupSet.GroupDirection;
import buildcraftreborn.lib.client.guide.ref.GuideGroupSet;
import buildcraftreborn.lib.gui.ISimpleDrawable;

/** A group of related things (such as the blocks that need a full power provider), each a link to its page. */
public class GuidePartGroup extends GuidePart {
    public final GuideGroupSet group;
    private final GuideText[] texts;
    private final Object[] values;

    public GuidePartGroup(GuiGuide gui, GuideGroupSet group, GroupDirection direction) {
        super(gui);
        this.group = group;
        List<PageValue<?>> groupValues = group.getValues(direction);
        values = new Object[groupValues.size()];
        for (int i = 0; i < values.length; i++) {
            values[i] = groupValues.get(i).value;
        }
        texts = new GuideText[1 + values.length];
        texts[0] = new GuideText(gui, group.getTitle(direction));
        int i = 1;
        for (PageValue<?> single : groupValues) {
            ISimpleDrawable icon = single.createDrawable();
            texts[i++] = new GuideText(gui, new PageLine(icon, icon, 1, single.title, true, single::getTooltip));
        }
    }

    @Override
    public int hashCode() {
        return group.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return group == ((GuidePartGroup) obj).group;
    }

    @Override
    public void setFontRenderer(IFontRenderer fontRenderer) {
        super.setFontRenderer(fontRenderer);
        for (GuideText text : texts) {
            text.setFontRenderer(fontRenderer);
        }
    }

    @Override
    public PagePosition renderIntoArea(int x, int y, int width, int height, PagePosition current, int index) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        for (GuideText text : texts) {
            current = text.renderIntoArea(x, y, width, height, current, index);
        }
        return current;
    }

    @Override
    public PagePosition handleMouseClick(int x, int y, int width, int height, PagePosition current, int index,
        int mouseX, int mouseY) {
        current = current.guaranteeSpace(getFontRenderer().getMaxFontHeight() * 4, height);
        for (int i = 0; i < texts.length; i++) {
            GuideText text = texts[i];
            current = text.handleMouseClick(x, y, width, height, current, index, mouseX, mouseY);
            if (text.wasHovered && current.page == index && i > 0) {
                GuidePageFactory factory = GuideManager.INSTANCE.getFactoryFor(values[i - 1]);
                if (factory != null) {
                    gui.openPage(factory.createNew(gui));
                    return new PagePosition(Integer.MAX_VALUE / 4, 0);
                }
            }
        }
        return current;
    }
}
