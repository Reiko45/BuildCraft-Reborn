/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.font;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;

/** Implements a font that delegates to Minecraft's own {@link Font}. (1.12 could also use a smooth system font, behind
 * a system property; that isn't ported.) */
public enum MinecraftFont implements IFontRenderer {
    INSTANCE;

    private static Font getFont() {
        return Minecraft.getInstance().font;
    }

    @Override
    public int getStringWidth(String text) {
        return getFont().width(text);
    }

    @Override
    public int getFontHeight(String text) {
        return getMaxFontHeight();
    }

    @Override
    public int getMaxFontHeight() {
        return getFont().lineHeight;
    }

    @Override
    public int drawString(GuiGraphics graphics, String text, int x, int y, int colour, boolean shadow,
        boolean centered, float scale) {
        boolean scaled = scale != 1;
        if (scaled) {
            graphics.pose().pushPose();
            graphics.pose().scale(scale, scale, 1);
            x = (int) (x / scale);
            y = (int) (y / scale);
        }
        if (centered) {
            x -= getStringWidth(text) / 2;
        }
        int end = graphics.drawString(getFont(), text, x, y, colour, shadow);
        int width = end - x;
        if (scaled) {
            graphics.pose().popPose();
            width = (int) (width * scale);
        }
        return width;
    }

    @Override
    public List<String> wrapString(String text, int maxWidth, boolean shadow, float scale) {
        return wrap(text, (int) (maxWidth / scale));
    }

    /** 1.12's {@code FontRenderer.listFormattedStringToWidth}: splits at the last space that fits (or mid-word if
     * there is none), at line breaks, and carries the formatting on to the next line. */
    private static List<String> wrap(String text, int maxWidth) {
        List<String> lines = new ArrayList<>();
        String remaining = text;
        while (true) {
            int split = sizeStringToWidth(remaining, maxWidth);
            if (remaining.length() <= split) {
                lines.add(remaining);
                return lines;
            }
            String line = remaining.substring(0, split);
            char next = remaining.charAt(split);
            boolean gap = next == ' ' || next == '\n';
            String rest = getFormatFromString(line) + remaining.substring(split + (gap ? 1 : 0));
            lines.add(line);
            remaining = rest;
        }
    }

    private static int sizeStringToWidth(String str, int wrapWidth) {
        Font font = getFont();
        int length = str.length();
        int lastSpace = -1;
        int i = 0;
        boolean bold = false;
        float width = 0;
        for (; i < length; i++) {
            char c = str.charAt(i);
            if (c == '\n') {
                lastSpace = i;
                break;
            }
            if (c == '§' && i < length - 1) {
                i++;
                ChatFormatting format = ChatFormatting.getByCode(str.charAt(i));
                if (format == ChatFormatting.BOLD) {
                    bold = true;
                } else if (format != null && (format == ChatFormatting.RESET || format.isColor())) {
                    bold = false;
                }
                continue;
            }
            if (c == ' ') {
                lastSpace = i;
            }
            width += font.width(String.valueOf(c));
            if (bold) {
                width++;
            }
            if (width > wrapWidth) {
                break;
            }
        }
        return i != length && lastSpace != -1 && lastSpace < i ? lastSpace : Math.max(1, i);
    }

    /** @return The formatting codes that are active at the end of the string. */
    private static String getFormatFromString(String text) {
        StringBuilder result = new StringBuilder();
        int i = -1;
        int length = text.length();
        while ((i = text.indexOf('§', i + 1)) != -1) {
            if (i < length - 1) {
                ChatFormatting format = ChatFormatting.getByCode(text.charAt(i + 1));
                if (format == null) {
                    continue;
                }
                if (format.isColor() || format == ChatFormatting.RESET) {
                    result.setLength(0);
                }
                if (format != ChatFormatting.RESET) {
                    result.append(format);
                }
            }
        }
        return result.toString();
    }
}
