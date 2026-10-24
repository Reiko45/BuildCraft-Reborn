/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.jei;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.component.DataComponentType;
import net.minecraft.world.item.ItemStack;

import mezz.jei.api.ingredients.subtypes.ISubtypeInterpreter;
import mezz.jei.api.ingredients.subtypes.UidContext;

/** Tells stacks of one item apart by one data component (gate variant, lens, facade, paint colour, guide book). */
final class ComponentSubtypeInterpreter implements ISubtypeInterpreter<ItemStack> {
    private final DataComponentType<?> type;

    ComponentSubtypeInterpreter(DataComponentType<?> type) {
        this.type = type;
    }

    @Override
    @Nullable
    public Object getSubtypeData(ItemStack ingredient, UidContext context) {
        return ingredient.get(type);
    }

    @Override
    @SuppressWarnings("removal")
    public String getLegacyStringSubtypeInfo(ItemStack ingredient, UidContext context) {
        Object data = ingredient.get(type);
        return data == null ? "" : data.toString();
    }
}
