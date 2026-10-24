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
import java.util.concurrent.TimeUnit;

import org.jetbrains.annotations.Nullable;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.api.core.render.EnumRenderLayer;
import buildcraftreborn.lib.client.model.ModelItemSimple;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.silicon.client.model.key.KeyPlugFacade;
import buildcraftreborn.silicon.item.ItemPluggableFacade;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.plug.FacadePhasedState;
import buildcraftreborn.transport.client.BCTransportModels;

/** The facade item: the facade (and the plug behind a solid one), facing west. A phased facade shows each of its
 * states in turn. */
public enum ModelFacadeItem implements BakedModel {
    INSTANCE;

    private static final LoadingCache<KeyPlugFacade, BakedModel> CACHE = CacheBuilder.newBuilder()
        .expireAfterAccess(1, TimeUnit.MINUTES)
        .build(CacheLoader.from(key -> new ModelItemSimple(bakeForKey(key), ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK,
            true)));

    public static void onModelBake() {
        CACHE.invalidateAll();
    }

    private static List<BakedQuad> bakeForKey(KeyPlugFacade key) {
        List<BakedQuad> quads = new ArrayList<>();
        for (MutableQuad quad : PlugBakerFacade.INSTANCE.bakeForKey(key)) {
            quads.add(quad.toBakedItem());
        }
        if (PlugBakerFacade.showsBlocker(key)) {
            for (MutableQuad quad : BCTransportModels.BLOCKER.getCutoutQuads()) {
                quads.add(quad.toBakedItem());
            }
        }
        return quads;
    }

    @Override
    public List<BakedQuad> getQuads(@Nullable BlockState state, @Nullable Direction side, RandomSource rand) {
        return List.of();
    }

    @Override
    public boolean useAmbientOcclusion() {
        return false;
    }

    @Override
    public boolean isGui3d() {
        return true;
    }

    @Override
    public boolean usesBlockLight() {
        return true;
    }

    @Override
    public boolean isCustomRenderer() {
        return false;
    }

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return SpriteUtil.missingSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return FacadeOverride.INSTANCE;
    }

    private static final class FacadeOverride extends ItemOverrides {
        static final FacadeOverride INSTANCE = new FacadeOverride();

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
            @Nullable LivingEntity entity, int seed) {
            FacadeInstance inst = ItemPluggableFacade.getStates(stack);
            FacadePhasedState state = inst.getCurrentStateForStack();
            return CACHE.getUnchecked(new KeyPlugFacade(EnumRenderLayer.TRANSLUCENT, Direction.WEST,
                state.stateInfo.state, inst.isHollow));
        }
    }
}
