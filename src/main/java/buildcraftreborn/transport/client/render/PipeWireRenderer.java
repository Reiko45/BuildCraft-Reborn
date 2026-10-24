/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import java.util.EnumMap;
import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.transport.EnumWirePart;
import buildcraftreborn.lib.client.model.ModelUtil;
import buildcraftreborn.lib.client.model.ModelUtil.UvFaceData;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.sprite.SpriteHolderRegistry;
import buildcraftreborn.lib.client.sprite.SpriteHolderRegistry.SpriteHolder;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.transport.tile.TilePipeHolder;
import buildcraftreborn.transport.wire.EnumWireBetween;
import buildcraftreborn.transport.wire.WireManager;

/** Draws pipe wires: a small cube on each wired corner, and the strips between them.
 * <p>
 * The wire textures have 16 rows, one per power level (only "off" and "on" are used). 1.12 compiled each wire into a
 * display list; here the quads (with their texture and shading) are built once per wire, colour and state, and cached
 * until the textures reload. */
public final class PipeWireRenderer {
    private static final Map<EnumWirePart, MutableQuad[]> PART_QUADS = new EnumMap<>(EnumWirePart.class);
    private static final Map<EnumWireBetween, MutableQuad[]> BETWEEN_QUADS = new EnumMap<>(EnumWireBetween.class);
    private static final Map<DyeColor, SpriteHolder> WIRE_SPRITES = new EnumMap<>(DyeColor.class);

    /** Ready to draw quads, by [part or between][colour][off / on]. */
    private static final MutableQuad[][][][] CACHE =
        new MutableQuad[EnumWirePart.VALUES.length + EnumWireBetween.VALUES.length][16][2][];

    static {
        for (DyeColor colour : DyeColor.values()) {
            WIRE_SPRITES.put(colour, SpriteHolderRegistry.getHolder(BuildCraftReborn.id("block/wires/"
                + colour.getName())));
        }
        for (EnumWirePart part : EnumWirePart.VALUES) {
            PART_QUADS.put(part, getQuads(part));
        }
        for (EnumWireBetween part : EnumWireBetween.VALUES) {
            BETWEEN_QUADS.put(part, getQuads(part));
        }
    }

    private PipeWireRenderer() {}

    /** Forgets the built wire quads (after the textures reload). */
    public static void clearWireCache() {
        for (MutableQuad[][][] a : CACHE) {
            for (MutableQuad[][] b : a) {
                b[0] = null;
                b[1] = null;
            }
        }
    }

    public static SpriteHolder getWireSprite(DyeColor colour) {
        return WIRE_SPRITES.get(colour);
    }

    private static MutableQuad[] getQuads(EnumWirePart part) {
        MutableQuad[] quads = new MutableQuad[6];
        Vector3f center = new Vector3f(//
            0.5f + (part.x.getStep() * 4.51f / 16f), //
            0.5f + (part.y.getStep() * 4.51f / 16f), //
            0.5f + (part.z.getStep() * 4.51f / 16f) //
        );
        Vector3f radius = new Vector3f(1 / 32f, 1 / 32f, 1 / 32f);
        UvFaceData uvs = new UvFaceData();
        int off = func(part.x) * 4 + func(part.y) * 2 + func(part.z);
        uvs.minU = off / 16f;
        uvs.maxU = (off + 1) / 16f;
        uvs.minV = 0;
        uvs.maxV = 1 / 16f;
        for (Direction face : Direction.values()) {
            quads[face.ordinal()] = ModelUtil.createFace(face, center, radius, uvs);
        }
        return quads;
    }

    private static int func(AxisDirection dir) {
        return dir == AxisDirection.POSITIVE ? 1 : 0;
    }

    private static MutableQuad[] getQuads(EnumWireBetween between) {
        // 4 rather than 6 -- don't render the end caps
        MutableQuad[] quads = new MutableQuad[4];
        int i = 0;

        Vec3 center;
        Vec3 radius;

        boolean ax = between.mainAxis == Axis.X;
        boolean ay = between.mainAxis == Axis.Y;
        boolean az = between.mainAxis == Axis.Z;

        if (between.to == null) {
            double cL = 0.5f - 4.51f / 16f;
            double cU = 0.5f + 4.51f / 16f;
            center = new Vec3(//
                ax ? 0.5f : (between.xy ? cU : cL), //
                ay ? 0.5f : ((ax ? between.xy : between.yz) ? cU : cL), //
                az ? 0.5f : (between.yz ? cU : cL) //
            );
            double rC = 4.01f / 16f;
            double rN = 1f / 16f / 2;
            radius = new Vec3(ax ? rC : rN, ay ? rC : rN, az ? rC : rN);
        } else {// we are a connection
            double cL = (8 - 4.51) / 16;
            double cU = (8 + 4.51) / 16;
            radius = new Vec3(//
                ax ? 2.99 / 32 : 1 / 32.0, //
                ay ? 2.99 / 32 : 1 / 32.0, //
                az ? 2.99 / 32 : 1 / 32.0 //
            );
            center = new Vec3(//
                ax ? (0.5 + 6.505 / 16 * between.to.getStepX()) : (between.xy ? cU : cL), //
                ay ? (0.5 + 6.505 / 16 * between.to.getStepY()) : ((ax ? between.xy : between.yz) ? cU : cL), //
                az ? (0.5 + 6.505 / 16 * between.to.getStepZ()) : (between.yz ? cU : cL) //
            );
        }

        UvFaceData uvBase = new UvFaceData();
        uvBase.minU = (float) VecUtil.getValue(center.subtract(radius), between.mainAxis);
        uvBase.maxU = (float) VecUtil.getValue(center.add(radius), between.mainAxis);
        uvBase.minV = 0;
        uvBase.maxV = 1 / 16f;

        Vector3f centerFloat = new Vector3f((float) center.x, (float) center.y, (float) center.z);
        Vector3f radiusFloat = new Vector3f((float) radius.x, (float) radius.y, (float) radius.z);

        for (Direction face : Direction.values()) {
            if (face.getAxis() == between.mainAxis) {
                continue;
            }
            UvFaceData uvs = new UvFaceData(uvBase);

            Axis aAxis = between.mainAxis;
            Axis fAxis = face.getAxis();
            boolean fPositive = face.getAxisDirection() == AxisDirection.POSITIVE;

            int rotations = 0;
            boolean swapU = false;
            boolean swapV = false;

            if (aAxis == Axis.X) {
                swapV = fPositive;
            } else if (aAxis == Axis.Y) {
                rotations = 1;
                swapU = (fAxis == Axis.X) != fPositive;
                swapV = fAxis == Axis.Z;
            } else {// aAxis == Axis.Z
                if (fAxis == Axis.Y) {
                    rotations = 1;
                }
                swapU = face == Direction.DOWN;
                swapV = face != Direction.EAST;
            }

            if (swapU) {
                float t = uvs.minU;
                uvs.minU = uvs.maxU;
                uvs.maxU = t;
            }
            if (swapV) {
                float t = uvs.minV;
                uvs.minV = uvs.maxV;
                uvs.maxV = t;
            }

            MutableQuad quad = ModelUtil.createFace(face, centerFloat, radiusFloat, uvs);
            if (rotations > 0) quad.rotateTextureUp(rotations);
            quads[i++] = quad;
        }
        return quads;
    }

    /** Maps the quads onto the wire texture row for the given power level, and shades them. */
    private static MutableQuad[] build(MutableQuad[] quads, DyeColor colour, int level) {
        TextureAtlasSprite sprite = WIRE_SPRITES.get(colour).getSprite();
        float vOffset = (level & 0xF) / 16f;
        MutableQuad[] built = new MutableQuad[quads.length];
        for (int i = 0; i < quads.length; i++) {
            MutableQuad q = new MutableQuad(quads[i]);
            if (q.getFace() != Direction.UP && level != 15) {
                float shade = 1 - q.getCalculatedDiffuse();
                shade = shade * (15 - level) / 15;
                q.multColourd(1 - shade);
            }
            q.vertex_0.texf(q.vertex_0.tex_u, q.vertex_0.tex_v + vOffset);
            q.vertex_1.texf(q.vertex_1.tex_u, q.vertex_1.tex_v + vOffset);
            q.vertex_2.texf(q.vertex_2.tex_u, q.vertex_2.tex_v + vOffset);
            q.vertex_3.texf(q.vertex_3.tex_u, q.vertex_3.tex_v + vOffset);
            q.texFromSprite(sprite);
            built[i] = q;
        }
        return built;
    }

    private static MutableQuad[] get(int index, MutableQuad[] raw, DyeColor colour, boolean isOn) {
        MutableQuad[][] byState = CACHE[index][colour.ordinal()];
        int s = isOn ? 1 : 0;
        if (byState[s] == null) {
            byState[s] = build(raw, colour, isOn ? 15 : 0);
        }
        return byState[s];
    }

    public static void renderWires(TilePipeHolder pipe, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight) {
        WireManager wires = pipe.getWireManager();
        if (wires.parts.isEmpty()) {
            return;
        }
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());
        int sky = LightTexture.sky(combinedLight);
        int onLight = LightTexture.pack(15, sky);
        PoseStack.Pose pose = poseStack.last();
        for (Map.Entry<EnumWirePart, DyeColor> partColour : wires.parts.entrySet()) {
            EnumWirePart part = partColour.getKey();
            boolean isOn = wires.isPowered(part);
            MutableQuad[] quads = get(part.ordinal(), PART_QUADS.get(part), partColour.getValue(), isOn);
            render(quads, pose, vc, isOn ? onLight : combinedLight);
        }
        for (Map.Entry<EnumWireBetween, DyeColor> betweenColour : wires.betweens.entrySet()) {
            EnumWireBetween between = betweenColour.getKey();
            boolean isOn = wires.isPowered(between.parts[0]);
            MutableQuad[] quads = get(EnumWirePart.VALUES.length + between.ordinal(), BETWEEN_QUADS.get(between),
                betweenColour.getValue(), isOn);
            render(quads, pose, vc, isOn ? onLight : combinedLight);
        }
    }

    private static void render(MutableQuad[] quads, PoseStack.Pose pose, VertexConsumer vc, int light) {
        for (MutableQuad q : quads) {
            if (q != null) {
                q.render(pose, vc, light, OverlayTexture.NO_OVERLAY);
            }
        }
    }
}
