/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.render;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import buildcraftreborn.core.marker.volume.Addon;

/** The client side of each addon type: how it is drawn. 1.21: 1.12's {@code Addon.getRenderer()}. */
public final class AddonRenderers {
    private static final Map<Class<? extends Addon>, IFastAddonRenderer<?>> RENDERERS = new HashMap<>();

    private AddonRenderers() {}

    public static <A extends Addon> void register(Class<A> clazz, IFastAddonRenderer<A> renderer) {
        RENDERERS.put(clazz, renderer);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    public static <A extends Addon> IFastAddonRenderer<A> get(A addon) {
        return (IFastAddonRenderer<A>) RENDERERS.get(addon.getClass());
    }
}
