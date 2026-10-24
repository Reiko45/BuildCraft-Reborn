/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.minecraft.client.renderer.block.model.BakedQuad;

import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.silicon.client.BCSiliconModels;
import buildcraftreborn.silicon.client.model.key.KeyPlugGate;

public enum PlugGateBaker implements IPluggableStaticBaker<KeyPlugGate> {
    INSTANCE;

    private static final Map<KeyPlugGate, List<BakedQuad>> CACHED = new ConcurrentHashMap<>();

    public static void onModelBake() {
        CACHED.clear();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugGate key) {
        return CACHED.computeIfAbsent(key, k -> {
            List<BakedQuad> list = new ArrayList<>();
            for (MutableQuad q : BCSiliconModels.getGateStaticQuads(k.side, k.variant)) {
                q.multShade();
                list.add(q.toBakedBlock());
            }
            return list;
        });
    }
}
