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
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;

import net.neoforged.neoforge.client.event.EntityRenderersEvent;

import buildcraftreborn.BuildCraftReborn;

/** The robot's body: 7.1's 8 pixel cube (on a 64x32 texture), with the two glowing overlays on top. Shared by the
 * entity and the item. */
public final class RobotModels {
    public static final ModelLayerLocation BODY = new ModelLayerLocation(BuildCraftReborn.id("robot"), "main");
    /** A helmet worn over the body: a humanoid head, so armour textures fit it. */
    public static final ModelLayerLocation HELMET = new ModelLayerLocation(BuildCraftReborn.id("robot"), "helmet");

    public static final ResourceLocation OVERLAY_SIDE = BuildCraftReborn.id("textures/entity/robot/overlay_side.png");
    public static final ResourceLocation OVERLAY_BOTTOM = BuildCraftReborn.id(
        "textures/entity/robot/overlay_bottom.png");

    private static ModelPart itemBody;

    private RobotModels() {}

    public static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(BODY, RobotModels::createBody);
        event.registerLayerDefinition(HELMET, RobotModels::createHelmet);
    }

    private static LayerDefinition createBody() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("box", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -4, -4, 8, 8, 8),
            PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    private static LayerDefinition createHelmet() {
        MeshDefinition mesh = new MeshDefinition();
        mesh.getRoot().addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4, -8, -4, 8, 8, 8,
            CubeDeformation.NONE), PartPose.ZERO);
        return LayerDefinition.create(mesh, 64, 32);
    }

    /** The body for the item renderer (baked once: the layer is defined in code, so it never changes). */
    static ModelPart getItemBody() {
        if (itemBody == null) {
            itemBody = Minecraft.getInstance().getEntityModels().bakeLayer(BODY);
        }
        return itemBody;
    }

    /** Draws the body, centred on the pose. 7.1 drew the model without the usual entity flip, so the lens (on the
     * texture's "top") faces down.
     *
     * @param charge How full the battery is (0 to 1): the side lights fade with it.
     * @param awake Asleep robots don't light up. */
    public static void renderBody(ModelPart body, PoseStack poseStack, MultiBufferSource buffers,
        ResourceLocation texture, int light, int overlay, int colour, float charge, boolean awake) {
        body.render(poseStack, buffers.getBuffer(RenderType.entityCutout(texture)), light, overlay, colour);
        if (awake) {
            int alpha = Math.round(Math.max(0, Math.min(1, charge)) * 255);
            if (alpha > 0) {
                body.render(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(OVERLAY_SIDE)),
                    LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, FastColor.ARGB32.color(alpha, 0xFF_FF_FF));
            }
            body.render(poseStack, buffers.getBuffer(RenderType.entityTranslucentEmissive(OVERLAY_BOTTOM)),
                LightTexture.FULL_BRIGHT, OverlayTexture.NO_OVERLAY, -1);
        }
    }
}
