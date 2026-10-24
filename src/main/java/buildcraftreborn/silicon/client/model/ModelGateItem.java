/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.model;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.jetbrains.annotations.Nullable;

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

import buildcraftreborn.lib.client.model.ModelItemSimple;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.silicon.client.BCSiliconModels;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.item.ItemPluggableGate;
import buildcraftreborn.silicon.plug.PluggableGate;

/** The gate item: the gate pluggable's model (unlit), for the stack's variant. */
public enum ModelGateItem implements BakedModel {
    INSTANCE;

    private static final Map<GateVariant, BakedModel> CACHED = new ConcurrentHashMap<>();

    public static void onModelBake() {
        CACHED.clear();
    }

    private static BakedModel getModel(GateVariant variant) {
        return CACHED.computeIfAbsent(variant, v -> {
            List<BakedQuad> list = new ArrayList<>();
            for (MutableQuad q : BCSiliconModels.getGateStaticQuads(Direction.WEST, v)) {
                list.add(q.toBakedItem());
            }
            synchronized (BCSiliconModels.class) {
                PluggableGate.setClientModelVariables(Direction.WEST, v);
                for (MutableQuad q : BCSiliconModels.GATE_DYNAMIC.getCutoutQuads()) {
                    list.add(q.toBakedItem());
                }
            }
            return new ModelItemSimple(list, ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER, false);
        });
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

    @Override
    public TextureAtlasSprite getParticleIcon() {
        return SpriteUtil.missingSprite();
    }

    @Override
    public ItemTransforms getTransforms() {
        return ModelItemSimple.TRANSFORM_PLUG_AS_ITEM_BIGGER;
    }

    @Override
    public ItemOverrides getOverrides() {
        return GateOverride.INSTANCE;
    }

    private static final class GateOverride extends ItemOverrides {
        static final GateOverride INSTANCE = new GateOverride();

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
            @Nullable LivingEntity entity, int seed) {
            return getModel(ItemPluggableGate.getVariant(stack));
        }
    }
}
