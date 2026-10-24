/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.builders.client.render;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.builders.BCBuildersSprites;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.core.client.render.AddonDefaultRenderer;
import buildcraftreborn.core.client.render.IFastAddonRenderer;
import buildcraftreborn.lib.misc.SpriteUtil;

/** Draws the filler planner addon (its corner cube), and a see-through block wherever its pattern would place one. */
public class AddonRendererFillerPlanner implements IFastAddonRenderer<AddonFillerPlanner> {
    private final AddonDefaultRenderer<AddonFillerPlanner> corner =
        new AddonDefaultRenderer<>(BCBuildersSprites.FILLER_PLANNER);

    @Override
    public void renderAddonFast(AddonFillerPlanner addon, Player player, float partialTicks, PoseStack pose,
        MultiBufferSource buffers, Vec3 camera) {
        corner.renderAddonFast(addon, player, partialTicks, pose, buffers, camera);
        Template.BuildingInfo info = addon.buildingInfo;
        if (info == null) {
            return;
        }
        List<BlockPos> list = new ArrayList<>();
        for (BlockPos blockPos : BlockPos.betweenClosed(info.box.min(), info.box.max())) {
            if (info.getSnapshot().data.get(info.getSnapshot().posToIndex(info.fromWorld(blockPos)))
                && player.level().isEmptyBlock(blockPos)) {
                list.add(blockPos.immutable());
            }
        }
        Vec3 eye = player.getEyePosition(partialTicks);
        list.sort(Comparator.<BlockPos>comparingDouble(p -> p.getCenter().distanceToSqr(eye)).reversed());
        TextureAtlasSprite white = SpriteUtil.getBlockSprite(BuildCraftReborn.MODID + ":block/white");
        var buffer = buffers.getBuffer(RenderType.translucent());
        for (BlockPos p : list) {
            AABB bb = new AABB(p).deflate(0.1).move(camera.scale(-1));
            AddonDefaultRenderer.renderCube(bb, white, 127, pose, buffer);
        }
    }
}
