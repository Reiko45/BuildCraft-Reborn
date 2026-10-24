/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.gate;

import java.util.Locale;

public enum EnumGateLogic {
    AND,
    OR;

    public static final EnumGateLogic[] VALUES = values();

    public final String tag = name().toLowerCase(Locale.ROOT);

    /** @return The value with the given {@link #tag}, or AND if there's none. */
    public static EnumGateLogic byTag(String tag) {
        for (EnumGateLogic value : VALUES) {
            if (value.tag.equals(tag)) {
                return value;
            }
        }
        return AND;
    }

    public static EnumGateLogic getByOrdinal(int ord) {
        if (ord < 0 || ord >= VALUES.length) {
            return EnumGateLogic.AND;
        }
        return VALUES[ord];
    }
}
