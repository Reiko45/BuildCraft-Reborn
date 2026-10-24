/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.render;

import java.util.HashSet;
import java.util.Set;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import buildcraftreborn.api.core.IBox;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.core.item.ItemMapLocation;
import buildcraftreborn.core.item.ItemMarkerConnector;
import buildcraftreborn.core.marker.PathCache;
import buildcraftreborn.core.marker.PathConnection;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.VolumeConnection;
import buildcraftreborn.core.marker.VolumeSubCache;
import buildcraftreborn.core.tile.TileMarkerVolume;
import buildcraftreborn.lib.BCLibConfig;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.lib.marker.MarkerCache;
import buildcraftreborn.lib.marker.MarkerSubCache;
import buildcraftreborn.lib.misc.VecUtil;
import buildcraftreborn.lib.misc.data.Box;

/** Draws marker lasers in the world: every connection always (1.12's {@code MarkerRenderer}), the signal lasers of
 * powered volume markers (1.12's {@code RenderMarkerVolume}), and every possible connection while a marker connector
 * is held (1.12's {@code RenderTickListener}). */
public final class MarkerRenderer {
    private static final double CONNECTION_SCALE = 1 / 16.05;
    private static final double SIGNAL_SCALE = 1 / 16.2; // smaller than normal lasers

    private MarkerRenderer() {}

    /** Runs after block entities: by then the camera's rotation is in the model-view matrix (it isn't yet during the
     * block layer stages), and water, drawn later, still covers lasers under it. */
    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        ClientLevel level = mc.level;
        LocalPlayer player = mc.player;
        if (level == null || player == null) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());

        mc.getProfiler().push("bc_markers");
        VolumeSubCache volumes = VolumeCache.INSTANCE.getSubCache(level);
        for (VolumeConnection connection : volumes.getConnections()) {
            LaserBoxRenderer.renderLaserBox(connection.getBox(), BuildCraftLaserManager.MARKER_VOLUME_CONNECTED, true,
                pose, buffer, camera);
        }
        for (PathConnection connection : PathCache.INSTANCE.getSubCache(level).getConnections()) {
            renderPath(connection, pose, buffer, camera);
        }
        for (BlockPos pos : volumes.getAllMarkers()) {
            if (volumes.getMarker(pos) instanceof TileMarkerVolume marker && marker.isShowingSignals()) {
                renderSignals(marker, pose, buffer, camera);
            }
        }
        if (player.getMainHandItem().is(BCCoreItems.MARKER_CONNECTOR.get())
            || player.getOffhandItem().is(BCCoreItems.MARKER_CONNECTOR.get())) {
            for (MarkerCache<?> cache : MarkerCache.CACHES) {
                renderPossibleConnections(player, cache.getSubCache(level), getPossibleLaserType(cache), pose, buffer,
                    camera);
            }
        }
        if (player.getMainHandItem().is(BCCoreItems.MAP_LOCATION.get())) {
            renderMapLocation(player.getMainHandItem(), pose, buffer, camera);
        }
        mc.getProfiler().pop();
        buffers.endBatch(RenderType.cutout());
    }

    /** 1.12's {@code RenderTickListener.renderMapLocation}: an arrow onto a spot, or the box of an area. (Paths and
     * zones weren't drawn in 1.12 either.) */
    private static void renderMapLocation(ItemStack stack, PoseStack pose, VertexConsumer buffer, Vec3 camera) {
        MapLocationType type = MapLocationType.getFromStack(stack);
        if (type == MapLocationType.SPOT) {
            IBox box = ItemMapLocation.getPointBox(stack);
            if (box != null) {
                Vec3 origin = Vec3.atLowerCornerOf(box.min());
                for (Vec3[] line : getMapLocationArrow(ItemMapLocation.getPointFace(stack))) {
                    LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.STRIPES_WRITE,
                        origin.add(line[0]), origin.add(line[1]), 1 / 16.0);
                    LaserRenderer_BC8.renderLaser(laser, pose, buffer, camera);
                }
            }
        } else if (type == MapLocationType.AREA) {
            IBox area = ItemMapLocation.getAreaBox(stack);
            Box box = new Box();
            box.initialize(area);
            LaserBoxRenderer.renderLaserBox(box, BuildCraftLaserManager.STRIPES_WRITE, true, pose, buffer, camera);
        }
    }

    /** The arrow (a line with four barbs) pointing onto the given face of a block, in block coordinates. */
    private static Vec3[][] getMapLocationArrow(Direction face) {
        double[][][] upFace = {
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.6, 0.5 } }, // Main line
            { { 0.5, 0.9, 0.5 }, { 0.8, 1.2, 0.5 } }, // First arrow part (+X)
            { { 0.5, 0.9, 0.5 }, { 0.2, 1.2, 0.5 } }, // Second arrow part (-X)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.8 } }, // Third arrow part (+Z)
            { { 0.5, 0.9, 0.5 }, { 0.5, 1.2, 0.2 } }, // Forth arrow part (-Z)
        };
        Quaternionf rotation = new Quaternionf().rotationTo(0, 1, 0, face.getStepX(), face.getStepY(),
            face.getStepZ());
        Vec3[][] arrow = new Vec3[upFace.length][2];
        for (int i = 0; i < upFace.length; i++) {
            for (int j = 0; j < 2; j++) {
                Vector3f point = new Vector3f((float) upFace[i][j][0] - 0.5f, (float) upFace[i][j][1] - 0.5f,
                    (float) upFace[i][j][2] - 0.5f);
                rotation.transform(point);
                arrow[i][j] = new Vec3(point.x + 0.5, point.y + 0.5, point.z + 0.5);
            }
        }
        return arrow;
    }

    /** 1.12's {@code MarkerSubCache.getPossibleLaserType}, kept on the client side. */
    private static LaserType getPossibleLaserType(MarkerCache<?> cache) {
        if (cache == VolumeCache.INSTANCE) {
            return BuildCraftLaserManager.MARKER_VOLUME_POSSIBLE;
        } else if (cache == PathCache.INSTANCE) {
            return BuildCraftLaserManager.MARKER_PATH_POSSIBLE;
        }
        return BuildCraftLaserManager.MARKER_DEFAULT_POSSIBLE;
    }

    private static void renderPath(PathConnection connection, PoseStack pose, VertexConsumer buffer, Vec3 camera) {
        BlockPos last = null;
        for (BlockPos p : connection.getPathPositions()) {
            if (last != null) {
                renderPathLaser(VecUtil.convertCenter(last), VecUtil.convertCenter(p), pose, buffer, camera);
            }
            last = p;
        }
        if (connection.isLoop() && last != null) {
            BlockPos first = connection.getPathPositions().get(0);
            renderPathLaser(VecUtil.convertCenter(last), VecUtil.convertCenter(first), pose, buffer, camera);
        }
    }

    private static void renderPathLaser(Vec3 from, Vec3 to, PoseStack pose, VertexConsumer buffer, Vec3 camera) {
        Vec3 one = offset(from, to);
        Vec3 two = offset(to, from);
        LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_PATH_CONNECTED, one, two,
            CONNECTION_SCALE);
        LaserRenderer_BC8.renderLaser(data, pose, buffer, camera);
    }

    private static Vec3 offset(Vec3 from, Vec3 to) {
        Vec3 dir = to.subtract(from).normalize();
        return from.add(dir.scale(0.125));
    }

    private static void renderSignals(TileMarkerVolume marker, PoseStack pose, VertexConsumer buffer, Vec3 camera) {
        VolumeConnection volume = marker.getCurrentConnection();
        Set<Axis> taken = volume == null ? Set.of() : volume.getConnectedAxis();
        Vec3 start = VecUtil.convertCenter(marker.getBlockPos());
        int max = BCLibConfig.markerMaxDistance();
        for (Direction face : Direction.values()) {
            if (taken.contains(face.getAxis())) {
                continue;
            }
            Vec3 end = VecUtil.offset(start, face, max);
            Direction faceForMin = VecUtil.getFacing(face.getAxis(), true);
            Direction faceForMax = VecUtil.getFacing(face.getAxis(), false);
            Vec3 one = VecUtil.offset(start, faceForMin, 1 / 16.0);
            Vec3 two = VecUtil.offset(end, faceForMax, 1 / 16.0);
            LaserData_BC8 data = new LaserData_BC8(BuildCraftLaserManager.MARKER_VOLUME_SIGNAL, one, two,
                SIGNAL_SCALE);
            LaserRenderer_BC8.renderLaser(data, pose, buffer, camera);
        }
    }

    private static void renderPossibleConnections(LocalPlayer player, MarkerSubCache<?> cache, LaserType type,
        PoseStack pose, VertexConsumer buffer, Vec3 camera) {
        Set<LaserData_BC8> toRender = new HashSet<>();
        for (final BlockPos a : cache.getAllMarkers()) {
            for (final BlockPos b : cache.getValidConnections(a)) {
                if (a.asLong() > b.asLong()) {
                    // Only render each pair once
                    continue;
                }

                Vec3 start = VecUtil.convertCenter(a);
                Vec3 end = VecUtil.convertCenter(b);

                Vec3 startToEnd = end.subtract(start).normalize();
                Vec3 endToStart = start.subtract(end).normalize();
                start = start.add(startToEnd.scale(0.125));
                end = end.add(endToStart.scale(0.125));

                LaserType laserType = type;
                if (ItemMarkerConnector.doesInteract(a, b, player)) {
                    laserType = BuildCraftLaserManager.MARKER_DEFAULT_POSSIBLE;
                }
                toRender.add(new LaserData_BC8(laserType, start, end, 1 / 16.0));
            }
        }
        for (LaserData_BC8 laser : toRender) {
            LaserRenderer_BC8.renderLaser(laser, pose, buffer, camera);
        }
    }
}
