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

/** The decoration blocks. In 1.12 these were the {@code decoration_type} of one block; in 1.21 each is its own block,
 * {@code decorated_<name>}. */
public enum EnumDecoratedBlock implements StringRepresentable {
    DESTROY(0),
    BLUEPRINT(10),
    TEMPLATE(10),
    PAPER(10),
    LEATHER(10),
    LASER_BACK(0);

    public static final EnumDecoratedBlock[] VALUES = values();

    public final int lightValue;

    EnumDecoratedBlock(int lightValue) {
        this.lightValue = lightValue;
    }

    @Override
    public String getSerializedName() {
        return name().toLowerCase(Locale.ROOT);
    }
}
