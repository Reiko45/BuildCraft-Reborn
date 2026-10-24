/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.render;

import java.util.Random;

import org.jetbrains.annotations.Nullable;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import buildcraftreborn.lib.BCLibConfig;

/** Draws small item stacks in the world (items travelling through pipes), a few copies for bigger stacks.
 * <p>
 * 1.21: draws through the item renderer with no display transform, at 0.3 scale, like 1.12's quad copying did. */
public final class ItemRenderUtil {
    private static final float SCALE = 0.30f;
    private static final Random MODEL_OFFSET_RANDOM = new Random(0);

    private ItemRenderUtil() {}

    /** @param x, y, z The centre of the stack, relative to the pose.
     * @param dir The direction the item travels in (it faces that way), or null for east. */
    public static void renderItemStack(PoseStack poseStack, MultiBufferSource buffers, double x, double y, double z,
        ItemStack stack, int stackCount, int light, @Nullable Direction dir, @Nullable Level level) {
        if (stack.isEmpty()) {
            return;
        }
        if (dir == null) {
            dir = Direction.EAST;
        }
        dir = BCLibConfig.itemRenderRotation().changeFacing(dir);

        int count = getStackModelCount(stackCount);
        if (count > 1) {
            setupModelOffsetRandom(stack);
        }
        for (int i = 0; i < count; i++) {
            poseStack.pushPose();
            poseStack.translate(x, y, z);
            if (i > 0) {
                float dx = (MODEL_OFFSET_RANDOM.nextFloat() * 2.0F - 1.0F) * 0.08F;
                float dy = (MODEL_OFFSET_RANDOM.nextFloat() * 2.0F - 1.0F) * 0.08F;
                float dz = (MODEL_OFFSET_RANDOM.nextFloat() * 2.0F - 1.0F) * 0.08F;
                poseStack.translate(dx, dy, dz);
            }
            rotateFromSouth(poseStack, dir);
            poseStack.scale(SCALE, SCALE, SCALE);
            Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.NONE, light,
                OverlayTexture.NO_OVERLAY, poseStack, buffers, level, 0);
            poseStack.popPose();
        }
    }

    /** Turns a model facing south so it faces the given way. */
    private static void rotateFromSouth(PoseStack poseStack, Direction dir) {
        switch (dir) {
            case NORTH -> poseStack.mulPose(Axis.YP.rotationDegrees(180));
            case EAST -> poseStack.mulPose(Axis.YP.rotationDegrees(90));
            case WEST -> poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            case UP -> poseStack.mulPose(Axis.XP.rotationDegrees(-90));
            case DOWN -> poseStack.mulPose(Axis.XP.rotationDegrees(90));
            default -> {}
        }
    }

    private static void setupModelOffsetRandom(ItemStack stack) {
        int seed = BuiltInRegistries.ITEM.getKey(stack.getItem()).hashCode();
        MODEL_OFFSET_RANDOM.setSeed(seed & 0x7F_FF_FF_FFL);
    }

    private static int getStackModelCount(int stackCount) {
        if (stackCount > 48) return 5;
        if (stackCount > 32) return 4;
        if (stackCount > 16) return 3;
        if (stackCount > 1) return 2;
        return 1;
    }
}
