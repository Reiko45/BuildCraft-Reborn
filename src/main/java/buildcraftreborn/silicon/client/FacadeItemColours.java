/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.silicon.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.color.item.ItemColor;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.silicon.item.ItemPluggableFacade;
import buildcraftreborn.silicon.plug.FacadePhasedState;

/** Tints facade items like their block (grass, leaves...). The facade's tint indexes are
 * {@code tint * 6 + side}. */
public enum FacadeItemColours implements ItemColor {
    INSTANCE;

    @Override
    public int getColor(ItemStack stack, int tintIndex) {
        FacadePhasedState state = ItemPluggableFacade.getStates(stack).getCurrentStateForStack();
        int colour;
        try {
            colour = Minecraft.getInstance().getBlockColors().getColor(state.stateInfo.state, null, null,
                tintIndex / Direction.values().length);
        } catch (RuntimeException e) {
            // The block didn't like the missing level and position
            colour = -1;
        }
        // Item colours need the alpha set
        return colour == -1 ? -1 : 0xFF_00_00_00 | colour;
    }
}
