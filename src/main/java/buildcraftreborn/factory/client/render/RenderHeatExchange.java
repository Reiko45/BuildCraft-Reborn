/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.factory.client.render;

import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileHeatExchange;
import buildcraftreborn.factory.tile.TileHeatExchange.EnumProgressState;
import buildcraftreborn.factory.tile.TileHeatExchange.ExchangeSectionEnd;
import buildcraftreborn.factory.tile.TileHeatExchange.ExchangeSectionStart;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer;
import buildcraftreborn.lib.client.render.fluid.FluidRenderer.TankSize;
import buildcraftreborn.lib.client.render.fluid.FluidSpriteType;
import buildcraftreborn.lib.fluid.FluidSmoother;
import buildcraftreborn.lib.fluid.FluidSmoother.FluidStackInterp;
import buildcraftreborn.lib.misc.VecUtil;

/** Draws a heat exchanger's fluids: the four tanks at its start and end, and while it runs, the two fluids flowing
 * past each other through the middle sections. Everything is drawn by the start section's block entity. */
public class RenderHeatExchange implements BlockEntityRenderer<TileHeatExchange> {
    private static final Map<Direction, TankSideData> TANK_SIDES = new EnumMap<>(Direction.class);
    private static final TankSize TANK_BOTTOM, TANK_TOP;

    static {
        double s = 1 / 64.0;
        TANK_BOTTOM = new TankSize(2, 0, 2, 14, 2, 14).shrink(s, 0, s);
        TANK_TOP = new TankSize(2, 14, 2, 14, 16, 14).shrink(s, 0, s);
        TankSize start = new TankSize(0, 4, 4, 2, 12, 12).shrink(0, s, s);
        TankSize end = new TankSize(14, 4, 4, 16, 12, 12).shrink(0, s, s);
        TankSideData sides = new TankSideData(start, end);
        Direction face = Direction.EAST;
        for (int i = 0; i < 4; i++) {
            TANK_SIDES.put(face, sides);
            face = face.getClockWise();
            sides = sides.rotateY();
        }
    }

    record TankSideData(TankSize start, TankSize end) {
        TankSideData rotateY() {
            return new TankSideData(start.rotateY(), end.rotateY());
        }
    }

    public RenderHeatExchange(BlockEntityRendererProvider.Context context) {}

    @Override
    public void render(TileHeatExchange tile, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int packedLight, int packedOverlay) {
        if (!tile.isStart() || tile.getLevel() == null) {
            return;
        }

        ExchangeSectionStart section = (ExchangeSectionStart) tile.getSection();
        ExchangeSectionEnd sectionEnd = section.getEndSection();

        BlockState state = tile.getCurrentStateForBlock(BCFactoryBlocks.HEAT_EXCHANGE.get());
        if (state == null) {
            return;
        }

        int combinedLight = LevelRenderer.getLightColor(tile.getLevel(), tile.getBlockPos());
        VertexConsumer vc = buffers.getBuffer(RenderType.translucent());

        Direction face = state.getValue(BlockBCBase_Neptune.PROP_FACING).getCounterClockWise();
        TankSideData sideTank = TANK_SIDES.get(face);

        renderTank(TANK_BOTTOM, section.smoothedTankInput, combinedLight, partialTicks, poseStack, vc);
        renderTank(sideTank.start, section.smoothedTankOutput, combinedLight, partialTicks, poseStack, vc);

        int middles = section.middleCount;
        if (sectionEnd != null) {
            BlockPos diff = sectionEnd.getTile().getBlockPos().subtract(tile.getBlockPos());
            poseStack.pushPose();
            poseStack.translate(diff.getX(), diff.getY(), diff.getZ());
            renderTank(TANK_TOP, sectionEnd.smoothedTankOutput, combinedLight, partialTicks, poseStack, vc);
            renderTank(sideTank.end, sectionEnd.smoothedTankInput, combinedLight, partialTicks, poseStack, vc);
            poseStack.popPose();
        }

        if (middles > 0 && sectionEnd != null) {
            EnumProgressState progressState = section.getProgressState();
            double progress = section.getProgress(partialTicks);
            if (progress > 0) {
                double length = middles + 2 - 4 / 16.0 - 0.02;
                double p0 = 2 / 16.0 + 0.01;
                double p1 = p0 + length - 0.01;
                double progressStart = p0;
                double progressEnd = p0 + length * progress;

                boolean flip = progressState == EnumProgressState.PREPARING;
                flip ^= face.getAxisDirection() == AxisDirection.NEGATIVE;

                if (flip) {
                    progressStart = p1 - length * progress;
                    progressEnd = p1;
                }
                BlockPos diff = BlockPos.ZERO;
                if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
                    diff = diff.relative(face, middles + 1);
                }
                double otherStart = flip ? p0 : p1 - length * progress;
                double otherEnd = flip ? p0 + length * progress : p1;
                Vec3 vDiff = Vec3.atLowerCornerOf(diff);
                long gameTime = tile.getLevel().getGameTime();
                renderFlow(vDiff, face, poseStack, vc, progressStart + 0.01, progressEnd - 0.01,
                    sectionEnd.smoothedTankInput.getFluidForRender(), 4, gameTime, partialTicks, combinedLight);
                renderFlow(vDiff, face.getOpposite(), poseStack, vc, otherStart, otherEnd,
                    section.smoothedTankInput.getFluidForRender(), 2, gameTime, partialTicks, combinedLight);
            }
        }
    }

    private static void renderTank(TankSize size, FluidSmoother tank, int combinedLight, float partialTicks,
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

    private static void renderFlow(Vec3 diff, Direction face, PoseStack poseStack, VertexConsumer vc, double s,
        double e, @Nullable FluidStack fluid, int point, long gameTime, float partialTicks, int light) {
        if (fluid == null || fluid.isEmpty()) {
            return;
        }
        double offset = ((gameTime % 31) + partialTicks) % 31 / 31.0;
        if (face.getAxisDirection() == AxisDirection.NEGATIVE) {
            offset = -offset;
            face = face.getOpposite();
        }
        Vec3 dirVec = Vec3.atLowerCornerOf(face.getNormal());
        double ds = (point + 0.1) / 16.0;
        Vec3 vs = new Vec3(ds, ds, ds);
        Vec3 ve = new Vec3(1 - ds, 1 - ds, 1 - ds);
        diff = diff.subtract(VecUtil.scale(dirVec, offset));
        s += offset;
        e += offset;
        if (s < 0) {
            s++;
            e++;
            diff = diff.subtract(dirVec);
        }
        for (int i = 0; i <= e; i++) {
            Vec3 d = diff;
            diff = diff.add(dirVec);
            if (i < s - 1) {
                continue;
            }

            double s1 = s < i ? 0 : (s % 1);
            double e1 = e > i + 1 ? 1 : (e % 1);
            vs = VecUtil.replaceValue(vs, face.getAxis(), s1);
            ve = VecUtil.replaceValue(ve, face.getAxis(), e1);
            boolean[] sides = new boolean[6];
            Arrays.fill(sides, true);
            if (s < i) {
                sides[face.getOpposite().ordinal()] = false;
            }
            if (e > i + 1) {
                sides[face.ordinal()] = false;
            }
            poseStack.pushPose();
            poseStack.translate(d.x, d.y, d.z);
            FluidRenderer.renderFluid(FluidSpriteType.FROZEN, fluid, 1, 1, vs, ve, poseStack.last(), vc, light,
                sides);
            poseStack.popPose();
        }
    }

    @Override
    public boolean shouldRenderOffScreen(TileHeatExchange tile) {
        return tile.isStart();
    }

    @Override
    public AABB getRenderBoundingBox(TileHeatExchange tile) {
        AABB box = new AABB(tile.getBlockPos());
        if (tile.getSection() instanceof ExchangeSectionStart start && start.getEndSection() != null) {
            box = box.minmax(new AABB(start.getEndSection().getTile().getBlockPos()));
        }
        return box;
    }
}
