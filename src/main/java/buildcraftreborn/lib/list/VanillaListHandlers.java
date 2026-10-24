/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.list;

import buildcraftreborn.api.lists.ListRegistry;

/** Registers the list handlers for vanilla items (1.12's {@code VanillaListHandlers}; the ore dictionary handler is
 * the item tag handler now). */
public final class VanillaListHandlers {
    private static boolean registered = false;

    private VanillaListHandlers() {}

    public static void init() {
        if (registered) {
            return;
        }
        registered = true;
        ListRegistry.registerHandler(new ListMatchHandlerClass());
        ListRegistry.registerHandler(new ListMatchHandlerFood());
        ListRegistry.registerHandler(new ListMatchHandlerFluid());
        ListRegistry.registerHandler(new ListMatchHandlerTools());
        ListRegistry.registerHandler(new ListMatchHandlerArmor());
        ListRegistry.registerHandler(new ListMatchHandlerTags());
    }
}
