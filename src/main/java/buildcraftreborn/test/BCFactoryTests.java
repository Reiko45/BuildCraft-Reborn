/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.properties.BuildCraftProperties;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.energy.BCEnergyFluids;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.BCFactoryItems;
import buildcraftreborn.factory.block.BlockFloodGate;
import buildcraftreborn.factory.block.BlockHeatExchange;
import buildcraftreborn.factory.block.BlockHeatExchange.EnumExchangePart;
import buildcraftreborn.factory.block.BlockWaterGel;
import buildcraftreborn.factory.block.BlockWaterGel.GelStage;
import buildcraftreborn.factory.tile.TileAutoWorkbenchItems;
import buildcraftreborn.factory.tile.TileChute;
import buildcraftreborn.factory.tile.TileDistiller_BC8;
import buildcraftreborn.factory.tile.TileHeatExchange;
import buildcraftreborn.factory.tile.TileHeatExchange.ExchangeSection;
import buildcraftreborn.factory.tile.TilePump;
import buildcraftreborn.factory.tile.TileTank;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** Phase 3 tests: one or more for each factory machine. Machines that need power get it straight into their MJ
 * receiver every tick, so the tests don't depend on engine behaviour. Most use the 7x9x7 {@code factory} template. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCFactoryTests {
    private BCFactoryTests() {}

    // Tank

    @GameTest(template = "empty")
    public static void tanksStackAndFillFromTheBottom(GameTestHelper helper) {
        BlockPos bottom = new BlockPos(1, 0, 1);
        for (int i = 0; i < 3; i++) {
            place(helper, bottom.above(i), BCFactoryBlocks.TANK.get().defaultBlockState());
        }
        helper.assertBlockProperty(bottom, BuildCraftProperties.JOINED_BELOW, false);
        helper.assertBlockProperty(bottom.above(), BuildCraftProperties.JOINED_BELOW, true);
        helper.assertBlockProperty(bottom.above(2), BuildCraftProperties.JOINED_BELOW, true);

        // Fill through the top tank: the fluid sinks to the bottom of the column
        IFluidHandler top = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
            helper.absolutePos(bottom.above(2)), Direction.UP);
        helper.assertTrue(top != null, "Tank has no fluid handler");
        int filled = top.fill(new FluidStack(Fluids.WATER, 40_000), FluidAction.EXECUTE);
        helper.assertTrue(filled == 40_000, "Column took " + filled + " mB");
        int[] expected = { 16_000, 16_000, 8_000 };
        for (int i = 0; i < 3; i++) {
            TileTank tank = helper.getBlockEntity(bottom.above(i));
            helper.assertTrue(tank.tank.getFluidAmount() == expected[i],
                "Tank " + i + " holds " + tank.tank.getDebugString());
        }
        // Draining takes from the top
        FluidStack drained = top.drain(10_000, FluidAction.EXECUTE);
        TileTank topTank = helper.getBlockEntity(bottom.above(2));
        TileTank middleTank = helper.getBlockEntity(bottom.above());
        helper.assertTrue(drained.getAmount() == 10_000 && topTank.tank.isEmpty()
            && middleTank.tank.getFluidAmount() == 14_000, "Drained " + drained.getAmount() + ", left "
                + middleTank.tank.getDebugString() + " / " + topTank.tank.getDebugString());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tankTakesBucketsAndComparatorReadsIt(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 0, 1);
        helper.setBlock(pos, BCFactoryBlocks.TANK.get());
        TileTank tank = helper.getBlockEntity(pos);
        FakePlayer player = player(helper, "bc_tank");
        ItemStack bucket = new ItemStack(Items.WATER_BUCKET);
        player.setItemInHand(InteractionHand.MAIN_HAND, bucket);
        helper.assertTrue(tank.onActivated(player, InteractionHand.MAIN_HAND, bucket), "Bucket click not handled");
        helper.assertTrue(tank.tank.getFluidAmount() == 1000, "Tank holds " + tank.tank.getDebugString());
        int level = helper.getBlockState(pos).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(pos));
        helper.assertTrue(level > 0, "Comparator reads " + level);
        helper.succeed();
    }

    // Mining well

    @GameTest(template = "factory", timeoutTicks = 600)
    public static void miningWellDigsDownToBedrock(GameTestHelper helper) {
        BlockPos well = new BlockPos(3, 6, 3);
        helper.setBlock(new BlockPos(3, 1, 3), Blocks.BEDROCK);
        for (int y = 2; y < 6; y++) {
            helper.setBlock(new BlockPos(3, y, 3), Blocks.STONE);
        }
        BlockPos chestPos = new BlockPos(3, 6, 4);
        helper.setBlock(chestPos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        helper.setBlock(well, BCFactoryBlocks.MINING_WELL.get());
        powerEachTick(helper, well, 10 * MjAPI.MJ);
        helper.succeedWhen(() -> {
            // Once the well has nothing left to mine its tube pulls back up (as in 1.12)
            for (int y = 2; y < 6; y++) {
                BlockState state = helper.getBlockState(new BlockPos(3, y, 3));
                helper.assertTrue(state.isAir(), "y=" + y + " is " + state);
            }
            helper.assertBlockPresent(Blocks.BEDROCK, new BlockPos(3, 1, 3));
            ChestBlockEntity chest = helper.getBlockEntity(chestPos);
            helper.assertTrue(chest.countItem(Items.COBBLESTONE) == 4, "Chest holds "
                + chest.countItem(Items.COBBLESTONE) + " cobblestone");
        });
    }

    // Pump

    @GameTest(template = "factory", timeoutTicks = 400)
    public static void pumpDrainsLavaPool(GameTestHelper helper) {
        // A 2x2 lava pool on a stone floor, walled in
        for (int x = 1; x <= 6; x++) {
            for (int z = 1; z <= 6; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                boolean pool = (x == 3 || x == 4) && (z == 3 || z == 4);
                if (!pool && x >= 2 && x <= 5 && z >= 2 && z <= 5) {
                    helper.setBlock(new BlockPos(x, 1, z), Blocks.STONE);
                }
            }
        }
        BlockPos[] pool = { new BlockPos(3, 1, 3), new BlockPos(4, 1, 3), new BlockPos(3, 1, 4),
            new BlockPos(4, 1, 4) };
        for (BlockPos p : pool) {
            helper.setBlock(p, Blocks.LAVA);
        }
        BlockPos pumpPos = new BlockPos(3, 4, 3);
        helper.setBlock(pumpPos, BCFactoryBlocks.PUMP.get());
        powerEachTick(helper, pumpPos, 10 * MjAPI.MJ);
        // While it works, its tube reaches down to the pool
        AtomicBoolean sawTube = new AtomicBoolean();
        helper.onEachTick(() -> sawTube.compareAndSet(false,
            helper.getBlockState(new BlockPos(3, 2, 3)).is(BCFactoryBlocks.TUBE.get())));
        helper.succeedWhen(() -> {
            for (BlockPos p : pool) {
                helper.assertTrue(!helper.getBlockState(p).getFluidState().isSource(), "Lava source left at " + p);
            }
            IFluidHandler handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(pumpPos), Direction.UP);
            helper.assertTrue(sawTube.get(), "The tube never reached the pool");
            FluidStack inTank = handler.getFluidInTank(0);
            helper.assertTrue(inTank.is(Fluids.LAVA) && inTank.getAmount() == 4000, "Pump holds " + inTank.getAmount()
                + " mB of " + inTank.getFluid());
        });
    }

    @GameTest(template = "empty")
    public static void pumpTankCannotBeFilled(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCFactoryBlocks.PUMP.get());
        TilePump pump = helper.getBlockEntity(pos);
        IFluidHandler handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
            helper.absolutePos(pos), Direction.UP);
        helper.assertTrue(handler != null && pump != null, "Pump has no fluid handler");
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE) == 0,
            "Pump tank accepted water");
        helper.succeed();
    }

    // Flood gate

    @GameTest(template = "factory", timeoutTicks = 400)
    public static void floodGateFillsTheHoleBelow(GameTestHelper helper) {
        BlockPos hole = new BlockPos(3, 1, 3);
        helper.setBlock(hole.below(), Blocks.STONE);
        for (Direction side : Direction.Plane.HORIZONTAL) {
            helper.setBlock(hole.relative(side), Blocks.STONE);
        }
        BlockPos gate = hole.above();
        BlockState state = BCFactoryBlocks.FLOOD_GATE.get().defaultBlockState();
        for (Direction side : Direction.Plane.HORIZONTAL) {
            state = state.setValue(BlockFloodGate.CONNECTED_MAP.get(side), false);
        }
        helper.setBlock(gate, state);
        IFluidHandler handler = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
            helper.absolutePos(gate), Direction.UP);
        helper.assertTrue(handler.fill(new FluidStack(Fluids.WATER, 2000), FluidAction.EXECUTE) == 2000,
            "Flood gate didn't take the water");
        helper.succeedWhen(() -> {
            helper.assertTrue(helper.getBlockState(hole).getFluidState().isSourceOfType(Fluids.WATER),
                "Hole holds " + helper.getBlockState(hole));
            // Closed sides are left alone
            for (Direction side : Direction.Plane.HORIZONTAL) {
                helper.assertTrue(helper.getBlockState(gate.relative(side)).isAir(), "Flooded " + side);
            }
        });
    }

    @GameTest(template = "empty")
    public static void wrenchOpensAndClosesFloodGateSides(GameTestHelper helper) {
        BlockPos gate = new BlockPos(1, 1, 1);
        helper.setBlock(gate, BCFactoryBlocks.FLOOD_GATE.get());
        helper.assertBlockProperty(gate, BuildCraftProperties.CONNECTED_NORTH, true);
        FakePlayer player = player(helper, "bc_flood_gate");
        useWrench(helper, player, gate, Direction.NORTH);
        helper.assertBlockProperty(gate, BuildCraftProperties.CONNECTED_NORTH, false);
        useWrench(helper, player, gate, Direction.NORTH);
        helper.assertBlockProperty(gate, BuildCraftProperties.CONNECTED_NORTH, true);
        helper.succeed();
    }

    // Chute

    @GameTest(template = "factory", timeoutTicks = 300)
    public static void chutePicksUpItemsIntoChest(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(3, 1, 3);
        helper.setBlock(chestPos, Blocks.CHEST);
        BlockPos chutePos = chestPos.above();
        place(helper, chutePos, BCFactoryBlocks.CHUTE.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.BLOCK_FACING_6, Direction.UP));
        helper.assertBlockProperty(chutePos, BuildCraftProperties.CONNECTED_DOWN, true);
        helper.assertBlockProperty(chutePos, BuildCraftProperties.CONNECTED_NORTH, false);
        // Upward-facing chutes pick up for free (gravity), so no power is needed
        Vec3 above = Vec3.atBottomCenterOf(helper.absolutePos(chutePos.above()));
        ItemEntity item = new ItemEntity(helper.getLevel(), above.x, above.y, above.z,
            new ItemStack(Items.DIAMOND, 3));
        item.setDeltaMovement(Vec3.ZERO);
        helper.getLevel().addFreshEntity(item);
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(chestPos);
            helper.assertTrue(chest.countItem(Items.DIAMOND) == 3, "Chest holds " + chest.countItem(Items.DIAMOND)
                + " diamonds");
        });
    }

    @GameTest(template = "empty")
    public static void chuteInsertsIntoInventoryBelow(GameTestHelper helper) {
        BlockPos chestPos = new BlockPos(1, 0, 1);
        helper.setBlock(chestPos, Blocks.CHEST);
        BlockPos chutePos = chestPos.above();
        helper.setBlock(chutePos, BCFactoryBlocks.CHUTE.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.BLOCK_FACING_6, Direction.UP));
        TileChute chute = helper.getBlockEntity(chutePos);
        chute.inv.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 10));
        helper.succeedWhen(() -> {
            ChestBlockEntity chest = helper.getBlockEntity(chestPos);
            helper.assertTrue(chest.countItem(Items.COBBLESTONE) == 10, "Chest holds "
                + chest.countItem(Items.COBBLESTONE));
        });
    }

    // Auto workbench

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void autoWorkbenchCraftsFromItsBlueprint(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCFactoryBlocks.AUTO_WORKBENCH_ITEMS.get());
        TileAutoWorkbenchItems bench = helper.getBlockEntity(pos);
        for (int slot : new int[] { 0, 1, 3, 4 }) {
            bench.invBlueprint.setStackInSlot(slot, new ItemStack(Items.OAK_PLANKS));
        }
        bench.invMaterials.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 8));
        helper.succeedWhen(() -> {
            ItemStack result = bench.invResult.getStackInSlot(0);
            helper.assertTrue(result.is(Items.CRAFTING_TABLE) && result.getCount() >= 1, "Result is " + result);
        });
    }

    // Distiller

    @GameTest(template = "empty", timeoutTicks = 400)
    public static void distillerSplitsOil(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCFactoryBlocks.DISTILLER.get());
        TileDistiller_BC8 distiller = helper.getBlockEntity(pos);
        IFluidHandler in = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos),
            Direction.NORTH);
        helper.assertTrue(in.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.SIMULATE) == 0,
            "Distiller accepts water");
        helper.assertTrue(in.fill(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 4000),
            FluidAction.EXECUTE) == 4000, "Distiller didn't take oil");
        powerEachTick(helper, pos, 6 * MjAPI.MJ);
        helper.succeedWhen(() -> {
            helper.assertTrue(distiller.tankGasOut.getFluid().is(BCEnergyFluids.fuelGaseous[0].getSource()),
                "Gas tank holds " + distiller.tankGasOut.getDebugString());
            helper.assertTrue(distiller.tankLiquidOut.getFluid().is(BCEnergyFluids.oilHeavy[0].getSource()),
                "Liquid tank holds " + distiller.tankLiquidOut.getDebugString());
            // Gas comes out of the top, liquid out of the bottom
            IFluidHandler up = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(pos), Direction.UP);
            IFluidHandler down = helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK,
                helper.absolutePos(pos), Direction.DOWN);
            helper.assertTrue(up.drain(1, FluidAction.SIMULATE).is(BCEnergyFluids.fuelGaseous[0].getSource()),
                "Top doesn't give gas");
            helper.assertTrue(down.drain(1, FluidAction.SIMULATE).is(BCEnergyFluids.oilHeavy[0].getSource()),
                "Bottom doesn't give heavy oil");
        });
    }

    // Heat exchanger

    /** Three exchangers in a row facing north: the start is the east end (facing.getClockWise()). */
    private static final BlockPos HX_END = new BlockPos(1, 1, 2);
    private static final BlockPos HX_MIDDLE = new BlockPos(2, 1, 2);
    private static final BlockPos HX_START = new BlockPos(3, 1, 2);

    private static void placeHeatExchanger(GameTestHelper helper) {
        for (BlockPos p : new BlockPos[] { HX_END, HX_MIDDLE, HX_START }) {
            helper.setBlock(p, BCFactoryBlocks.HEAT_EXCHANGE.get().defaultBlockState()
                .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.NORTH));
        }
    }

    @GameTest(template = "factory", timeoutTicks = 400)
    public static void heatExchangerHeatsOilWithLava(GameTestHelper helper) {
        placeHeatExchanger(helper);
        helper.runAfterDelay(2, () -> {
            helper.assertBlockProperty(HX_START, BlockHeatExchange.PROP_PART, EnumExchangePart.START);
            helper.assertBlockProperty(HX_MIDDLE, BlockHeatExchange.PROP_PART, EnumExchangePart.MIDDLE);
            helper.assertBlockProperty(HX_END, BlockHeatExchange.PROP_PART, EnumExchangePart.END);
            helper.assertBlockProperty(HX_MIDDLE, BlockHeatExchange.PROP_CONNECTED_LEFT, true);
            helper.assertBlockProperty(HX_MIDDLE, BlockHeatExchange.PROP_CONNECTED_RIGHT, true);
            ServerLevel level = helper.getLevel();
            // Heatable fluid goes in the bottom of the start, coolant in the side of the end
            IFluidHandler startIn = level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(HX_START),
                Direction.DOWN);
            IFluidHandler endIn = level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(HX_END),
                Direction.WEST);
            helper.assertTrue(startIn != null && endIn != null, "Missing fluid handlers");
            helper.assertTrue(level.getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(HX_MIDDLE),
                Direction.DOWN) == null, "The middle section has a fluid handler");
            helper.assertTrue(startIn.fill(new FluidStack(Fluids.LAVA, 1000), FluidAction.SIMULATE) == 0,
                "Start takes lava as the fluid to heat");
            helper.assertTrue(startIn.fill(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 2000),
                FluidAction.EXECUTE) == 2000, "Start didn't take oil");
            helper.assertTrue(endIn.fill(new FluidStack(Fluids.LAVA, 2000), FluidAction.EXECUTE) == 2000,
                "End didn't take lava");
            TileHeatExchange end = helper.getBlockEntity(HX_END);
            helper.succeedWhen(() -> {
                ExchangeSection section = end.getSection();
                helper.assertTrue(section != null, "End has no section");
                FluidStack out = section.getTankOutput().getFluid();
                helper.assertTrue(out.is(BCEnergyFluids.crudeOil[1].getSource()) && out.getAmount() >= 100,
                    "End output holds " + section.getTankOutput().getDebugString());
            });
        });
    }

    @GameTest(template = "factory")
    public static void heatExchangerSavesBothSections(GameTestHelper helper) {
        placeHeatExchanger(helper);
        helper.runAfterDelay(2, () -> {
            TileHeatExchange start = helper.getBlockEntity(HX_START);
            helper.assertTrue(start.isStart(), "Not the start");
            start.getSection().getTankInput().fill(new FluidStack(BCEnergyFluids.crudeOil[0].getSource(), 1500),
                FluidAction.EXECUTE);
            ServerLevel level = helper.getLevel();
            CompoundTag nbt = start.saveWithoutMetadata(level.registryAccess());
            TileHeatExchange copy = new TileHeatExchange(start.getBlockPos(), start.getBlockState());
            copy.loadWithComponents(nbt, level.registryAccess());
            helper.assertTrue(copy.isStart(), "Reloaded tile isn't the start");
            helper.assertTrue(copy.getSection().getTankInput().getFluidAmount() == 1500,
                "Reloaded start tank holds " + copy.getSection().getTankInput().getDebugString());
            helper.succeed();
        });
    }

    @GameTest(template = "factory")
    public static void wrenchSwapsHeatExchangerEnds(GameTestHelper helper) {
        placeHeatExchanger(helper);
        helper.runAfterDelay(2, () -> {
            FakePlayer player = player(helper, "bc_heat_exchange");
            useWrench(helper, player, HX_MIDDLE, Direction.UP);
            for (BlockPos p : new BlockPos[] { HX_END, HX_MIDDLE, HX_START }) {
                helper.assertBlockProperty(p, BlockBCBase_Neptune.PROP_FACING, Direction.SOUTH);
            }
            helper.runAfterDelay(2, () -> {
                // Facing south, the start is now the west end
                helper.assertBlockProperty(HX_END, BlockHeatExchange.PROP_PART, EnumExchangePart.START);
                helper.assertBlockProperty(HX_START, BlockHeatExchange.PROP_PART, EnumExchangePart.END);
                helper.assertBlockProperty(HX_MIDDLE, BlockHeatExchange.PROP_CONNECTED_LEFT, true);
                helper.assertBlockProperty(HX_MIDDLE, BlockHeatExchange.PROP_CONNECTED_RIGHT, true);
                helper.assertBlockProperty(HX_END, BlockHeatExchange.PROP_CONNECTED_RIGHT, true);
                helper.succeed();
            });
        });
    }

    @GameTest(template = "empty")
    public static void wrenchTurnsLoneHeatExchanger(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCFactoryBlocks.HEAT_EXCHANGE.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.NORTH));
        useWrench(helper, player(helper, "bc_heat_exchange_lone"), pos, Direction.UP);
        helper.assertBlockProperty(pos, BlockBCBase_Neptune.PROP_FACING, Direction.EAST);
        helper.succeed();
    }

    @GameTest(template = "factory")
    public static void twoHeatExchangersDontForm(GameTestHelper helper) {
        helper.setBlock(HX_END, BCFactoryBlocks.HEAT_EXCHANGE.get());
        helper.setBlock(HX_MIDDLE, BCFactoryBlocks.HEAT_EXCHANGE.get());
        helper.runAfterDelay(2, () -> {
            TileHeatExchange a = helper.getBlockEntity(HX_END);
            TileHeatExchange b = helper.getBlockEntity(HX_MIDDLE);
            helper.assertTrue(a.getSection() == null && b.getSection() == null, "Two exchangers formed a section");
            helper.assertBlockProperty(HX_END, BlockHeatExchange.PROP_PART, EnumExchangePart.MIDDLE);
            helper.succeed();
        });
    }

    // Water gel

    @GameTest(template = "factory")
    public static void waterGelSpreadsThroughWater(GameTestHelper helper) {
        for (int x = 1; x <= 5; x++) {
            for (int z = 1; z <= 5; z++) {
                helper.setBlock(new BlockPos(x, 0, z), Blocks.STONE);
                boolean water = x >= 2 && x <= 4 && z >= 2 && z <= 4;
                helper.setBlock(new BlockPos(x, 1, z), water ? Blocks.WATER : Blocks.STONE);
            }
        }
        BlockPos centre = new BlockPos(3, 1, 3);
        helper.setBlock(centre, BCFactoryBlocks.WATER_GEL.get().defaultBlockState()
            .setValue(BlockWaterGel.PROP_STAGE, GelStage.SPREAD_0));
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(centre);
        level.getBlockState(abs).tick(level, abs, level.random);
        int gel = 0;
        for (int x = 2; x <= 4; x++) {
            for (int z = 2; z <= 4; z++) {
                if (helper.getBlockState(new BlockPos(x, 1, z)).is(BCFactoryBlocks.WATER_GEL.get())) {
                    gel++;
                }
            }
        }
        helper.assertTrue(gel == 4, gel + " blocks are gel after one spread");
        helper.assertBlockProperty(centre, BlockWaterGel.PROP_STAGE, GelStage.SPREAD_1);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void gelCraftsBackIntoWater(GameTestHelper helper) {
        var recipe = helper.getLevel().getRecipeManager().byKey(BuildCraftReborn.id("water_gel_to_bucket"));
        helper.assertTrue(recipe.isPresent(), "No gel to bucket recipe");
        ItemStack result = recipe.get().value().getResultItem(helper.getLevel().registryAccess());
        helper.assertTrue(result.is(Items.WATER_BUCKET), "Recipe makes " + result);
        helper.assertTrue(new ItemStack(BCFactoryItems.WATER_GEL_SPAWN.get()).getMaxStackSize() == 16,
            "Water gelifier stacks to " + new ItemStack(BCFactoryItems.WATER_GEL_SPAWN.get()).getMaxStackSize());
        helper.succeed();
    }

    // Helpers

    /** Gives the machine at {@code relative} this much MJ every tick (through its MJ receiver). */
    private static void powerEachTick(GameTestHelper helper, BlockPos relative, long microJoules) {
        BlockPos abs = helper.absolutePos(relative);
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, abs, Direction.UP);
            if (receiver != null && receiver.canReceive()) {
                receiver.receivePower(microJoules, false);
            }
        });
    }

    /** Right clicks the block with a wrench, the way a player does (block first, then the item). */
    private static void useWrench(GameTestHelper helper, FakePlayer player, BlockPos relative, Direction side) {
        BlockPos abs = helper.absolutePos(relative);
        ItemStack wrench = new ItemStack(BCCoreItems.WRENCH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, wrench);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).relative(side, 0.5), side, abs, false);
        player.gameMode.useItemOn(player, helper.getLevel(), wrench, InteractionHand.MAIN_HAND, hit);
    }

    /** Sets a block with the state it would get from its neighbours (like a placed block, unlike
     * {@link GameTestHelper#setBlock}). */
    private static void place(GameTestHelper helper, BlockPos relative, BlockState state) {
        BlockPos abs = helper.absolutePos(relative);
        helper.setBlock(relative, Block.updateFromNeighbourShapes(state, helper.getLevel(), abs));
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }
}
