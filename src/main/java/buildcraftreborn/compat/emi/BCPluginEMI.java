/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.emi;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Rect2i;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraftreborn.compat.viewer.AssemblyDisplay;
import buildcraftreborn.compat.viewer.CoolantDisplay;
import buildcraftreborn.compat.viewer.LaserTableDisplay;
import buildcraftreborn.compat.viewer.RecipeDisplays;
import buildcraftreborn.compat.viewer.ScreenAreas;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.lib.fluid.FuelRegistry;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconPackaging;

import dev.emi.emi.api.EmiEntrypoint;
import dev.emi.emi.api.EmiPlugin;
import dev.emi.emi.api.EmiRegistry;
import dev.emi.emi.api.recipe.EmiRecipeCategory;
import dev.emi.emi.api.recipe.VanillaEmiRecipeCategories;
import dev.emi.emi.api.stack.Comparison;
import dev.emi.emi.api.stack.EmiStack;
import dev.emi.emi.api.widget.Bounds;

/** EMI support, the same as the JEI plugin's: assembly, programming and integration table, distiller, heat exchanger
 * and combustion engine recipes; the stirling engine as a furnace fuel user; the auto workbench, advanced crafting
 * table and stamping table as crafting tables, with recipes filled into the blueprints (and the recipe packager's
 * pattern); assembly recipes moved into the assembly table; ledgers kept clear of EMI's item list; and phantom slots
 * that items can be dragged into from EMI.
 * <p>
 * EMI finds this class by its annotation, and only loads it on the client. With JEI installed too, EMI's JEI support
 * skips BuildCraft's JEI plugin, since this one is here. */
@EmiEntrypoint
public class BCPluginEMI implements EmiPlugin {
    public static final EmiRecipeCategory ASSEMBLY = new BCEmiCategory("assembly", BCSiliconBlocks.ASSEMBLY_TABLE.get(),
        Component.translatable("jei.buildcraftreborn.assembly"));
    public static final EmiRecipeCategory DISTILLATION = new BCEmiCategory("distillation",
        BCFactoryBlocks.DISTILLER.get(), Component.translatable("jei.buildcraftreborn.distillation"));
    public static final EmiRecipeCategory HEATABLE = new BCEmiCategory("heatable", BCFactoryBlocks.HEAT_EXCHANGE.get(),
        Component.translatable("jei.buildcraftreborn.heatable"));
    public static final EmiRecipeCategory COOLABLE = new BCEmiCategory("coolable", BCFactoryBlocks.HEAT_EXCHANGE.get(),
        Component.translatable("jei.buildcraftreborn.coolable"));
    public static final EmiRecipeCategory COMBUSTION_FUEL = new BCEmiCategory("combustion_fuel",
        BCEnergyBlocks.ENGINE_IRON.get(), Component.translatable("jei.buildcraftreborn.combustion_fuel"));
    public static final EmiRecipeCategory COMBUSTION_COOLANT = new BCEmiCategory("combustion_coolant",
        BCEnergyBlocks.ENGINE_IRON.get(), Component.translatable("jei.buildcraftreborn.combustion_coolant"));
    public static final EmiRecipeCategory PROGRAMMING = tableCategory("programming",
        BCRoboticsBlocks.PROGRAMMING_TABLE.get());
    public static final EmiRecipeCategory INTEGRATION = tableCategory("integration",
        BCSiliconBlocks.INTEGRATION_TABLE.get());

    private static EmiRecipeCategory tableCategory(String path, Block table) {
        return new BCEmiCategory(path, table, Component.translatable(table.getDescriptionId()));
    }

    @Override
    public void register(EmiRegistry registry) {
        // Items told apart only by a component
        RecipeDisplays.componentSubtypes().forEach((item, type) -> registry.setDefaultComparison(item,
            Comparison.compareData(stack -> stack.get(type))));

        registry.addCategory(ASSEMBLY);
        registry.addCategory(DISTILLATION);
        registry.addCategory(HEATABLE);
        registry.addCategory(COOLABLE);
        registry.addCategory(COMBUSTION_FUEL);
        registry.addCategory(COMBUSTION_COOLANT);
        registry.addCategory(PROGRAMMING);
        registry.addCategory(INTEGRATION);

        addWorkstations(registry);
        addRecipes(registry);

        // Filling recipes in
        registry.addRecipeHandler(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS_MENU.get(),
            new BlueprintRecipeHandler<>(c -> c.tile.invBlueprint));
        registry.addRecipeHandler(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_MENU.get(),
            new BlueprintRecipeHandler<>(c -> c.tile.invBlueprint));
        registry.addRecipeHandler(BCSiliconPackaging.PACKAGER_MENU.get(),
            new BlueprintRecipeHandler<>(c -> c.tile.invPattern));
        registry.addRecipeHandler(BCSiliconBlocks.ASSEMBLY_TABLE_MENU.get(), new AssemblyRecipeHandler());

        // (EMI matches a screen's exact class, so these are for every screen)
        registry.addGenericExclusionArea((screen, out) -> {
            if (screen instanceof GuiBC8<?> gui) {
                for (Rect2i area : ScreenAreas.ledgerAreas(gui)) {
                    out.accept(new Bounds(area.getX(), area.getY(), area.getWidth(), area.getHeight()));
                }
            }
        });
        registry.addGenericDragDropHandler(new PhantomDragDropHandler());
    }

    private static void addWorkstations(EmiRegistry registry) {
        registry.addWorkstation(ASSEMBLY, EmiStack.of(BCSiliconBlocks.ASSEMBLY_TABLE.get()));
        registry.addWorkstation(PROGRAMMING, EmiStack.of(BCRoboticsBlocks.PROGRAMMING_TABLE.get()));
        registry.addWorkstation(INTEGRATION, EmiStack.of(BCSiliconBlocks.INTEGRATION_TABLE.get()));
        registry.addWorkstation(DISTILLATION, EmiStack.of(BCFactoryBlocks.DISTILLER.get()));
        registry.addWorkstation(HEATABLE, EmiStack.of(BCFactoryBlocks.HEAT_EXCHANGE.get()));
        registry.addWorkstation(COOLABLE, EmiStack.of(BCFactoryBlocks.HEAT_EXCHANGE.get()));
        registry.addWorkstation(COMBUSTION_FUEL, EmiStack.of(BCEnergyBlocks.ENGINE_IRON.get()));
        registry.addWorkstation(COMBUSTION_COOLANT, EmiStack.of(BCEnergyBlocks.ENGINE_IRON.get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.FUEL, EmiStack.of(BCEnergyBlocks.ENGINE_STONE.get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING,
            EmiStack.of(BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING,
            EmiStack.of(BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get()));
        registry.addWorkstation(VanillaEmiRecipeCategories.CRAFTING,
            EmiStack.of(BCSiliconPackaging.STAMPING_TABLE.get()));
    }

    private static void addRecipes(EmiRegistry registry) {
        Level level = Minecraft.getInstance().level;
        if (level != null) {
            for (AssemblyDisplay display : RecipeDisplays.assembly(level)) {
                boolean real = level.getRecipeManager().byKey(display.id()).isPresent();
                registry.addRecipe(new EmiAssemblyRecipe(display, real));
            }
        }

        RefineryRecipeRegistry refinery = RefineryRecipeRegistry.INSTANCE;
        for (IDistillationRecipe recipe : refinery.getDistillationRegistry().getAllRecipes()) {
            registry.addRecipe(new EmiDistillationRecipe(recipe));
        }
        for (IHeatExchangerRecipe recipe : refinery.getHeatableRegistry().getAllRecipes()) {
            registry.addRecipe(new EmiHeatExchangeRecipe(recipe, true));
        }
        for (IHeatExchangerRecipe recipe : refinery.getCoolableRegistry().getAllRecipes()) {
            registry.addRecipe(new EmiHeatExchangeRecipe(recipe, false));
        }

        for (IFuel fuel : FuelRegistry.INSTANCE.getFuels()) {
            registry.addRecipe(new EmiCombustionFuelRecipe(fuel));
        }
        for (CoolantDisplay coolant : RecipeDisplays.coolants()) {
            registry.addRecipe(new EmiCombustionCoolantRecipe(coolant));
        }
        for (LaserTableDisplay display : RecipeDisplays.programming()) {
            registry.addRecipe(new EmiLaserTableRecipe(PROGRAMMING, display));
        }
        for (LaserTableDisplay display : RecipeDisplays.integration()) {
            registry.addRecipe(new EmiLaserTableRecipe(INTEGRATION, display));
        }
    }
}
