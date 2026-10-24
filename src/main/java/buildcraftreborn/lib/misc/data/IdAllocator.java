/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc.data;

import java.util.ArrayList;
import java.util.List;

import buildcraftreborn.BuildCraftReborn;

/** Hands out sequential network message IDs, with readable names for debugging. A subclass makes a child allocator so
 * its IDs continue after its parent's. */
public class IdAllocator {
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.id_alloc");

    private final String name;
    private final List<String> idNameMap = new ArrayList<>();
    private boolean hasChildren;
    private int nextId;

    private IdAllocator(IdAllocator parent, String name) {
        this.name = parent == null ? name : (parent.name + "." + name);
        if (parent != null) {
            idNameMap.addAll(parent.idNameMap);
        }
        nextId = parent == null ? 0 : parent.nextId;
    }

    public IdAllocator() {
        this(null, "unknown");
    }

    public IdAllocator(String name) {
        this(null, name);
    }

    public IdAllocator makeChild(String childName) {
        hasChildren = true;
        return new IdAllocator(this, childName);
    }

    public String getNameFor(int id) {
        if (id < 0) return "NEGATIVE ID " + id;
        if (id >= idNameMap.size()) return "UNKNOWN_CHILD " + id;
        return idNameMap.get(id);
    }

    public int allocId(String allocName) {
        if (hasChildren) {
            throw new IllegalStateException("A child of this object has already allocated ID's!"//
                + " You have probably set the calling class up wrong!");
        }
        if (DEBUG) {
            BuildCraftReborn.LOGGER.info("[lib.id_alloc] {} allocated {} as {}", name, allocName, nextId);
        }
        idNameMap.add(allocName);
        return nextId++;
    }
}
