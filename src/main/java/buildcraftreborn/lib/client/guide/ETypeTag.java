/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide;

/** The parts of a guide entry's tags that the contents page can be sorted by. */
public enum ETypeTag {
    MOD("mod."),
    SUB_MOD("submod."),
    TYPE("type."),
    SUB_TYPE("subtype.");

    public final String preText;

    ETypeTag(String preText) {
        this.preText = "buildcraft.guide.chapter." + preText;
    }
}
