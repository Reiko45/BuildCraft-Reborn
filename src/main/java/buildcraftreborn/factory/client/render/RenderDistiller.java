/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.render;

import java.util.EnumMap;
import java.util.Map;
import java.util.function.UnaryOperator;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.state.BlockState;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileDistiller_BC8;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraftreborn.lib.client.render.fluid.FluidSpriteType;
import buildcraftreborn.lib.client.render.model.CuboidRenderer;
import buildcraftreborn.lib.client.render.model.CuboidRenderer.SpriteRegion;
import buildcraftreborn.lib.fluid.FluidSmoother;
import buildcraftreborn.lib.fluid.FluidSmoother.FluidStackInterp;

/** Draws the distiller's two moving power blocks (which bob faster and change colour with the power going in) and the
 * fluid in its three tanks.
 * <p>
 * The power blocks are 1.12's {@code models/tiles/distiller.json} variable model, drawn directly: the same
 * {@code y1}/{@code y2} formulas and textures, with the animation state kept by {@link TileDistiller_BC8}. */
public class RenderDistiller implements BlockEntityRenderer<TileDistiller_BC8> {
    private static final Map<Direction, TankRenderSizes> TANK_SIZES = new EnumMap<>(Direction.class);

    private static final ResourceLocation[] POWER_SPRITES = {
        BuildCraftReborn.id("block/distiller/power_sprite_a"),
        BuildCraftReborn.id("block/distiller/power_sprite_b"),
        BuildCraftReborn.id("block/distiller/power_sprite_c"),
        BuildCraftReborn.id("block/distiller/power_sprite_d")
    };

    private static final float[][] POWER_BLOCK_UVS = new float[6][];

    static {
        Direction face = Direction.WEST;
        TankSize tankIn = new TankSize(0, 0, 4, 8, 16, 12).shrink(1 / 64.0);
        TankSize tankGasOut = new TankSize(8, 8, 0, 16, 16, 16).shrink(1 / 64.0);
        TankSize tankLiquidOut = new TankSize(8, 0, 0, 16, 8, 16).shrink(1 / 64.0);
        TankRenderSizes sizes = new TankRenderSizes(tankIn, tankGasOut, tankLiquidOut);
        for (int i = 0; i < 4; i++) {
            TANK_SIZES.put(face, sizes);
            face = face.getClockWise();
            sizes = sizes.rotateY();
        }

        POWER_BLOCK_UVS[Direction.DOWN.ordinal()] = new float[] { 4, 0, 12, 8 };
        POWER_BLOCK_UVS[Direction.UP.ordinal()] = new float[] { 4, 0, 12, 8 };
        POWER_BLOCK_UVS[Direction.NORTH.ordinal()] = new float[] { 4, 8, 12, 16 };
        POWER_BLOCK_UVS[Direction.SOUTH.ordinal()] = new float[] { 4, 8, 12, 16 };
        POWER_BLOCK_UVS[Direction.WEST.ordinal()] = new float[] { 0, 8, 4, 16 };
        POWER_BLOCK_UVS[Direction.EAST.ordinal()] = new float[] { 0, 8, 4, 16 };
    }

    public RenderDistiller(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileDistiller_BC8 tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, int packedOverlay) {
        BlockState state = tile.getLevel().getBlockState(tile.getBlockPos());
        if (state.getBlock() != BCFactoryBlocks.DISTILLER.get()) {
            return;
        }

        int combinedLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos());
        Direction face = state.getValue(BlockBCBase_Neptune.PROP_FACING);
        TankRenderSizes sizes = TANK_SIZES.get(face);

        renderPowerBlocks(tile, partialTicks, face, poseStack, buffers.getBuffer(RenderType.cutout()), combinedLight);

        VertexConsumer fluids = buffers.getBuffer(RenderType.translucent());
        renderTank(sizes.tankIn, tile.smoothedTankIn, combinedLight, partialTicks, poseStack, fluids);
        renderTank(sizes.tankOutGas, tile.smoothedTankGasOut, combinedLight, partialTicks, poseStack, fluids);
        renderTank(sizes.tankOutLiquid, tile.smoothedTankLiquidOut, combinedLight, partialTicks, poseStack, fluids);
    }

    private static void renderPowerBlocks(TileDistiller_BC8 tile, float partialTicks, Direction facing,
        PoseStack poseStack, VertexConsumer vc, int light) {
        double stateValue = tile.getModelState(partialTicks);
        double y1 = 1 - Math.abs((stateValue % 1) - 0.5) * 2;
        double y2 = 1 - Math.abs((stateValue <= 0.5 ? 0 : (stateValue - 0.5) % 1) - 0.5) * 2;

        SpriteRegion tex = powerTexture(tile.getModelPowerAverage(), TileDistiller_BC8.getModelPowerMax());

        poseStack.pushPose();
        // The model faces west; rotate it to the block's facing
        poseStack.translate(0.5, 0, 0.5);
        poseStack.mulPose(Axis.YP.rotationDegrees(Direction.WEST.toYRot() - facing.toYRot()));
        poseStack.translate(-0.5, 0, -0.5);

        PoseStack.Pose pose = poseStack.last();
        int steps = 0;
        for (Direction d = Direction.WEST; d != facing; d = d.getClockWise()) {
            steps++;
        }
        final int rotSteps = steps;
        UnaryOperator<Direction> shadeFace = d -> {
            if (d.getAxis() == Direction.Axis.Y) {
                return d;
            }
            for (int i = 0; i < rotSteps; i++) {
                d = d.getClockWise();
            }
            return d;
        };
        // Right
        float r = (float) (y1 * 12);
        CuboidRenderer.render(pose, vc, tex, light, -1, new float[] { 0, r, 12 }, new float[] { 8, r + 4, 16 },
            POWER_BLOCK_UVS, shadeFace);
        // Left
        float l = (float) (y2 * 12);
        CuboidRenderer.render(pose, vc, tex, light, -1, new float[] { 0, l, 0 }, new float[] { 8, l + 4, 4 },
            POWER_BLOCK_UVS, shadeFace);
        poseStack.popPose();
    }

    /** 1.12's {@code tex} variable: {@code #off}, or {@code #power_N} for N = power * 6 / max. */
    private static SpriteRegion powerTexture(long power, long powerMax) {
        if (power <= 0) {
            return region(0, false);
        }
        int index = (int) (power * 6 / powerMax);
        return switch (index) {
            case 0, 1 -> region(0, true);
            case 2 -> region(1, false);
            case 3 -> region(1, true);
            case 4 -> region(2, false);
            case 5 -> region(2, true);
            default -> region(3, false);
        };
    }

    private static SpriteRegion region(int sprite, boolean bottomHalf) {
        TextureAtlasSprite s = FluidRenderer.getBlockSprite(POWER_SPRITES[sprite]);
        return bottomHalf ? new SpriteRegion(s, 0, 8, 16, 16) : new SpriteRegion(s, 0, 0, 16, 8);
    }

    public static void renderTank(TankSize size, FluidSmoother tank, int combinedLight, float partialTicks,
        PoseStack poseStack, VertexConsumer vc) {
        FluidStackInterp fluid = tank.getFluidForRender(partialTicks);
        if (fluid == null || fluid.amount <= 0) {
            return;
        }
        int blockLight = fluid.fluid.getFluid().getFluidType().getLightLevel(fluid.fluid) & 0xF;
        combinedLight |= blockLight << 4;
        FluidRenderer.renderFluid(FluidSpriteType.STILL, fluid.fluid, fluid.amount, tank.getCapacity(), size.min,
            size.max, poseStack.last(), vc, combinedLight, null);
    }

    static class TankRenderSizes {
        final TankSize tankIn, tankOutGas, tankOutLiquid;

        public TankRenderSizes(TankSize tankIn, TankSize tankOutGas, TankSize tankOutLiquid) {
            this.tankIn = tankIn;
            this.tankOutGas = tankOutGas;
            this.tankOutLiquid = tankOutLiquid;
        }

        public TankRenderSizes rotateY() {
            return new TankRenderSizes(tankIn.rotateY(), tankOutGas.rotateY(), tankOutLiquid.rotateY());
        }
    }
}
