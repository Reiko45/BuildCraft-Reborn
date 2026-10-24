/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.items;

import net.minecraft.world.item.ItemStack;

/** An item that carries a name the player can set (the list's label).
 * <p>
 * 1.21: 1.12's {@code getName}/{@code setName} are {@code getLabel}/{@code setLabel}, as items have a
 * {@code getName(ItemStack)} of their own now. */
public interface INamedItem {
    String getLabel(ItemStack stack);

    /** @return true if the label was changed. */
    boolean setLabel(ItemStack stack, String label);
}
