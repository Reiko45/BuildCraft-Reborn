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

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.client.guide.GuiGuide;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.gui.GuiStack;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.LegacyText;

/** A link to an item's page (which may be a page of just its recipes). It can be found by any line of its tooltip. */
public final class PageLinkItemStack extends PageLink {
    public final ItemStack stack;
    public final List<String> tooltip;
    public final String searchText;

    public static PageLinkItemStack create(boolean startVisible, ItemStack stack) {
        List<String> tooltip = LegacyText.getUnformattedTooltip(stack);
        if (tooltip.isEmpty()) {
            tooltip = List.of(stack.getHoverName().getString());
        }
        String searchText = joinTooltip(tooltip);
        ISimpleDrawable icon = new GuiStack(stack);
        PageLine text = new PageLine(icon, icon, 2, tooltip.get(0), true);
        return new PageLinkItemStack(text, startVisible, stack, tooltip, searchText);
    }

    private static String joinTooltip(List<String> tooltip) {
        StringBuilder joiner = new StringBuilder();
        for (int i = 0; i < tooltip.size(); i++) {
            if (i > 0) {
                joiner.append('\n');
            }
            joiner.append(ChatFormatting.stripFormatting(tooltip.get(i)).toLowerCase(Locale.ROOT));
        }
        return joiner.toString();
    }

    private PageLinkItemStack(PageLine text, boolean startVisible, ItemStack stack, List<String> tooltip,
        String searchText) {
        super(text, startVisible);
        this.stack = stack;
        this.tooltip = tooltip;
        this.searchText = searchText;
    }

    @Override
    public String getSearchName() {
        return searchText;
    }

    @Override
    @Nullable
    public List<String> getTooltip() {
        return tooltip.size() == 1 ? null : tooltip;
    }

    @Override
    public void appendTooltip(GuiGuide gui) {
        if (tooltip.size() > 1) {
            gui.tooltipStack = stack;
        }
    }

    @Override
    public GuidePageFactory getFactoryLink() {
        return GuideManager.INSTANCE.getPageFor(stack);
    }
}
