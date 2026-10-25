/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.world.level.ItemLike;

import buildcraftreborn.BuildCraftReborn;

import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiStack;

/** A BuildCraft recipe category, with the same id and name as its JEI category. */
public class BCEmiCategory extends EmiRecipeCategory {
    private final Component name;

    public BCEmiCategory(String path, ItemLike icon, Component name) {
        super(BuildCraftReborn.id(path), EmiStack.of(icon));
        this.name = name;
    }

    @Override
    public Component getName() {
        return name;
    }
}
