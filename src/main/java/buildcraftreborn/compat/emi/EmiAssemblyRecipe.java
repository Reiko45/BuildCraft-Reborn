/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.compat.viewer.AssemblyDisplay;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiIngredient;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** One output of an assembly recipe, on the assembly table's own background (as in JEI). */
public class EmiAssemblyRecipe extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/assembly_table.png");
    private static final int TOP = 10;
    public static final int MAX_INPUTS = 12;

    private final long microJoules;

    /** @param real True if the id is the recipe's own (the first output of a recipe in the recipe manager). */
    public EmiAssemblyRecipe(AssemblyDisplay display, boolean real) {
        super(BCPluginEMI.ASSEMBLY, real ? display.id() : BCEmiUtil.synthetic(display.id()), 166, 76 + TOP);
        for (IngredientStack in : display.inputs()) {
            if (inputs.size() >= MAX_INPUTS) {
                break;
            }
            inputs.add(EmiIngredient.of(in.ingredient, in.count));
        }
        outputs.add(EmiStack.of(display.output()));
        microJoules = display.microJoules();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 0, TOP, 166, 76, 5, 34);
        // The power bar fills from the bottom over five seconds
        widgets.addAnimatedTexture(TEXTURE, 81, TOP + 2, 4, 70, 176, 48, 5000, false, true, false);
        for (int i = 0; i < inputs.size(); i++) {
            widgets.addSlot(inputs.get(i), 2 + (i % 3) * 18, TOP + 1 + (i / 3) * 18).drawBack(false);
        }
        widgets.addSlot(outputs.get(0), 110, TOP + 1).drawBack(false).recipeContext(this);
        widgets.addText(Component.literal(MjAPI.formatMj(microJoules) + " MJ"), 2, 0, 0xFF_80_80_80, false);
    }
}
