/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.text.DecimalFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.IllegalFormatException;
import java.util.List;
import java.util.function.Function;

import com.google.common.base.Splitter;

import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;

public final class StringUtilBC {
    public static final Splitter newLineSplitter = Splitter.on("\\n");

    private static final DecimalFormat displayDecimalFormat = new DecimalFormat("#####0.00");

    private StringUtilBC() {}

    public static List<String> splitIntoLines(String string) {
        return newLineSplitter.splitToList(string.replaceAll("\\n", "\n"));
    }

    public static String blockPosToString(BlockPos pos) {
        if (pos == null) {
            return "null";
        }
        return pos.getX() + ", " + pos.getY() + ", " + pos.getZ();
    }

    public static String fluidToString(FluidStack fluid) {
        if (fluid == null || fluid.isEmpty()) {
            return "empty";
        }
        return fluid.getAmount() + "mb " + fluid.getFluid().builtInRegistryHolder().getRegisteredName();
    }

    public static String vec3ToDispString(Vec3 vec) {
        if (vec == null) {
            return "null";
        }
        return displayDecimalFormat.format(vec.x) + ", " + displayDecimalFormat.format(vec.y) + ", "
            + displayDecimalFormat.format(vec.z);
    }

    public static String vec3ToDispString(Vec3i vec) {
        if (vec == null) {
            return "null";
        }
        return vec.getX() + ", " + vec.getY() + ", " + vec.getZ();
    }

    /** A direct replacement for {@link String#format(String, Object...)} which returns a descriptive string if the
     * given format is invalid. */
    public static String formatSafe(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (IllegalFormatException error) {
            return "![" + error.getMessage() + "]! for '" + format + "' " + Arrays.toString(args);
        }
    }

    /** A direct replacement for {@link String#format(String, Object...)} which includes the full format argument if
     * {@link String#format(String, Object...)} throws an {@link IllegalFormatException} */
    public static String formatDirect(String format, Object... args) {
        try {
            return String.format(format, args);
        } catch (IllegalFormatException error) {
            throw new IllegalArgumentException("Invalid format: '" + format + "'", error);
        }
    }

    /** Formats a string to be displayed on a white background (for example a book background), replacing any
     * close-to-white colours with darker variants. */
    public static String formatStringForWhite(String string) {
        return formatStringImpl(string, ColourUtil.getTextFormatForWhite);
    }

    /** Formats a string to be displayed on a black background (for example an item tooltip), replacing any
     * close-to-black colours with lighter variants. */
    public static String formatStringForBlack(String string) {
        return formatStringImpl(string, ColourUtil.getTextFormatForBlack);
    }

    private static String formatStringImpl(String string, Function<ChatFormatting, ChatFormatting> fn) {
        StringBuilder out = new StringBuilder(string.length());
        for (int i = 0; i < string.length(); i++) {
            char c = string.charAt(i);
            if (c == '\u00a7' && string.length() > i + 1) {
                i++;
                char after = string.charAt(i);
                ChatFormatting format = ChatFormatting.getByCode(after);
                if (format == null || !format.isColor()) {
                    out.append(c).append(after);
                } else {
                    out.append(fn.apply(format));
                }
            } else {
                out.append(c);
            }
        }
        return out.toString();
    }

    public static <T> Comparator<T> compareBasicReadable(Function<T, String> keyExtractor) {
        return Comparator.comparing(keyExtractor, compareBasicReadable());
    }

    /** @return A comparator that only compares the text that we can see - so this will remove any format codes, and
     *         ignore case when comparing. */
    public static Comparator<String> compareBasicReadable() {
        return (o1, o2) -> ColourUtil.stripAllFormatCodes(o1).compareToIgnoreCase(ColourUtil.stripAllFormatCodes(o2));
    }
}
