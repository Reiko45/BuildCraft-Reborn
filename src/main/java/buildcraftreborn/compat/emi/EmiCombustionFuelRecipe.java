/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.IFuelManager.IDirtyFuel;
import buildcraftreborn.compat.viewer.RecipeDisplays;
import buildcraftreborn.lib.misc.LocaleUtil;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** A combustion engine fuel: how long a bucket burns, the power it gives, and what a dirty fuel leaves behind. */
public class EmiCombustionFuelRecipe extends BasicEmiRecipe {
    private static final int BUCKET = 1000;

    private final int burnTime;
    private final long power;
    private final EmiStack residue;

    public EmiCombustionFuelRecipe(IFuel fuel) {
        super(BCPluginEMI.COMBUSTION_FUEL, BCEmiUtil.synthetic(RecipeDisplays.fluidRecipeId("combustion_fuel",
            fuel.getFluid())), 120, 34);
        inputs.add(BCEmiUtil.fluid(fuel.getFluid().copyWithAmount(BUCKET)));
        residue = fuel instanceof IDirtyFuel dirty ? BCEmiUtil.fluid(dirty.getResidue()) : EmiStack.EMPTY;
        outputs.addAll(BCEmiUtil.present(residue));
        burnTime = fuel.getTotalBurningTime();
        power = fuel.getPowerPerCycle();
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 16);
        if (!residue.isEmpty()) {
            widgets.addSlot(residue, 102, 16).recipeContext(this);
        }
        widgets.addTexture(EmiTexture.EMPTY_FLAME, 2, 1);
        int cycleMillis = Math.max(1, burnTime / 10) * 50;
        widgets.addAnimatedTexture(EmiTexture.FULL_FLAME, 2, 1, cycleMillis, false, true, true);
        int colour = 0xFF_40_40_40;
        widgets.addText(Component.translatable("jei.buildcraftreborn.burn_time", burnTime / 20), 22, 2, colour,
            false);
        widgets.addText(Component.translatable("jei.buildcraftreborn.burn_power", LocaleUtil.localizeMjFlow(power)),
            22, 12, colour, false);
        Component total = Component.translatable("jei.buildcraftreborn.burn_total",
            LocaleUtil.localizeMj(power * burnTime));
        widgets.addDrawable(22, 23, 98, 8, (graphics, mouseX, mouseY, delta) -> {
            graphics.pose().pushPose();
            graphics.pose().scale(0.7f, 0.7f, 1);
            graphics.drawString(Minecraft.getInstance().font, total, 0, 0, 0xFF_80_80_80, false);
            graphics.pose().popPose();
        });
    }
}
