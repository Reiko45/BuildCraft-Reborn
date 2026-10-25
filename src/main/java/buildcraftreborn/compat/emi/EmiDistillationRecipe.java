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
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.compat.viewer.RecipeDisplays;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** A distiller recipe: one fluid in, a gas out of the top and a liquid out of the bottom. */
public class EmiDistillationRecipe extends BasicEmiRecipe {
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/distiller.png");

    private final EmiStack gas, liquid;
    private final long power;

    public EmiDistillationRecipe(IDistillationRecipe recipe) {
        super(BCPluginEMI.DISTILLATION, BCEmiUtil.synthetic(RecipeDisplays.fluidRecipeId("distillation", recipe.in())),
            92, 65);
        inputs.add(BCEmiUtil.fluid(recipe.in()));
        gas = BCEmiUtil.fluid(recipe.outGas());
        liquid = BCEmiUtil.fluid(recipe.outLiquid());
        outputs.addAll(BCEmiUtil.present(gas, liquid));
        power = recipe.powerRequired();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addTexture(TEXTURE, 20, 4, 36, 57, 61, 12);
        widgets.addAnimatedTexture(TEXTURE, 20, 4, 36, 57, 212, 0, 2000, true, false, false);
        widgets.addSlot(inputs.get(0), 0, 25);
        widgets.addSlot(gas, 56, 0).recipeContext(this);
        widgets.addSlot(liquid, 56, 45).recipeContext(this);
        widgets.addText(Component.literal(MjAPI.formatMj(power) + " MJ"), 58, 28, 0xFF_00_A0_A0, false);
    }
}
