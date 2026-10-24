/*
 * Copyright (c) 2017 SpaceToad and the BuildCraft team
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/
 */
package buildcraftreborn.lib.inventory.filter;


import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;

public class InvertedStackFilter implements IStackFilter {

    private final IStackFilter filter;

    public InvertedStackFilter(IStackFilter filter) {
        this.filter = filter;
    }

    @Override
    public boolean matches(ItemStack stack) {
        return !stack.isEmpty() && !filter.matches(stack);
    }
}
