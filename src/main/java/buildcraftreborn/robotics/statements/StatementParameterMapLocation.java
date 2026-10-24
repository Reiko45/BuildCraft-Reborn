/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.items.IMapLocation;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.StatementMouseClick;
import buildcraftreborn.api.statements.StatementParameterItemStack;

/** An item parameter that only takes map locations (an area for robots to work in). */
public class StatementParameterMapLocation extends StatementParameterItemStack {
    public StatementParameterMapLocation() {}

    public StatementParameterMapLocation(ItemStack stack) {
        super(stack);
    }

    public StatementParameterMapLocation(CompoundTag nbt, HolderLookup.Provider registries) {
        super(nbt, registries);
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:maplocation";
    }

    @Override
    public StatementParameterMapLocation onClick(IStatementContainer source, IStatement stmt, ItemStack stackIn,
        StatementMouseClick mouse) {
        if (stackIn.isEmpty() || !(stackIn.getItem() instanceof IMapLocation)) {
            return new StatementParameterMapLocation();
        }
        return new StatementParameterMapLocation(stackIn.copyWithCount(1));
    }
}
