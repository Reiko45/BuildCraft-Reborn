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

import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;

import buildcraftreborn.api.transport.pluggable.IPlugDynamicRenderer;
import buildcraftreborn.lib.client.model.AdvModelCache;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.silicon.client.BCSiliconModels;
import buildcraftreborn.silicon.plug.PluggableGate;

/** Draws a gate's light, which glows while the gate is on. */
public enum PlugGateRenderer implements IPlugDynamicRenderer<PluggableGate> {
    INSTANCE;

    private static final AdvModelCache CACHE = new AdvModelCache(BCSiliconModels.GATE_DYNAMIC,
        PluggableGate.MODEL_VAR_INFO);

    public static void onModelBake() {
        CACHE.reset();
    }

    @Override
    public void render(PluggableGate gate, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        MutableQuad[] quads;
        // The variables are shared with the gate models baked on the chunk threads
        synchronized (BCSiliconModels.class) {
            gate.setClientModelVariables();
            if (gate.clientModelData.hasNoNodes()) {
                gate.clientModelData.setNodes(BCSiliconModels.GATE_DYNAMIC.createTickableNodes());
            }
            gate.clientModelData.refresh();
            quads = CACHE.getCutoutQuads();
        }
        int light = gate.logic.isOn ? LightTexture.pack(15, LightTexture.sky(combinedLight)) : combinedLight;
        VertexConsumer vc = buffers.getBuffer(RenderType.cutout());
        MutableQuad copy = new MutableQuad();
        for (MutableQuad q : quads) {
            copy.copyFrom(q);
            copy.multShade();
            copy.render(poseStack.last(), vc, light, combinedOverlay);
        }
    }
}
