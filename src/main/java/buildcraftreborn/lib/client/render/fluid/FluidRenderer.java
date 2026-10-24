/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render.fluid;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.level.BlockAndTintGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.extensions.common.IClientFluidTypeExtensions;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.IFluidTank;

import buildcraftreborn.lib.misc.MathUtil;
import buildcraftreborn.lib.misc.VecUtil;

/** Can render 3D fluid cuboid's, up to 1x1x1 in size. Note that they *must* be contained within the 1x1x1 block space
 * - you can't use this to render off large multiblocks. Not thread safe: only call this from the main client thread.
 * <p>
 * 1.21: writes to a {@link VertexConsumer} (normally {@code RenderType.translucent()} from the block entity renderer's
 * buffer source) with a {@link PoseStack.Pose}, instead of a {@code BufferBuilder} with a translation. The light
 * value is passed in rather than set on a shared vertex. Sprites come from {@link IClientFluidTypeExtensions}; the
 * average colours used by GUIs are computed from the stitched sprite the first time they are asked for. */
public class FluidRenderer {
    private static final boolean[] DEFAULT_FACES = { true, true, true, true, true, true };

    private static final Map<ResourceLocation, Integer> SPRITE_AVG_COLOURS = new HashMap<>();

    // Cached fields that prevent lots of arguments on most methods
    private static VertexConsumer vc;
    private static PoseStack.Pose pose;
    private static TextureAtlasSprite sprite;
    private static TexMap texmap;
    private static int colour;
    private static int light;
    private static float nx, ny, nz;
    private static double xTexDiff, yTexDiff, zTexDiff;

    /** Clears cached sprite colours. Called when the block atlas is re-stitched. */
    public static void clearCaches() {
        SPRITE_AVG_COLOURS.clear();
    }

    /** Renders a fluid cuboid. The cube shouldn't cross over any {@literal 0->1} boundary (so the cube must be
     * contained within a block).
     *
     * @see #renderFluid(FluidSpriteType, FluidStack, double, double, Vec3, Vec3, PoseStack.Pose, VertexConsumer, int,
     *      boolean[]) */
    public static void renderFluid(FluidSpriteType type, IFluidTank tank, Vec3 min, Vec3 max, PoseStack.Pose pose,
        VertexConsumer vc, int light, @Nullable boolean[] sideRender) {
        FluidStack fluid = tank.getFluid();
        renderFluid(type, fluid, fluid.getAmount(), tank.getCapacity(), min, max, pose, vc, light, sideRender);
    }

    /** Render's a fluid cuboid. The cube shouldn't cross over any {@literal 0->1} boundary (so the cube must be
     * contained within a block).
     *
     * @param type The type of sprite to use. See {@link FluidSpriteType} for more details.
     * @param fluid The stack that represents the fluid to render. Note that the amount from the stack is NOT used.
     * @param amount The actual amount of fluid in the stack. Is a "double" rather than an "int" as then you can
     *            interpolate between frames.
     * @param cap The maximum amount of fluid that could be in the stack. Usually the capacity of the tank.
     * @param min The minimum coordinate that the tank should be rendered from
     * @param max The maximum coordinate that the tank will be rendered to.
     * @param light The packed light value (see {@link LightTexture#pack(int, int)}).
     * @param sideRender A size 6 boolean array that determines if the face will be rendered. If it is null then all
     *            faces will be rendered. The indexes are determined by what {@link Direction#ordinal()} returns. */
    public static void renderFluid(FluidSpriteType type, @Nullable FluidStack fluid, double amount, double cap, Vec3 min,
        Vec3 max, PoseStack.Pose poseIn, VertexConsumer vcIn, int lightIn, @Nullable boolean[] sideRender) {
        if (fluid == null || fluid.isEmpty() || amount <= 0) {
            return;
        }
        if (sideRender == null) {
            sideRender = DEFAULT_FACES;
        }

        double height = Mth.clamp(amount / cap, 0, 1);
        final Vec3 realMin, realMax;
        if (isGaseous(fluid)) {
            realMin = VecUtil.replaceValue(min, Axis.Y, MathUtil.interp(1 - height, min.y, max.y));
            realMax = max;
        } else {
            realMin = min;
            realMax = VecUtil.replaceValue(max, Axis.Y, MathUtil.interp(height, min.y, max.y));
        }

        vc = vcIn;
        pose = poseIn;
        light = lightIn;

        if (type == null) {
            type = FluidSpriteType.STILL;
        }
        sprite = getFluidSprite(type, fluid);

        final double xs = realMin.x;
        final double ys = realMin.y;
        final double zs = realMin.z;

        final double xb = realMax.x;
        final double yb = realMax.y;
        final double zb = realMax.z;

        if (type == FluidSpriteType.FROZEN) {
            xTexDiff = min.x > 1 || min.x < 0 ? Math.floor(min.x) : 0;
            yTexDiff = min.y > 1 || min.y < 0 ? Math.floor(min.y) : 0;
            zTexDiff = min.z > 1 || min.z < 0 ? Math.floor(min.z) : 0;
        } else {
            xTexDiff = 0;
            yTexDiff = 0;
            zTexDiff = 0;
        }

        colour = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);

        texmap = TexMap.XZ;
        if (sideRender[Direction.UP.ordinal()]) {
            normal(0, 1, 0);
            vertex(xs, yb, zb);
            vertex(xb, yb, zb);
            vertex(xb, yb, zs);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.DOWN.ordinal()]) {
            normal(0, -1, 0);
            vertex(xs, ys, zs);
            vertex(xb, ys, zs);
            vertex(xb, ys, zb);
            vertex(xs, ys, zb);
        }

        texmap = TexMap.ZY;
        if (sideRender[Direction.WEST.ordinal()]) {
            normal(-1, 0, 0);
            vertex(xs, ys, zs);
            vertex(xs, ys, zb);
            vertex(xs, yb, zb);
            vertex(xs, yb, zs);
        }

        if (sideRender[Direction.EAST.ordinal()]) {
            normal(1, 0, 0);
            vertex(xb, yb, zs);
            vertex(xb, yb, zb);
            vertex(xb, ys, zb);
            vertex(xb, ys, zs);
        }

        texmap = TexMap.XY;
        if (sideRender[Direction.NORTH.ordinal()]) {
            normal(0, 0, -1);
            vertex(xs, yb, zs);
            vertex(xb, yb, zs);
            vertex(xb, ys, zs);
            vertex(xs, ys, zs);
        }

        if (sideRender[Direction.SOUTH.ordinal()]) {
            normal(0, 0, 1);
            vertex(xs, ys, zb);
            vertex(xb, ys, zb);
            vertex(xb, yb, zb);
            vertex(xs, yb, zb);
        }

        sprite = null;
        texmap = null;
        vc = null;
        pose = null;
    }

    public static boolean isGaseous(FluidStack fluid) {
        return fluid.getFluid().getFluidType().isLighterThanAir();
    }

    public static TextureAtlasSprite getFluidSprite(FluidSpriteType type, FluidStack fluid) {
        IClientFluidTypeExtensions ext = IClientFluidTypeExtensions.of(fluid.getFluid());
        ResourceLocation location = type == FluidSpriteType.FLOWING ? ext.getFlowingTexture(fluid)
            : ext.getStillTexture(fluid);
        return getBlockSprite(location);
    }

    public static TextureAtlasSprite getBlockSprite(@Nullable ResourceLocation location) {
        if (location == null) {
            location = net.minecraft.client.renderer.texture.MissingTextureAtlasSprite.getLocation();
        }
        return Minecraft.getInstance().getTextureAtlas(InventoryMenu.BLOCK_ATLAS).apply(location);
    }

    /** @return The average colour of the fluid's still sprite multiplied by its tint (ARGB, alpha always 0xFF), or
     *         -1 if the sprite has no pixels. */
    public static int getAverageFluidColour(FluidStack fluid) {
        if (fluid.isEmpty()) {
            return -1;
        }
        TextureAtlasSprite still = getFluidSprite(FluidSpriteType.STILL, fluid);
        int avgSprite = SPRITE_AVG_COLOURS.computeIfAbsent(still.contents().name(), k -> computeAverage(still));
        if (avgSprite == -1) {
            return -1;
        }
        int tint = IClientFluidTypeExtensions.of(fluid.getFluid()).getTintColor(fluid);
        int r = ((avgSprite >> 16) & 0xFF) * ((tint >> 16) & 0xFF) / 255;
        int g = ((avgSprite >> 8) & 0xFF) * ((tint >> 8) & 0xFF) / 255;
        int b = (avgSprite & 0xFF) * (tint & 0xFF) / 255;
        return 0xFF_00_00_00 | (r << 16) | (g << 8) | b;
    }

    private static int computeAverage(TextureAtlasSprite sprite) {
        NativeImage image = sprite.contents().getOriginalImage();
        long r = 0, g = 0, b = 0;
        long count = 0;
        for (int y = 0; y < image.getHeight(); y++) {
            for (int x = 0; x < image.getWidth(); x++) {
                int abgr = image.getPixelRGBA(x, y);
                r += abgr & 0xFF;
                g += (abgr >> 8) & 0xFF;
                b += (abgr >> 16) & 0xFF;
                count++;
            }
        }
        if (count == 0) {
            return -1;
        }
        return 0xFF_00_00_00 | ((int) (r / count) << 16) | ((int) (g / count) << 8) | (int) (b / count);
    }

    /** Packed light for a fluid drawn at the given position: the world light there, with the block light raised to
     * at least the fluid's own light level (1.12: {@code world.getCombinedLight(pos, fluid.getLuminosity())}). */
    public static int getCombinedLight(BlockAndTintGetter world, BlockPos pos, int minBlockLight) {
        int sky = world.getBrightness(LightLayer.SKY, pos);
        int block = Math.max(world.getBrightness(LightLayer.BLOCK, pos), minBlockLight);
        return LightTexture.pack(block, sky);
    }

    public static int getCombinedLight(Level world, BlockPos pos, FluidStack fluid) {
        int lum = fluid.isEmpty() ? 0 : fluid.getFluid().getFluidType().getLightLevel(fluid);
        return getCombinedLight(world, pos, lum);
    }

    private static void normal(float x, float y, float z) {
        nx = x;
        ny = y;
        nz = z;
    }

    /** Helper function to add a vertex. */
    private static void vertex(double x, double y, double z) {
        texmap.apply(x - xTexDiff, y - yTexDiff, z - zTexDiff);
        vc.addVertex(pose, (float) x, (float) y, (float) z)
            .setColor(colour)
            .setUv(u, v)
            .setOverlay(OverlayTexture.NO_OVERLAY)
            .setLight(light)
            .setNormal(pose, nx, ny, nz);
    }

    private static float u, v;

    /** Used to keep track of what position maps to what texture co-ord.
     * <p>
     * For example XY maps X to U and Y to V, and ignores Z */
    private enum TexMap {
        XY(true, true),
        XZ(true, false),
        ZY(false, true);

        /** If true, then X maps to U. Otherwise Z maps to U. */
        private final boolean ux;
        /** If true, then Y maps to V. Otherwise Z maps to V. */
        private final boolean vy;

        TexMap(boolean ux, boolean vy) {
            this.ux = ux;
            this.vy = vy;
        }

        /** Changes the vertex's texture co-ord to be the same as the position, for that face. (Uses {@link #ux} and
         * {@link #vy} to determine how they are mapped). */
        private void apply(double x, double y, double z) {
            double realu = ux ? x : z;
            double realv = vy ? y : z;
            u = sprite.getU((float) realu);
            v = sprite.getV((float) realv);
        }
    }

    /** The region of a block that a tank's fluid fills, in block coordinates (0 to 1). */
    public static class TankSize {
        public final Vec3 min;
        public final Vec3 max;

        public TankSize(int sx, int sy, int sz, int ex, int ey, int ez) {
            this(new Vec3(sx, sy, sz).scale(1 / 16.0), new Vec3(ex, ey, ez).scale(1 / 16.0));
        }

        public TankSize(Vec3 min, Vec3 max) {
            this.min = min;
            this.max = max;
        }

        public TankSize shrink(double by) {
            return shrink(by, by, by);
        }

        public TankSize shrink(double x, double y, double z) {
            return new TankSize(min.add(x, y, z), max.subtract(x, y, z));
        }

        public TankSize shink(Vec3 by) {
            return shrink(by.x, by.y, by.z);
        }

        public TankSize rotateY() {
            Vec3 _min = rotateY(min);
            Vec3 _max = rotateY(max);
            return new TankSize(VecUtil.min(_min, _max), VecUtil.max(_min, _max));
        }

        private static Vec3 rotateY(Vec3 vec) {
            return new Vec3(1 - vec.z, vec.y, vec.x);
        }
    }
}
