/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.client.render;

import com.mojang.blaze3d.vertex.PoseStack;

import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.core.marker.volume.Addon;

/** Draws one kind of volume box addon. The pose has the camera at the origin; {@code camera} is its position. */
public interface IFastAddonRenderer<T extends Addon> {
    void renderAddonFast(T addon, Player player, float partialTicks, PoseStack pose, MultiBufferSource buffers,
        Vec3 camera);

    default IFastAddonRenderer<T> then(IFastAddonRenderer<? super T> after) {
        return (addon, player, partialTicks, pose, buffers, camera) -> {
            renderAddonFast(addon, player, partialTicks, pose, buffers, camera);
            after.renderAddonFast(addon, player, partialTicks, pose, buffers, camera);
        };
    }
}
