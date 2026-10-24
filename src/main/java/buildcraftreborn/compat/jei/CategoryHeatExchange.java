/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import org.jetbrains.annotations.Nullable;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraftreborn.factory.BCFactoryBlocks;

import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.builder.ITooltipBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.AbstractRecipeCategory;

/** Heat exchanger recipes: a fluid that gets heated up (the cold side) or one that gets cooled down (the hot side).
 * A fluid with no result, like water or lava, is used up. The arrow's tooltip shows the heat levels. */
public class CategoryHeatExchange extends AbstractRecipeCategory<IHeatExchangerRecipe> {
    public static final RecipeType<IHeatExchangerRecipe> HEATABLE =
        RecipeType.create(BuildCraftReborn.MODID, "heatable", IHeatExchangerRecipe.class);
    public static final RecipeType<IHeatExchangerRecipe> COOLABLE =
        RecipeType.create(BuildCraftReborn.MODID, "coolable", IHeatExchangerRecipe.class);
    private static final ResourceLocation HEATER = BuildCraftReborn.id("textures/gui/energy_heater.png");
    private static final ResourceLocation EXCHANGER = BuildCraftReborn.id("textures/gui/heat_exchanger.png");
    private static final int ARROW_X = 18, ARROW_WIDTH = 54;

    private final boolean heating;
    private final IDrawable background, slot;
    private final IDrawableAnimated[] animated;

    public CategoryHeatExchange(IGuiHelper helper, boolean heating) {
        super(heating ? HEATABLE : COOLABLE,
            Component.translatable(heating ? "jei.buildcraftreborn.heatable" : "jei.buildcraftreborn.coolable"),
            helper.createDrawableItemLike(BCFactoryBlocks.HEAT_EXCHANGE.get()), 92, 19);
        this.heating = heating;
        if (heating) {
            background = helper.createDrawable(HEATER, 176, 19, 54, 19);
            slot = helper.createDrawable(HEATER, 7, 22, 18, 18);
            animated = new IDrawableAnimated[] { helper.createAnimatedDrawable(
                helper.createDrawable(HEATER, 176, 152, 54, 19), 40, IDrawableAnimated.StartDirection.LEFT, false) };
        } else {
            background = helper.createDrawable(EXCHANGER, 61, 38, 54, 17);
            slot = helper.createDrawable(EXCHANGER, 7, 22, 18, 18);
            animated = new IDrawableAnimated[] {
                helper.createAnimatedDrawable(helper.createDrawable(EXCHANGER, 52, 171, 54, 17), 40,
                    IDrawableAnimated.StartDirection.LEFT, false),
                helper.createAnimatedDrawable(helper.createDrawable(EXCHANGER, 52, 188, 54, 17), 40,
                    IDrawableAnimated.StartDirection.RIGHT, false) };
        }
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder builder, IHeatExchangerRecipe recipe, IFocusGroup focuses) {
        CategoryDistiller.addFluid(builder.addInputSlot(1, 1), recipe.in());
        CategoryDistiller.addFluid(builder.addOutputSlot(73, 1), recipe.out());
    }

    @Override
    public void draw(IHeatExchangerRecipe recipe, IRecipeSlotsView recipeSlotsView, GuiGraphics graphics,
        double mouseX, double mouseY) {
        slot.draw(graphics, 0, 0);
        slot.draw(graphics, 72, 0);
        background.draw(graphics, ARROW_X, 0);
        for (IDrawableAnimated anim : animated) {
            anim.draw(graphics, ARROW_X, 0);
        }
    }

    @Override
    public void getTooltip(ITooltipBuilder tooltip, IHeatExchangerRecipe recipe, IRecipeSlotsView recipeSlotsView,
        double mouseX, double mouseY) {
        if (mouseX >= ARROW_X && mouseX < ARROW_X + ARROW_WIDTH && mouseY >= 0 && mouseY < getHeight()) {
            tooltip.add(Component.translatable("jei.buildcraftreborn.heat_levels", recipe.heatFrom(),
                recipe.heatTo()));
            if (recipe.out() == null || recipe.out().isEmpty()) {
                tooltip.add(Component.translatable("jei.buildcraftreborn.used_up"));
            }
        }
    }

    @Override
    @Nullable
    public ResourceLocation getRegistryName(IHeatExchangerRecipe recipe) {
        return BCPluginJEI.fluidRecipeId(heating ? "heatable" : "coolable", recipe.in());
    }
}
