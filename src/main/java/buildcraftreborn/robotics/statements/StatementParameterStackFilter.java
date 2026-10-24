/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.lib.inventory.filter.ArrayStackOrListFilter;

/** A filter of the stacks (and lists) in a statement's item parameters. */
public class StatementParameterStackFilter extends ArrayStackOrListFilter {

    public StatementParameterStackFilter(IStatementParameter... parameters) {
        super(collect(parameters));
    }

    private static ItemStack[] collect(IStatementParameter... parameters) {
        List<ItemStack> tmp = new ArrayList<>();
        for (IStatementParameter s : parameters) {
            if (s instanceof StatementParameterItemStack) {
                tmp.add(s.getItemStack());
            }
        }
        return tmp.toArray(new ItemStack[0]);
    }
}
