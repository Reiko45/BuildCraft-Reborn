/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraftreborn.compat.viewer.RecipeDisplays;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** A fluid the heat exchanger heats up or cools down (one category each, as in JEI). */
public class EmiHeatExchangeRecipe extends BasicEmiRecipe {
    private static final ResourceLocation HEATER = BuildCraftReborn.id("textures/gui/energy_heater.png");
    private static final ResourceLocation EXCHANGER = BuildCraftReborn.id("textures/gui/heat_exchanger.png");
    private static final int ARROW_X = 18, ARROW_WIDTH = 54;

    private final boolean heating;
    private final EmiStack out;
    private final int heatFrom, heatTo;

    public EmiHeatExchangeRecipe(IHeatExchangerRecipe recipe, boolean heating) {
        super(heating ? BCPluginEMI.HEATABLE : BCPluginEMI.COOLABLE, BCEmiUtil.synthetic(
            RecipeDisplays.fluidRecipeId(heating ? "heatable" : "coolable", recipe.in())), 92, 19);
        this.heating = heating;
        inputs.add(BCEmiUtil.fluid(recipe.in()));
        out = BCEmiUtil.fluid(recipe.out());
        outputs.addAll(BCEmiUtil.present(out));
        heatFrom = recipe.heatFrom();
        heatTo = recipe.heatTo();
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        widgets.addSlot(out, 72, 0).recipeContext(this);
        if (heating) {
            widgets.addTexture(HEATER, ARROW_X, 0, ARROW_WIDTH, 19, 176, 19);
            widgets.addAnimatedTexture(HEATER, ARROW_X, 0, ARROW_WIDTH, 19, 176, 152, 2000, true, false, false);
        } else {
            widgets.addTexture(EXCHANGER, ARROW_X, 0, ARROW_WIDTH, 17, 61, 38);
            widgets.addAnimatedTexture(EXCHANGER, ARROW_X, 0, ARROW_WIDTH, 17, 52, 171, 2000, true, false, false);
            widgets.addAnimatedTexture(EXCHANGER, ARROW_X, 0, ARROW_WIDTH, 17, 52, 188, 2000, true, true, false);
        }
        List<Component> tooltip = new ArrayList<>();
        tooltip.add(Component.translatable("jei.buildcraftreborn.heat_levels", heatFrom, heatTo));
        if (out.isEmpty()) {
            tooltip.add(Component.translatable("jei.buildcraftreborn.used_up"));
        }
        widgets.addTooltipText(tooltip, ARROW_X, 0, ARROW_WIDTH, 19);
    }
}
