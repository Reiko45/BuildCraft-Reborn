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
import net.minecraft.core.Direction.Axis;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import org.joml.Vector3f;

import buildcraftreborn.api.transport.pipe.EnumPipeColourType;
import buildcraftreborn.api.transport.pipe.IItemPipe;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.transport.pipe.PipeFaceTex;
import buildcraftreborn.lib.client.model.ModelItemSimple;
import buildcraftreborn.lib.client.model.ModelUtil;
import buildcraftreborn.lib.client.model.ModelUtil.UvFaceData;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.transport.client.BCTransportClientSprites;

/** The item model of every pipe item: a short pipe standing up, with the definition's item textures (and the paint,
 * from the {@code colour} component). The model for each pipe and colour is built once and cached until the next
 * resource reload. */
public enum ModelPipeItem implements BakedModel {
    INSTANCE;

    private static final int INDEX_TOP = 0;
    private static final int INDEX_CENTER = 1;
    private static final int INDEX_BOTTOM = 2;

    private static final MutableQuad[] QUADS_SAME;
    private static final MutableQuad[][] QUADS_DIFFERENT;
    private static final MutableQuad[] QUADS_COLOUR;

    private static final Map<Item, BakedModel[]> CACHE = new ConcurrentHashMap<>();

    static {
        // Same sprite for all 3 sections
        QUADS_SAME = new MutableQuad[6];
        {
            Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f radius = new Vector3f(0.25f, 0.5f, 0.25f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (Direction face : Direction.values()) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_SAME[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }

        // Different sprite for any of the 3 sections
        QUADS_DIFFERENT = new MutableQuad[3][];
        QUADS_DIFFERENT[INDEX_TOP] = section(0.875f, 0.125f, Direction.DOWN, UvFaceData.from16(4, 0, 12, 4));
        QUADS_DIFFERENT[INDEX_CENTER] = section(0.5f, 0.25f, null, UvFaceData.from16(4, 4, 12, 12));
        QUADS_DIFFERENT[INDEX_BOTTOM] = section(0.125f, 0.125f, Direction.UP, UvFaceData.from16(4, 12, 12, 16));

        // Translucent coloured pipes
        QUADS_COLOUR = new MutableQuad[6];
        {
            Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
            Vector3f radius = new Vector3f(0.24f, 0.49f, 0.24f);
            UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
            UvFaceData uvsXZ = UvFaceData.from16(4, 0, 12, 16);
            for (Direction face : Direction.values()) {
                UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
                QUADS_COLOUR[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
            }
        }
    }

    /** @param skip The face not to draw, or null to draw neither vertical face (the centre section). */
    private static MutableQuad[] section(float centreY, float radiusY, @Nullable Direction skip, UvFaceData uvsXZ) {
        MutableQuad[] cube = new MutableQuad[6];
        Vector3f center = new Vector3f(0.5f, centreY, 0.5f);
        Vector3f radius = new Vector3f(0.25f, radiusY, 0.25f);
        UvFaceData uvsY = UvFaceData.from16(4, 4, 12, 12);
        for (Direction face : Direction.values()) {
            if (skip == null ? face.getAxis() == Axis.Y : face == skip) {
                continue;
            }
            UvFaceData uvs = face.getAxis() == Axis.Y ? uvsY : uvsXZ;
            cube[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return cube;
    }

    /** Forgets every built model (after a resource reload). */
    public static void clearCache() {
        CACHE.clear();
    }

    private static BakedModel getModel(ItemStack stack) {
        Item item = stack.getItem();
        DyeColor colour = stack.get(BCItemComponents.COLOUR.get());
        int index = colour == null ? 0 : colour.getId() + 1;
        BakedModel[] models = CACHE.computeIfAbsent(item, i -> new BakedModel[17]);
        BakedModel model = models[index];
        if (model == null) {
            model = new ModelItemSimple(createQuads(item, colour), ModelItemSimple.TRANSFORM_BLOCK, true);
            models[index] = model;
        }
        return model;
    }

    private static List<BakedQuad> createQuads(Item item, @Nullable DyeColor colour) {
        PipeFaceTex center = PipeFaceTex.NO_SPRITE;
        PipeFaceTex top = center;
        PipeFaceTex bottom = center;
        TextureAtlasSprite[] sprites = { SpriteUtil.missingSprite() };
        EnumPipeColourType type = EnumPipeColourType.TRANSLUCENT;
        if (item instanceof IItemPipe pipe) {
            PipeDefinition def = pipe.getDefinition();
            top = def.itemModelTop;
            center = def.itemModelCenter;
            bottom = def.itemModelBottom;
            type = def.getColourType();
            sprites = PipeBaseModelGenStandard.getSprites(def);
        }

        List<BakedQuad> quads = new ArrayList<>();
        if (center == top && center == bottom) {
            addQuads(QUADS_SAME, sprites, quads, center);
        } else {
            addQuads(QUADS_DIFFERENT[INDEX_BOTTOM], sprites, quads, bottom);
            addQuads(QUADS_DIFFERENT[INDEX_CENTER], sprites, quads, center);
            addQuads(QUADS_DIFFERENT[INDEX_TOP], sprites, quads, top);
        }

        if (colour != null) {
            int rgb = PipeBaseModelGenStandard.getPipeModelColour(colour);
            if (type == EnumPipeColourType.TRANSLUCENT) {
                addQuadsColoured(QUADS_COLOUR, quads, BCTransportClientSprites.PIPE_COLOUR.getSprite(), rgb);
            } else if (type == EnumPipeColourType.BORDER_OUTER) {
                addQuadsColoured(QUADS_SAME, quads, BCTransportClientSprites.PIPE_COLOUR_BORDER_OUTER.getSprite(),
                    rgb);
            } else if (type == EnumPipeColourType.BORDER_INNER) {
                addQuadsColoured(QUADS_SAME, quads, BCTransportClientSprites.PIPE_COLOUR_BORDER_INNER.getSprite(),
                    rgb);
            }
        }
        return quads;
    }

    private static void addQuads(MutableQuad[] from, TextureAtlasSprite[] sprites, List<BakedQuad> to,
        PipeFaceTex face) {
        MutableQuad copy = new MutableQuad();
        for (int i = 0; i < face.getCount(); i++) {
            int colour = face.getColour(i);
            TextureAtlasSprite sprite = PipeBaseModelGenStandard.getSprite(sprites, face.getTexture(i));
            for (MutableQuad f : from) {
                if (f == null) {
                    continue;
                }
                copy.copyFrom(f);
                copy.setSprite(sprite);
                copy.texFromSprite(sprite);
                copy.colouri(colour);
                to.add(copy.toBakedItem());
            }
        }
    }

    private static void addQuadsColoured(MutableQuad[] from, List<BakedQuad> to, TextureAtlasSprite sprite,
        int colour) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            copy.texFromSprite(sprite);
            copy.colouri(colour);
            to.add(copy.toBakedItem());
        }
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
        return ModelItemSimple.TRANSFORM_BLOCK;
    }

    @Override
    public ItemOverrides getOverrides() {
        return PipeItemOverride.INSTANCE;
    }

    private static class PipeItemOverride extends ItemOverrides {
        static final PipeItemOverride INSTANCE = new PipeItemOverride();

        @Override
        public BakedModel resolve(BakedModel model, ItemStack stack, @Nullable ClientLevel level,
            @Nullable LivingEntity entity, int seed) {
            return getModel(stack);
        }
    }
}
