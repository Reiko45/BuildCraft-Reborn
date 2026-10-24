/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.laser;

import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.joml.Vector4f;

import net.minecraft.world.phys.Vec3;

/** Turns a laser's local coordinates (x along the laser, in texture pixels) into coordinates relative to the laser's
 * start, and passes the finished quads to an {@link ILaserRenderer}. (1.12 baked world coordinates; keeping them
 * relative avoids float jitter far from the world origin. The renderer adds the start back.) */
public class LaserContext {
    public final Matrix4f matrix = new Matrix4f();
    private final Vector3f point = new Vector3f();
    private final Vector4f normal = new Vector4f();
    private final ILaserRenderer renderer;
    public final double length;
    private final boolean useNormalColour, drawBothSides;
    private final int minBlockLight;
    private final Vec3 origin;

    public LaserContext(ILaserRenderer renderer, LaserData_BC8 data, boolean useNormalColour, boolean isCullEnabled) {
        this.renderer = renderer;
        this.useNormalColour = useNormalColour;
        this.drawBothSides = isCullEnabled;
        this.minBlockLight = data.minBlockLight;
        Vec3 delta = data.start.subtract(data.end);
        double dx = delta.x;
        double dy = delta.y;
        double dz = delta.z;

        final double angleY, angleZ;

        double realLength = delta.length();
        length = realLength / data.scale;
        angleZ = Math.PI - Math.atan2(dz, dx);
        double rl_squared = realLength * realLength;
        double dy_dy = dy * dy;
        if (dx == 0 && dz == 0) {
            final double angle = Math.PI / 2;
            if (dy < 0) {
                angleY = angle;
            } else {
                angleY = -angle;
            }
        } else {
            dx = Math.sqrt(rl_squared - dy_dy);
            angleY = -Math.atan2(dy, dx);
        }

        // Matrix steps (each one applied before the ones above it):
        // 1: rotate angles (Y) to make everything work
        // 2: rotate angles (Z) to make everything work
        // 3: scale it by the laser's scale
        // (1.12's step 4, translating forward by "start", is done by the renderer)
        origin = data.start;
        matrix.identity()
            .scale((float) data.scale)
            .rotateY((float) angleZ)
            .rotateZ((float) angleY);
    }

    public void setFaceNormal(double nx, double ny, double nz) {
        if (useNormalColour) {
            normal.set((float) nx, (float) ny, (float) nz, 0);
            matrix.transform(normal);
            n[0] = normal.x;
            n[1] = normal.y;
            n[2] = normal.z;
            diffuse = diffuseLight(n[0], n[1], n[2]);
        }
    }

    /** Vanilla's directional block shading for a normal (1.12's {@code MutableQuad.diffuseLight}). */
    public static float diffuseLight(float x, float y, float z) {
        boolean up = y >= 0;
        float xx = x * x;
        float yy = y * y;
        float zz = z * z;
        float t = xx + yy + zz;
        float light = (xx * 0.6f + zz * 0.8f) / t;
        float yyt = yy / t;
        if (!up) yyt *= 0.5;
        light += yyt;
        return light;
    }

    private int index = 0;
    private final double[] x = { 0, 0, 0, 0 };
    private final double[] y = { 0, 0, 0, 0 };
    private final double[] z = { 0, 0, 0, 0 };
    private final double[] u = { 0, 0, 0, 0 };
    private final double[] v = { 0, 0, 0, 0 };
    private final int[] l = { 0, 0, 0, 0 };
    private final float[] n = { 0, 1, 0 };
    private float diffuse;

    public void addPoint(double xIn, double yIn, double zIn, double uIn, double vIn) {
        point.set((float) xIn, (float) yIn, (float) zIn);
        matrix.transformPosition(point);
        int lmap = LaserRenderer_BC8.computeLightmap(origin.x + point.x, origin.y + point.y, origin.z + point.z,
            minBlockLight);
        x[index] = point.x;
        y[index] = point.y;
        z[index] = point.z;
        u[index] = uIn;
        v[index] = vIn;
        l[index] = lmap;
        index++;
        if (index == 4) {
            index = 0;
            vertex(0);
            vertex(1);
            vertex(2);
            vertex(3);
            if (drawBothSides) {
                n[0] = -n[0];
                n[1] = -n[1];
                n[2] = -n[2];
                diffuse = diffuseLight(n[0], n[1], n[2]);
                vertex(3);
                vertex(2);
                vertex(1);
                vertex(0);
            }
            n[0] = 0;
            n[1] = 1;
            n[2] = 0;
        }
    }

    private void vertex(int i) {
        if (useNormalColour) {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], n[0], n[1], n[2], diffuse);
        } else {
            renderer.vertex(x[i], y[i], z[i], u[i], v[i], l[i], 0, 1, 0, 1);
        }
    }
}
