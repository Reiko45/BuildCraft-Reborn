/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.node;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.collect.ImmutableSet;

import net.minecraft.ChatFormatting;

import buildcraftreborn.lib.client.guide.font.IFontRenderer;

/** Text with formatting codes, split into {@link FormatSegment}s so that it can be wrapped onto several lines. */
public class FormatString {
    private static final String WORD_GAP = " \n\t";
    /** Brackets and quotes that stay with what follows them. */
    private static final String OPENING_PUNCTUATION = "「『（【〈《〔［｛“‘";

    /** Chinese, Japanese and Korean text has no spaces between words, and a line can break after any of its
     * characters (1.12 only broke a formatted line's later parts at spaces, so these moved whole to the next line). */
    static boolean canBreakAfter(char c) {
        if (WORD_GAP.indexOf(c) >= 0) {
            return true;
        }
        if (OPENING_PUNCTUATION.indexOf(c) >= 0) {
            return false;
        }
        Character.UnicodeScript script = Character.UnicodeScript.of(c);
        if (script == Character.UnicodeScript.HAN || script == Character.UnicodeScript.HIRAGANA
            || script == Character.UnicodeScript.KATAKANA) {
            return true;
        }
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);
        return block == Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION
            || block == Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS;
    }
    private static final Pattern FORMATTING_CODE_PATTERN = Pattern.compile("(?i)§[0-9A-FK-OR]");

    public final FormatSegment[] segments;
    private final String unformatted, formatted;

    public FormatString(FormatSegment[] segments) {
        this.segments = segments;
        StringBuilder s = new StringBuilder();
        StringBuilder sf = new StringBuilder();
        for (FormatSegment seg : segments) {
            s.append(seg.text);
            sf.append(seg.toFormatString());
        }
        unformatted = s.toString();
        formatted = sf.toString();
    }

    public String getFormatted() {
        return formatted;
    }

    public String getUnformatted() {
        return unformatted;
    }

    public static FormatString split(String formattedText) {
        List<FormatSegment> segments = new ArrayList<>();
        ChatFormatting lastColour = null;
        Set<ChatFormatting> lastMisc = EnumSet.noneOf(ChatFormatting.class);
        int lastEnd = 0;
        Matcher matcher = FORMATTING_CODE_PATTERN.matcher(formattedText);
        while (matcher.find()) {
            int start = matcher.start();
            if (start != lastEnd) {
                segments.add(new FormatSegment(formattedText.substring(lastEnd, start), lastColour,
                    ImmutableSet.copyOf(lastMisc)));
            }
            ChatFormatting format = ChatFormatting.getByCode(Character.toLowerCase(matcher.group().charAt(1)));
            if (format != null) {
                if (format == ChatFormatting.RESET) {
                    lastColour = null;
                    lastMisc.clear();
                } else if (format.isColor()) {
                    lastColour = format;
                } else {
                    lastMisc.add(format);
                }
            }
            lastEnd = matcher.end();
        }
        if (lastEnd != formattedText.length()) {
            segments.add(new FormatSegment(formattedText.substring(lastEnd), lastColour,
                ImmutableSet.copyOf(lastMisc)));
        }
        return new FormatString(segments.toArray(new FormatSegment[0]));
    }

    /** @return An array of length 1 or 2, with this wrapped down onto the next line. Note that this only wraps a single
     *         line. */
    public FormatString[] wrap(IFontRenderer font, int maxWidth) {
        return wrap(font, maxWidth, true);
    }

    /** @return An array of length 1 or 2, with this wrapped down onto the next line. Note that this only wraps a single
     *         line. */
    public FormatString[] wrap(IFontRenderer font, int maxWidth, boolean onWords) {
        List<FormatSegment> thisLine = new ArrayList<>();
        int widthUsed = 0;
        for (int segmentIndex = 0; segmentIndex < segments.length; segmentIndex++) {
            FormatSegment segment = segments[segmentIndex];
            int width = font.getStringWidth(segment.toFormatString());
            if (width + widthUsed <= maxWidth) {
                thisLine.add(segment);
                widthUsed += width;
                continue;
            }
            String text = segment.toFormatString();
            // the formatting codes at the start of the text (which must not be split)
            int prefix = text.length() - segment.text.length();
            int allowedLength = 1;
            boolean words = onWords;
            outer: while (true) {
                for (int i = text.length(); i > prefix + 1; i--) {
                    if (words && !canBreakAfter(text.charAt(i - 1))) {
                        continue;
                    }
                    int w = font.getStringWidth(text.substring(0, i));
                    if (w + widthUsed <= maxWidth) {
                        allowedLength = i;
                        break outer;
                    }
                }
                if (words && segmentIndex == 0) {
                    words = false;
                } else {
                    break;
                }
            }
            int left = segments.length - segmentIndex;
            FormatSegment[] next = new FormatSegment[left];
            if (allowedLength > prefix + 1) {
                int textSplit = allowedLength - prefix;
                thisLine.add(new FormatSegment(segment.text.substring(0, textSplit), segment.colour, segment.misc));
                next[0] = new FormatSegment(segment.text.substring(textSplit), segment.colour, segment.misc);
                System.arraycopy(segments, segmentIndex + 1, next, 1, left - 1);
            } else if (thisLine.isEmpty()) {
                // Nothing fits at all: put at least one character on this line
                thisLine.add(new FormatSegment(segment.text.substring(0, 1), segment.colour, segment.misc));
                next[0] = new FormatSegment(segment.text.substring(1), segment.colour, segment.misc);
                System.arraycopy(segments, segmentIndex + 1, next, 1, left - 1);
            } else {
                System.arraycopy(segments, segmentIndex, next, 0, left);
            }
            return new FormatString[] { new FormatString(thisLine.toArray(new FormatSegment[0])),
                new FormatString(next) };
        }
        return new FormatString[] { this };
    }
}
