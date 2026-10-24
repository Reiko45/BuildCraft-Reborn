/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.render;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraftreborn.lib.debug.DebugRenderHelper;
import buildcraftreborn.lib.misc.VolumeUtil;
import buildcraftreborn.silicon.block.BlockLaser;
import buildcraftreborn.silicon.tile.TileLaser;

/** The laser's advanced debug view: the blocks in its search cone, green where it can see them and blue where it
 * can't. */
public class AdvDebuggerLaser implements IDetachedRenderer {
    private static final int COLOUR_VISIBLE = 0xFF_99_FF_99;
    private static final int COLOUR_NOT_VISIBLE = 0xFF_11_11_99;

    private final BlockPos pos;
    @Nullable
    private final Direction face;

    public AdvDebuggerLaser(TileLaser tile) {
        pos = tile.getBlockPos();
        BlockState state = tile.getBlockState();
        face = state.getBlock() instanceof BlockLaser laser ? state.getValue(laser.getFacingProperty()) : null;
    }

    @Override
    public void render(Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers, Vec3 camera) {
        if (face == null) {
            return;
        }
        VertexConsumer vc = DebugRenderHelper.buffer(buffers);
        VolumeUtil.iterateCone(player.level(), pos, face, TileLaser.TARGETING_RANGE, true,
            (world, start, p, visible) -> DebugRenderHelper.renderSmallCuboid(pose, vc, camera, p,
                visible ? COLOUR_VISIBLE : COLOUR_NOT_VISIBLE));
    }
}
