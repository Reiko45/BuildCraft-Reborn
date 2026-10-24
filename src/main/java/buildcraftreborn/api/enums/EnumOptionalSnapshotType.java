/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.enums;

import java.util.Locale;

import net.minecraft.util.StringRepresentable;

/**
 * Version of {@link EnumSnapshotType} with {@link EnumOptionalSnapshotType#NONE} value.
 * Shouldn't be used where it's possible to use {@link EnumSnapshotType}.
 */
public enum EnumOptionalSnapshotType implements StringRepresentable {
    NONE(null),
    TEMPLATE(EnumSnapshotType.TEMPLATE),
    BLUEPRINT(EnumSnapshotType.BLUEPRINT);

    public final EnumSnapshotType type;

    EnumOptionalSnapshotType(EnumSnapshotType type) {
        this.type = type;
    }

    public static EnumOptionalSnapshotType fromNullable(EnumSnapshotType type) {
        if (type == null) {
            return NONE;
        }
        switch (type) {
            case TEMPLATE:
                return TEMPLATE;
            case BLUEPRINT:
                return BLUEPRINT;
            default:
                return NONE;
        }
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
