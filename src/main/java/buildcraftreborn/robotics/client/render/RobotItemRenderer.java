/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.item.ItemRobot;

/** The robot item: the robot's body with its board's texture, its lights showing how charged it is. */
public class RobotItemRenderer extends BlockEntityWithoutLevelRenderer {
    public static final RobotItemRenderer INSTANCE = new RobotItemRenderer();

    public static final IClientItemExtensions EXTENSIONS = new IClientItemExtensions() {
        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return INSTANCE;
        }
    };

    private RobotItemRenderer() {
        super(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }

    @Override
    public void renderByItem(ItemStack stack, ItemDisplayContext context, PoseStack poseStack,
        MultiBufferSource buffers, int light, int overlay) {
        poseStack.pushPose();
        poseStack.translate(0.5, 0.5, 0.5);
        boolean programmed = ItemRobot.hasBoard(stack);
        float charge = (float) ItemRobot.getPower(stack) / EntityRobotBase.MAX_POWER;
        RobotModels.renderBody(RobotModels.getItemBody(), poseStack, buffers,
            ItemRobot.getRobotNBT(stack).getRobotTexture(), light, overlay, -1, charge, programmed);
        poseStack.popPose();
    }
}
