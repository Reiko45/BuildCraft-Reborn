/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.emi;

import java.text.DecimalFormat;

import net.minecraft.network.chat.Component;

import buildcraftreborn.compat.viewer.CoolantDisplay;

import dev.emi.emi.api.recipe.BasicEmiRecipe;
import dev.emi.emi.api.render.EmiTexture;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.WidgetHolder;

/** A combustion engine coolant: a fluid and how much each mB cools the engine, or an item and the fluid it melts
 * into. */
public class EmiCombustionCoolantRecipe extends BasicEmiRecipe {
    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    private final CoolantDisplay coolant;
    private final EmiStack fluid;

    public EmiCombustionCoolantRecipe(CoolantDisplay coolant) {
        super(BCPluginEMI.COMBUSTION_COOLANT, BCEmiUtil.synthetic(coolant.id()), 140, 18);
        this.coolant = coolant;
        if (coolant.solid() != null) {
            inputs.add(EmiStack.of(coolant.solid()));
            fluid = BCEmiUtil.fluid(coolant.fluid());
            outputs.addAll(BCEmiUtil.present(fluid));
        } else {
            fluid = BCEmiUtil.fluid(coolant.fluid().copyWithAmount(1000));
            inputs.add(fluid);
        }
    }

    @Override
    public boolean supportsRecipeTree() {
        return false;
    }

    @Override
    public void addWidgets(WidgetHolder widgets) {
        widgets.addSlot(inputs.get(0), 0, 0);
        if (coolant.solid() != null) {
            // (the fluid's own recipe says how much it cools)
            widgets.addTexture(EmiTexture.EMPTY_ARROW, 22, 1);
            widgets.addSlot(fluid, 50, 0).recipeContext(this);
        } else if (coolant.degreesPerMb() > 0) {
            widgets.addText(Component.translatable("jei.buildcraftreborn.cooling",
                FORMAT.format(coolant.degreesPerMb())), 22, 5, 0xFF_40_40_40, false);
        }
    }
}
