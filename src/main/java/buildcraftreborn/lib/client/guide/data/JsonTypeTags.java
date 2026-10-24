/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.data;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.guide.ETypeTag;
import buildcraftreborn.lib.client.guide.TypeOrder;

/** Where a guide entry goes in the contents: its domain (mod), type ("item", "block"...) and subtype ("tool",
 * "engine"...). An entry with only a type (no domain or subtype) is one of the built-in "others" groups. */
public class JsonTypeTags {
    public static final JsonTypeTags EMPTY = new JsonTypeTags("", "", "");

    @Nullable
    public final String domain;
    public final String type;
    @Nullable
    public final String subType;

    public JsonTypeTags(@Nullable String domain, String type, @Nullable String subType) {
        this.domain = domain;
        this.type = type;
        this.subType = subType;
    }

    public JsonTypeTags(String type) {
        this(null, type, null);
    }

    public String[] getOrdered(TypeOrder typeOrder) {
        if (domain == null && subType == null) {
            // Built-in type for "others"
            return new String[] { type };
        }
        String[] strings = new String[typeOrder.tags.size()];
        for (int i = 0; i < strings.length; i++) {
            strings[i] = getTyped(typeOrder.tags.get(i));
        }
        return strings;
    }

    private String getTyped(ETypeTag tag) {
        String typed = switch (tag) {
            case MOD -> getMod(domain, 0);
            case SUB_MOD -> getMod(domain, 1);
            case TYPE -> type;
            case SUB_TYPE -> subType;
        };
        return tag.preText + typed;
    }

    /** 1.21: BuildCraft is a single mod ({@code buildcraftreborn}), so it has no sub-mods. */
    private static String getMod(@Nullable String domain, int index) {
        if (domain == null) {
            return "unknown";
        }
        if (domain.equals(BuildCraftReborn.MODID)) {
            return index == 0 ? "buildcraft" : "";
        }
        return index == 0 ? domain : "";
    }
}
