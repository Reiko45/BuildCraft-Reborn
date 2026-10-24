/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import com.google.gson.JsonObject;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;

/** A guide book: a title, and whether its contents also list everything without a page of this book. */
public final class GuideBook {
    public final ResourceLocation name;
    public final Component title;
    public final boolean appendAllEntries;
    public final GuideContentsData data = new GuideContentsData(this);

    public GuideBook(ResourceLocation name, Component title, boolean appendAllEntries) {
        this.name = name;
        this.title = title;
        this.appendAllEntries = appendAllEntries;
    }

    static GuideBook deserialize(ResourceLocation name, JsonObject json) {
        Component title = Component.translatable(GsonHelper.getAsString(json, "title", ""));
        boolean addAll = GsonHelper.getAsBoolean(json, "all_entries", true);
        return new GuideBook(name, title, addAll);
    }

    @Override
    public String toString() {
        return "GuideBook [ " + name + ", title = " + title + " ]";
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        return name.equals(((GuideBook) obj).name);
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
