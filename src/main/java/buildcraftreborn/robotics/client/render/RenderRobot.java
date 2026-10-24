/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.client.render;

import java.util.Map;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;

import net.minecraft.client.Minecraft;
import net.minecraft.client.model.SkullModelBase;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.blockentity.SkullBlockRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.armortrim.ArmorTrim;
import net.minecraft.world.level.block.AbstractSkullBlock;
import net.minecraft.world.level.block.SkullBlock;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.client.ClientHooks;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRobotOverlayItem;
import buildcraftreborn.core.client.BuildCraftLaserManager;
import buildcraftreborn.lib.client.render.laser.LaserData_BC8;
import buildcraftreborn.lib.client.render.laser.LaserRenderer_BC8;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Draws a robot as 7.1 did: its body (lit up while it's awake), the four items it carries floating on top, the tool it
 * holds pointing where it works, its laser, and what it wears. */
public class RenderRobot extends EntityRenderer<EntityRobot> {
    private static final float[][] ITEM_OFFSETS = { { -0.125F, -0.125F }, { 0.125F, -0.125F }, { 0.125F, 0.125F },
        { -0.125F, 0.125F } };

    private final ModelPart body;
    private final ModelPart helmet;
    private final Map<SkullBlock.Type, SkullModelBase> skullModels;
    private final ItemRenderer itemRenderer;

    public RenderRobot(EntityRendererProvider.Context context) {
        super(context);
        body = context.bakeLayer(RobotModels.BODY);
        helmet = context.bakeLayer(RobotModels.HELMET);
        skullModels = SkullBlockRenderer.createSkullRenderers(context.getModelSet());
        itemRenderer = context.getItemRenderer();
        shadowRadius = 0;
    }

    @Override
    public ResourceLocation getTextureLocation(EntityRobot robot) {
        return robot.getTexture();
    }

    @Override
    public void render(EntityRobot robot, float entityYaw, float partialTicks, PoseStack poseStack,
        MultiBufferSource buffers, int light) {
        if (robot.laserVisible) {
            Vec3 head = robot.getPosition(partialTicks);
            LaserData_BC8 laser = new LaserData_BC8(BuildCraftLaserManager.POWER_MED, head, robot.laserTail, 1 / 16D);
            LaserRenderer_BC8.renderLaser(laser, poseStack, buffers.getBuffer(RenderType.cutout()), head);
        }

        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(-Mth.rotLerp(partialTicks, robot.prevRenderYaw, robot.renderYaw)));

        for (int i = 0; i < EntityRobot.INVENTORY_SIZE; i++) {
            ItemStack stack = robot.getStackInSlot(i);
            if (!stack.isEmpty()) {
                poseStack.pushPose();
                poseStack.translate(ITEM_OFFSETS[i][0], 0.28F, ITEM_OFFSETS[i][1]);
                poseStack.scale(0.5F, 0.5F, 0.5F);
                itemRenderer.renderStatic(stack, ItemDisplayContext.GROUND, light, OverlayTexture.NO_OVERLAY,
                    poseStack, buffers, robot.level(), robot.getId() + i);
                poseStack.popPose();
            }
        }

        if (!robot.itemInUse.isEmpty()) {
            renderItemInUse(robot, poseStack, buffers, light);
        }

        float charge = (float) robot.getBattery().getStored() / EntityRobotBase.MAX_POWER;
        int colour = robot.hurtTime > 0 ? 0xFF_FF_99_99 : -1;
        poseStack.pushPose();
        if (robot.hurtTime > 0) {
            poseStack.mulPose(Axis.ZP.rotationDegrees(robot.hurtTime * 0.01F));
        }
        RobotModels.renderBody(body, poseStack, buffers, robot.getTexture(), light, OverlayTexture.NO_OVERLAY, colour,
            charge, !robot.isAsleep());
        poseStack.popPose();

        for (ItemStack wearable : robot.getWearables()) {
            renderWearable(robot, wearable, poseStack, buffers, light);
        }

        poseStack.popPose();
        super.render(robot, entityYaw, partialTicks, poseStack, buffers, light);
    }

    /** The tool sticks out of the robot's side, aimed at what it works on, and swings while it's in use. */
    private void renderItemInUse(EntityRobot robot, PoseStack poseStack, MultiBufferSource buffers, int light) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.ZP.rotationDegrees(robot.itemAimPitch));
        if (robot.itemActive) {
            long now = System.currentTimeMillis();
            robot.itemActiveStage = (robot.itemActiveStage + (now - robot.lastUpdateTime) / 10F) % 45;
            robot.lastUpdateTime = now;
            poseStack.mulPose(Axis.ZP.rotationDegrees(robot.itemActiveStage));
        }
        poseStack.translate(-0.4F, 0, 0);
        poseStack.mulPose(Axis.YP.rotationDegrees(-45F + 180F));
        poseStack.scale(0.8F, 0.8F, 0.8F);
        itemRenderer.renderStatic(robot, robot.itemInUse, ItemDisplayContext.THIRD_PERSON_RIGHT_HAND, false,
            poseStack, buffers, robot.level(), light, OverlayTexture.NO_OVERLAY, robot.getId());
        poseStack.popPose();
    }

    private void renderWearable(EntityRobot robot, ItemStack wearable, PoseStack poseStack, MultiBufferSource buffers,
        int light) {
        if (wearable.getItem() instanceof IRobotOverlayItem overlay) {
            overlay.renderRobotOverlay(wearable, poseStack, buffers, light);
        } else if (wearable.getItem() instanceof ArmorItem armor) {
            poseStack.pushPose();
            poseStack.scale(1.0125F, 1.0125F, 1.0125F);
            poseStack.translate(0, -0.25F, 0);
            poseStack.mulPose(Axis.ZP.rotationDegrees(180));
            poseStack.mulPose(Axis.YP.rotationDegrees(-90));
            renderHelmet(robot, wearable, armor, poseStack, buffers, light);
            poseStack.popPose();
        } else if (wearable.getItem() instanceof BlockItem blockItem
            && blockItem.getBlock() instanceof AbstractSkullBlock skull) {
            SkullModelBase model = skullModels.get(skull.getType());
            if (model != null) {
                poseStack.pushPose();
                poseStack.scale(1.0125F, 1.0125F, 1.0125F);
                poseStack.translate(-0.5F, -0.25F, -0.5F);
                RenderType type = SkullBlockRenderer.getRenderType(skull.getType(),
                    wearable.get(DataComponents.PROFILE));
                SkullBlockRenderer.renderSkull(null, -90, 0, poseStack, buffers, light, model, type);
                poseStack.popPose();
            }
        }
    }

    /** As vanilla draws a helmet (each layer tinted, then the trim and the glint), but on 7.1's head-sized box. */
    private void renderHelmet(EntityRobot robot, ItemStack stack, ArmorItem armor, PoseStack poseStack,
        MultiBufferSource buffers, int light) {
        ArmorMaterial material = armor.getMaterial().value();
        IClientItemExtensions extensions = IClientItemExtensions.of(stack);
        int fallback = extensions.getDefaultDyeColor(stack);
        for (int i = 0; i < material.layers().size(); i++) {
            ArmorMaterial.Layer layer = material.layers().get(i);
            int colour = extensions.getArmorLayerTintColor(stack, robot, layer, i, fallback);
            if (colour != 0) {
                ResourceLocation texture = ClientHooks.getArmorTexture(robot, stack, layer, false, EquipmentSlot.HEAD);
                helmet.render(poseStack, buffers.getBuffer(RenderType.armorCutoutNoCull(texture)), light,
                    OverlayTexture.NO_OVERLAY, colour);
            }
        }
        ArmorTrim trim = stack.get(DataComponents.TRIM);
        if (trim != null) {
            TextureAtlas atlas = Minecraft.getInstance().getModelManager().getAtlas(Sheets.ARMOR_TRIMS_SHEET);
            TextureAtlasSprite sprite = atlas.getSprite(trim.outerTexture(armor.getMaterial()));
            helmet.render(poseStack, sprite.wrap(buffers.getBuffer(Sheets.armorTrimsSheet(trim.pattern().value()
                .decal()))), light, OverlayTexture.NO_OVERLAY);
        }
        if (stack.hasFoil()) {
            helmet.render(poseStack, buffers.getBuffer(RenderType.armorEntityGlint()), light,
                OverlayTexture.NO_OVERLAY);
        }
    }
}
