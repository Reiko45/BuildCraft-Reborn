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
import net.minecraft.core.Direction;
import net.minecraft.world.item.DyeColor;

import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.silicon.client.BCSiliconModels;
import buildcraftreborn.silicon.client.model.key.KeyPlugLens;

public enum PlugBakerLens implements IPluggableStaticBaker<KeyPlugLens> {
    INSTANCE;

    private static final Map<KeyPlugLens, List<BakedQuad>> CACHED = new ConcurrentHashMap<>();

    private static final IQuadGetter LENS_CUTOUT = BCSiliconModels::getLensCutoutQuads;
    private static final IQuadGetter LENS_TRANSLUCENT = BCSiliconModels::getLensTranslucentQuads;
    private static final IQuadGetter FILTER_CUTOUT = BCSiliconModels::getFilterCutoutQuads;
    private static final IQuadGetter FILTER_TRANSLUCENT = BCSiliconModels::getFilterTranslucentQuads;

    public static void onModelBake() {
        CACHED.clear();
    }

    private static IQuadGetter getGetter(KeyPlugLens key) {
        return switch (key.layer) {
            case CUTOUT -> key.isFilter ? FILTER_CUTOUT : LENS_CUTOUT;
            case TRANSLUCENT -> key.isFilter ? FILTER_TRANSLUCENT : LENS_TRANSLUCENT;
            default -> throw new IllegalArgumentException("Unknown layer " + key.layer);
        };
    }

    @Override
    public List<BakedQuad> bake(KeyPlugLens key) {
        return CACHED.computeIfAbsent(key, k -> {
            MutableQuad[] quads = getGetter(k).get(k.side, k.colour);
            List<BakedQuad> list = new ArrayList<>(quads.length);
            for (MutableQuad q : quads) {
                q.multShade();
                list.add(q.toBakedBlock());
            }
            return list;
        });
    }

    interface IQuadGetter {
        MutableQuad[] get(Direction side, DyeColor colour);
    }
}
