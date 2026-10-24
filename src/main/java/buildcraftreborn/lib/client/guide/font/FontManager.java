/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.font;

/** Picks the guide's font. 1.21: always Minecraft's (1.12's optional smooth system font isn't ported). */
public enum FontManager {
    INSTANCE;

    public IFontRenderer getOrLoadFont(String name, int size) {
        return MinecraftFont.INSTANCE;
    }
}
