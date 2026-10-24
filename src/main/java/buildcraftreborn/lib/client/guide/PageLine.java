/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.lib.gui.ISimpleDrawable;

/** Stores information about a single line of text. This may be displayed as more than a single line though. */
public class PageLine implements Comparable<PageLine> {
    /** Can be any of the boxes, any icon with dimensions different to these will render incorrectly. */
    @Nullable
    public ISimpleDrawable startIcon;
    @Nullable
    public ISimpleDrawable startIconHovered;
    public final int indent;
    /** This will be wrapped automatically when it is rendered. (With legacy formatting codes.) */
    public final String text;
    public final boolean link;
    @Nullable
    public final Supplier<List<String>> tooltipSupplier;

    public PageLine(int indent, String text, boolean isLink) {
        this(null, null, indent, text, isLink);
    }

    public PageLine(@Nullable ISimpleDrawable startIcon, @Nullable ISimpleDrawable startIconHovered, int indent,
        String text, boolean isLink) {
        this(startIcon, startIconHovered, indent, text, isLink, null);
    }

    public PageLine(@Nullable ISimpleDrawable startIcon, @Nullable ISimpleDrawable startIconHovered, int indent,
        String text, boolean link, @Nullable Supplier<List<String>> tooltipSupplier) {
        this.startIcon = startIcon;
        this.startIconHovered = startIconHovered;
        this.indent = indent;
        this.text = Objects.requireNonNull(text, "text");
        this.link = link;
        this.tooltipSupplier = tooltipSupplier;
    }

    @Override
    public String toString() {
        return "PageLine [indent = " + indent + ", text=" + text + "]";
    }

    @Override
    public int compareTo(PageLine o) {
        return text.toLowerCase(Locale.ROOT).compareTo(o.text.toLowerCase(Locale.ROOT));
    }

    @Override
    public int hashCode() {
        return Objects.hash(indent, link, startIcon, text);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;
        PageLine other = (PageLine) obj;
        return indent == other.indent && link == other.link && Objects.equals(startIcon, other.startIcon)
            && text.equals(other.text);
    }

    @Nullable
    public List<String> getTooltip() {
        return tooltipSupplier == null ? null : tooltipSupplier.get();
    }
}
