/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.enums;

import java.util.Locale;

import javax.annotation.Nullable;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

/** The two kinds of spring. In 1.12 these were the {@code type} property of the single {@code buildcraftcore:spring}
 * block; in 1.21 each one is its own block ({@code water_spring} and {@code oil_spring}). */
public enum EnumSpring implements StringRepresentable {
    WATER(5, -1, Blocks.WATER.defaultBlockState()),
    OIL(6000, 8, null); // Set by the energy module

    public static final EnumSpring[] VALUES = values();

    /** How many ticks there are between attempts to make a fluid block above the spring. */
    public final int tickRate;
    /** One in this many attempts makes a fluid block, or -1 for every attempt. */
    public final int chance;
    /** The fluid block a spring makes above itself, or null if it doesn't make one. */
    @Nullable
    public BlockState liquidBlock;
    public boolean canGen = true;
    private final String lowerCaseName = name().toLowerCase(Locale.ROOT);

    EnumSpring(int tickRate, int chance, @Nullable BlockState liquidBlock) {
        this.tickRate = tickRate;
        this.chance = chance;
        this.liquidBlock = liquidBlock;
    }

    @Override
    public String getSerializedName() {
        return lowerCaseName;
    }
}
