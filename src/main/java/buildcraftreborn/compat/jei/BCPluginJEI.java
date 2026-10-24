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
import java.util.Map;

import net.minecraft.client.Minecraft;
import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IProgrammingRecipe;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatExchangerRecipe;
import buildcraftreborn.compat.jei.CategoryCombustionCoolant.CoolantDisplay;
import buildcraftreborn.compat.jei.CategoryLaserTable.LaserTableDisplay;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.client.gui.GuiEngineStone_BC8;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.client.gui.GuiAutoCraftItems;
import buildcraftreborn.factory.client.gui.GuiDistiller;
import buildcraftreborn.factory.container.ContainerAutoCraftItems;
import buildcraftreborn.lib.fluid.BCFluidDataMaps;
import buildcraftreborn.lib.fluid.CoolantRegistry;
import buildcraftreborn.lib.fluid.FuelRegistry;
import buildcraftreborn.lib.gui.GuiBC8;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.recipe.AssemblyRecipeRegistry;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.client.gui.GuiProgrammingTable;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;
import buildcraftreborn.robotics.recipe.RobotIntegrationRecipe;
import buildcraftreborn.robotics.tile.TileProgrammingTable;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.client.gui.GuiAdvancedCraftingTable;
import buildcraftreborn.silicon.client.gui.GuiAssemblyTable;
import buildcraftreborn.silicon.client.gui.GuiIntegrationTable;
import buildcraftreborn.silicon.container.ContainerAdvancedCraftingTable;
import buildcraftreborn.silicon.container.ContainerAssemblyTable;
import buildcraftreborn.silicon.recipe.FacadeAssemblyRecipe;

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
 * recipes; the auto workbench and
 * advanced crafting table as crafting tables (with recipe transfer into their blueprints); ledgers kept clear of
 * JEI's item list; and phantom slots that ingredients can be dragged into from JEI.
 * <p>
 * 1.21: on top of 1.12's BuildCraftCompat this also shows facade assembly recipes, combustion engine coolants,
 * clickable progress areas, and the ghost ingredient dragging. JEI loads this class itself, and only on the client;
 * nothing else in the mod refers to it. */
@JeiPlugin
public class BCPluginJEI implements IModPlugin {
    private static final ResourceLocation UID = BuildCraftReborn.id("jei_plugin");

    @Override
    public ResourceLocation getPluginUid() {
        return UID;
    }

    @Override
    public void registerItemSubtypes(ISubtypeRegistration registration) {
        // These items are told apart only by a component (1.12: metadata or NBT)
        componentSubtype(registration, BCSiliconItems.PLUG_GATE.get(), BCSiliconItems.GATE_VARIANT.get());
        componentSubtype(registration, BCSiliconItems.PLUG_LENS.get(), BCSiliconItems.LENS.get());
        componentSubtype(registration, BCSiliconItems.PLUG_FACADE.get(), BCSiliconItems.FACADE.get());
        componentSubtype(registration, BCCoreItems.PAINTBRUSH.get(), BCItemComponents.COLOUR.get());
        componentSubtype(registration, BCCoreItems.GUIDE.get(), BCItemComponents.GUIDE_BOOK.get());
        componentSubtype(registration, BCRoboticsItems.ROBOT.get(), BCRoboticsItems.BOARD.get());
        componentSubtype(registration, BCRoboticsItems.REDSTONE_BOARD.get(), BCRoboticsItems.BOARD.get());
    }

    private static void componentSubtype(ISubtypeRegistration registration, Item item, DataComponentType<?> type) {
        registration.registerSubtypeInterpreter(VanillaTypes.ITEM_STACK, item, new ComponentSubtypeInterpreter(type));
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
            List<AssemblyDisplay> assembly = new ArrayList<>();
            boolean facades = false;
            for (Map.Entry<ResourceLocation, AssemblyRecipe> entry : AssemblyRecipeRegistry.getAll(level).entrySet()) {
                AssemblyDisplay.addAll(entry.getKey(), entry.getValue(), assembly);
                facades |= entry.getValue() instanceof FacadeAssemblyRecipe;
            }
            if (facades) {
                AssemblyDisplay.addFacades(assembly);
            }
            registration.addRecipes(CategoryAssemblyTable.TYPE, assembly);
        }

        RefineryRecipeRegistry refinery = RefineryRecipeRegistry.INSTANCE;
        registration.addRecipes(CategoryDistiller.TYPE, List.copyOf(refinery.getDistillationRegistry().getAllRecipes()));
        registration.addRecipes(CategoryHeatExchange.HEATABLE,
            List.<IHeatExchangerRecipe>copyOf(refinery.getHeatableRegistry().getAllRecipes()));
        registration.addRecipes(CategoryHeatExchange.COOLABLE,
            List.<IHeatExchangerRecipe>copyOf(refinery.getCoolableRegistry().getAllRecipes()));

        registration.addRecipes(CategoryCombustionFuel.TYPE, List.<IFuel>copyOf(FuelRegistry.INSTANCE.getFuels()));
        registration.addRecipes(CategoryCombustionCoolant.TYPE, coolants());
        registration.addRecipes(CategoryLaserTable.PROGRAMMING, programming());
        registration.addRecipes(CategoryLaserTable.INTEGRATION, integration());
    }

    /** Each option of each programming recipe, from the item it programs (a blank board). */
    private static List<LaserTableDisplay> programming() {
        List<LaserTableDisplay> list = new ArrayList<>();
        ItemStack blank = new ItemStack(BCRoboticsItems.REDSTONE_BOARD.get());
        for (IProgrammingRecipe recipe : BuildcraftRecipeRegistry.programmingRecipes.getRecipes()) {
            if (!recipe.canCraft(blank)) {
                continue;
            }
            for (ItemStack option : recipe.getOptions(TileProgrammingTable.WIDTH, TileProgrammingTable.HEIGHT)) {
                ResourceLocation board = ResourceLocation.tryParse(ItemRedstoneBoard.getBoardNBT(option).getID());
                String path = board == null ? Integer.toString(list.size()) : board.getPath();
                list.add(new LaserTableDisplay(BuildCraftReborn.id("programming/" + path), List.of(List.of(blank)),
                    option, recipe.getPowerCost(option)));
            }
        }
        return list;
    }

    /** The robot recipe for each board (the integration table has no other recipes in BuildCraft). */
    private static List<LaserTableDisplay> integration() {
        List<LaserTableDisplay> list = new ArrayList<>();
        RobotIntegrationRecipe recipe = new RobotIntegrationRecipe();
        ItemStack robot = new ItemStack(BCRoboticsItems.ROBOT.get());
        for (RedstoneBoardNBT<?> nbt : RedstoneBoardRegistry.instance.getAllBoardNBTs()) {
            ItemStack board = ItemRedstoneBoard.createStack(nbt);
            ItemStack output = recipe.getOutput(robot, NonNullList.of(ItemStack.EMPTY, board));
            ResourceLocation id = ResourceLocation.tryParse(nbt.getID());
            if (!output.isEmpty() && id != null) {
                list.add(new LaserTableDisplay(BuildCraftReborn.id("integration/robot_" + id.getPath()),
                    List.of(List.of(robot), List.of(board)), output, recipe.getRequiredMicroJoules(output)));
            }
        }
        return list;
    }

    private static List<CoolantDisplay> coolants() {
        List<CoolantDisplay> list = new ArrayList<>();
        CoolantRegistry.INSTANCE.getCoolants().forEach(c -> {
            if (c instanceof CoolantRegistry.Coolant coolant) {
                list.add(new CoolantDisplay(null, coolant.getFluid(), coolant.getDegreesCoolingPerMB()));
            }
        });
        CoolantRegistry.INSTANCE.getSolidCoolants().forEach(c -> {
            if (c instanceof CoolantRegistry.SolidCoolant solid) {
                FluidStack fluid = solid.getMeltedFluid();
                list.add(new CoolantDisplay(solid.getSolid(), fluid, degreesPerMb(fluid)));
            }
        });
        return list;
    }

    private static float degreesPerMb(FluidStack fluid) {
        BCFluidDataMaps.CoolantData data = fluid.getFluidHolder().getData(BCFluidDataMaps.COOLANTS);
        if (data != null) {
            return data.degreesPerMb();
        }
        for (var c : CoolantRegistry.INSTANCE.getCoolants()) {
            if (c instanceof CoolantRegistry.Coolant coolant && coolant.matchesFluid(fluid.copyWithAmount(1))) {
                return coolant.getDegreesCoolingPerMB();
            }
        }
        return 0;
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
    }

    @Override
    public void registerRecipeTransferHandlers(IRecipeTransferRegistration registration) {
        registration.addRecipeTransferHandler(new BlueprintTransferHandler<>(ContainerAutoCraftItems.class,
            BCFactoryBlocks.AUTO_WORKBENCH_ITEMS_MENU.get(), c -> c.tile.invBlueprint), RecipeTypes.CRAFTING);
        registration.addRecipeTransferHandler(new BlueprintTransferHandler<>(ContainerAdvancedCraftingTable.class,
            BCSiliconBlocks.ADVANCED_CRAFTING_TABLE_MENU.get(), c -> c.tile.invBlueprint), RecipeTypes.CRAFTING);
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

    /** A path for a recipe made from a fluid data map entry: {@code <fluid namespace>:<prefix>/<fluid path>}. */
    static ResourceLocation fluidRecipeId(String prefix, FluidStack fluid) {
        ResourceKey<net.minecraft.world.level.material.Fluid> key = fluid.getFluidHolder().unwrapKey().orElse(null);
        ResourceLocation id = key == null ? BuiltInRegistries.FLUID.getKey(fluid.getFluid()) : key.location();
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), prefix + "/" + id.getPath());
    }
}
