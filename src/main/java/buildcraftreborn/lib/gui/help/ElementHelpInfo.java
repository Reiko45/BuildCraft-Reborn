/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.gui.help;

import buildcraftreborn.lib.gui.pos.IGuiArea;

/** Describes a "help" overlay for a GUI element: a title, a colour and the translation keys of its text. The help
 * ledger ({@code LedgerHelp}) draws these; {@code GuiElementContainerHelp.addHelpInfo} turns one into text. */
public class ElementHelpInfo {
    public final String title;
    public final int colour;
    public final String[] localeKeys;

    public final boolean isPreTranslated;

    public ElementHelpInfo(String title, int colour, String... localeKeys) {
        this(title, colour, false, localeKeys);
    }

    public ElementHelpInfo(String title, int colour, boolean isPreTranslated, String... localeKeys) {
        this.title = title;
        this.colour = colour;
        this.localeKeys = localeKeys;
        this.isPreTranslated = isPreTranslated;
    }

    public static ElementHelpInfo preTranslated(String title, int colour, String... lines) {
        return new ElementHelpInfo(title, colour, true, lines);
    }

    public HelpPosition target(IGuiArea target) {
        return new HelpPosition(this, target);
    }

    /** A help entry placed over a particular area of the screen. */
    public record HelpPosition(ElementHelpInfo info, IGuiArea target) {}
}
