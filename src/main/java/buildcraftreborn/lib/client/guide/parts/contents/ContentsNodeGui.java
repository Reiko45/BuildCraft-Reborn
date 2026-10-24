/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.font.IFontRenderer;
import buildcraftreborn.lib.client.guide.parts.GuideChapter;
import buildcraftreborn.lib.client.guide.parts.GuidePageBase;
import buildcraftreborn.lib.client.guide.parts.GuidePart.PagePosition;
import buildcraftreborn.lib.client.guide.parts.GuidePart;

/** The visible part of the contents tree, as guide parts (headings and links) in order. */
public class ContentsNodeGui {
    public final GuiGuide gui;
    public final ContentsNode node;
    private IFontRenderer fontRenderer;
    private List<GuideChapter> chapters;
    private GuidePart[] parts;
    private PageLink[] links;

    public ContentsNodeGui(GuiGuide gui, ContentsNode node) {
        this.gui = gui;
        this.node = node;
    }

    public List<GuideChapter> getChapters() {
        if (populate() || chapters == null) {
            chapters = new ArrayList<>();
            for (GuidePart part : parts) {
                if (part instanceof GuideChapter chapter) {
                    chapters.add(chapter);
                }
            }
        }
        return chapters;
    }

    public void setFontRenderer(IFontRenderer fontRenderer) {
        this.fontRenderer = fontRenderer;
        if (parts != null) {
            for (GuidePart part : parts) {
                part.setFontRenderer(fontRenderer);
            }
        }
    }

    public void invalidate() {
        parts = null;
        links = null;
        chapters = null;
    }

    private boolean populate() {
        if (parts != null) {
            return false;
        }
        List<GuidePart> allText = new ArrayList<>();
        List<PageLink> allLinks = new ArrayList<>();
        // Depth first search
        Deque<IContentsNode> queue = new ArrayDeque<>();
        addAllReversed(queue, node.getVisibleChildren());
        while (!queue.isEmpty()) {
            IContentsNode next = queue.removeLast();
            GuidePart part = next.createGuidePart(gui);
            if (fontRenderer != null) {
                part.setFontRenderer(fontRenderer);
            }
            allText.add(part);
            allLinks.add(next instanceof PageLink link ? link : null);
            addAllReversed(queue, next.getVisibleChildren());
        }
        parts = allText.toArray(new GuidePart[0]);
        links = allLinks.toArray(new PageLink[0]);
        return true;
    }

    private static void addAllReversed(Deque<IContentsNode> queue, IContentsNode[] nodes) {
        for (int i = nodes.length - 1; i >= 0; i--) {
            queue.addLast(nodes[i]);
        }
    }

    public PagePosition render(int x, int y, int width, int height, PagePosition current, int index) {
        return iterate(current, height, (pos, part, link) -> part.renderIntoArea(x, y, width, height, pos, index));
    }

    public void onClicked(int x, int y, int width, int height, PagePosition current, int index) {
        iterate(current, height, (pos, part, link) -> {
            pos = part.renderIntoArea(x, y, width, height, pos, -1);
            if (pos.page == index && part.wasHovered() && link != null) {
                GuidePageBase page = link.getFactoryLink().createNew(gui);
                if (page != null) {
                    gui.openPage(page);
                    return null;
                }
            }
            return pos;
        });
    }

    @FunctionalInterface
    private interface IGuideBitIter {
        @Nullable
        PagePosition iterate(PagePosition pos, GuidePart part, @Nullable PageLink link);
    }

    @Nullable
    private PagePosition iterate(PagePosition pos, int height, IGuideBitIter iter) {
        populate();
        for (int i = 0; i < links.length; i++) {
            GuidePart part = parts[i];
            PageLink link = links[i];
            int space = 16;
            if (link == null) {
                // Keep a heading on the same page as its first link
                for (int j = i; j < links.length; j++) {
                    if (links[j] != null) {
                        pos = pos.guaranteeSpace(space * (1 + j - i), height);
                        break;
                    }
                }
            }
            pos = iter.iterate(pos, part, link);
            if (pos == null) {
                return null;
            }
        }
        return pos;
    }
}
