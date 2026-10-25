/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IngredientStack;
import buildcraftreborn.compat.viewer.AssemblyDisplay;
import buildcraftreborn.silicon.BCSiliconBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** Assembly table recipes: up to twelve inputs on the left (as in the table's GUI), the output on the right, the
 * power bar filling between them and the power needed above. */
public class CategoryAssemblyTable extends AbstractRecipeCategory<AssemblyDisplay> {
    public static final RecipeType<AssemblyDisplay> TYPE =
        RecipeType.create(BuildCraftReborn.MODID, "assembly", AssemblyDisplay.class);
    private static final ResourceLocation TEXTURE = BuildCraftReborn.id("textures/gui/assembly_table.png");
    private static final int TOP = 10;

    private final IDrawable background;
    private final IDrawableAnimated progress;

    public CategoryAssemblyTable(IGuiHelper helper) {
        super(TYPE, Component.translatable("jei.buildcraftreborn.assembly"),
            helper.createDrawableItemLike(BCSiliconBlocks.ASSEMBLY_TABLE.get()), 166, 76 + TOP);
        background = helper.createDrawable(TEXTURE, 5, 34, 166, 76);
        IDrawableStatic bar = helper.createDrawable(TEXTURE, 176, 48, 4, 70);
        progress = helper.createAnimatedDrawable(bar, 100, IDrawableAnimated.StartDirection.BOTTOM, false);
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, AssemblyDisplay recipe, IFocusGroup focuses) {
        int i = 0;
        for (IngredientStack in : recipe.inputs()) {
            if (i >= 12) {
                break;
            }
            List<ItemStack> stacks = new ArrayList<>();
            for (ItemStack matching : in.ingredient.getItems()) {
                stacks.add(matching.copyWithCount(in.count));
            }
            builder.addInputSlot(3 + (i % 3) * 18, TOP + 2 + (i / 3) * 18).addItemStacks(stacks);
            i++;
        }
        builder.addOutputSlot(111, TOP + 2).addItemStack(recipe.output());
    }

    @Override
    public void draw(AssemblyDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
        double mouseY) {
        background.draw(graphics, 0, TOP);
        progress.draw(graphics, 81, TOP + 2);
        graphics.drawString(Minecraft.getInstance().font, MjAPI.formatMj(recipe.microJoules()) + " MJ", 2, 0,
            0xFF_80_80_80, false);
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(AssemblyDisplay recipe) {
        return recipe.id();
    }
}
