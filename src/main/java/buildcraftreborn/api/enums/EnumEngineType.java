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

import buildcraftreborn.api.core.IEngineType;

/** The BuildCraft engines. 1.12 stored these as metadata on one block; 1.21 registers one block per type, named
 * {@code engine_<type>} (for example {@code buildcraftreborn:engine_stone}). */
public enum EnumEngineType implements StringRepresentable, IEngineType {
    /** Redstone engine (1.12 "wood"). */
    WOOD,
    /** Stirling engine. */
    STONE,
    /** Combustion engine. */
    IRON,
    CREATIVE,
    /** RF (Forge Energy) to MJ engine, disabled by default. */
    RF;

    public static final EnumEngineType[] VALUES = values();

    public final String unlocalizedTag = name().toLowerCase(Locale.ROOT);

    @Override
    public String getEngineName() {
        return unlocalizedTag;
    }

    @Override
    public String getSerializedName() {
        return unlocalizedTag;
    }

    public static EnumEngineType fromMeta(int meta) {
        if (meta < 0 || meta >= VALUES.length) {
            meta = 0;
        }
        return VALUES[meta];
    }
}
