/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;

import net.neoforged.fml.ModList;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.fuels.BuildcraftFuelRegistry;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.energy.BCRefinedFuels;
import buildcraftreborn.energy.tile.TileEngineIron_BC8;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileDistiller_BC8;

/** Tests with another mod's fluids, run only when that mod is loaded ({@code gradlew runGameTestServer
 * -Pinterop_runtime=true} adds PneumaticCraft). They check that oil and refined fuels from another mod work in
 * BuildCraft's machines through the common tags, and that BuildCraft's oil is in the tag the other mod's machines
 * take. */
@GameTestHolder(BuildCraftReborn.MODID)
public final class BCInteropTests {
    private static final String TEMPLATE = BuildCraftReborn.MODID + ":empty";
    private static final ResourceLocation PNC_OIL = ResourceLocation.fromNamespaceAndPath("pneumaticcraft", "oil");

    private BCInteropTests() {}

    @GameTestGenerator
    public static List<TestFunction> interopTests() {
        List<TestFunction> tests = new ArrayList<>();
        if (ModList.get().isLoaded("pneumaticcraft")) {
            add(tests, "pneumaticcraft_oil_burns_in_combustion_engine", 400,
                BCInteropTests::pneumaticCraftOilBurnsInCombustionEngine);
            add(tests, "pneumaticcraft_oil_distils", 400, BCInteropTests::pneumaticCraftOilDistils);
            add(tests, "pneumaticcraft_refines_buildcraft_oil", 20, BCInteropTests::pneumaticCraftRefinesBuildCraftOil);
            add(tests, "pneumaticcraft_diesel_burns_in_combustion_engine", 400,
                BCInteropTests::pneumaticCraftDieselBurnsInCombustionEngine);
            add(tests, "pneumaticcraft_fuels_burn_with_their_values", 20,
                BCInteropTests::pneumaticCraftFuelsBurnWithTheirValues);
        }
        return tests;
    }

    private static void add(List<TestFunction> tests, String name, int timeout, Consumer<GameTestHelper> test) {
        tests.add(new TestFunction("defaultBatch", "bcinteroptests." + name, TEMPLATE, timeout, 0, true, test));
    }

    private static Fluid pncOil(GameTestHelper helper) {
        Fluid oil = BuiltInRegistries.FLUID.get(PNC_OIL);
        helper.assertTrue(oil != Fluids.EMPTY, "PneumaticCraft has no " + PNC_OIL);
        return oil;
    }

    private static Fluid pncFluid(GameTestHelper helper, String path) {
        ResourceLocation id = ResourceLocation.fromNamespaceAndPath("pneumaticcraft", path);
        Fluid fluid = BuiltInRegistries.FLUID.get(id);
        helper.assertTrue(fluid != Fluids.EMPTY, "PneumaticCraft has no " + id);
        return fluid;
    }

    /** A running combustion engine with a bucket of the fluid, on the test machine (which takes the engine's MJ).
     * Succeeds once {@code burning} passes. */
    private static void burnInCombustionEngine(GameTestHelper helper, Fluid fuel,
        BiConsumer<TileEngineIron_BC8, TileTestMachine> burning) {
        BlockPos machinePos = new BlockPos(1, 0, 1);
        BlockPos enginePos = new BlockPos(1, 1, 1);
        helper.setBlock(machinePos, BCTestContent.TEST_MACHINE_BLOCK.get());
        helper.setBlock(enginePos, BCEnergyBlocks.ENGINE_IRON.get());
        TileEngineIron_BC8 engine = helper.getBlockEntity(enginePos);
        engine.rotateIfInvalid();
        int filled = engine.tankFuel.fill(new FluidStack(fuel, 1000), FluidAction.EXECUTE);
        helper.assertTrue(filled == 1000, "Fuel tank took " + filled + " mB of " + BuiltInRegistries.FLUID.getKey(fuel));
        engine.tankCoolant.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        helper.setBlock(new BlockPos(1, 2, 1), Blocks.REDSTONE_BLOCK);
        TileTestMachine machine = helper.getBlockEntity(machinePos);
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.tankFuel.getFluidAmount() < 1000, "Nothing was burnt");
            helper.assertTrue(machine.battery.getStored() > 0, "Machine battery is still empty");
            burning.accept(engine, machine);
        });
    }

    private static void pneumaticCraftOilBurnsInCombustionEngine(GameTestHelper helper) {
        burnInCombustionEngine(helper, pncOil(helper), (engine, machine) -> {
            // Oil burns dirty: the residue is BuildCraft's
            helper.assertTrue(engine.tankResidue.getFluid().is(BCEnergyFluids.oilResidue[0].getSource()),
                "Residue tank holds " + engine.tankResidue.getDebugString());
        });
    }

    private static void pneumaticCraftDieselBurnsInCombustionEngine(GameTestHelper helper) {
        burnInCombustionEngine(helper, pncFluid(helper, "diesel"), (engine, machine) -> {
            helper.assertTrue(engine.tankResidue.isEmpty(), "Diesel left " + engine.tankResidue.getDebugString());
        });
    }

    /** PneumaticCraft's refined fuels are in the common tags BuildCraft burns them through (BCRefinedFuels), and burn
     * with those values. */
    private static void pneumaticCraftFuelsBurnWithTheirValues(GameTestHelper helper) {
        Map<String, TagKey<Fluid>> fuels = Map.of("diesel", BCEnergyTags.DIESEL, "gasoline", BCEnergyTags.GASOLINE,
            "kerosene", BCEnergyTags.KEROSENE, "lpg", BCEnergyTags.LPG, "biodiesel", BCEnergyTags.BIODIESEL,
            "ethanol", BCEnergyTags.ETHANOL);
        fuels.forEach((path, tag) -> {
            Fluid fluid = pncFluid(helper, path);
            helper.assertTrue(fluid.builtInRegistryHolder().is(tag), "PneumaticCraft's " + path + " isn't in "
                + tag.location() + " any more");
            BCRefinedFuels.RefinedFuel expected = BCRefinedFuels.ALL.stream().filter(f -> f.tag().equals(tag))
                .findFirst().orElseThrow();
            IFuel burns = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(fluid, 1));
            helper.assertTrue(burns != null && burns.getPowerPerCycle() == Math.round(expected.powerPerTick() * MjAPI.MJ)
                && burns.getTotalBurningTime() == expected.burnTime(), "PneumaticCraft's " + path
                    + " doesn't burn with " + tag.location() + "'s values");
        });
        helper.succeed();
    }

    private static void pneumaticCraftOilDistils(GameTestHelper helper) {
        Fluid oil = pncOil(helper);
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCFactoryBlocks.DISTILLER.get());
        TileDistiller_BC8 distiller = helper.getBlockEntity(pos);
        BlockPos abs = helper.absolutePos(pos);
        IFluidHandler in = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, abs, Direction.NORTH);
        helper.assertTrue(in.fill(new FluidStack(oil, 4000), FluidAction.EXECUTE) == 4000,
            "Distiller didn't take PneumaticCraft oil");
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, abs, Direction.UP);
            if (receiver != null && receiver.canReceive()) {
                receiver.receivePower(6 * MjAPI.MJ, false);
            }
        });
        helper.succeedWhen(() -> {
            // The same products as BuildCraft's own oil
            helper.assertTrue(distiller.tankGasOut.getFluid().is(BCEnergyFluids.fuelGaseous[0].getSource()),
                "Gas tank holds " + distiller.tankGasOut.getDebugString());
            helper.assertTrue(distiller.tankLiquidOut.getFluid().is(BCEnergyFluids.oilHeavy[0].getSource()),
                "Liquid tank holds " + distiller.tankLiquidOut.getDebugString());
        });
    }

    /** PneumaticCraft's refinery and liquid compressor take {@code #c:crude_oil}, and PneumaticCraft puts its own oil
     * there: BuildCraft's oil must be in that same tag, next to it. */
    private static void pneumaticCraftRefinesBuildCraftOil(GameTestHelper helper) {
        Fluid oil = pncOil(helper);
        helper.assertTrue(oil.builtInRegistryHolder().is(BCEnergyTags.CRUDE_OIL),
            "PneumaticCraft's oil isn't in c:crude_oil any more");
        helper.assertTrue(BCEnergyFluids.crudeOil[0].getSource().builtInRegistryHolder().is(BCEnergyTags.CRUDE_OIL),
            "BuildCraft's oil isn't in c:crude_oil");
        helper.succeed();
    }
}
