/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.registry;

import java.util.HashMap;
import java.util.Map;

import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.api.transport.pluggable.IPluggableRegistry;
import buildcraftreborn.api.transport.pluggable.PluggableDefinition;

/** Every pipe pluggable definition (blockers, gates, facades...), by id. */
public enum PluggableRegistry implements IPluggableRegistry {
    INSTANCE;

    private final Map<ResourceLocation, PluggableDefinition> registered = new HashMap<>();

    @Override
    public void register(ResourceLocation id, PluggableDefinition definition) {
        registered.put(id, definition);
    }

    @Override
    public PluggableDefinition getDefinition(ResourceLocation identifier) {
        return registered.get(identifier);
    }
}
