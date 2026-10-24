/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

/** Converts text components to strings with legacy formatting codes, for code (such as the guide book) that works
 * with formatted strings like 1.12 did. Colours that aren't one of the 16 chat colours are left out. */
public final class LegacyText {
    private LegacyText() {}

    public static String toLegacy(Component component) {
        StringBuilder out = new StringBuilder();
        component.visit((style, text) -> {
            if (!text.isEmpty()) {
                out.append(ChatFormatting.RESET);
                ChatFormatting colour = getColour(style.getColor());
                if (colour != null) {
                    out.append(colour);
                }
                if (style.isBold()) out.append(ChatFormatting.BOLD);
                if (style.isItalic()) out.append(ChatFormatting.ITALIC);
                if (style.isUnderlined()) out.append(ChatFormatting.UNDERLINE);
                if (style.isStrikethrough()) out.append(ChatFormatting.STRIKETHROUGH);
                if (style.isObfuscated()) out.append(ChatFormatting.OBFUSCATED);
                out.append(text);
            }
            return Optional.empty();
        }, Style.EMPTY);
        String result = out.toString();
        // drop a leading reset when nothing comes before it
        return result.startsWith(ChatFormatting.RESET.toString()) ? result.substring(2) : result;
    }

    public static List<String> toLegacy(List<Component> components) {
        List<String> list = new ArrayList<>(components.size());
        for (Component component : components) {
            list.add(toLegacy(component));
        }
        return list;
    }

    @Nullable
    private static ChatFormatting getColour(@Nullable TextColor colour) {
        if (colour == null) {
            return null;
        }
        for (ChatFormatting format : ChatFormatting.values()) {
            if (format.isColor() && format.getColor() != null && format.getColor() == colour.getValue()) {
                return format;
            }
        }
        return null;
    }

    /** @return The stack's tooltip (as the client would show it, without advanced information). Client only. */
    public static List<String> getFormattedTooltip(ItemStack stack) {
        Minecraft mc = Minecraft.getInstance();
        try {
            Item.TooltipContext context = mc.level == null ? Item.TooltipContext.EMPTY
                : Item.TooltipContext.of(mc.level);
            return toLegacy(stack.getTooltipLines(context, mc.player, TooltipFlag.NORMAL));
        } catch (RuntimeException e) {
            // Some items need more than a guide book can give them
            return List.of(toLegacy(stack.getHoverName()));
        }
    }

    /** @return The stack's tooltip without formatting. Client only. */
    public static List<String> getUnformattedTooltip(ItemStack stack) {
        List<String> lines = new ArrayList<>();
        for (String line : getFormattedTooltip(stack)) {
            lines.add(ChatFormatting.stripFormatting(line));
        }
        return lines;
    }
}
