/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.lists;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.minecraft.world.item.Item;

/** The {@link ListMatchHandler}s that list items use. */
public final class ListRegistry {
    /** Item classes whose items all count as one "type". */
    public static final List<Class<? extends Item>> itemClassAsType = new ArrayList<>();
    private static final List<ListMatchHandler> handlers = new ArrayList<>();

    private ListRegistry() {}

    public static void registerHandler(ListMatchHandler h) {
        if (h != null) {
            handlers.add(h);
        }
    }

    public static List<ListMatchHandler> getHandlers() {
        return Collections.unmodifiableList(handlers);
    }
}
