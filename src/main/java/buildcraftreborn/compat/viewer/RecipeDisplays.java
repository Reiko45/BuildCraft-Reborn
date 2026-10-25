/*
 * Based on BuildCraftCompat, (c) the BuildCraft Team, http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraftCompat is distributed under the terms of the Minecraft Mod Public License 1.0.1, or MMPL (a copy is in
 * LICENSE-ASSETS).
 */
package buildcraftreborn.compat.viewer;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import net.minecraft.core.NonNullList;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluid;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardNBT;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.api.recipes.BuildcraftRecipeRegistry;
import buildcraftreborn.api.recipes.IProgrammingRecipe;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.lib.fluid.BCFluidDataMaps;
import buildcraftreborn.lib.fluid.CoolantRegistry;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.lib.recipe.AssemblyRecipeRegistry;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;
import buildcraftreborn.robotics.recipe.RobotIntegrationRecipe;
import buildcraftreborn.robotics.tile.TileProgrammingTable;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.recipe.FacadeAssemblyRecipe;

/** What the JEI and EMI plugins show that isn't in the vanilla recipe manager, built the same way for both: the items
 * told apart by a component, and the recipes of BuildCraft's own machines. Neither viewer's API is used here, so this
 * can be loaded with either (or both) installed. */
public final class RecipeDisplays {
    private RecipeDisplays() {}

    /** The items that are told apart only by a component (1.12: metadata or NBT), and that component. */
    public static Map<Item, DataComponentType<?>> componentSubtypes() {
        Map<Item, DataComponentType<?>> map = new LinkedHashMap<>();
        map.put(BCSiliconItems.PLUG_GATE.get(), BCSiliconItems.GATE_VARIANT.get());
        map.put(BCSiliconItems.PLUG_LENS.get(), BCSiliconItems.LENS.get());
        map.put(BCSiliconItems.PLUG_FACADE.get(), BCSiliconItems.FACADE.get());
        map.put(BCCoreItems.PAINTBRUSH.get(), BCItemComponents.COLOUR.get());
        map.put(BCCoreItems.GUIDE.get(), BCItemComponents.GUIDE_BOOK.get());
        // (a robot's power isn't part of what it is, as in 7.1's NEI)
        map.put(BCRoboticsItems.ROBOT.get(), BCRoboticsItems.BOARD.get());
        map.put(BCRoboticsItems.REDSTONE_BOARD.get(), BCRoboticsItems.BOARD.get());
        return map;
    }

    /** Each output of each assembly recipe, and each facade (normal and hollow) if the facade recipe is loaded. */
    public static List<AssemblyDisplay> assembly(Level level) {
        List<AssemblyDisplay> assembly = new ArrayList<>();
        boolean facades = false;
        for (Map.Entry<ResourceLocation, AssemblyRecipe> entry : AssemblyRecipeRegistry.getAll(level).entrySet()) {
            AssemblyDisplay.addAll(entry.getKey(), entry.getValue(), assembly);
            facades |= entry.getValue() instanceof FacadeAssemblyRecipe;
        }
        if (facades) {
            AssemblyDisplay.addFacades(assembly);
        }
        return assembly;
    }

    /** Each option of each programming recipe, from the item it programs (a blank board). */
    public static List<LaserTableDisplay> programming() {
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
    public static List<LaserTableDisplay> integration() {
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

    /** The fluid coolants, then the items that melt into one. */
    public static List<CoolantDisplay> coolants() {
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

    /** A path for a recipe made from a fluid data map entry: {@code <fluid namespace>:<prefix>/<fluid path>}. */
    public static ResourceLocation fluidRecipeId(String prefix, FluidStack fluid) {
        ResourceKey<Fluid> key = fluid.getFluidHolder().unwrapKey().orElse(null);
        ResourceLocation id = key == null ? BuiltInRegistries.FLUID.getKey(fluid.getFluid()) : key.location();
        return ResourceLocation.fromNamespaceAndPath(id.getNamespace(), prefix + "/" + id.getPath());
    }
}
