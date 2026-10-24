/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.jei;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.MjAPI;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** A laser table that turns a few items into one, with power: the programming table (a blank board and the board
 * picked) and the integration table (a robot and the board put in it). */
public class CategoryLaserTable extends AbstractRecipeCategory<CategoryLaserTable.LaserTableDisplay> {
    public static final RecipeType<LaserTableDisplay> PROGRAMMING =
        RecipeType.create(BuildCraftReborn.MODID, "programming", LaserTableDisplay.class);
    public static final RecipeType<LaserTableDisplay> INTEGRATION =
        RecipeType.create(BuildCraftReborn.MODID, "integration", LaserTableDisplay.class);
    private static final int TOP = 10;

    private final IDrawableAnimated arrow;

    public CategoryLaserTable(IGuiHelper helper, RecipeType<LaserTableDisplay> type, Block table) {
        super(type, Component.translatable(table.getDescriptionId()), helper.createDrawableItemLike(table), 116,
            TOP + 20);
        arrow = helper.createAnimatedRecipeArrow(100);
    }

    /** What goes in (each slot can show several items), what comes out, and the power it takes. */
    public record LaserTableDisplay(ResourceLocation id, List<List<ItemStack>> inputs, ItemStack output,
        long microJoules) {}

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, LaserTableDisplay recipe, IFocusGroup focuses) {
        for (int i = 0; i < recipe.inputs().size() && i < 2; i++) {
            builder.addInputSlot(1 + i * 20, TOP + 1).setStandardSlotBackground()
                .addItemStacks(recipe.inputs().get(i));
        }
        builder.addOutputSlot(95, TOP + 1).setOutputSlotBackground().addItemStack(recipe.output());
    }

    @Override
    public void draw(LaserTableDisplay recipe, IRecipeSlotsView slots, GuiGraphics graphics, double mouseX,
        double mouseY) {
        arrow.draw(graphics, 60, TOP + 1);
        graphics.drawString(Minecraft.getInstance().font, MjAPI.formatMj(recipe.microJoules()) + " MJ", 0, 0,
            0xFF_80_80_80, false);
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(LaserTableDisplay recipe) {
        return recipe.id();
    }
}
