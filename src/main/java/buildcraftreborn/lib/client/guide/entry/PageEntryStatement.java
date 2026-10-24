/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import java.util.List;
import java.util.TreeMap;

import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import buildcraftreborn.api.statements.IAction;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.ITrigger;
import buildcraftreborn.api.statements.StatementManager;
import buildcraftreborn.lib.client.guide.GuideManager;
import buildcraftreborn.lib.client.guide.data.JsonTypeTags;
import buildcraftreborn.lib.client.guide.parts.contents.PageLinkStatement;
import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.gui.statement.GuiElementStatementSource;
import buildcraftreborn.lib.misc.LegacyText;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** Pages about gate triggers and actions. */
public class PageEntryStatement extends PageValueType<IStatement> {
    public static final PageEntryStatement INSTANCE = new PageEntryStatement();

    private static final JsonTypeTags TRIGGER_TAGS = new JsonTypeTags("buildcraft.guide.contents.triggers");
    private static final JsonTypeTags ACTION_TAGS = new JsonTypeTags("buildcraft.guide.contents.actions");

    @Override
    public Class<IStatement> getEntryClass() {
        return IStatement.class;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer) {
        for (IStatement statement : new TreeMap<>(StatementManager.statements).values()) {
            if (!GuideManager.INSTANCE.objectsAdded.add(statement)) {
                continue;
            }
            final JsonTypeTags parent;
            if (statement instanceof ITrigger) {
                parent = TRIGGER_TAGS;
            } else if (statement instanceof IAction) {
                parent = ACTION_TAGS;
            } else {
                continue;
            }
            consumer.addChild(parent, new PageLinkStatement(false, statement));
        }
    }

    /** 1.21: statements that don't exist (for example from mods that aren't installed) disable the entry instead of
     * failing to load the guide. */
    @Override
    public OptionallyDisabled<PageEntry<IStatement>> deserialize(ResourceLocation name, JsonObject json) {
        String statementName = GsonHelper.getAsString(json, "statement");
        IStatement statement = StatementManager.statements.get(statementName);
        if (statement == null) {
            if (!json.has("statement")) {
                throw new JsonSyntaxException("Missing 'statement' in " + json);
            }
            return new OptionallyDisabled<>("Unknown statement '" + statementName + "'");
        }
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, statement));
    }

    @Override
    public List<String> getTooltip(IStatement value) {
        return LegacyText.toLegacy(value.getTooltip());
    }

    @Override
    public String getTitle(IStatement value) {
        List<String> tooltip = getTooltip(value);
        return tooltip.isEmpty() ? value.getUniqueTag() : tooltip.get(0);
    }

    @Override
    @Nullable
    public ISimpleDrawable createDrawable(IStatement value) {
        return (graphics, x, y) -> GuiElementStatementSource.drawGuiSlot(graphics, value, x, y);
    }
}
