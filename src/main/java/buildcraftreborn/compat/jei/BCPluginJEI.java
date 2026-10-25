/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.jei;

import java.util.List;

import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraftreborn.compat.viewer.RecipeDisplays;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.client.gui.GuiEngineStone_BC8;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.client.gui.GuiAutoCraftItems;
import buildcraftreborn.factory.client.gui.GuiDistiller;
import buildcraftreborn.factory.container.ContainerAutoCraftItems;
import buildcraftreborn.lib.fluid.FuelRegistry;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.client.gui.GuiProgrammingTable;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.client.gui.GuiAdvancedCraftingTable;
import buildcraftreborn.silicon.client.gui.GuiAssemblyTable;
import buildcraftreborn.silicon.client.gui.GuiIntegrationTable;
import buildcraftreborn.silicon.container.ContainerAdvancedCraftingTable;
import buildcraftreborn.silicon.container.ContainerAssemblyTable;
import buildcraftreborn.silicon.container.ContainerPackager;

import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.constants.RecipeTypes;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.registration.IGuiHandlerRegistration;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import mezz.jei.api.registration.IRecipeTransferRegistration;
import mezz.jei.api.registration.ISubtypeRegistration;

/** JEI support: assembly, programming and integration table, distiller, heat exchanger and combustion engine
 * recipes; the auto workbench, advanced crafting table and stamping table as crafting tables (with recipe transfer
 * into the blueprints, and into the recipe packager's pattern); ledgers kept clear of JEI's item list; and phantom
 * slots that ingredients can be dragged into from JEI.
 * <p>
 * 1.21: on top of 1.12's BuildCraftCompat this also shows facade assembly recipes, combustion engine coolants,
 * clickable progress areas, and the ghost ingredient dragging. JEI loads this class itself, and only on the client;
 * nothing else in the mod refers to it. {@link buildcraftreborn.compat.emi.BCPluginEMI} does the same for EMI, and
 * the recipes both show come from {@link RecipeDisplays}. */
@JeiPlugin
public class BCPluginJEI implements IModPlugin {
    private static final ResourceLocation UID = BuildCraftReborn.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        RecipeDisplays.componentSubtypes().forEach((item, type) -> registration
            .registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, new ComponentSubtypeInterpreter(type)));
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration registration) {
        IGuiHelper helper = registration.getJeiHelpers().getGuiHelper();
        registration.addRecipeCategories(
            new CategoryAssemblyTable(helper),
            new CategoryDistiller(helper),
            new CategoryHeatExchange(helper, true),
            new CategoryHeatExchange(helper, false),
            new CategoryCombustionFuel(helper),
            new CategoryCombustionCoolant(helper),
            new CategoryLaserTable(helper, CategoryLaserTable.PROGRAMMING, BCRoboticsBlocks.PROGRAMMING_TABLE.get()),
            new CategoryLaserTable(helper, CategoryLaserTable.INTEGRATION, BCSiliconBlocks.INTEGRATION_TABLE.get()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration registration) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            registration.addRecipes(CategoryAssemblyTable.TYPE, RecipeDisplays.assembly(level));
        }

        RefineryRecipeRegistry refinery = RefineryRecipeRegistry.INSTANCE;
        registration.addRecipes(CategoryDistiller.TYPE, List.copyOf(refinery.getDistillationRegistry().getAllRecipes()));
        registration.addRecipes(CategoryHeatExchange.HEATABLE,
            List.<IHeatExchangerRecipe>copyOf(refinery.getHeatableRegistry().getAllRecipes()));
        registration.addRecipes(CategoryHeatExchange.COOLABLE,
            List.<IHeatExchangerRecipe>copyOf(refinery.getCoolableRegistry().getAllRecipes()));

        registration.addRecipes(CategoryCombustionFuel.TYPE, List.<IFuel>copyOf(FuelRegistry.INSTANCE.getFuels()));
        registration.addRecipes(CategoryCombustionCoolant.TYPE, RecipeDisplays.coolants());
        registration.addRecipes(CategoryLaserTable.PROGRAMMING, RecipeDisplays.programming());
        registration.addRecipes(CategoryLaserTable.INTEGRATION, RecipeDisplays.integration());
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration registration) {
        registration.addRecipeCatalyst(BCSiliconBlocks.ASSEMBLY_TABLE.get(), CategoryAssemblyTable.TYPE);
        registration.addRecipeCatalyst(BCRoboticsBlocks.PROGRAMMING_TABLE.get(), CategoryLaserTable.PROGRAMMING);
        registration.addRecipeCatalyst(BCSiliconBlocks.INTEGRATION_TABLE.get(), CategoryLaserTable.INTEGRATION);
        registration.addRecipeCatalyst(BCFactoryBlocks.DISTILLER.get(), CategoryDistiller.TYPE);
        registration.addRecipeCatalyst(BCFactoryBlocks.HEAT_EXCHANGE.get(), CategoryHeatExchange.HEATABLE,
            CategoryHeatExchange.COOLABLE);
        registration.addRecipeCatalyst(BCEnergyBlocks.ENGINE_IRON.get(), CategoryCombustionFuel.TYPE,
            CategoryCombustionCoolant.TYPE);
        registration.addRecipeCatalyst(BCEnergyBlocks.ENGINE_STONE.get(), RecipeTypes.FUELING);
        registration.addRecipeCatalyst(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get(), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get(), RecipeTypes.CRAFTING);
        registration.addRecipeCatalyst(BCSiliconPackaging.STAMPING_TABLE.get(), RecipeTypes.CRAFTING);
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new BlueprintTransferHandler<>(ContainerAutoCraftItems.class,
            BCFactoryBlocks.AUTO_WORKBENCH_ITEMS_MENU.get(), c -> c.tile.invBlueprint), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new BlueprintTransferHandler<>(ContainerAdvancedCraftingTable.class,
            BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_MENU.get(), c -> c.tile.invBlueprint), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new BlueprintTransferHandler<>(ContainerPackager.class,
            BCSiliconPackaging.PACKAGER_MENU.get(), c -> c.tile.invPattern), RecipeTypes.CRAFTING);
        // Player inventory (0-35) into the table's 12 input slots (36-47)
        registration.addRecipeTransferHandler(ContainerAssemblyTable.class,
            BCSiliconBlocks.ASSEMBLY_TABLE_MENU.get(), CategoryAssemblyTable.TYPE, 36, 12, 0, 36);
    }

    @Override
    @SuppressWarnings({ "unchecked", "rawtypes" })
    public void registerGuiHandlers(IGuiHandlerRegistration registration) {
        registration.addGenericGuiContainerHandler(GuiBC8.class, new GuiHandlerBuildCraft());
        registration.addGhostIngredientHandler((Class) GuiBC8.class, new GhostIngredientHandlerBC());

        registration.addRecipeClickArea(GuiAutoCraftItems.class, 90, 47, 23, 10, RecipeTypes.CRAFTING);
        registration.addRecipeClickArea(GuiAdvancedCraftingTable.class, 164, 7, 4, 70, RecipeTypes.CRAFTING);
        registration.addRecipeClickArea(GuiAssemblyTable.class, 86, 36, 4, 70, CategoryAssemblyTable.TYPE);
        registration.addRecipeClickArea(GuiProgrammingTable.class, 164, 36, 4, 70, CategoryLaserTable.PROGRAMMING);
        registration.addRecipeClickArea(GuiIntegrationTable.class, 164, 22, 4, 70, CategoryLaserTable.INTEGRATION);
        registration.addRecipeClickArea(GuiDistiller.class, 61, 12, 36, 57, CategoryDistiller.TYPE);
        registration.addRecipeClickArea(GuiEngineStone_BC8.class, 81, 25, 14, 14, RecipeTypes.FUELING);
    }
}
