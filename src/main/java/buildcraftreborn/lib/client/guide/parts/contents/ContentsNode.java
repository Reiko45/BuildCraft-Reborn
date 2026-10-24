/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.parts.GuideChapterWithin;
import buildcraftreborn.lib.client.guide.parts.GuidePart;
import buildcraftreborn.lib.client.guide.parts.GuideText;
import buildcraftreborn.lib.misc.StringUtilBC;

/** A heading of the contents page, with its (sorted) children. */
public class ContentsNode implements IContentsNode {
    public final String title;
    public final int indent;
    private final Map<String, IContentsNode> nodes = new HashMap<>();
    private IContentsNode[] sortedNodes = new IContentsNode[0];
    IContentsNode[] visibleNodes = new IContentsNode[0];
    private boolean needsSorting = false;

    public ContentsNode(String title, int indent) {
        this.title = title;
        this.indent = indent;
    }

    @Override
    public String getSearchName() {
        return title;
    }

    @Override
    public GuidePart createGuidePart(GuiGuide gui) {
        if (indent == 0) {
            return new GuideChapterWithin(gui, ChatFormatting.UNDERLINE + title);
        }
        return new GuideText(gui, new PageLine(indent + 1, ChatFormatting.UNDERLINE + title, false));
    }

    @Nullable
    public IContentsNode getChild(String childKey) {
        return nodes.get(childKey);
    }

    @Override
    public void addChild(IContentsNode node) {
        nodes.put(node.getSearchName(), node);
        needsSorting = true;
    }

    @Override
    public IContentsNode[] getVisibleChildren() {
        return visibleNodes;
    }

    @Override
    public boolean isVisible() {
        return visibleNodes.length != 0;
    }

    @Override
    public void sort() {
        if (!needsSorting) {
            return;
        }
        needsSorting = false;
        sortedNodes = nodes.values().toArray(new IContentsNode[0]);
        Arrays.sort(sortedNodes, StringUtilBC.compareBasicReadable(IContentsNode::getSearchName));
        for (IContentsNode node : sortedNodes) {
            node.sort();
        }
        calcVisibility();
    }

    @Override
    public void calcVisibility() {
        List<IContentsNode> visible = new ArrayList<>();
        for (IContentsNode node : sortedNodes) {
            if (node.isVisible()) {
                visible.add(node);
            }
        }
        visibleNodes = visible.toArray(new IContentsNode[0]);
    }

    @Override
    public void resetVisibility() {
        for (IContentsNode node : sortedNodes) {
            node.resetVisibility();
        }
        calcVisibility();
    }

    @Override
    public void setVisible(Set<PageLink> matches) {
        for (IContentsNode node : sortedNodes) {
            node.setVisible(matches);
        }
        calcVisibility();
    }
}
