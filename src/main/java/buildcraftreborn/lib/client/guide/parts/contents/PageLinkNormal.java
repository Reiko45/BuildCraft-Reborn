/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.parts.contents;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;

import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;

/** A link to a page that was loaded from a file. */
public class PageLinkNormal extends PageLink {
    public final GuidePageFactory factory;
    @Nullable
    public final List<String> tooltip;

    public PageLinkNormal(PageLine text, boolean startVisible, @Nullable List<String> tooltip,
        GuidePageFactory factory) {
        super(text, startVisible);
        this.factory = factory;
        this.tooltip = (tooltip == null || tooltip.size() != 1 || strip(tooltip.get(0)).equals(strip(text.text)))
            ? null : tooltip;
    }

    private static String strip(String text) {
        return ChatFormatting.stripFormatting(text.trim());
    }

    @Override
    @Nullable
    public List<String> getTooltip() {
        return tooltip;
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return factory;
    }
}
