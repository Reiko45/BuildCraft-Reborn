/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.client.guide.entry;

import java.util.Objects;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.lib.misc.ItemStackKey;

/** Which item stacks a guide page is about: an item, and maybe its exact components too. (1.12 could also match the
 * damage value, which 1.21 items don't use for variants.) */
public class ItemStackValueFilter {
    public final ItemStackKey stack;
    public final boolean matchComponents;

    public ItemStackValueFilter(ItemStack stack) {
        this(new ItemStackKey(stack), false);
    }

    public ItemStackValueFilter(ItemStackKey stack, boolean matchComponents) {
        this.stack = stack;
        this.matchComponents = matchComponents;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != getClass()) return false;
        ItemStackValueFilter other = (ItemStackValueFilter) obj;
        return stack.equals(other.stack) && matchComponents == other.matchComponents;
    }

    @Override
    public int hashCode() {
        return Objects.hash(stack, matchComponents);
    }

    @Override
    public String toString() {
        return (matchComponents ? "Matching components of " : "") + stack.baseStack;
    }
}
