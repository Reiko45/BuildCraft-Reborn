/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.ref;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.lib.client.guide.entry.PageValue;
import buildcraftreborn.lib.misc.LocaleUtil;

/** A group of related things: the pages of its {@link #sources} list its {@link #entries}, and the pages of its
 * entries link back to its sources. */
public class GuideGroupSet {
    public enum GroupDirection {
        SRC_TO_ENTRY("to."),
        ENTRY_TO_SRC("from.");

        public final String localePrefix;

        GroupDirection(String localePrefix) {
            this.localePrefix = "buildcraft.guide.group." + localePrefix;
        }
    }

    public final ResourceLocation group;
    /** Everything that points to this group, and so will include this group in pages. */
    public final List<PageValue<?>> sources = new ArrayList<>();
    /** Everything that is in the group, and will be displayed under this group. */
    public final List<PageValue<?>> entries = new ArrayList<>();

    public GuideGroupSet(ResourceLocation group) {
        this.group = group;
    }

    public String getTitle(GroupDirection dir) {
        return LocaleUtil.localize(dir.localePrefix + group.getNamespace() + "." + group.getPath());
    }

    public List<PageValue<?>> getValues(GroupDirection direction) {
        return direction == GroupDirection.SRC_TO_ENTRY ? entries : sources;
    }

    public GuideGroupSet addSingle(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) {
            entries.add(entry);
        }
        return this;
    }

    public GuideGroupSet addArray(Object... values) {
        for (Object value : values) {
            addSingle(value);
        }
        return this;
    }

    public GuideGroupSet addCollection(Collection<?> values) {
        for (Object value : values) {
            addSingle(value);
        }
        return this;
    }

    public GuideGroupSet addKey(Object value) {
        PageValue<?> entry = GuideGroupManager.toPageValue(value);
        if (entry != null) {
            sources.add(entry);
        }
        return this;
    }

    public GuideGroupSet addKeyArray(Object... values) {
        for (Object value : values) {
            addKey(value);
        }
        return this;
    }

    public GuideGroupSet addKeyCollection(Collection<?> values) {
        for (Object value : values) {
            addKey(value);
        }
        return this;
    }
}
