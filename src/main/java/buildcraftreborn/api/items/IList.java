/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.items;

import net.minecraft.world.item.ItemStack;

/** A list item: matches item stacks against the stacks and options stored in it. Filters (diamond pipes, gate
 * triggers...) accept a list in place of a single item. */
public interface IList extends INamedItem {
    boolean matches(ItemStack stackList, ItemStack item);
}
