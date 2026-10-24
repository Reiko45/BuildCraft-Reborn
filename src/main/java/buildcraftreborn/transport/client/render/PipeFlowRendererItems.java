/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.client.render;

import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import org.joml.Vector3f;

import buildcraftreborn.api.transport.pipe.IPipeFlowRenderer;
import buildcraftreborn.lib.client.model.ModelUtil;
import buildcraftreborn.lib.client.model.ModelUtil.UvFaceData;
import buildcraftreborn.lib.client.model.MutableQuad;
import buildcraftreborn.lib.client.render.ItemRenderUtil;
import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.transport.client.BCTransportClientSprites;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;
import buildcraftreborn.transport.pipe.flow.TravellingItem;

/** Draws the items travelling through a pipe, with a coloured box around painted ones. */
public enum PipeFlowRendererItems implements IPipeFlowRenderer<PipeFlowItems> {
    INSTANCE;

    private static final MutableQuad[] COLOURED_QUADS = new MutableQuad[6];

    static {
        Vector3f center = new Vector3f();
        Vector3f radius = new Vector3f(0.2f, 0.2f, 0.2f);
        UvFaceData uvs = new UvFaceData(0, 0, 1, 1);
        for (Direction face : Direction.values()) {
            MutableQuad q = ModelUtil.createFace(face, center, radius, uvs);
            q.setCalculatedDiffuse();
            COLOURED_QUADS[face.ordinal()] = q;
        }
    }

    @Override
    public void render(PipeFlowItems flow, float partialTicks, PoseStack poseStack, MultiBufferSource buffers,
        int combinedLight, int combinedOverlay) {
        Level level = flow.pipe.getHolder().getPipeWorld();
        long now = level.getGameTime();

        List<TravellingItem> toRender = flow.getAllItemsForRender();
        VertexConsumer boxes = null;
        TextureAtlasSprite boxSprite = null;

        for (TravellingItem item : toRender) {
            Vec3 pos = item.getRenderPosition(BlockPos.ZERO, now, partialTicks, flow);

            ItemStack stack = item.clientStack;
            if (!stack.isEmpty()) {
                ItemRenderUtil.renderItemStack(poseStack, buffers, pos.x, pos.y, pos.z, stack, item.stackSize,
                    combinedLight, item.getRenderDirection(now, partialTicks), level);
            }
            if (item.colour != null) {
                if (boxes == null) {
                    boxes = buffers.getBuffer(RenderType.translucent());
                    boxSprite = BCTransportClientSprites.COLOUR_ITEM_BOX.getSprite();
                }
                int col = ColourUtil.getLightHex(item.colour);
                int r = (col >> 16) & 0xFF;
                int g = (col >> 8) & 0xFF;
                int b = col & 0xFF;
                poseStack.pushPose();
                poseStack.translate(pos.x, pos.y, pos.z);
                for (MutableQuad q : COLOURED_QUADS) {
                    MutableQuad q2 = new MutableQuad(q);
                    q2.texFromSprite(boxSprite);
                    q2.multColouri(r, g, b, 255);
                    q2.render(poseStack.last(), boxes, combinedLight, combinedOverlay);
                }
                poseStack.popPose();
            }
        }
    }
}
