/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import com.google.common.collect.ImmutableList;

/** A way of sorting the contents page: by the given tags, in order. */
public class TypeOrder {
    public final String localeKey;
    public final ImmutableList<ETypeTag> tags;

    public TypeOrder(String localeKey, ETypeTag... tags) {
        this.localeKey = localeKey;
        this.tags = ImmutableList.copyOf(tags);
    }

    @Override
    public int hashCode() {
        return tags.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        return tags.equals(((TypeOrder) obj).tags);
    }
}
