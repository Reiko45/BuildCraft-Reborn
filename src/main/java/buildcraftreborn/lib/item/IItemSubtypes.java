/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.item;

import java.util.function.Consumer;

import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

/** An item that shows several stacks in creative tabs (1.12's {@code getSubItems}), such as one per gate variant. */
public interface IItemSubtypes {
    void addSubtypes(Consumer<ItemStack> output);

    /** Adds every stack of the item to a creative tab. */
    static void accept(CreativeModeTab.Output output, Item item) {
        if (item instanceof IItemSubtypes subtypes) {
            subtypes.addSubtypes(output::accept);
        } else {
            output.accept(item);
        }
    }
}
