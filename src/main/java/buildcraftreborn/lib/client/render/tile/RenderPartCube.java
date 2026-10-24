/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.tile;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;

/** A variable sized element (like LED) that can render somewhere in a block entity renderer. Set the location,
 * colour, light and size of the single element, then call {@link #render}. This does not allow for different
 * textures: it is drawn with a plain white sprite ({@code buildcraftreborn:block/white}, standing in for 1.12's
 * {@code ModelLoader.White}) so the colour shows as-is. */
public class RenderPartCube {
    public double x, y, z;
    public double sizeX = 1 / 16.0, sizeY = 1 / 16.0, sizeZ = 1 / 16.0;
    /** ARGB. */
    public int colour = -1;
    /** Packed light. */
    public int light = LightTexture.FULL_BRIGHT;

    /** Constructs a simple cube configured for a LED. */
    public RenderPartCube() {
        this(1 / 16.0, 1 / 16.0, 1 / 16.0);
    }

    public RenderPartCube(double x, double y, double z) {
        position(x, y, z);
    }

    public RenderPartCube position(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        return this;
    }

    /** Sets the light from separate block and sky light values (0 to 15). */
    public void light(int block, int sky) {
        light = LightTexture.pack(block, sky);
    }

    /** Raises the block and sky light to at least the given values (1.12's {@code maxLighti}). */
    public void maxLight(int block, int sky) {
        int b = Math.max(LightTexture.block(light), block);
        int s = Math.max(LightTexture.sky(light), sky);
        light = LightTexture.pack(b, s);
    }

    /** Renders the element into a {@code RenderType.cutout()} (or similar block atlas) buffer. */
    public void render(PoseStack.Pose pose, VertexConsumer vc) {
        TextureAtlasSprite sprite = FluidRenderer.getBlockSprite(BuildCraftReborn.id("block/white"));
        float u = sprite.getU(0.5f);
        float v = sprite.getV(0.5f);
        double rX = sizeX / 2;
        double rY = sizeY / 2;
        double rZ = sizeZ / 2;
        // up
        vertex(pose, vc, u, v, x - rX, y + rY, z + rZ, 0, 1, 0);
        vertex(pose, vc, u, v, x + rX, y + rY, z + rZ, 0, 1, 0);
        vertex(pose, vc, u, v, x + rX, y + rY, z - rZ, 0, 1, 0);
        vertex(pose, vc, u, v, x - rX, y + rY, z - rZ, 0, 1, 0);
        // down
        vertex(pose, vc, u, v, x - rX, y - rY, z - rZ, 0, -1, 0);
        vertex(pose, vc, u, v, x + rX, y - rY, z - rZ, 0, -1, 0);
        vertex(pose, vc, u, v, x + rX, y - rY, z + rZ, 0, -1, 0);
        vertex(pose, vc, u, v, x - rX, y - rY, z + rZ, 0, -1, 0);
        // west
        vertex(pose, vc, u, v, x - rX, y - rY, z + rZ, -1, 0, 0);
        vertex(pose, vc, u, v, x - rX, y + rY, z + rZ, -1, 0, 0);
        vertex(pose, vc, u, v, x - rX, y + rY, z - rZ, -1, 0, 0);
        vertex(pose, vc, u, v, x - rX, y - rY, z - rZ, -1, 0, 0);
        // east
        vertex(pose, vc, u, v, x + rX, y - rY, z - rZ, 1, 0, 0);
        vertex(pose, vc, u, v, x + rX, y + rY, z - rZ, 1, 0, 0);
        vertex(pose, vc, u, v, x + rX, y + rY, z + rZ, 1, 0, 0);
        vertex(pose, vc, u, v, x + rX, y - rY, z + rZ, 1, 0, 0);
        // north
        vertex(pose, vc, u, v, x - rX, y - rY, z - rZ, 0, 0, -1);
        vertex(pose, vc, u, v, x - rX, y + rY, z - rZ, 0, 0, -1);
        vertex(pose, vc, u, v, x + rX, y + rY, z - rZ, 0, 0, -1);
        vertex(pose, vc, u, v, x + rX, y - rY, z - rZ, 0, 0, -1);
        // south
        vertex(pose, vc, u, v, x + rX, y - rY, z + rZ, 0, 0, 1);
        vertex(pose, vc, u, v, x + rX, y + rY, z + rZ, 0, 0, 1);
        vertex(pose, vc, u, v, x - rX, y + rY, z + rZ, 0, 0, 1);
        vertex(pose, vc, u, v, x - rX, y - rY, z + rZ, 0, 0, 1);
    }

    private void vertex(PoseStack.Pose pose, VertexConsumer vc, float u, float v, double px, double py, double pz,
        float nx, float ny, float nz) {
        vc.addVertex(pose, (float) px, (float) py, (float) pz)
            .setColor(colour)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }
}
