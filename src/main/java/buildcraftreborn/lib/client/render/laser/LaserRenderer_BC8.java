/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.laser;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import it.unimi.dsi.fastutil.floats.FloatArrayList;
import it.unimi.dsi.fastutil.ints.IntArrayList;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;

/** Draws lasers. 1.12 compiled each laser into a display list or VBO; here each laser's quads are baked once into an
 * array (kept for 5 seconds after it was made, like 1.12, so light changes show up) and copied into a
 * {@link RenderType#cutout()} buffer every frame. */
public class LaserRenderer_BC8 {
    /** Floats per vertex: x, y, z, u, v, nx, ny, nz, diffuse. */
    private static final int STRIDE = 9;

    private static final Map<LaserType, CompiledLaserType> COMPILED_LASER_TYPES = new ConcurrentHashMap<>();
    private static final LoadingCache<LaserData_BC8, BakedLaser> BAKED_LASERS = CacheBuilder.newBuilder()
        .expireAfterWrite(5, TimeUnit.SECONDS)
        .build(CacheLoader.from(LaserRenderer_BC8::bake));

    private record BakedLaser(float[] data, int[] light) {}

    public static void clearModels() {
        COMPILED_LASER_TYPES.clear();
        BAKED_LASERS.invalidateAll();
    }

    private static CompiledLaserType compileType(LaserType laserType) {
        return COMPILED_LASER_TYPES.computeIfAbsent(laserType, CompiledLaserType::new);
    }

    private static BakedLaser bake(LaserData_BC8 data) {
        FloatArrayList floats = new FloatArrayList();
        IntArrayList lights = new IntArrayList();
        ILaserRenderer sink = (x, y, z, u, v, lmap, nx, ny, nz, diffuse) -> {
            floats.add((float) x);
            floats.add((float) y);
            floats.add((float) z);
            floats.add((float) u);
            floats.add((float) v);
            floats.add(nx);
            floats.add(ny);
            floats.add(nz);
            floats.add(diffuse);
            lights.add(lmap);
        };
        LaserContext ctx = new LaserContext(sink, data, data.enableDiffuse, data.doubleFace);
        compileType(data.laserType).bakeFor(ctx);
        return new BakedLaser(floats.toFloatArray(), lights.toIntArray());
    }

    public static int computeLightmap(double x, double y, double z, int minBlockLight) {
        Level world = Minecraft.getInstance().level;
        if (world == null) return 0;
        int blockLight = minBlockLight >= 15 ? 15
            : Math.max(minBlockLight, getLightFor(world, LightLayer.BLOCK, x, y, z));
        int skyLight = getLightFor(world, LightLayer.SKY, x, y, z);
        return skyLight << 20 | blockLight << 4;
    }

    private static int getLightFor(Level world, LightLayer type, double x, double y, double z) {
        int max = 0;
        int count = 0;
        int sum = 0;
        boolean ao = Minecraft.useAmbientOcclusion();
        double xn = (x % 1 + 1) % 1;
        double yn = (y % 1 + 1) % 1;
        double zn = (z % 1 + 1) % 1;
        final double lowerBound = 0.3;
        final double upperBound = 1 - lowerBound;
        int xl = ao ? (xn < lowerBound ? -1 : 0) : -1;
        int yl = ao ? (yn < lowerBound ? -1 : 0) : -1;
        int zl = ao ? (zn < lowerBound ? -1 : 0) : -1;
        int xu = ao ? (xn > upperBound ? 1 : 0) : 1;
        int yu = ao ? (yn > upperBound ? 1 : 0) : 1;
        int zu = ao ? (zn > upperBound ? 1 : 0) : 1;
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int xp = xl; xp <= xu; xp++) {
            for (int yp = yl; yp <= yu; yp++) {
                for (int zp = zl; zp <= zu; zp++) {
                    pos.set(x + xp, y + yp, z + zp);
                    int light = world.getBrightness(type, pos);
                    if (light > 0) {
                        sum += light;
                        count++;
                    }
                    max = Math.max(max, light);
                }
            }
        }
        if (ao) {
            return count == 0 ? 0 : sum / count;
        } else {
            return max;
        }
    }

    /** Draws a laser into a {@link RenderType#cutout()} buffer. The pose must be the world's, with the camera at the
     * origin; {@code camera} is the camera's position. */
    public static void renderLaser(LaserData_BC8 data, PoseStack poseStack, VertexConsumer buffer, Vec3 camera) {
        BakedLaser baked = BAKED_LASERS.getUnchecked(data);
        poseStack.pushPose();
        poseStack.translate(data.start.x - camera.x, data.start.y - camera.y, data.start.z - camera.z);
        PoseStack.Pose pose = poseStack.last();
        float[] f = baked.data;
        int[] light = baked.light;
        for (int i = 0; i < light.length; i++) {
            int o = i * STRIDE;
            float d = f[o + 8];
            buffer.addVertex(pose, f[o], f[o + 1], f[o + 2])
                .setColor(d, d, d, 1.0f)
                .setUv(f[o + 3], f[o + 4])
                .setLight(light[i])
                .setNormal(pose, f[o + 5], f[o + 6], f[o + 7]);
        }
        poseStack.popPose();
    }
}
