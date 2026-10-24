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

import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import buildcraftreborn.api.transport.pipe.EnumPipeColourType;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.transport.pipe.PipeFaceTex;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.model.ModelUtil;
import buildcraftreborn.lib.client.model.ModelUtil.UvFaceData;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.lib.misc.SpriteUtil;
import buildcraftreborn.transport.client.BCTransportClientSprites;
import buildcraftreborn.transport.client.model.key.PipeBaseCutoutKey;
import buildcraftreborn.transport.client.model.key.PipeBaseTranslucentKey;

/** Builds the quads of a pipe: its centre, its connections (stretched out to the block edge or further), and paint.
 * <p>
 * 1.21: the textures are looked up from the block atlas when first needed (every {@code textures/block/} png is
 * stitched automatically). In colour blind mode a texture's {@code _cb} variant is used when there is one, which 1.12
 * did with swappable sprites. This runs on chunk building threads, so it only reads shared state. */
public final class PipeBaseModelGenStandard {
    /** How much darker the inside of a pipe is (1.12's {@code render.pipe.misc.inside.shade}). */
    private static final float INSIDE_COLOUR_MULT = 0.725f;

    private static final Map<PipeDefinition, TextureAtlasSprite[]> SPRITES = new ConcurrentHashMap<>();

    // Models
    private static final MutableQuad[][][] QUADS;
    private static final MutableQuad[][][] QUADS_COLOURED;

    static {
        QUADS = new MutableQuad[2][][];
        QUADS_COLOURED = new MutableQuad[2][][];
        final double colourOffset = 0.01;
        Vec3[] faceOffset = new Vec3[6];
        for (Direction face : Direction.values()) {
            faceOffset[face.ordinal()] = Vec3.atLowerCornerOf(face.getOpposite().getNormal()).scale(colourOffset);
        }

        // not connected
        QUADS[0] = new MutableQuad[6][2];
        QUADS_COLOURED[0] = new MutableQuad[6][2];
        Vector3f center = new Vector3f(0.5f, 0.5f, 0.5f);
        Vector3f radius = new Vector3f(0.25f, 0.25f, 0.25f);
        UvFaceData uvs = new UvFaceData();
        uvs.minU = uvs.minV = 4 / 16f;
        uvs.maxU = uvs.maxV = 12 / 16f;
        for (Direction face : Direction.values()) {
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            QUADS[0][face.ordinal()][0] = quad;
            dupDarker(QUADS[0][face.ordinal()]);

            MutableQuad[] colQuads = ModelUtil.createDoubleFace(face, center, radius, uvs);
            for (MutableQuad q : colQuads) {
                q.translatevd(faceOffset[face.ordinal()]);
            }
            QUADS_COLOURED[0][face.ordinal()] = colQuads;
        }
    }

    private PipeBaseModelGenStandard() {}

    /** Forgets the looked up textures (after a resource reload, or a colour blind mode change). */
    public static void clearSprites() {
        SPRITES.clear();
    }

    /** @return The textures of the given pipe (indexed as {@link PipeDefinition#textures}). */
    public static TextureAtlasSprite[] getSprites(@Nullable PipeDefinition def) {
        if (def == null) {
            return new TextureAtlasSprite[] { SpriteUtil.missingSprite() };
        }
        return SPRITES.computeIfAbsent(def, PipeBaseModelGenStandard::lookupSprites);
    }

    private static TextureAtlasSprite[] lookupSprites(PipeDefinition def) {
        boolean colourBlind = BCLibConfig.colourBlindMode();
        TextureAtlasSprite missing = SpriteUtil.missingSprite();
        TextureAtlasSprite[] array = new TextureAtlasSprite[def.textures.length];
        for (int i = 0; i < array.length; i++) {
            ResourceLocation name = ResourceLocation.parse(def.textures[i]);
            TextureAtlasSprite sprite = null;
            if (colourBlind) {
                TextureAtlasSprite cb = SpriteUtil.getBlockSprite(name.withSuffix("_cb"));
                if (cb != missing) {
                    sprite = cb;
                }
            }
            array[i] = sprite != null ? sprite : SpriteUtil.getBlockSprite(name);
        }
        return array;
    }

    private static MutableQuad[] setupSideQuads(Direction side, float size, boolean isColour) {
        boolean extended = size > 0.25;

        MutableQuad[] quads = new MutableQuad[(extended ? 16 : 8)];

        Vector3f center = new Vector3f(//
            0.5f + side.getStepX() * 0.375f, //
            0.5f + side.getStepY() * 0.375f, //
            0.5f + side.getStepZ() * 0.375f //
        );
        Vector3f radius = new Vector3f(//
            side.getAxis() == Axis.X ? 0.125f : 0.25f, //
            side.getAxis() == Axis.Y ? 0.125f : 0.25f, //
            side.getAxis() == Axis.Z ? 0.125f : 0.25f //
        );

        UvFaceData[] types = { //
            UvFaceData.from16(4, 0, 12, 4), //
            UvFaceData.from16(4, 12, 12, 16), //
            UvFaceData.from16(0, 4, 4, 12), //
            UvFaceData.from16(12, 4, 16, 12) //
        };

        int[][] uvTypeIndices = { //
            // D, U, N, S, W, E
            { 0, 0, 1, 0, 2, 3 }, // D
            { 0, 0, 0, 1, 2, 3 }, // U
            { 1, 0, 0, 0, 3, 2 }, // N
            { 1, 0, 0, 0, 2, 3 }, // S
            { 1, 0, 2, 3, 0, 0 }, // W
            { 1, 0, 3, 2, 0, 0 }, // E
        };

        int quadIndex = 0;
        for (Direction face : Direction.values()) {
            if (face.getAxis() == side.getAxis()) {
                continue;
            }
            UvFaceData uvs = types[uvTypeIndices[face.ordinal()][side.ordinal()]];
            MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
            quad.setDiffuse(quad.normalvf());
            if (isColour) {
                quad.translated(0.01 * -face.getStepX(), 0.01 * -face.getStepY(), 0.01 * -face.getStepZ());
            }
            quads[quadIndex++] = quad;
        }

        if (extended) {
            float extensionLength = size - 0.25f;
            radius = new Vector3f(//
                side.getAxis() == Axis.X ? extensionLength / 2 : radius.x(), //
                side.getAxis() == Axis.Y ? extensionLength / 2 : radius.y(), //
                side.getAxis() == Axis.Z ? extensionLength / 2 : radius.z() //
            );
            center = new Vector3f(//
                0.5f + side.getStepX() * (0.5f + extensionLength / 2), //
                0.5f + side.getStepY() * (0.5f + extensionLength / 2), //
                0.5f + side.getStepZ() * (0.5f + extensionLength / 2) //
            );
            types = new UvFaceData[] { //
                new UvFaceData(0.25, 1 - extensionLength, 0.75, 1), //
                new UvFaceData(0.25, 0, 0.75, extensionLength), //
                new UvFaceData(1 - extensionLength, 0.25, 1, 0.75), //
                new UvFaceData(0, 0.25, extensionLength, 0.75) //
            };

            for (Direction face : Direction.values()) {
                if (face.getAxis() == side.getAxis()) {
                    continue;
                }
                UvFaceData uvs = types[uvTypeIndices[face.ordinal()][side.ordinal()]];
                MutableQuad quad = ModelUtil.createFace(face, center, radius, uvs);
                quad.setDiffuse(quad.normalvf());
                if (isColour) {
                    quad.translated(0.01 * -face.getStepX(), 0.01 * -face.getStepY(), 0.01 * -face.getStepZ());
                }
                quads[quadIndex++] = quad;
            }
        }

        if (isColour) {
            dupInverted(quads);
        } else {
            dupDarker(quads);
        }
        return quads;
    }

    private static void dupDarker(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            MutableQuad from = quads[i];
            if (from != null) {
                MutableQuad to = from.copyAndInvertNormal();
                to.setCalculatedDiffuse();
                to.multColourd(INSIDE_COLOUR_MULT);
                quads[i + halfLength] = to;
            }
        }
    }

    private static void dupInverted(MutableQuad[] quads) {
        int halfLength = quads.length / 2;
        for (int i = 0; i < halfLength; i++) {
            MutableQuad from = quads[i];
            if (from != null) {
                quads[i + halfLength] = from.copyAndInvertNormal();
            }
        }
    }

    // Model Usage

    public static List<BakedQuad> generateCutout(PipeBaseCutoutKey key) {
        List<MutableQuad> quads = new ArrayList<>();

        TextureAtlasSprite[] spriteArray = getSprites(key.definition);
        TextureAtlasSprite borderSprite = getBorderSprite(key);
        int colour = borderSprite == null ? -1 : getPipeModelColour(key.colour);
        int border_r = (colour >> 0) & 0xFF;
        int border_g = (colour >> 8) & 0xFF;
        int border_b = (colour >> 16) & 0xFF;
        for (Direction face : Direction.values()) {
            float size = key.connections[face.ordinal()];
            PipeFaceTex tex = size > 0 || key.centerSprite == null ? key.sideSprites[face.ordinal()]
                : key.centerSprite;
            MutableQuad[] quadArray = size == 0 ? QUADS[0][face.ordinal()] : setupSideQuads(face, size, false);

            int startIndex = quads.size();

            for (int i = 0; i < tex.getCount(); i++) {
                addQuads(quadArray, quads, getSprite(spriteArray, tex.getTexture(i)));

                int c = tex.getColour(i);
                int r = (c >> 0) & 0xFF;
                int g = (c >> 8) & 0xFF;
                int b = (c >> 16) & 0xFF;
                for (int q = startIndex; q < quads.size(); q++) {
                    quads.get(q).multColouri(r, g, b, 0xFF);
                }
                startIndex = quads.size();
            }

            if (borderSprite != null) {
                addQuads(quadArray, quads, borderSprite);
                for (int i = startIndex; i < quads.size(); i++) {
                    quads.get(i).multColouri(border_r, border_g, border_b, 0xFF);
                }
            }
        }
        List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
        for (MutableQuad q : quads) {
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    @Nullable
    private static TextureAtlasSprite getBorderSprite(PipeBaseCutoutKey key) {
        if (key.colour == null) {
            return null;
        }
        if (key.colourType == EnumPipeColourType.BORDER_INNER) {
            return BCTransportClientSprites.PIPE_COLOUR_BORDER_INNER.getSprite();
        }
        if (key.colourType == EnumPipeColourType.BORDER_OUTER) {
            return BCTransportClientSprites.PIPE_COLOUR_BORDER_OUTER.getSprite();
        }
        return null;
    }

    static TextureAtlasSprite getSprite(TextureAtlasSprite[] array, int index) {
        if (array == null || index < 0 || index >= array.length) {
            return SpriteUtil.missingSprite();
        }
        return array[index];
    }

    public static List<BakedQuad> generateTranslucent(PipeBaseTranslucentKey key) {
        if (!key.shouldRender()) return List.of();
        List<MutableQuad> quads = new ArrayList<>();
        TextureAtlasSprite sprite = BCTransportClientSprites.PIPE_COLOUR.getSprite();

        for (Direction face : Direction.values()) {
            float size = key.connections[face.ordinal()];
            if (size > 0) {
                addQuads(setupSideQuads(face, size, true), quads, sprite);
            } else {
                addQuads(QUADS_COLOURED[0][face.ordinal()], quads, sprite);
            }
        }
        int colour = getPipeModelColour(key.colour);
        List<BakedQuad> bakedQuads = new ArrayList<>(quads.size());
        for (MutableQuad q : quads) {
            q.colouri(colour);
            bakedQuads.add(q.toBakedBlock());
        }
        return bakedQuads;
    }

    /** @return The colour (ABGR, as quad vertex colours are stored) that paint of the given colour is drawn with. */
    static int getPipeModelColour(DyeColor c) {
        return 0xFF_00_00_00 | ColourUtil.swapArgbToAbgr(ColourUtil.getLightHex(c));
    }

    private static void addQuads(MutableQuad[] from, List<MutableQuad> to, TextureAtlasSprite sprite) {
        for (MutableQuad f : from) {
            if (f == null) {
                continue;
            }
            MutableQuad copy = new MutableQuad(f);
            copy.setSprite(sprite);
            copy.texFromSprite(sprite);
            to.add(copy);
        }
    }
}
