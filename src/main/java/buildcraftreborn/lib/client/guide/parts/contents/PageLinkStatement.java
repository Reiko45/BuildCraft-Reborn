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

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;

import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.lib.client.guide.PageLine;
import buildcraftreborn.lib.client.guide.entry.PageEntryStatement;
import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.client.guide.parts.GuidePage;
import buildcraftreborn.lib.client.guide.parts.GuidePageFactory;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.gui.statement.GuiElementStatementSource;
import buildcraftreborn.lib.misc.LegacyText;
import buildcraftreborn.lib.misc.StringUtilBC;

/** A link to a trigger's or action's page. */
public class PageLinkStatement extends PageLink {
    public final IStatement statement;
    public final List<String> tooltip;
    public final String searchText;

    public PageLinkStatement(boolean startVisible, IStatement statement) {
        super(createPageLine(statement), startVisible);
        this.statement = statement;
        List<String> tip = LegacyText.toLegacy(statement.getTooltip());
        if (tip.isEmpty()) {
            String uniqueTag = statement.getUniqueTag();
            this.tooltip = ImmutableList.of(uniqueTag);
            this.searchText = uniqueTag.toLowerCase(Locale.ROOT);
        } else {
            this.tooltip = tip;
            this.searchText = ChatFormatting.stripFormatting(String.join(" ", tip)).toLowerCase(Locale.ROOT);
        }
    }

    private static PageLine createPageLine(IStatement statement) {
        ISimpleDrawable icon = (graphics, x, y) -> GuiElementStatementSource.drawGuiSlot(graphics, statement, x, y);
        List<String> tooltip = LegacyText.toLegacy(statement.getTooltip());
        String title = tooltip.isEmpty() ? statement.getUniqueTag() : tooltip.get(0);
        // (tooltip colours are for a dark background)
        return new PageLine(icon, icon, 2, StringUtilBC.formatStringForWhite(title), true);
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
    public GuidePageFactory getFactoryLink() {
        return g -> new GuidePage(g, ImmutableList.of(), new PageValue<>(PageEntryStatement.INSTANCE, statement));
    }
}
