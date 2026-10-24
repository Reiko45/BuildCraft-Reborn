/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.client.guide.parts.GuideText;

/** A link (on the contents page, or a page) to a page. */
public abstract class PageLink implements IContentsLeaf {
    public final PageLine text;
    public final boolean startVisible;
    private final String lowerCaseName;
    private boolean visible;

    public PageLink(PageLine text, boolean startVisible) {
        this.text = text;
        this.startVisible = startVisible;
        lowerCaseName = text.text.toLowerCase(Locale.ROOT);
        visible = startVisible;
    }

    @Override
    public String getSearchName() {
        return lowerCaseName;
    }

    /** @return The tooltip to be shown if it is different to the search name and displayed text. */
    @Nullable
    protected List<String> getTooltip() {
        return null;
    }

    public void appendTooltip(GuiGuide gui) {
        List<String> tooltip = getTooltip();
        if (tooltip != null && !tooltip.isEmpty()) {
            gui.tooltips.add(tooltip);
        }
    }

    @Override
    public boolean isVisible() {
        return visible;
    }

    @Override
    public void setVisible(Set<PageLink> matches) {
        visible = matches.contains(this);
    }

    @Override
    public void resetVisibility() {
        visible = startVisible;
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        return new GuideText(gui, text) {
            @Override
            protected void renderTooltip() {
                appendTooltip(gui);
            }
        };
    }

    public abstract GuidePageFactory getFactoryLink();
}
