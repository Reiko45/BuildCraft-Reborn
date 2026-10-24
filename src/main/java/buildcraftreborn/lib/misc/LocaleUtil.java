/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.text.DecimalFormat;
import java.util.IllegalFormatException;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.Direction;
import net.minecraft.locale.Language;
import net.minecraft.world.item.DyeColor;

import net.minecraft.network.chat.Component;

import net.neoforged.neoforge.common.ModConfigSpec;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.BCLibConfig;

/** Player-facing formatting of fluid amounts and power, ported from 1.12's {@code LocaleUtil}.
 * <p>
 * 1.21: returns translatable {@link Component}s instead of pre-localized strings, so the client's language is applied
 * at display time. Translation keys are the same {@code buildcraft.fluid.*} / {@code buildcraft.mj.*} keys as 1.12,
 * which lets the 1.12 translations be reused unchanged. */
public final class LocaleUtil {
    private static final DecimalFormat FORMAT_FLUID = new DecimalFormat("#,##0.##");
    private static final DecimalFormat FORMAT_RF = new DecimalFormat("#,##0");

    private LocaleUtil() {}

    /** Reads a client display option, falling back to its default when the client config isn't loaded (for example on
     * a dedicated server). */
    private static boolean clientFlag(ModConfigSpec.BooleanValue value) {
        return BCLibConfig.CLIENT_SPEC.isLoaded() ? value.get() : value.getDefault();
    }

    private static String lengthSuffix() {
        return clientFlag(BCLibConfig.USE_LONG_LOCALIZED_NAME) ? "long" : "short";
    }

    /** Localizes a key with the current language (the client's on a client, English on a dedicated server). Works on
     * both sides, unlike the client-only {@code I18n}. */
    public static String localize(String key) {
        return Language.getInstance().getOrDefault(key);
    }

    /** Localizes a key and formats the arguments into it (like 1.12's {@code I18n.format}). */
    public static String localize(String key, Object... args) {
        String format = localize(key);
        try {
            return String.format(format, args);
        } catch (IllegalFormatException e) {
            return "Format error: " + format;
        }
    }

    public static boolean canLocalize(String key) {
        return Language.getInstance().has(key);
    }

    /** @return The localized name of a dye colour (vanilla's {@code color.minecraft.*} keys). */
    public static String localizeColour(DyeColor colour) {
        return localize("color.minecraft." + colour.getName());
    }

    /** @return The localized name of a face, or of the centre for null. */
    public static String localizeFacing(@Nullable Direction face) {
        return localize("direction." + (face == null ? "center" : face.getName()));
    }

    public static Component localizeFluidStaticAmount(int fluidAmount) {
        return localizeFluidStaticAmount(fluidAmount, -1);
    }

    /** Localizes the given fluid amount, out of a given capacity (or -1 for no capacity). */
    public static Component localizeFluidStaticAmount(int fluidAmount, int capacity) {
        boolean buckets = clientFlag(BCLibConfig.USE_BUCKETS_STATIC);
        String unit = buckets ? "bucket." : "milli.";
        String len = lengthSuffix();
        if (fluidAmount <= 0) {
            if (capacity > 0) {
                String cap = buckets ? FORMAT_FLUID.format(capacity / 1000.0) : FORMAT_FLUID.format(capacity);
                return Component.translatable("buildcraft.fluid.empty." + unit + len, cap);
            }
            return Component.translatable("buildcraft.fluid.empty");
        }
        String amount = buckets ? FORMAT_FLUID.format(fluidAmount / 1000.0) : FORMAT_FLUID.format(fluidAmount);
        String cap = buckets ? FORMAT_FLUID.format(capacity / 1000.0) : FORMAT_FLUID.format(capacity);
        if (capacity == fluidAmount) {
            return Component.translatable("buildcraft.fluid.full." + unit + len, amount);
        }
        if (capacity > 0) {
            return Component.translatable("buildcraft.fluid.static.cap." + unit + len, amount, cap);
        }
        return Component.translatable("buildcraft.fluid.static." + unit + len, amount);
    }

    public static Component localizeFluidFlow(int milliBucketsPerTick) {
        boolean buckets = clientFlag(BCLibConfig.USE_BUCKETS_FLOW);
        String amount = buckets ? FORMAT_FLUID.format(milliBucketsPerTick / 50.0)
            : FORMAT_FLUID.format(milliBucketsPerTick);
        return Component.translatable("buildcraft.fluid.flow." + (buckets ? "bucket." : "milli.") + lengthSuffix(),
            amount);
    }

    /** Formats a stored MJ amount (in micro MJ). */
    public static Component localizeMj(long microMj) {
        return Component.translatable("buildcraft.mj.static." + lengthSuffix(), MjAPI.formatMj(microMj));
    }

    /** Formats an MJ rate (micro MJ per tick). */
    public static Component localizeMjFlow(long microMjPerTick) {
        return Component.translatable("buildcraft.mj.flow." + lengthSuffix(), MjAPI.formatMj(microMjPerTick));
    }

    /** Formats a stored Forge Energy amount (1.12's RF). */
    public static Component localizeRf(int rf) {
        return Component.translatable("buildcraft.rf.static." + lengthSuffix(), formatRf(rf));
    }

    /** Formats a Forge Energy rate (1.12's RF per tick). */
    public static Component localizeRfFlow(int rfPerTick) {
        return Component.translatable("buildcraft.rf.flow." + lengthSuffix(), formatRf(rfPerTick));
    }

    public static String formatRf(int rf) {
        return FORMAT_RF.format(rf);
    }

    /** Formats a heat value in degrees celsius, like 1.12 ("25.00 °C"). */
    public static Component localizeHeat(double heat) {
        return Component.literal(String.format("%.2f \u00B0C", heat));
    }
}
