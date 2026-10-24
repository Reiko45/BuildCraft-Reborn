/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.event.RenderLevelStageEvent;

import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.core.marker.volume.Addon;
import buildcraftreborn.core.marker.volume.ClientVolumeBoxes;
import buildcraftreborn.core.marker.volume.Lock;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.lib.client.render.laser.LaserBoxRenderer;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8.LaserType;

/** Draws every volume box of the client's level as a laser box, with its addons. */
public final class RenderVolumeBoxes {
    private RenderVolumeBoxes() {}

    public static void onRenderLevel(RenderLevelStageEvent event) {
        if (event.getStage() != RenderLevelStageEvent.Stage.AFTER_BLOCK_ENTITIES) {
            return;
        }
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer player = mc.player;
        if (mc.level == null || player == null || ClientVolumeBoxes.INSTANCE.volumeBoxes.isEmpty()) {
            return;
        }
        Vec3 camera = event.getCamera().getPosition();
        PoseStack pose = event.getPoseStack();
        float partialTicks = event.getPartialTick().getGameTimeDeltaPartialTick(false);
        MultiBufferSource.BufferSource buffers = mc.renderBuffers().bufferSource();
        VertexConsumer buffer = buffers.getBuffer(RenderType.cutout());

        mc.getProfiler().push("bc_volume_boxes");
        for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            LaserType type;
            if (volumeBox.isEditingBy(player)) {
                type = BuildCraftLaserManager.MARKER_VOLUME_SIGNAL;
            } else {
                type = volumeBox.getLockTargetsStream()
                    .filter(Lock.Target.TargetUsedByMachine.class::isInstance)
                    .map(target -> ((Lock.Target.TargetUsedByMachine) target).type)
                    .map(RenderVolumeBoxes::getLaserType)
                    .findFirst()
                    .orElse(BuildCraftLaserManager.MARKER_VOLUME_CONNECTED);
            }
            LaserBoxRenderer.renderLaserBox(volumeBox.box, type, false, pose, buffer, camera);
        }
        buffers.endBatch(RenderType.cutout());
        for (VolumeBox volumeBox : ClientVolumeBoxes.INSTANCE.volumeBoxes) {
            for (Addon addon : volumeBox.addons.values()) {
                IFastAddonRenderer<Addon> renderer = AddonRenderers.get(addon);
                if (renderer != null) {
                    renderer.renderAddonFast(addon, player, partialTicks, pose, buffers, camera);
                }
            }
        }
        buffers.endBatch(RenderType.cutout());
        buffers.endBatch(RenderType.translucent());
        mc.getProfiler().pop();
    }

    private static LaserType getLaserType(Lock.Target.TargetUsedByMachine.EnumType type) {
        return switch (type) {
            case STRIPES_WRITE -> BuildCraftLaserManager.STRIPES_WRITE;
            case STRIPES_READ -> BuildCraftLaserManager.STRIPES_READ;
        };
    }
}
