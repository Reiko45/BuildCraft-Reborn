/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

/** Dispatches "detached renderer elements" - rendering that does not require a specific tile or entity in the world
 * (perhaps held item HUD elements).
 * <p>
 * 1.21: renderers get the pose stack (in camera space), a buffer source and the camera position, and are drawn after
 * translucent blocks. 1.12's "from player" and "from world origin" matrix types are gone: positions are made relative
 * to {@code camera}. Only the client uses it ({@link IDetachedRenderer} is referenced by common tile code, so this
 * isn't marked client-only). */
public enum DetachedRenderer {
    INSTANCE;

    @FunctionalInterface
    public interface IDetachedRenderer {
        /** @param camera The camera position: subtract it from world positions before drawing. */
        void render(Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers, Vec3 camera);
    }

    private final List<IDetachedRenderer> renders = new ArrayList<>();

    public void addRenderer(IDetachedRenderer renderer) {
        renders.add(renderer);
    }

    public void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_TRANSLUCENT_BLOCKS) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || renders.isEmpty()) {
            return;
        }
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        Vec3 camera = event.getCamera().getPosition();
        for (IDetachedRenderer render : renders) {
            render.render(player, partialTicks, event.getPoseStack(), buffers, camera);
        }
        buffers.endBatch();
    }
}
