/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import com.google.gson.JsonObject;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

import buildcraftreborn.lib.client.guide.data.JsonTypeTags;

/** A guide entry: a value that has a page, in a book, sorted under some tags. */
public final class PageEntry<T> extends PageValue<T> {
    public final JsonTypeTags typeTags;
    public final ResourceLocation book;

    public PageEntry(PageValueType<T> type, JsonTypeTags typeTags, ResourceLocation book, T value) {
        super(type, value);
        this.typeTags = typeTags;
        this.book = book;
    }

    public PageEntry(PageValueType<T> type, ResourceLocation name, JsonObject json, T value) {
        super(type, value);
        this.book = ResourceLocation.parse(GsonHelper.getAsString(json, "book"));
        String tagType = GsonHelper.getAsString(json, "tag_type");
        String subType = GsonHelper.getAsString(json, "tag_subtype");
        this.typeTags = new JsonTypeTags(name.getNamespace(), tagType, subType);
    }

    @Override
    public String toString() {
        return value.getClass().getSimpleName() + ": " + value;
    }
}
