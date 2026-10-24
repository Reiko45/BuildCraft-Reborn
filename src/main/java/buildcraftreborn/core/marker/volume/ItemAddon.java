/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.core.marker.volume;

import org.apache.commons.lang3.tuple.Pair;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** An item that puts an addon on the volume box corner the player looks at. */
public abstract class ItemAddon extends Item {
    public ItemAddon(Properties properties) {
        super(properties);
    }

    public abstract Addon createAddon();

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        if (level.isClientSide) {
            return InteractionResultHolder.pass(player.getItemInHand(hand));
        }

        WorldSavedDataVolumeBoxes volumeBoxes = WorldSavedDataVolumeBoxes.get(level);
        Pair<VolumeBox, EnumAddonSlot> selectingVolumeBoxAndSlot =
            EnumAddonSlot.getSelectingVolumeBoxAndSlot(player, volumeBoxes.volumeBoxes);
        VolumeBox volumeBox = selectingVolumeBoxAndSlot.getLeft();
        EnumAddonSlot slot = selectingVolumeBoxAndSlot.getRight();
        if (volumeBox != null && slot != null) {
            if (!volumeBox.addons.containsKey(slot)) {
                Addon addon = createAddon();
                if (addon.canBePlaceInto(volumeBox)) {
                    addon.volumeBox = volumeBox;
                    volumeBox.addons.put(slot, addon);
                    volumeBox.addons.get(slot).onAdded();
                    volumeBoxes.setDirty();
                    return InteractionResultHolder.success(player.getItemInHand(hand));
                }
            }
        }

        return InteractionResultHolder.pass(player.getItemInHand(hand));
    }
}
