/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.lib.client.render.model;

import java.util.function.UnaryOperator;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;

/** Draws a cuboid from a 1.12 JSON variable model (an "element": from/to in pixels, and a UV rectangle in pixels per
 * face) into a block atlas buffer, with vanilla's per-face shading. Stands in for the moving parts of those models,
 * which this port draws directly.
 * <p>
 * Texture orientation follows 1.12's {@code ModelUtil.createFace}, which differs from a vanilla block model element
 * on three faces: on {@code up} the V axis is flipped, and on {@code north} and {@code east} the U axis is flipped. */
public final class CuboidRenderer {
    private CuboidRenderer() {}

    /** Vanilla's directional shade (the same values 1.12's {@code MutableQuad.multShade} used). */
    public static float shade(Direction face) {
        return switch (face) {
            case DOWN -> 0.5f;
            case UP -> 1.0f;
            case NORTH, SOUTH -> 0.8f;
            case WEST, EAST -> 0.6f;
        };
    }

    /** A region of a sprite, in pixels (0 to 16), standing for a whole 16x16 texture (1.12's sub-textures such as
     * {@code {"location": ..., "uv": [0, 8, 16, 16]}}). */
    public record SpriteRegion(TextureAtlasSprite sprite, float u0, float v0, float u1, float v1) {
        public static SpriteRegion whole(TextureAtlasSprite sprite) {
            return new SpriteRegion(sprite, 0, 0, 16, 16);
        }

        float u(float px) {
            return sprite.getU((u0 + px / 16 * (u1 - u0)) / 16);
        }

        float v(float px) {
            return sprite.getV((v0 + px / 16 * (v1 - v0)) / 16);
        }
    }

    /** @param from The element's minimum corner, in pixels.
     * @param to The element's maximum corner, in pixels.
     * @param faceUv The UV rectangle {u0, v0, u1, v1} (in pixels of the region) for each face, by
     *            {@link Direction#ordinal()}; null entries are not drawn.
     * @param colour ARGB tint, multiplied by the face shade. */
    public static void render(PoseStack.Pose pose, VertexConsumer vc, SpriteRegion tex, int light, int colour,
        float[] from, float[] to, @Nullable float[][] faceUv) {
        render(pose, vc, tex, light, colour, from, to, faceUv, UnaryOperator.identity());
    }

    /** @param shadeFace Maps a face of the element to the direction it ends up facing in the world (when the pose
     *            rotates the element), so the face gets the shade of its final direction as baked models do. */
    public static void render(PoseStack.Pose pose, VertexConsumer vc, SpriteRegion tex, int light, int colour,
        float[] from, float[] to, @Nullable float[][] faceUv, UnaryOperator<Direction> shadeFace) {
        float xs = from[0] / 16, ys = from[1] / 16, zs = from[2] / 16;
        float xb = to[0] / 16, yb = to[1] / 16, zb = to[2] / 16;
        for (Direction face : Direction.values()) {
            float[] uv = faceUv == null ? null : faceUv[face.ordinal()];
            if (uv == null) {
                continue;
            }
            float u0 = uv[0], v0 = uv[1], u1 = uv[2], v1 = uv[3];
            if (face == Direction.UP) {
                float t = v0;
                v0 = v1;
                v1 = t;
            } else if (face == Direction.NORTH || face == Direction.EAST) {
                float t = u0;
                u0 = u1;
                u1 = t;
            }
            int c = shaded(colour, shade(shadeFace.apply(face)));
            float nx = face.getStepX(), ny = face.getStepY(), nz = face.getStepZ();
            switch (face) {
                case UP -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zb, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zb, u1, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zs, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zs, u0, v0);
                }
                case DOWN -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zs, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zs, u1, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zb, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zb, u0, v0);
                }
                case WEST -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zs, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zb, u1, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zb, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zs, u0, v0);
                }
                case EAST -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zs, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zb, u0, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zb, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zs, u1, v1);
                }
                case NORTH -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zs, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zs, u0, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zs, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zs, u1, v1);
                }
                case SOUTH -> {
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, ys, zb, u0, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, ys, zb, u1, v1);
                    v(pose, vc, tex, light, c, nx, ny, nz, xb, yb, zb, u1, v0);
                    v(pose, vc, tex, light, c, nx, ny, nz, xs, yb, zb, u0, v0);
                }
            }
        }
    }

    private static int shaded(int argb, float shade) {
        int a = (argb >>> 24) & 0xFF;
        int r = (int) (((argb >> 16) & 0xFF) * shade);
        int g = (int) (((argb >> 8) & 0xFF) * shade);
        int b = (int) ((argb & 0xFF) * shade);
        return (a << 24) | (r << 16) | (g << 8) | b;
    }

    private static void v(PoseStack.Pose pose, VertexConsumer vc, SpriteRegion tex, int light, int colour, float nx,
        float ny, float nz, float x, float y, float z, float u, float v) {
        vc.addVertex(pose, x, y, z)
            .setColor(colour)
            .setUv(tex.u(u), tex.v(v))
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }
}
