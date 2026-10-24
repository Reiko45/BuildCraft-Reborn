/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.model;

import java.util.List;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.client.ChunkRenderTypeSet;
import net.neoforged.neoforge.client.model.IDynamicBakedModel;
import net.neoforged.neoforge.client.model.data.ModelData;

import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.transport.client.model.key.PipeAllCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeAllTranslucentKey;
import buildcraftreborn.transport.client.model.key.PipeBaseCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeModelKey;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** The block model of every pipe. It draws whatever the tile's model data describes: the pipe in the cutout layer, and
 * translucent paint and pluggables in the translucent layer. Without a pipe it draws a bare pipe centre. */
public enum ModelPipe implements IDynamicBakedModel {
    INSTANCE;

    private static final ChunkRenderTypeSet RENDER_TYPES =
        ChunkRenderTypeSet.of(RenderType.cutout(), RenderType.translucent());

    @Override
    public @NotNull List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side,
        @NotNull RandomSource rand, @NotNull ModelData data, @Nullable RenderType renderType) {
        if (side != null) {
            return List.of();
        }
        if (renderType == RenderType.translucent()) {
            PipeAllTranslucentKey key = data.get(TilePipeHolder.PROP_TRANSLUCENT);
            if (key == null || key.isEmpty()) {
                return List.of();
            }
            return PipeModelCaches.ALL_TRANSLUCENT.bake(key);
        }
        // Cutout, and also null (the block breaking overlay)
        PipeAllCutoutKey key = data.get(TilePipeHolder.PROP_CUTOUT);
        if (key == null) {
            return PipeModelCaches.BASE_CUTOUT.bake(new PipeBaseCutoutKey(PipeModelKey.DEFAULT_KEY));
        }
        return PipeModelCaches.ALL_CUTOUT.bake(key);
    }

    @Override
    public @NotNull ChunkRenderTypeSet getRenderTypes(@NotNull BlockState state, @NotNull RandomSource rand,
        @NotNull ModelData data) {
        return RENDER_TYPES;
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return false;
    }

    @Override
    public boolean usesBlockLight() {
        return false;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    /** Breaking and running particles use the pipe's first texture. */
    @Override
    public @NotNull TextureAtlasSprite getParticleIcon(@NotNull ModelData data) {
        PipeAllCutoutKey key = data.get(TilePipeHolder.PROP_CUTOUT);
        if (key != null && key.getBaseCutout().definition != null) {
            return PipeBaseModelGenStandard.getSprites(key.getBaseCutout().definition)[0];
        }
        return getParticleIcon();
    }

    @Override
    public @NotNull TextureAtlasSprite getParticleIcon() {
        return SpriteUtil.missingSprite();
    }

    @Override
    public @NotNull ItemTransforms getTransforms() {
        return ItemTransforms.NO_TRANSFORMS;
    }

    @Override
    public @NotNull ItemOverrides getOverrides() {
        return ItemOverrides.EMPTY;
    }
}
