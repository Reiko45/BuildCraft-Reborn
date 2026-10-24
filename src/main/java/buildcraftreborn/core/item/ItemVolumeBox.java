/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.item;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;

import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;

/** Makes a one block volume box next to the clicked face. Resize it with the marker connector. */
public class ItemVolumeBox extends Item {
    public ItemVolumeBox(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        if (context.getLevel().isClientSide) {
            return InteractionResult.PASS;
        }

        BlockPos offset = context.getClickedPos().relative(context.getClickedFace());

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(context.getLevel());
        VolumeBox current = volumeBoxes.getVolumeBoxAt(offset);

        if (current == null) {
            volumeBoxes.addVolumeBox(offset);
            volumeBoxes.setDirty();
            return InteractionResult.SUCCESS;
        }

        return InteractionResult.FAIL;
    }
}
