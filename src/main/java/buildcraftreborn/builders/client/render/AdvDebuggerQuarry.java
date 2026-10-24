/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.lib.chunkload.ChunkLoaderManager;
import buildcraftreborn.lib.client.render.DetachedRenderer.IDetachedRenderer;
import buildcraftreborn.lib.debug.DebugRenderHelper;

/** The quarry's advanced debug view: the chunks it keeps loaded, as see-through green columns. */
public class AdvDebuggerQuarry implements IDetachedRenderer {
    private static final int COLOUR_CHUNK = 0x55_99_FF_99;

    private final WeakReference<TileQuarry> tileReference;

    public AdvDebuggerQuarry(TileQuarry tile) {
        tileReference = new WeakReference<>(tile);
    }

    @Override
    public void render(Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers, Vec3 camera) {
        TileQuarry tile = tileReference.get();
        if (tile == null || !tile.frameBox.isInitialized()) {
            return;
        }
        Vec3 eyes = player.getEyePosition(partialTicks);
        List<ChunkPos> chunkPoses = new ArrayList<>(ChunkLoaderManager.getChunksToLoad(tile));
        // Furthest first, so the see-through boxes blend properly
        chunkPoses.sort(Comparator.comparingDouble(chunkPos -> -eyes.distanceTo(new Vec3(chunkPos.getMiddleBlockX(),
            eyes.y, chunkPos.getMiddleBlockZ()))));
        VertexConsumer vc = DebugRenderHelper.buffer(buffers);
        for (ChunkPos chunkPos : chunkPoses) {
            DebugRenderHelper.renderAABB(pose, vc, camera, new AABB(chunkPos.getMinBlockX() + 0.5D,
                tile.frameBox.min().getY() + 0.5D, chunkPos.getMinBlockZ() + 0.5D, chunkPos.getMaxBlockX() + 0.5D,
                tile.frameBox.max().getY() + 0.5D, chunkPos.getMaxBlockZ() + 0.5D), COLOUR_CHUNK);
        }
    }
}
