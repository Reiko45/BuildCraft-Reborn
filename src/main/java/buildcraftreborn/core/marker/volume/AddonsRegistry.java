/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.resources.ResourceLocation;

/** The addon types, by name. 1.21: registered with a factory instead of being created by reflection. */
public enum AddonsRegistry {
    INSTANCE;

    private final Map<ResourceLocation, Class<? extends Addon>> addonClasses = new HashMap<>();
    private final Map<ResourceLocation, Supplier<? extends Addon>> factories = new HashMap<>();

    public <A extends Addon> void register(ResourceLocation name, Class<A> clazz, Supplier<A> factory) {
        if (!addonClasses.containsKey(name)) {
            addonClasses.put(name, clazz);
            factories.put(name, factory);
        }
    }

    @Nullable
    public Class<? extends Addon> getClassByName(ResourceLocation name) {
        return addonClasses.get(name);
    }

    @Nullable
    public Addon create(ResourceLocation name) {
        Supplier<? extends Addon> factory = factories.get(name);
        return factory == null ? null : factory.get();
    }

    public ResourceLocation getNameByClass(Class<? extends Addon> clazz) {
        return addonClasses.entrySet().stream().filter(nameClass -> nameClass.getValue().equals(clazz)).findFirst()
            .orElseThrow(() -> new IllegalStateException("Unregistered addon " + clazz)).getKey();
    }
}
