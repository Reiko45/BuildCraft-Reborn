/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.model.plug;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.function.Function;

import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.block.model.BakedQuad;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.model.data.ModelData;

import buildcraftreborn.api.transport.pluggable.IPluggableStaticBaker;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.model.MutableVertex;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.silicon.client.model.key.KeyPlugFacade;
import buildcraftreborn.silicon.plug.PluggableFacade;
import buildcraftreborn.transport.client.BCTransportModels;
import buildcraftreborn.transport.client.model.key.KeyPlugBlocker;

/** Bakes a facade: the block's model faces squeezed onto the facade's plate (and, for a hollow facade, onto its
 * frame). Each face of the block model is stretched over the part of the plate facing the same way. Tint indexes
 * become {@code tint * 6 + side} (see {@link PluggableFacade#getBlockColor(int)}). */
public enum PlugBakerFacade implements IPluggableStaticBaker<KeyPlugFacade> {
    INSTANCE;

    private static final RandomSource RANDOM = RandomSource.create();

    private int getVertexIndex(List<Vec3> positions, Direction.Axis axis, boolean minOrMax1, boolean minOrMax2) {
        Direction.Axis axis1, axis2;
        switch (axis) {
            case X -> {
                axis1 = Direction.Axis.Y;
                axis2 = Direction.Axis.Z;
            }
            case Y -> {
                axis1 = Direction.Axis.X;
                axis2 = Direction.Axis.Z;
            }
            default -> {
                axis1 = Direction.Axis.X;
                axis2 = Direction.Axis.Y;
            }
        }
        double min1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).min().orElse(0);
        double min2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).min().orElse(0);
        double max1 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis1)).max().orElse(0);
        double max2 = positions.stream().mapToDouble(pos -> VecUtil.getValue(pos, axis2)).max().orElse(0);
        double center1 = (min1 + max1) / 2;
        double center2 = (min2 + max2) / 2;
        return positions.indexOf(positions.stream()
            .filter(pos -> (minOrMax1 ? VecUtil.getValue(pos, axis1) < center1
                : VecUtil.getValue(pos, axis1) > center1)
                && (minOrMax2 ? VecUtil.getValue(pos, axis2) < center2 : VecUtil.getValue(pos, axis2) > center2))
            .findFirst().orElse(positions.get(0)));
    }

    private static List<BakedQuad> getQuads(BlockState state, BakedModel model, Direction side) {
        List<BakedQuad> quads = new ArrayList<>(model.getQuads(state, side, RANDOM, ModelData.EMPTY, null));
        if (quads.isEmpty()) {
            // Some models (glass panes' cousins, a few modded blocks) only have unculled quads
            for (BakedQuad quad : model.getQuads(state, null, RANDOM, ModelData.EMPTY, null)) {
                if (quad.getDirection() == side) {
                    quads.add(quad);
                }
            }
        }
        return quads;
    }

    private List<MutableQuad> getTransformedQuads(BlockState state, BakedModel model, Direction side, Vec3 pos0,
        Vec3 pos1, Vec3 pos2, Vec3 pos3) {
        List<MutableQuad> result = new ArrayList<>();
        boolean positive = side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
        Function<Vec3, Vec3> transformPosition = pos -> switch (side.getAxis()) {
            case X -> new Vec3(positive ? 1 - pos.z : pos.z, pos.y, pos.x);
            case Y -> new Vec3(pos.x, positive ? 1 - pos.z : pos.z, pos.y);
            case Z -> new Vec3(pos.y, pos.x, positive ? 1 - pos.z : pos.z);
        };
        List<Vec3> poses = Arrays.asList(transformPosition.apply(pos0), transformPosition.apply(pos1),
            transformPosition.apply(pos2), transformPosition.apply(pos3));
        for (BakedQuad quad : getQuads(state, model, side)) {
            MutableQuad mutableQuad = new MutableQuad().fromBakedBlock(quad);
            List<MutableVertex> vertexes = Arrays.asList(mutableQuad.vertex_0, mutableQuad.vertex_1,
                mutableQuad.vertex_2, mutableQuad.vertex_3);
            List<Vec3> vertexesPoses = new ArrayList<>(4);
            for (MutableVertex vertex : vertexes) {
                vertexesPoses.add(new Vec3(vertex.position_x, vertex.position_y, vertex.position_z));
            }
            double minU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).min().orElse(0);
            double minV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).min().orElse(0);
            double maxU = vertexes.stream().mapToDouble(vertex -> vertex.tex_u).max().orElse(0);
            double maxV = vertexes.stream().mapToDouble(vertex -> vertex.tex_v).max().orElse(0);
            boolean[][] corners = { { false, false }, { false, true }, { true, true }, { true, false } };
            // Work out every vertex's new place first: moving one changes where the others look like they are
            int[] targets = new int[4];
            int[] sources = new int[4];
            for (int c = 0; c < 4; c++) {
                targets[c] = getVertexIndex(poses, side.getAxis(), corners[c][0], corners[c][1]);
                sources[c] = getVertexIndex(vertexesPoses, side.getAxis(), corners[c][0], corners[c][1]);
            }
            for (int c = 0; c < 4; c++) {
                Vec3 newPos = poses.get(targets[c]);
                MutableVertex vertex = vertexes.get(sources[c]);
                vertex.positiond(newPos.x, newPos.y, newPos.z);
                switch (side.getAxis()) {
                    case X -> vertex.texf((float) (minU + (maxU - minU) * (positive ? (1 - newPos.z) : newPos.z)),
                        (float) (minV + (maxV - minV) * (1 - newPos.y)));
                    case Y -> vertex.texf((float) (minU + (maxU - minU) * newPos.x),
                        (float) (minV + (maxV - minV) * (positive ? newPos.z : (1 - newPos.z))));
                    case Z -> vertex.texf((float) (minU + (maxU - minU) * (positive ? newPos.x : (1 - newPos.x))),
                        (float) (minV + (maxV - minV) * (1 - newPos.y)));
                }
            }
            // The face may now point another way (the frame's inner edges), so light it by where it faces
            Vector3f normal = mutableQuad.getCalculatedNormal();
            mutableQuad.normalf(normal.x, normal.y, normal.z);
            mutableQuad.setFace(Direction.getNearest(normal.x, normal.y, normal.z));
            result.add(mutableQuad);
        }
        return result;
    }

    private static Vec3 rotate(Vec3 vec, Rotation rotation) {
        return switch (rotation) {
            case NONE -> new Vec3(vec.x, vec.y, vec.z);
            case CLOCKWISE_90 -> new Vec3(1 - vec.y, 1 - vec.x, vec.z);
            case CLOCKWISE_180 -> new Vec3(1 - vec.x, 1 - vec.y, vec.z);
            case COUNTERCLOCKWISE_90 -> new Vec3(vec.y, vec.x, vec.z);
        };
    }

    private void addRotatedQuads(List<MutableQuad> quads, BlockState state, BakedModel model, Direction side,
        Rotation rotation, Vec3 pos0, Vec3 pos1, Vec3 pos2, Vec3 pos3) {
        quads.addAll(getTransformedQuads(state, model, side, rotate(pos0, rotation), rotate(pos1, rotation),
            rotate(pos2, rotation), rotate(pos3, rotation)));
    }

    private static Vec3 v(double x, double y, double z) {
        return new Vec3(x / 16D, y / 16D, z / 16D);
    }

    public List<MutableQuad> bakeForKey(KeyPlugFacade key) {
        BakedModel model = Minecraft.getInstance().getBlockRenderer().getBlockModel(key.state);
        List<MutableQuad> quads = new ArrayList<>();
        int pS = PluggableFacade.SIZE;
        int nS = 16 - pS;
        if (!key.isHollow) {
            quads.addAll(getTransformedQuads(key.state, model, key.side, v(0, 16, 0), v(16, 16, 0), v(16, 0, 0),
                v(0, 0, 0)));
            quads.addAll(getTransformedQuads(key.state, model, key.side.getOpposite(), v(pS, nS, nS),
                v(nS, nS, nS), v(nS, pS, nS), v(pS, pS, nS)));
        }
        for (Rotation rotation : Rotation.values()) {
            boolean even = rotation.ordinal() % 2 == 0;
            if (key.isHollow) {
                addRotatedQuads(quads, key.state, model, key.side, rotation, v(0, even ? 4 : 0, 0),
                    v(4, even ? 4 : 0, 0), v(4, even ? 16 : 12, 0), v(0, even ? 16 : 12, 0));
            }
            addRotatedQuads(quads, key.state, model, key.side.getOpposite(), rotation, v(0, 16, 16),
                v(pS, nS, nS), v(pS, pS, nS), v(0, 0, 16));
            if (key.isHollow) {
                addRotatedQuads(quads, key.state, model, key.side.getOpposite(), rotation,
                    v(pS, even ? nS : 12, nS), v(4, even ? nS : 12, nS), v(4, even ? 4 : pS, nS),
                    v(pS, even ? 4 : pS, nS));
            }
        }
        if (key.isHollow) {
            boolean positive = key.side.getAxisDirection() == Direction.AxisDirection.POSITIVE;
            for (Direction facing : Direction.values()) {
                if (facing.getAxis() == key.side.getAxis()) {
                    continue;
                }
                if (key.side.getAxis() == Direction.Axis.Z && facing.getAxis() == Direction.Axis.X
                    || key.side.getAxis() == Direction.Axis.X && facing.getAxis() == Direction.Axis.Y
                    || key.side.getAxis() == Direction.Axis.Y && facing.getAxis() == Direction.Axis.Z) {
                    quads.addAll(getTransformedQuads(key.state, model, facing,
                        v(positive ? 16 : pS, 4, 12.003), v(positive ? 16 : pS, 12, 12.003),
                        v(positive ? nS : 0, 12, 12.003), v(positive ? nS : 0, 4, 12.003)));
                } else {
                    quads.addAll(getTransformedQuads(key.state, model, facing,
                        v(4, positive ? 16 : pS, 12.003), v(4, positive ? nS : 0, 12.003),
                        v(12, positive ? nS : 0, 12.003), v(12, positive ? 16 : pS, 12.003)));
                }
            }
        }
        for (MutableQuad quad : quads) {
            int tint = quad.getTint();
            if (tint != -1) {
                quad.setTint(tint * Direction.values().length + key.side.ordinal());
            }
        }
        return quads;
    }

    /** Whether the facade covers the pipe end completely, so it shows the plug behind it. */
    public static boolean showsBlocker(KeyPlugFacade key) {
        return !key.isHollow && Block.isShapeFullBlock(key.state.getShape(EmptyBlockGetter.INSTANCE, BlockPos.ZERO))
            && key.state.canOcclude();
    }

    @Override
    public List<BakedQuad> bake(KeyPlugFacade key) {
        List<BakedQuad> baked = new ArrayList<>();
        for (MutableQuad quad : bakeForKey(key)) {
            baked.add(quad.toBakedBlock());
        }
        if (showsBlocker(key)) {
            baked.addAll(BCTransportModels.BAKER_PLUG_BLOCKER.bake(new KeyPlugBlocker(key.side)));
        }
        return baked;
    }
}
