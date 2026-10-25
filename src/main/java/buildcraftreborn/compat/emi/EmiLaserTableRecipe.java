/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.compat.viewer.LaserTableDisplay;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** A programming or integration table recipe: one or two items in, one out, and the power it takes. */
public class EmiLaserTableRecipe extends BasicEmiRecipe {
    private static final int TOP = 10;

    private final long microJoules;

    public EmiLaserTableRecipe(EmiRecipeCategory category, LaserTableDisplay display) {
        super(category, BCEmiUtil.synthetic(display.id()), 120, TOP + 26);
        for (List<ItemStack> options : display.inputs()) {
            if (inputs.size() >= 2) {
                break;
            }
            inputs.add(EmiIngredient.of(options.stream().map(EmiStack::of).toList()));
        }
        outputs.add(EmiStack.of(display.output()));
        microJoules = display.microJoules();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), i * 20, TOP + 4);
        }
        widgets.addFillingArrow(60, TOP + 5, 5000);
        widgets.addSlot(outputs.get(0), 90, TOP).large(true).recipeContext(this);
        widgets.addText(Component.literal(MjAPI.formatMj(microJoules) + " MJ"), 0, 0, 0xFF_80_80_80, false);
    }
}
