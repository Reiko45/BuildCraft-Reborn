/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.core.BCCoreSprites;

/** Silicon's gate statement icons (GUI atlas sprites, see {@link BCCoreSprites}). */
public final class BCSiliconSprites {
    public static final ISprite TRIGGER_LIGHT_LOW = BCCoreSprites.get("trigger_light_dark");
    public static final ISprite TRIGGER_LIGHT_HIGH = BCCoreSprites.get("trigger_light_bright");

    public static final ISprite TRIGGER_TIMER_SHORT = BCCoreSprites.get("trigger_timer_short");
    public static final ISprite TRIGGER_TIMER_MEDIUM = BCCoreSprites.get("trigger_timer_medium");
    public static final ISprite TRIGGER_TIMER_LONG = BCCoreSprites.get("trigger_timer_long");

    public static final ISprite ACTION_PULSAR_CONSTANT = BCCoreSprites.get("action_pulsar_on");
    public static final ISprite ACTION_PULSAR_SINGLE = BCCoreSprites.get("action_pulsar_single");

    private BCSiliconSprites() {}
}
