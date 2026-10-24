/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.lib.client.sprite.SpriteGui;

/** GUI icons. 1.12 stitched these into the block atlas; here they live in the GUI sprite atlas, under
 * {@code assets/buildcraftreborn/textures/gui/sprites/icons/}. Client only. */
public final class BCLibSprites {
    public static final ISprite LOCK = get("icons/lock");
    public static final ISprite WARNING_MINOR = get("icons/warning_minor");
    public static final ISprite WARNING_MAJOR = get("icons/warning_major");
    public static final ISprite LOADING = get("icons/loading");

    public static final ISprite LEDGER_LEFT = get("icons/ledger_left");
    public static final ISprite LEDGER_RIGHT = get("icons/ledger_right");
    public static final ISprite HELP = get("icons/help");
    public static final ISprite HELP_SPLIT = get("icons/help_split");

    public static final ISprite ENGINE_INACTIVE = get("icons/engine_inactive");
    public static final ISprite ENGINE_ACTIVE = get("icons/engine_active");
    public static final ISprite ENGINE_WARM = get("icons/engine_warm");
    public static final ISprite ENGINE_OVERHEAT = get("icons/engine_overheat");

    private BCLibSprites() {}

    private static ISprite get(String path) {
        return new SpriteGui(BuildCraftReborn.id(path));
    }
}
