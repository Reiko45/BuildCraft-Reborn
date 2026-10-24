/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.node;

import java.util.Set;

import org.jetbrains.annotations.Nullable;

import net.minecraft.ChatFormatting;

/** A piece of text with one colour and set of styles. */
public final class FormatSegment {
    public final String text;
    @Nullable
    public final ChatFormatting colour;
    public final Set<ChatFormatting> misc;

    FormatSegment(String text, @Nullable ChatFormatting colour, Set<ChatFormatting> misc) {
        this.text = text;
        this.colour = colour;
        this.misc = misc;
    }

    @Nullable
    public FormatSegment join(FormatSegment other) {
        if (colour == other.colour && misc.equals(other.misc)) {
            return new FormatSegment(text + other.text, colour, misc);
        }
        return null;
    }

    public String toFormatString() {
        StringBuilder miscString = new StringBuilder();
        for (ChatFormatting format : misc) {
            miscString.append(format);
        }
        return ChatFormatting.RESET + (colour == null ? "" : colour.toString()) + miscString + text;
    }

    @Override
    public String toString() {
        StringBuilder miscStr = new StringBuilder();
        for (ChatFormatting format : misc) {
            miscStr.append(format.getName()).append(' ');
        }
        return (colour == null ? "" : colour.getName()) + miscStr + text;
    }
}
