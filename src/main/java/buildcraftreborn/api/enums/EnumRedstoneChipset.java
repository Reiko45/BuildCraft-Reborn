/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.enums;

import java.util.Locale;
import java.util.function.Function;

import org.jetbrains.annotations.Nullable;

import net.minecraft.util.StringRepresentable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** The five redstone chipsets. 1.21: each chipset is its own item (1.12 used one item with metadata), found through
 * {@link #itemGetter}, which the silicon module sets. */
public enum EnumRedstoneChipset implements StringRepresentable {
    RED,
    IRON,
    GOLD,
    QUARTZ,
    DIAMOND;

    public static final EnumRedstoneChipset[] VALUES = values();

    /** Set by BuildCraft Silicon. Returns null if the chipsets aren't registered. */
    public static Function<EnumRedstoneChipset, @Nullable Item> itemGetter = chipset -> null;

    private final String name = name().toLowerCase(Locale.ROOT);

    public ItemStack getStack(int stackSize) {
        Item chipset = itemGetter.apply(this);
        if (chipset == null) {
            return ItemStack.EMPTY;
        }
        return new ItemStack(chipset, stackSize);
    }

    public ItemStack getStack() {
        return getStack(1);
    }

    public static EnumRedstoneChipset fromStack(ItemStack stack) {
        if (stack == null || stack.isEmpty()) {
            return RED;
        }
        for (EnumRedstoneChipset chipset : VALUES) {
            if (stack.getItem() == itemGetter.apply(chipset)) {
                return chipset;
            }
        }
        return RED;
    }

    public static EnumRedstoneChipset fromOrdinal(int ordinal) {
        if (ordinal < 0 || ordinal >= VALUES.length) {
            return RED;
        }
        return VALUES[ordinal];
    }

    @Override
    public String getSerializedName() {
        return name;
    }
}
