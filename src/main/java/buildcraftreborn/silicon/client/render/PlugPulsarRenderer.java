/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client.render;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import buildcraftreborn.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraftreborn.lib.client.model.AdvModelCache;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.silicon.client.BCSiliconModels;
import buildcraftreborn.silicon.plug.PluggablePulsar;

/** Draws the pulsar's moving part. */
public enum PlugPulsarRenderer implements IPlugDynamicRenderer<PluggablePulsar> {
    INSTANCE;

    private static final AdvModelCache CACHE = new AdvModelCache(BCSiliconModels.PULSAR_DYNAMIC,
        PluggablePulsar.MODEL_VAR_INFO);

    public static void onModelBake() {
        CACHE.reset();
    }

    @Override
    public void render(PluggablePulsar pulsar, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        if (pulsar.clientModelData.hasNoNodes()) {
            pulsar.clientModelData.setNodes(BCSiliconModels.PULSAR_DYNAMIC.createTickableNodes());
        }
        pulsar.setModelVariables(partialTicks);
        pulsar.clientModelData.refresh();
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());
        for (MutableQuad q : CACHE.getCutoutQuads()) {
            q.render(poseStack.last(), vc, combinedLight, combinedOverlay);
        }
    }
}
