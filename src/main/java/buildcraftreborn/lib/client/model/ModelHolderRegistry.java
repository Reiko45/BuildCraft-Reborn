/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import java.util.ArrayList;
import java.util.List;

import net.neoforged.neoforge.client.event.ModelEvent;

import buildcraftreborn.BuildCraftReborn;

/** Every {@link ModelHolder}. Reloads them all once the models (and with them the block atlas) are baked. */
public final class ModelHolderRegistry {
    public static final boolean DEBUG = Boolean.getBoolean("buildcraftreborn.debug.model");

    static final List<ModelHolder> HOLDERS = new ArrayList<>();

    private ModelHolderRegistry() {}

    /** Listener for {@link ModelEvent.BakingCompleted} (on the mod bus). */
    public static void onModelBake(ModelEvent.BakingCompleted event) {
        for (ModelHolder holder : HOLDERS) {
            holder.onModelBake();
        }
        if (DEBUG) {
            for (ModelHolder holder : HOLDERS) {
                String status = holder.failReason != null ? " (" + holder.failReason + ")" : "";
                BuildCraftReborn.LOGGER.info("[lib.model.holder]  - {}{}", holder.modelLocation, status);
            }
        }
    }
}
