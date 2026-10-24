/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.transport.item;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.misc.ColourUtil;
import buildcraftreborn.transport.BCTransportItems;

/** Pipe wire of one colour, placed on pipe corners by right clicking.
 * <p>
 * 1.21: one item per colour ({@code wire_red} and so on) instead of 1.12's 16 metadata values of one item. */
public class ItemWire extends Item {
    public final DyeColor colour;

    public ItemWire(DyeColor colour, Properties properties) {
        super(properties);
        this.colour = colour;
    }

    /** @return The wire item of the given colour. */
    public static ItemWire get(DyeColor colour) {
        return BCTransportItems.WIRES.get(colour).get();
    }

    @Override
    public Component getName(ItemStack stack) {
        return Component.literal(ColourUtil.getTextFullTooltipSpecial(colour) + " ").append(super.getName(stack));
    }
}
