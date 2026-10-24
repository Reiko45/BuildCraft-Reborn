/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import java.util.Collections;
import java.util.List;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.gui.ISimpleDrawable;
import buildcraftreborn.lib.misc.data.OptionallyDisabled;

/** A page that isn't about anything in the game (such as the guide's own file format). Its value is its title. */
public class PageEntryExternal extends PageValueType<String> {
    public static final PageEntryExternal INSTANCE = new PageEntryExternal();

    @Override
    public OptionallyDisabled<PageEntry<String>> deserialize(ResourceLocation name, JsonObject json) {
        return new OptionallyDisabled<>(new PageEntry<>(this, name, json, PageValue.getTitle(json)));
    }

    @Override
    public Class<String> getEntryClass() {
        return String.class;
    }

    @Override
    public List<String> getTooltip(String value) {
        return Collections.singletonList(value);
    }

    @Override
    public ISimpleDrawable createDrawable(String value) {
        return null;
    }

    @Override
    public String getTitle(String value) {
        return value;
    }

    @Override
    public void iterateAllDefault(IEntryLinkConsumer consumer) {
        // NO-OP: everything is provided as-is.
    }
}
