/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.model;

import java.util.List;
import java.util.function.Supplier;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

/** A baked model built the first time it's drawn. BuildCraft JSON models ({@link ModelHolder}) only load once baking
 * has finished, after the model registry can be changed, so item models made from them are put in the registry like
 * this and built later. A new one is made on every resource reload. */
public class ModelLazy implements BakedModel {
    private final Supplier<BakedModel> factory;
    private volatile BakedModel model;

    public ModelLazy(Supplier<BakedModel> factory) {
        this.factory = factory;
    }

    private BakedModel get() {
        BakedModel m = model;
        if (m == null) {
            m = factory.get();
            model = m;
        }
        return m;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return get().getQuads(state, side, rand);
    }

    @Override
    public boolean useAmbientOcclusion() {
        return get().useAmbientOcclusion();
    }

    @Override
    public boolean isGui3d() {
        return get().isGui3d();
    }

    @Override
    public boolean usesBlockLight() {
        return get().usesBlockLight();
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return get().getParticleIcon();
    }

    @Override
    public ItemTransforms getTransforms() {
        return get().getTransforms();
    }

    @Override
    public ItemOverrides getOverrides() {
        return get().getOverrides();
    }
}
