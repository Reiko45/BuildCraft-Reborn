/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BucketItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.FurnaceBlock;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.fuels.BuildcraftFuelRegistry;
import buildcraftreborn.api.fuels.IFuel;
import buildcraftreborn.api.fuels.IFuelManager.IDirtyFuel;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IDistillationRecipe;
import buildcraftreborn.api.recipes.IRefineryRecipeManager.IHeatableRecipe;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.tile.TileEngineCreative;
import buildcraftreborn.core.tile.TileEngineRedstone_BC8;
import buildcraftreborn.energy.BCEnergyBlocks;
import buildcraftreborn.energy.BCEnergyConfig;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.energy.BCEnergyTags;
import buildcraftreborn.energy.tile.TileDynamoMJ;
import buildcraftreborn.energy.tile.TileEngineIron_BC8;
import buildcraftreborn.energy.tile.TileEngineRF;
import buildcraftreborn.energy.tile.TileEngineStone_BC8;
import buildcraftreborn.lib.engine.TileEngineBase_BC8;
import buildcraftreborn.lib.fluid.BCFluid;
import buildcraftreborn.lib.recipe.RefineryRecipeRegistry;

/** Phase 2 tests for engines, fuels, fluids and the wrench. Engines sit on top of the test machine (which takes MJ
 * into its battery) with a redstone block above them. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCEnergyTests {
    private static final BlockPos MACHINE = new BlockPos(1, 0, 1);
    private static final BlockPos ENGINE = new BlockPos(1, 1, 1);
    private static final BlockPos POWER = new BlockPos(1, 2, 1);

    private BCEnergyTests() {}

    /** Places the test machine, then the engine on top of it, turns the engine to face it, then powers it. */
    private static <T extends TileEngineBase_BC8> T setUp(GameTestHelper helper, net.minecraft.world.level.block.Block engine) {
        helper.setBlock(MACHINE, BCTestContent.TEST_MACHINE_BLOCK.get());
        helper.setBlock(ENGINE, engine);
        T tile = helper.getBlockEntity(ENGINE);
        tile.rotateIfInvalid();
        return tile;
    }

    private static void power(GameTestHelper helper) {
        helper.setBlock(POWER, Blocks.REDSTONE_BLOCK);
    }

    private static TileTestMachine machine(GameTestHelper helper) {
        return helper.getBlockEntity(MACHINE);
    }

    @GameTest(template = "empty")
    public static void creativeEngineFillsMachine(GameTestHelper helper) {
        TileEngineCreative engine = setUp(helper, BCCoreBlocks.ENGINE_CREATIVE.get());
        helper.assertTrue(engine.getCurrentFacing() == Direction.DOWN, "Engine faces " + engine.getCurrentFacing());
        power(helper);
        helper.assertTrue(engine.isRedstonePowered, "Redstone block didn't power the engine");
        helper.succeedWhen(() -> helper.assertTrue(machine(helper).battery.getStored() > 0,
            "Machine battery is still empty"));
    }

    @GameTest(template = "empty")
    public static void creativeEngineWrenchChangesOutput(GameTestHelper helper) {
        TileEngineCreative engine = setUp(helper, BCCoreBlocks.ENGINE_CREATIVE.get());
        FakePlayer player = player(helper, "bc_creative_engine");
        ItemStack wrench = new ItemStack(BCCoreItems.WRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        long before = engine.getCurrentOutput();
        helper.assertTrue(engine.onActivated(player, InteractionHand.MAIN_HAND, wrench), "Wrench click not handled");
        helper.assertTrue(engine.getCurrentOutput() == 2 * before, "Output went from " + before + " to "
            + engine.getCurrentOutput());
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void stirlingEngineBurnsCoal(GameTestHelper helper) {
        TileEngineStone_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_STONE.get());
        engine.invFuel.setStackInSlot(0, new ItemStack(Items.COAL));
        helper.assertTrue(!engine.invFuel.insertItem(0, new ItemStack(Items.DIRT), true).isEmpty(),
            "Stirling engine accepted dirt as fuel");
        power(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.invFuel.getStackInSlot(0).isEmpty(), "Coal wasn't used");
            helper.assertTrue(engine.isBurning(), "Engine isn't burning");
            helper.assertTrue(machine(helper).battery.getStored() > 0, "Machine battery is still empty");
        });
    }

    @GameTest(template = "empty")
    public static void stirlingEngineReturnsBucket(GameTestHelper helper) {
        TileEngineStone_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_STONE.get());
        engine.invFuel.setStackInSlot(0, new ItemStack(Items.LAVA_BUCKET));
        power(helper);
        helper.succeedWhen(() -> helper.assertTrue(engine.invFuel.getStackInSlot(0).is(Items.BUCKET),
            "Fuel slot holds " + engine.invFuel.getStackInSlot(0)));
    }

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void combustionEngineBurnsOilIntoResidue(GameTestHelper helper) {
        TileEngineIron_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_IRON.get());
        int filled = engine.tankFuel.fill(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 1000),
            FluidAction.EXECUTE);
        helper.assertTrue(filled == 1000, "Fuel tank took " + filled + " mB of oil");
        helper.assertTrue(engine.tankCoolant.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE) == 1000,
            "Coolant tank didn't take water");
        helper.assertTrue(engine.tankFuel.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE) == 0,
            "Fuel tank accepts water");
        power(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.tankFuel.getFluidAmount() < 1000, "No oil was burnt");
            helper.assertTrue(engine.tankResidue.getFluidAmount() > 0, "No residue yet");
            helper.assertTrue(machine(helper).battery.getStored() > 0, "Machine battery is still empty");
        });
    }

    @GameTest(template = "empty")
    public static void combustionEngineTakesIceAsCoolant(GameTestHelper helper) {
        TileEngineIron_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_IRON.get());
        FakePlayer player = player(helper, "bc_ice");
        ItemStack left = engine.tankCoolant.transferStackToTank(player, new ItemStack(Items.ICE));
        helper.assertTrue(left.isEmpty(), "Ice left " + left);
        helper.assertTrue(engine.tankCoolant.getFluid().is(Fluids.WATER) && engine.tankCoolant.getFluidAmount() == 1500,
            "Coolant tank holds " + engine.tankCoolant.getDebugString());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void fuelsComeFromDataMaps(GameTestHelper helper) {
        IFuel oil = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 1));
        helper.assertTrue(oil instanceof IDirtyFuel, "Oil isn't a dirty fuel: " + oil);
        helper.assertTrue(oil.getPowerPerCycle() == 3 * MjAPI.MJ && oil.getTotalBurningTime() == 10_000,
            "Oil gives " + oil.getPowerPerCycle() + " for " + oil.getTotalBurningTime());
        IDirtyFuel dirty = (IDirtyFuel) oil;
        helper.assertTrue(dirty.getResidue().getAmount() == 125
            && dirty.getResidue().is(BCEnergyFluids.oilResidue[0].getSource()), "Residue is " + dirty.getResidue());
        IFuel gas = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(BCEnergyFluids.fuelGaseous[0].getSource(), 1));
        helper.assertTrue(gas != null && !(gas instanceof IDirtyFuel) && gas.getPowerPerCycle() == 8 * MjAPI.MJ,
            "Gaseous fuel is " + gas);
        helper.assertTrue(BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(BCEnergyFluids.crudeOil[1].getSource(), 1)) == null,
            "Hot oil shouldn't be a fuel");
        helper.assertTrue(BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(Fluids.WATER, 1)) == null,
            "Water shouldn't be a fuel");
        helper.assertTrue(BuildcraftFuelRegistry.coolant.getDegreesPerMb(new FluidStack(Fluids.WATER, 1), 100) == 0.0023f,
            "Water cooling is wrong");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void oilAndFuelAreInCommonTags(GameTestHelper helper) {
        Fluid oil = BCEnergyFluids.crudeOil[0].getSource();
        Fluid fuel = BCEnergyFluids.fuelLight[0].getSource();
        for (TagKey<Fluid> tag : List.of(BCEnergyTags.OIL, BCEnergyTags.CRUDE_OIL)) {
            helper.assertTrue(isIn(oil, tag), "Oil isn't in " + tag.location());
            helper.assertFalse(isIn(BCEnergyFluids.crudeOil[1].getSource(), tag), "Hot oil is in " + tag.location());
            helper.assertFalse(isIn(BCEnergyFluids.crudeOil[0].flowing.get(), tag),
                "Flowing oil is in " + tag.location());
        }
        for (TagKey<Fluid> tag : List.of(BCEnergyTags.FUEL, BCEnergyTags.LIGHT_FUEL)) {
            helper.assertTrue(isIn(fuel, tag), "Light fuel isn't in " + tag.location());
            helper.assertFalse(isIn(BCEnergyFluids.fuelDense[0].getSource(), tag), "Dense fuel is in " + tag.location());
        }
        for (BCFluid gas : BCEnergyFluids.fuelGaseous) {
            helper.assertTrue(isIn(gas.getSource(), Tags.Fluids.GASEOUS), gas.getSource() + " isn't gaseous");
        }
        helper.assertFalse(isIn(oil, Tags.Fluids.GASEOUS), "Oil is gaseous");
        helper.succeed();
    }

    /** Whatever is in the common oil and fuel tags (other mods' fluids too) gets BuildCraft's oil and light fuel
     * values. In a development run with {@code -Pinterop_runtime=true} the tags hold PneumaticCraft's oil as well. */
    @GameTest(template = "empty")
    public static void taggedOilAndFuelWorkInBuildCraftMachines(GameTestHelper helper) {
        FluidStack bcOil = new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 1);
        IFuel oilFuel = BuildcraftFuelRegistry.fuel.getFuel(bcOil);
        IDistillationRecipe oilDistilling = RefineryRecipeRegistry.INSTANCE.distillationRegistry.getRecipeForInput(bcOil);
        IHeatableRecipe oilHeating = RefineryRecipeRegistry.INSTANCE.heatableRegistry.getRecipeForInput(bcOil);
        helper.assertTrue(oilFuel != null && oilDistilling != null && oilHeating != null, "Oil isn't fully set up");
        for (Fluid fluid : tagged(List.of(BCEnergyTags.OIL, BCEnergyTags.CRUDE_OIL))) {
            FluidStack stack = new FluidStack(fluid, 1);
            String name = BuiltInRegistries.FLUID.getKey(fluid).toString();
            helper.assertTrue(sameFuel(BuildcraftFuelRegistry.fuel.getFuel(stack), oilFuel),
                name + " doesn't burn like oil");
            IDistillationRecipe distil = RefineryRecipeRegistry.INSTANCE.distillationRegistry.getRecipeForInput(stack);
            helper.assertTrue(distil != null && distil.powerRequired() == oilDistilling.powerRequired()
                && FluidStack.matches(distil.outGas(), oilDistilling.outGas())
                && FluidStack.matches(distil.outLiquid(), oilDistilling.outLiquid()), name + " doesn't distil like oil");
            IHeatableRecipe heat = RefineryRecipeRegistry.INSTANCE.heatableRegistry.getRecipeForInput(stack);
            helper.assertTrue(heat != null && FluidStack.matches(heat.out(), oilHeating.out()),
                name + " doesn't heat like oil");
        }
        IFuel lightFuel = BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(BCEnergyFluids.fuelLight[0].getSource(), 1));
        for (Fluid fluid : tagged(List.of(BCEnergyTags.FUEL, BCEnergyTags.LIGHT_FUEL))) {
            helper.assertTrue(sameFuel(BuildcraftFuelRegistry.fuel.getFuel(new FluidStack(fluid, 1)), lightFuel),
                BuiltInRegistries.FLUID.getKey(fluid) + " doesn't burn like light fuel");
        }
        helper.succeed();
    }

    private static boolean isIn(Fluid fluid, TagKey<Fluid> tag) {
        return fluid.builtInRegistryHolder().is(tag);
    }

    private static Set<Fluid> tagged(List<TagKey<Fluid>> tags) {
        Set<Fluid> fluids = new LinkedHashSet<>();
        tags.forEach(tag -> BuiltInRegistries.FLUID.getTagOrEmpty(tag).forEach(h -> fluids.add(h.value())));
        return fluids;
    }

    private static boolean sameFuel(IFuel a, IFuel b) {
        if (a == null || b == null || a.getPowerPerCycle() != b.getPowerPerCycle()
            || a.getTotalBurningTime() != b.getTotalBurningTime()) {
            return false;
        }
        if (a instanceof IDirtyFuel dirtyA) {
            return b instanceof IDirtyFuel dirtyB && FluidStack.matches(dirtyA.getResidue(), dirtyB.getResidue());
        }
        return !(b instanceof IDirtyFuel);
    }

    @GameTest(template = "empty")
    public static void redstoneEngineOnlyPowersRedstoneReceivers(GameTestHelper helper) {
        TileEngineRedstone_BC8 engine = setUp(helper, BCCoreBlocks.ENGINE_WOOD.get());
        // The test machine is a normal MJ receiver, which redstone engines can't power
        helper.assertTrue(engine.getReceiverToPower(Direction.DOWN) == null, "Redstone engine connected");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wrenchRotatesFurnace(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.FURNACE.defaultBlockState().setValue(FurnaceBlock.FACING, Direction.NORTH));
        FakePlayer player = player(helper, "bc_wrench");
        useWrench(helper, player, pos);
        helper.assertBlockProperty(pos, FurnaceBlock.FACING, Direction.EAST);
        useWrench(helper, player, pos);
        helper.assertBlockProperty(pos, FurnaceBlock.FACING, Direction.SOUTH);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void wrenchTurnsEngineBetweenReceivers(GameTestHelper helper) {
        TileEngineStone_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_STONE.get());
        helper.setBlock(new BlockPos(2, 1, 1), BCTestContent.TEST_MACHINE_BLOCK.get());
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(ENGINE);
        helper.assertTrue(engine.getCurrentFacing() == Direction.DOWN, "Engine faces " + engine.getCurrentFacing());
        helper.assertTrue(level.getCapability(MjAPI.CAP_CONNECTOR, abs, Direction.DOWN) != null,
            "No MJ connector on the output side");
        FakePlayer player = player(helper, "bc_engine_wrench");
        useWrench(helper, player, ENGINE);
        helper.assertTrue(engine.getCurrentFacing() == Direction.EAST, "Engine faces " + engine.getCurrentFacing());
        helper.assertTrue(level.getCapability(MjAPI.CAP_CONNECTOR, abs, Direction.EAST) != null,
            "No MJ connector on the new output side");
        helper.assertTrue(level.getCapability(MjAPI.CAP_CONNECTOR, abs, Direction.DOWN) == null,
            "Old output side still has a connector");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void bucketsPlaceOilButGasVaporises(GameTestHelper helper) {
        FakePlayer player = player(helper, "bc_bucket");
        ServerLevel level = helper.getLevel();
        BlockPos oilPos = helper.absolutePos(new BlockPos(0, 1, 0));
        BucketItem oilBucket = BCEnergyFluids.crudeOil[0].bucket.get();
        helper.assertTrue(oilBucket.emptyContents(player, level, oilPos, null, new ItemStack(oilBucket)),
            "Oil bucket didn't empty");
        helper.assertTrue(level.getBlockState(oilPos).is(BCEnergyFluids.crudeOil[0].block.get()),
            "Placed " + level.getBlockState(oilPos));

        BlockPos gasPos = helper.absolutePos(new BlockPos(2, 1, 2));
        BucketItem gasBucket = BCEnergyFluids.fuelGaseous[0].bucket.get();
        helper.assertTrue(BCEnergyFluids.fuelGaseous[0].block == null, "Gaseous fuel has a block");
        helper.assertTrue(gasBucket.emptyContents(player, level, gasPos, null, new ItemStack(gasBucket)),
            "Gas bucket didn't empty");
        helper.assertTrue(level.getBlockState(gasPos).isAir(), "Gas left " + level.getBlockState(gasPos));

        // BuildCraft buckets must have a fluid handler, so they can fill and empty tanks
        TileEngineIron_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_IRON.get());
        ItemStack left = engine.tankFuel.transferStackToTank(player, new ItemStack(BCEnergyFluids.fuelLight[0].bucket.get()));
        helper.assertTrue(left.is(Items.BUCKET), "Bucket became " + left);
        helper.assertTrue(engine.tankFuel.getFluidAmount() == 1000, "Fuel tank holds " + engine.tankFuel.getDebugString());
        helper.succeed();
    }

    private static void useWrench(GameTestHelper helper, FakePlayer player, BlockPos relative) {
        BlockPos abs = helper.absolutePos(relative);
        ItemStack wrench = new ItemStack(BCCoreItems.WRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        wrench.useOn(new UseOnContext(player, InteractionHand.MAIN_HAND, hit));
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    /** A broken engine drops the fluid in its tanks as fragile fluid shards of half a bucket each. A shard can be
     * emptied but not filled, and is used up once empty. */
    @GameTest(template = "empty")
    public static void brokenEngineDropsFluidShards(GameTestHelper helper) {
        TileEngineIron_BC8 engine = setUp(helper, BCEnergyBlocks.ENGINE_IRON.get());
        engine.tankFuel.fill(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 1200), FluidAction.EXECUTE);
        net.minecraft.core.NonNullList<ItemStack> drops = net.minecraft.core.NonNullList.create();
        engine.addDrops(drops);
        helper.assertTrue(drops.size() == 3, "Expected 3 shards, got " + drops);
        int total = 0;
        for (ItemStack drop : drops) {
            helper.assertTrue(drop.is(BCCoreItems.FRAGILE_FLUID_SHARD.get()), "Not a shard: " + drop);
            FluidStack inside = net.neoforged.neoforge.fluids.FluidUtil.getFluidContained(drop).orElse(FluidStack.EMPTY);
            helper.assertTrue(inside.getFluid() == BCEnergyFluids.crudeOil[0].getSource(), "Shard holds " + inside);
            total += inside.getAmount();
        }
        helper.assertTrue(total == 1200, "Shards hold " + total + " mB");

        ItemStack shard = drops.get(2).copy();
        var handler = net.neoforged.neoforge.fluids.FluidUtil.getFluidHandler(shard).orElseThrow();
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 100), FluidAction.EXECUTE) == 0,
            "Shards shouldn't fill");
        helper.assertTrue(handler.drain(1000, FluidAction.EXECUTE).getAmount() == 200, "Shard didn't empty");
        helper.assertTrue(handler.getContainer().isEmpty(), "Empty shard wasn't used up");
        String name = drops.get(0).getHoverName().getString();
        helper.assertTrue(name.startsWith("Fragile Oil Shard"), "Shard is called " + name);

        // Breaking the block drops them as items
        helper.destroyBlock(ENGINE);
        helper.assertItemEntityCountIs(BCCoreItems.FRAGILE_FLUID_SHARD.get(), ENGINE, 2.0, 3);
        helper.succeed();
    }

    /** Oil (density 900) floats on water (1000) instead of flowing down into it; residue (1200) sinks into it. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void oilFloatsOnWaterButResidueSinks(GameTestHelper helper) {
        for (int x = 0; x < 3; x++) {
            for (int z = 0; z < 3; z++) {
                for (int y = 0; y < 2; y++) {
                    helper.setBlock(new BlockPos(x, y, z), Blocks.STONE);
                }
            }
        }
        BlockPos oilColumn = new BlockPos(0, 0, 1);
        BlockPos residueColumn = new BlockPos(2, 0, 1);
        helper.setBlock(oilColumn, Blocks.WATER);
        helper.setBlock(residueColumn, Blocks.WATER);
        helper.setBlock(oilColumn.above(), BCEnergyFluids.crudeOil[0].getBlockOrNull());
        helper.setBlock(residueColumn.above(), BCEnergyFluids.oilResidue[0].getBlockOrNull());
        helper.runAfterDelay(60, () -> {
            helper.assertTrue(helper.getLevel().getFluidState(helper.absolutePos(oilColumn)).is(Fluids.WATER),
                "Oil sank into the water");
            helper.assertTrue(helper.getLevel().getFluidState(helper.absolutePos(residueColumn)).getType()
                .isSame(BCEnergyFluids.oilResidue[0].getSource()), "Residue didn't sink into the water");
            helper.succeed();
        });
    }

    // RF engine and MJ dynamo

    /** The RF engine takes Forge Energy on any side and powers the machine under it with MJ (4 MJ/t base). */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void rfEngineTurnsForgeEnergyIntoMj(GameTestHelper helper) {
        TileEngineRF engine = setUp(helper, BCEnergyBlocks.ENGINE_RF.get());
        helper.assertTrue(engine.getCurrentFacing() == Direction.DOWN, "Engine faces " + engine.getCurrentFacing());
        IEnergyStorage fe = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
            helper.absolutePos(ENGINE), Direction.NORTH);
        helper.assertTrue(fe != null && fe.canReceive() && !fe.canExtract(), "No Forge Energy input: " + fe);
        int accepted = fe.receiveEnergy(20_000, false);
        helper.assertTrue(accepted == TileEngineRF.MAX_RF, "Took " + accepted + " FE");
        helper.assertTrue(engine.getMjPerTick() == 4 * MjAPI.MJ, "Base rate is " + engine.getMjPerTick());
        power(helper);
        helper.succeedWhen(() -> {
            helper.assertTrue(engine.getCurrentRF() < TileEngineRF.MAX_RF, "No FE was used");
            helper.assertTrue(machine(helper).battery.getStored() > 0, "Machine battery is still empty");
        });
    }

    /** Gears in the upgrade slots raise the conversion rate: +2 MJ/t for iron, +3 for gold, one gear per slot (so
     * two iron gears and a gold one give 4 + 2 + 2 + 3 MJ/t). */
    @GameTest(template = "empty")
    public static void rfEngineUpgradesRaiseItsRate(GameTestHelper helper) {
        TileEngineRF engine = setUp(helper, BCEnergyBlocks.ENGINE_RF.get());
        ItemStack left = engine.invUpgrades.insert(new ItemStack(BCCoreItems.GEAR_IRON.get(), 2), false, false);
        helper.assertTrue(left.isEmpty() && engine.invUpgrades.getStackInSlot(0).getCount() == 1
            && engine.invUpgrades.getStackInSlot(1).getCount() == 1, "Two gears didn't take one slot each");
        engine.invUpgrades.insert(new ItemStack(BCCoreItems.GEAR_GOLD.get()), false, false);
        helper.assertFalse(engine.invUpgrades.insert(new ItemStack(Items.DIAMOND), false, true).isEmpty(),
            "Accepted a diamond");
        helper.assertTrue(engine.getMjPerTick() == 11 * MjAPI.MJ, "Upgraded rate is " + engine.getMjPerTick());
        helper.succeed();
    }

    /** MJ put into the dynamo comes out of its front as Forge Energy, here into an RF engine. */
    @GameTest(template = "empty", timeoutTicks = 200)
    public static void mjDynamoTurnsMjIntoForgeEnergy(GameTestHelper helper) {
        helper.setBlock(ENGINE, BCEnergyBlocks.ENGINE_RF.get());
        helper.setBlock(POWER, BCEnergyBlocks.MJ_DYNAMO.get());
        TileEngineRF receiver = helper.getBlockEntity(ENGINE);
        TileDynamoMJ dynamo = helper.getBlockEntity(POWER);
        dynamo.rotateIfInvalid();
        helper.assertTrue(dynamo.getCurrentDirection() == Direction.DOWN, "Dynamo faces "
            + dynamo.getCurrentDirection());
        IMjReceiver mj = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, helper.absolutePos(POWER), Direction.UP);
        helper.assertTrue(mj != null, "Dynamo takes no MJ on its back");
        long excess = mj.receivePower(500 * MjAPI.MJ, false);
        helper.assertTrue(excess == 0, "Dynamo refused " + MjAPI.formatMj(excess) + " MJ");
        helper.assertTrue(helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, helper.absolutePos(POWER),
            Direction.DOWN) == null, "Dynamo takes MJ on its front");
        helper.setBlock(POWER.east(), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> helper.assertTrue(receiver.getCurrentRF() > 0, "No FE reached the RF engine"));
    }

    /** Both are off by default (as in 1.12): no recipe is loaded for them. */
    @GameTest(template = "empty")
    public static void rfEngineAndDynamoRecipesFollowTheConfig(GameTestHelper helper) {
        var recipes = helper.getLevel().getRecipeManager();
        helper.assertTrue(recipes.byKey(BuildCraftReborn.id("engine_rf")).isPresent() == BCEnergyConfig.enableRfEngine(),
            "RF engine recipe doesn't follow enableRfEngine");
        helper.assertTrue(recipes.byKey(BuildCraftReborn.id("mj_dynamo")).isPresent() == BCEnergyConfig.enableMjDynamo(),
            "MJ dynamo recipe doesn't follow enableMjDynamo");
        helper.assertTrue(recipes.byKey(BuildCraftReborn.id("engine_iron")).isPresent(), "Engine recipes are missing");
        helper.succeed();
    }
}
