/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.sprite.SpriteHolderRegistry;
import buildcraftreborn.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;

/** Block atlas sprites drawn in the world by the transport renderers (pipe paint, power flow). Client only; the GUI
 * icons are in {@link buildcraftreborn.transport.BCTransportSprites}. */
public final class BCTransportClientSprites {
    public static final SpriteHolder PIPE_COLOUR = pipe("overlay_stained");
    public static final SpriteHolder COLOUR_ITEM_BOX = pipe("colour_item_box");
    public static final SpriteHolder PIPE_COLOUR_BORDER_OUTER = pipe("colour_border_outer");
    public static final SpriteHolder PIPE_COLOUR_BORDER_INNER = pipe("colour_border_inner");
    public static final SpriteHolder POWER_FLOW = pipe("power_flow");
    public static final SpriteHolder POWER_FLOW_OVERLOAD = pipe("power_flow_overload");
    public static final SpriteHolder POWER_FLOW_RF = pipe("rf_flow");

    private BCTransportClientSprites() {}

    private static SpriteHolder pipe(String name) {
        return SpriteHolderRegistry.getHolder(BuildCraftReborn.id("block/pipes/" + name));
    }
}
