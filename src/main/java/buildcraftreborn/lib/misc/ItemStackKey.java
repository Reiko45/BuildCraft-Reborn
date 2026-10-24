/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.misc;

import net.minecraft.world.item.ItemStack;

/** An {@link ItemStack} that can be used as a map key or compared: equal when the item, count and components are
 * (1.12's {@code ItemStackKey}, which compared the item, damage and NBT). */
public final class ItemStackKey {
    public static final ItemStackKey EMPTY = new ItemStackKey(ItemStack.EMPTY);

    public final ItemStack baseStack;
    private final int hash;

    public ItemStackKey(ItemStack stack) {
        this.baseStack = stack;
        this.hash = stack.isEmpty() ? 0 : ItemStack.hashItemAndComponents(stack) * 31 + stack.getCount();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (!(obj instanceof ItemStackKey other)) return false;
        if (hash != other.hash) return false;
        if (baseStack.isEmpty() || other.baseStack.isEmpty()) {
            return baseStack.isEmpty() == other.baseStack.isEmpty();
        }
        return ItemStack.matches(baseStack, other.baseStack);
    }

    @Override
    public int hashCode() {
        return hash;
    }

    @Override
    public String toString() {
        return "[ItemStackKey " + baseStack + "]";
    }
}
