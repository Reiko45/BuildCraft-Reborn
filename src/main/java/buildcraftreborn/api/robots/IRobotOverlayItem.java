/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemStack;

/** An item that can be worn by a robot (put on it by right clicking) and draws something over its body.
 * <p>
 * 1.21: {@link #renderRobotOverlay} draws with a pose stack and buffers instead of binding textures, and is only
 * called on the client. */
public interface IRobotOverlayItem {
    boolean isValidRobotOverlay(ItemStack stack);

    /** Draws the overlay. The pose is centred on the robot's body, which is a cube from -0.25 to 0.25 on each axis
     * (one unit is one block), facing south. */
    void renderRobotOverlay(ItemStack stack, PoseStack poseStack, MultiBufferSource buffers, int packedLight);
}
