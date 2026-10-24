/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.client.renderer.block.model.BakedQuad;

import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.api.transport.pluggable.PluggableModelKey;
import buildcraftreborn.lib.client.model.IModelCache;
import buildcraftreborn.lib.client.model.ModelCache;
import buildcraftreborn.lib.client.model.ModelCacheJoiner;
import buildcraftreborn.lib.client.model.ModelCacheJoiner.ModelKeyWrapper;
import buildcraftreborn.lib.client.model.ModelCacheMultipleSame;
import buildcraftreborn.transport.client.PipeRegistryClient;
import buildcraftreborn.transport.client.model.key.PipeAllCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeAllTranslucentKey;
import buildcraftreborn.transport.client.model.key.PipeBaseCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeBaseTranslucentKey;
import buildcraftreborn.transport.client.model.key.PluggableKey;

/** The pipe model caches: the pipe itself, each pluggable, and both joined (1.12's {@code PipeModelCacheBase},
 * {@code PipeModelCachePluggable} and {@code PipeModelCacheAll}). */
public final class PipeModelCaches {
    public static final IModelCache<PipeBaseCutoutKey> BASE_CUTOUT =
        new ModelCache<>(PipeBaseModelGenStandard::generateCutout);
    public static final IModelCache<PipeBaseTranslucentKey> BASE_TRANSLUCENT =
        new ModelCache<>(PipeBaseModelGenStandard::generateTranslucent);

    public static final ModelCache<PluggableModelKey> PLUG_CUTOUT_SINGLE =
        new ModelCache<>(PipeModelCaches::generatePlug);
    public static final ModelCache<PluggableModelKey> PLUG_TRANSLUCENT_SINGLE =
        new ModelCache<>(PipeModelCaches::generatePlug);
    public static final IModelCache<PluggableKey> PLUG_CUTOUT_ALL =
        new ModelCacheMultipleSame<>(PluggableKey::getKeys, PLUG_CUTOUT_SINGLE);
    public static final IModelCache<PluggableKey> PLUG_TRANSLUCENT_ALL =
        new ModelCacheMultipleSame<>(PluggableKey::getKeys, PLUG_TRANSLUCENT_SINGLE);

    public static final IModelCache<PipeAllCutoutKey> ALL_CUTOUT;
    public static final IModelCache<PipeAllTranslucentKey> ALL_TRANSLUCENT;

    static {
        List<ModelKeyWrapper<PipeAllCutoutKey, ?>> cutout = new ArrayList<>();
        cutout.add(new ModelKeyWrapper<>(PipeAllCutoutKey::getBaseCutout, BASE_CUTOUT));
        cutout.add(new ModelKeyWrapper<>(PipeAllCutoutKey::getPluggable, PLUG_CUTOUT_ALL));
        ALL_CUTOUT = new ModelCacheJoiner<>(cutout);

        List<ModelKeyWrapper<PipeAllTranslucentKey, ?>> translucent = new ArrayList<>();
        translucent.add(new ModelKeyWrapper<>(PipeAllTranslucentKey::getBaseTranslucent, BASE_TRANSLUCENT));
        translucent.add(new ModelKeyWrapper<>(PipeAllTranslucentKey::getPluggable, PLUG_TRANSLUCENT_ALL));
        ALL_TRANSLUCENT = new ModelCacheJoiner<>(translucent);
    }

    private PipeModelCaches() {}

    private static <K extends PluggableModelKey> List<BakedQuad> generatePlug(K key) {
        if (key == null) {
            return List.of();
        }
        IPluggableStaticBaker<K> baker = PipeRegistryClient.getPlugBaker(key);
        if (baker == null) {
            return List.of();
        }
        return baker.bake(key);
    }

    /** Forgets every cached model (after a resource reload). */
    public static void clearAll() {
        PipeBaseModelGenStandard.clearSprites();
        BASE_CUTOUT.clear();
        BASE_TRANSLUCENT.clear();
        PLUG_CUTOUT_ALL.clear();
        PLUG_TRANSLUCENT_ALL.clear();
        ALL_CUTOUT.clear();
        ALL_TRANSLUCENT.clear();
    }
}
