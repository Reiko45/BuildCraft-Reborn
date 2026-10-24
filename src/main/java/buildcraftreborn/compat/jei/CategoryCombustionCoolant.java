/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.compat.jei;

import java.text.DecimalFormat;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.energy.BCEnergyBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** Combustion engine coolants (new in BuildCraft Reborn's JEI support): each coolant fluid and how much it cools the
 * engine per mB, and each item that melts into a coolant in the engine (like ice into water). */
public class CategoryCombustionCoolant extends AbstractRecipeCategory<CategoryCombustionCoolant.CoolantDisplay> {
    public static final RecipeType<CoolantDisplay> TYPE =
        RecipeType.create(BuildCraftReborn.MODID, "combustion_coolant", CoolantDisplay.class);
    private static final DecimalFormat FORMAT = new DecimalFormat("0.####");

    /** @param solid The item that melts into {@code fluid}, or null for a fluid coolant. */
    public record CoolantDisplay(@Nullable ItemStack solid, FluidStack fluid, float degreesPerMb) {}

    private final IDrawable slot, arrow;

    public CategoryCombustionCoolant(IGuiHelper helper) {
        super(TYPE, Component.translatable("jei.buildcraftreborn.combustion_coolant"),
            helper.createDrawableItemLike(BCEnergyBlocks.ENGINE_IRON.get()), 140, 18);
        slot = helper.getSlotDrawable();
        arrow = helper.getRecipeArrow();
    }

    private static int fluidX(CoolantDisplay recipe) {
        return recipe.solid() == null ? 0 : 50;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, CoolantDisplay recipe, IFocusGroup focuses) {
        if (recipe.solid() != null) {
            builder.addInputSlot(1, 1).addItemStack(recipe.solid());
            CategoryDistiller.addFluid(builder.addOutputSlot(fluidX(recipe) + 1, 1), recipe.fluid());
        } else {
            CategoryDistiller.addFluid(builder.addInputSlot(1, 1), recipe.fluid().copyWithAmount(1000));
        }
    }

    @Override
    public void draw(CoolantDisplay recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics, double mouseX,
        double mouseY) {
        slot.draw(graphics, 0, 0);
        if (recipe.solid() != null) {
            arrow.draw(graphics, 22, 1);
            slot.draw(graphics, fluidX(recipe), 0);
        }
        // (a solid coolant's fluid has its own line with how much it cools)
        if (recipe.solid() == null && recipe.degreesPerMb() > 0) {
            graphics.drawString(Minecraft.getInstance().font, Component.translatable("jei.buildcraftreborn.cooling",
                FORMAT.format(recipe.degreesPerMb())), fluidX(recipe) + 22, 5, 0xFF_40_40_40, false);
        }
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(CoolantDisplay recipe) {
        if (recipe.solid() != null) {
            ResourceLocation item = BuiltInRegistries.ITEM.getKey(recipe.solid().getItem());
            return ResourceLocation.fromNamespaceAndPath(item.getNamespace(), "solid_coolant/" + item.getPath());
        }
        return BCPluginJEI.fluidRecipeId("combustion_coolant", recipe.fluid());
    }
}
