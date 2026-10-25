/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.compat.viewer.RecipeDisplays;
import buildcraftreborn.factory.BCFactoryBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.IRecipeSlotBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.neoforge.NeoForgeTypes;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** Distiller recipes: the input fluid on the left splits into a gas (top right) and a liquid (bottom right), drawn
 * with the distiller GUI's pipes. */
public class CategoryDistiller extends AbstractRecipeCategory<IDistillationRecipe> {
    public static final RecipeType<IDistillationRecipe> TYPE =
        RecipeType.create(BuildCraftReborn.MODID, "distillation", IDistillationRecipe.class);
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/distiller.png");

    private final IDrawable background, slot;
    private final IDrawableAnimated animated;

    public CategoryDistiller(IGuiHelper helper) {
        super(TYPE, Component.translatable("jei.buildcraftreborn.distillation"),
            helper.createDrawableItemLike(BCFactoryBlocks.DISTILLER.get()), 92, 65);
        background = helper.createDrawable(TEXTURE, 61, 12, 36, 57);
        slot = helper.createDrawable(TEXTURE, 7, 34, 18, 18);
        animated = helper.createAnimatedDrawable(helper.createDrawable(TEXTURE, 212, 0, 36, 57), 40,
            IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IDistillationRecipe recipe, IFocusGroup focuses) {
        addFluid(builder.addInputSlot(1, 26), recipe.in());
        addFluid(builder.addOutputSlot(57, 1), recipe.outGas());
        addFluid(builder.addOutputSlot(57, 46), recipe.outLiquid());
    }

    static void addFluid(IRecipeSlotBuilder slot, @Nullable FluidStack fluid) {
        if (fluid != null && !fluid.isEmpty()) {
            slot.setFluidRenderer(Math.max(1, fluid.getAmount()), false, 16, 16)
                .addIngredient(NeoForgeTypes.FLUID_STACK, fluid);
        }
    }

    @Override
    public void draw(IDistillationRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
        double mouseX, double mouseY) {
        background.draw(graphics, 20, 4);
        animated.draw(graphics, 20, 4);
        slot.draw(graphics, 0, 25);
        slot.draw(graphics, 56, 0);
        slot.draw(graphics, 56, 45);
        graphics.drawString(Minecraft.getInstance().font, MjAPI.formatMj(recipe.powerRequired()) + " MJ", 58, 28,
            0xFF_00_A0_A0, false);
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(IDistillationRecipe recipe) {
        return RecipeDisplays.fluidRecipeId("distillation", recipe.in());
    }
}
