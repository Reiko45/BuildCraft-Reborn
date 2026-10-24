/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.HashMap;
import java.util.Map;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.IFuelManager.IDirtyFuel;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.lib.misc.LocaleUtil;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** Combustion engine fuels: a bucket of the fuel, how long it burns and how much power it gives, and the residue a
 * dirty fuel leaves (per bucket). */
public class CategoryCombustionFuel extends AbstractRecipeCategory<IFuel> {
    public static final RecipeType<IFuel> TYPE =
        RecipeType.create(BuildCraftReborn.MODID, "combustion_fuel", IFuel.class);
    private static final int BUCKET = 1000;

    private final IGuiHelper helper;
    private final IDrawable slot, flameEmpty;
    private final Map<Integer, IDrawableAnimated> flames = new HashMap<>();

    public CategoryCombustionFuel(IGuiHelper helper) {
        super(TYPE, Component.translatable("jei.buildcraftreborn.combustion_fuel"),
            helper.createDrawableItemLike(BCEnergyBlocks.ENGINE_IRON.get()), 120, 34);
        this.helper = helper;
        slot = helper.getSlotDrawable();
        flameEmpty = helper.getRecipeFlameEmpty();
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IFuel recipe, IFocusGroup focuses) {
        CategoryDistiller.addFluid(builder.addInputSlot(1, 17), recipe.getFluid().copyWithAmount(BUCKET));
        if (recipe instanceof IDirtyFuel dirty) {
            CategoryDistiller.addFluid(builder.addOutputSlot(103, 17), dirty.getResidue());
        }
    }

    @Override
    public void draw(IFuel recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
        double mouseY) {
        slot.draw(graphics, 0, 16);
        if (recipe instanceof IDirtyFuel) {
            slot.draw(graphics, 102, 16);
        }
        flameEmpty.draw(graphics, 2, 1);
        int cycle = Math.max(1, recipe.getTotalBurningTime() / 10);
        flames.computeIfAbsent(cycle, c -> helper.createAnimatedRecipeFlame(c)).draw(graphics, 2, 1);

        Font font = Minecraft.getInstance().font;
        int colour = 0xFF_40_40_40;
        long power = recipe.getPowerPerCycle();
        graphics.drawString(font, Component.translatable("jei.buildcraftreborn.burn_time",
            recipe.getTotalBurningTime() / 20), 22, 2, colour, false);
        graphics.drawString(font, Component.translatable("jei.buildcraftreborn.burn_power",
            LocaleUtil.localizeMjFlow(power)), 22, 12, colour, false);
        graphics.pose().pushPose();
        graphics.pose().translate(22, 23, 0);
        graphics.pose().scale(0.7f, 0.7f, 1);
        graphics.drawString(font, Component.translatable("jei.buildcraftreborn.burn_total",
            LocaleUtil.localizeMj(power * recipe.getTotalBurningTime())), 0, 0, 0xFF_80_80_80, false);
        graphics.pose().popPose();
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(IFuel recipe) {
        return BCPluginJEI.fluidRecipeId("combustion_fuel", recipe.getFluid());
    }
}
