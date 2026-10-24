/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.enums;

public enum EnumSnapshotType {
    TEMPLATE(900),
    BLUEPRINT(300);

    public final int maxPerTick;

    EnumSnapshotType(int maxPerTick) {
        this.maxPerTick = maxPerTick;
    }
}
