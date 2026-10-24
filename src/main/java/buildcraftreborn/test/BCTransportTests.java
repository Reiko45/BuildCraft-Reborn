/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.transport.EnumWirePart;
import buildcraftreborn.api.transport.pipe.IPipe;
import buildcraftreborn.api.transport.pipe.PipeApi;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.factory.BCFactoryBlocks;
import buildcraftreborn.factory.tile.TileTank;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.transport.BCTransportBlocks;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.transport.BCTransportPipes;
import buildcraftreborn.transport.pipe.behaviour.PipeBehaviourStripes;
import buildcraftreborn.transport.pipe.flow.PipeFlowItems;
import buildcraftreborn.transport.plug.PluggableBlocker;
import buildcraftreborn.transport.recipe.PipeUpgradeRecipe;
import buildcraftreborn.transport.pipe.flow.PipeFlowPower;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Phase 4 transport tests: item, fluid, kinesis and FE pipes carrying things between real blocks, pipe connection
 * rules, plugs, wires, the stripes pipe, saving and loading, and the pipe upgrade recipes. Most use the 7x9x7
 * {@code factory} template, with pipes in a line along x at y = 1. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCTransportTests {
    private BCTransportTests() {}

    // Items

    @GameTest(template = "factory", timeoutTicks = 300)
    public static void woodenPipeExtractsItemsIntoChest(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 3);
        BlockPos dest = new BlockPos(5, 1, 3);
        chest(helper, source);
        chest(helper, dest);
        ChestBlockEntity sourceChest = helper.getBlockEntity(source);
        sourceChest.setItem(0, new ItemStack(Items.COBBLESTONE, 16));
        placePipe(helper, new BlockPos(1, 1, 3), BCTransportPipes.woodItem, null);
        for (int x = 2; x < 5; x++) {
            placePipe(helper, new BlockPos(x, 1, 3), BCTransportPipes.cobbleItem, null);
        }
        powerEachTick(helper, new BlockPos(1, 1, 3), MjAPI.MJ);
        helper.succeedWhen(() -> {
            ChestBlockEntity destChest = helper.getBlockEntity(dest);
            int count = countItems(destChest, Items.COBBLESTONE);
            helper.assertTrue(count == 16, "Destination has " + count + " cobblestone");
            helper.assertTrue(sourceChest.isEmpty(), "Source chest isn't empty");
        });
    }

    @GameTest(template = "factory")
    public static void pipesConnectByType(GameTestHelper helper) {
        // Stone and cobblestone pipes don't connect to each other; both connect to plain item pipes
        placePipe(helper, new BlockPos(1, 1, 3), BCTransportPipes.stoneItem, null);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleItem, null);
        placePipe(helper, new BlockPos(3, 1, 3), BCTransportPipes.goldItem, null);
        // A fluid pipe never connects to an item pipe
        placePipe(helper, new BlockPos(4, 1, 3), BCTransportPipes.goldFluid, null);
        helper.runAfterDelay(5, () -> {
            helper.assertFalse(pipe(helper, new BlockPos(1, 1, 3)).isConnected(Direction.EAST),
                "Stone connected to cobblestone");
            helper.assertTrue(pipe(helper, new BlockPos(2, 1, 3)).isConnected(Direction.EAST),
                "Cobblestone didn't connect to gold");
            helper.assertFalse(pipe(helper, new BlockPos(3, 1, 3)).isConnected(Direction.EAST),
                "Item pipe connected to fluid pipe");
            helper.succeed();
        });
    }

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void voidPipeDestroysItems(GameTestHelper helper) {
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleItem, null);
        placePipe(helper, new BlockPos(3, 1, 3), BCTransportPipes.voidItem, null);
        helper.runAfterDelay(2, () -> {
            PipeFlowItems flow = (PipeFlowItems) pipe(helper, new BlockPos(2, 1, 3)).getFlow();
            flow.insertItemsForce(new ItemStack(Items.DIRT, 5), Direction.WEST, null, 0.08);
        });
        helper.succeedWhen(() -> {
            helper.assertFalse(((PipeFlowItems) pipe(helper, new BlockPos(2, 1, 3)).getFlow()).doesContainItems(),
                "Items still in the first pipe");
            helper.assertFalse(((PipeFlowItems) pipe(helper, new BlockPos(3, 1, 3)).getFlow()).doesContainItems(),
                "Items still in the void pipe");
            helper.assertItemEntityNotPresent(Items.DIRT);
        });
    }

    @GameTest(template = "factory", timeoutTicks = 300)
    public static void diamondPipeSortsItems(GameTestHelper helper) {
        // Items come in from the west; the diamond pipe sends dirt north and anything else south
        BlockPos diamond = new BlockPos(3, 1, 3);
        BlockPos north = new BlockPos(3, 1, 2);
        BlockPos south = new BlockPos(3, 1, 4);
        chest(helper, north);
        chest(helper, south);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleItem, null);
        placePipe(helper, diamond, BCTransportPipes.diamondItem, null);
        helper.runAfterDelay(2, () -> {
            var behaviour = (buildcraftreborn.transport.pipe.behaviour.PipeBehaviourDiamond) pipe(helper, diamond)
                .getBehaviour();
            behaviour.filters.setStackInSlot(Direction.NORTH.ordinal() * 9, new ItemStack(Items.DIRT));
            PipeFlowItems flow = (PipeFlowItems) pipe(helper, new BlockPos(2, 1, 3)).getFlow();
            flow.insertItemsForce(new ItemStack(Items.DIRT, 4), Direction.WEST, null, 0.08);
            flow.insertItemsForce(new ItemStack(Items.SAND, 4), Direction.WEST, null, 0.08);
        });
        helper.succeedWhen(() -> {
            ChestBlockEntity n = helper.getBlockEntity(north);
            ChestBlockEntity s = helper.getBlockEntity(south);
            helper.assertTrue(countItems(n, Items.DIRT) == 4 && countItems(n, Items.SAND) == 0,
                "North chest: " + n.getItem(0) + ", " + n.getItem(1));
            helper.assertTrue(countItems(s, Items.SAND) == 4 && countItems(s, Items.DIRT) == 0,
                "South chest: " + s.getItem(0) + ", " + s.getItem(1));
        });
    }

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void pipeDropsItemsAtOpenEnd(GameTestHelper helper) {
        placePipe(helper, new BlockPos(3, 1, 3), BCTransportPipes.cobbleItem, null);
        helper.runAfterDelay(2, () -> {
            PipeFlowItems flow = (PipeFlowItems) pipe(helper, new BlockPos(3, 1, 3)).getFlow();
            flow.insertItemsForce(new ItemStack(Items.APPLE, 3), Direction.WEST, null, 0.08);
        });
        helper.succeedWhen(() -> helper.assertItemEntityPresent(Items.APPLE));
    }

    // Fluids

    @GameTest(template = "factory", timeoutTicks = 400)
    public static void woodenFluidPipeMovesWaterBetweenTanks(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 3);
        BlockPos dest = new BlockPos(5, 1, 3);
        helper.setBlock(source, BCFactoryBlocks.TANK.get());
        helper.setBlock(dest, BCFactoryBlocks.TANK.get());
        TileTank sourceTank = helper.getBlockEntity(source);
        sourceTank.tank.fill(new FluidStack(Fluids.WATER, 4000), FluidAction.EXECUTE);
        placePipe(helper, new BlockPos(1, 1, 3), BCTransportPipes.woodFluid, null);
        for (int x = 2; x < 5; x++) {
            placePipe(helper, new BlockPos(x, 1, 3), BCTransportPipes.cobbleFluid, null);
        }
        powerEachTick(helper, new BlockPos(1, 1, 3), MjAPI.MJ);
        helper.succeedWhen(() -> {
            TileTank destTank = helper.getBlockEntity(dest);
            helper.assertTrue(destTank.tank.getFluidAmount() >= 1000, "Destination tank holds "
                + destTank.tank.getDebugString());
        });
    }

    // Power

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void kinesisPipesCarryMj(GameTestHelper helper) {
        // Power goes into a wooden kinesis pipe and comes out of the far end into a chute's battery
        BlockPos chute = new BlockPos(5, 1, 3);
        helper.setBlock(chute, BCFactoryBlocks.CHUTE.get());
        placePipe(helper, new BlockPos(1, 1, 3), BCTransportPipes.woodPower, null);
        for (int x = 2; x < 5; x++) {
            placePipe(helper, new BlockPos(x, 1, 3), BCTransportPipes.cobblePower, null);
        }
        BlockPos input = helper.absolutePos(new BlockPos(1, 1, 3));
        long[] given = { 0 };
        int[] shown = { 0, 0 };
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, input, Direction.WEST);
            if (receiver != null && receiver.canReceive()) {
                given[0] += MjAPI.MJ - receiver.receivePower(MjAPI.MJ, false);
            }
            // The middle pipe shows the power going in one side and out of the other
            if (helper.getBlockEntity(new BlockPos(3, 1, 3)) instanceof TilePipeHolder tile
                && tile.getPipe() != null && tile.getPipe().getFlow() instanceof PipeFlowPower flow) {
                shown[0] = Math.max(shown[0], flow.getSection(Direction.WEST).displayPower);
                shown[1] = Math.max(shown[1], flow.getSection(Direction.EAST).displayPower);
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(given[0] > 0, "The wooden kinesis pipe took no power");
            helper.assertTrue(shown[0] > 0 && shown[1] > 0, "The pipe doesn't show its power flow (in "
                + shown[0] + ", out " + shown[1] + ")");
            var readable = helper.getLevel().getCapability(MjAPI.CAP_READABLE, helper.absolutePos(chute),
                Direction.WEST);
            helper.assertTrue(readable != null && readable.getStored() > 0, "The chute got no power");
        });
    }

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void energyPipesCarryFe(GameTestHelper helper) {
        // A cobblestone FE pipe hands energy offered by a neighbour on to the next pipe
        placePipe(helper, new BlockPos(1, 1, 3), BCTransportPipes.woodRf, null);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleRf, null);
        placePipe(helper, new BlockPos(3, 1, 3), BCTransportPipes.cobbleRf, null);
        helper.runAfterDelay(5, () -> {
            IEnergyStorage in = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                helper.absolutePos(new BlockPos(1, 1, 3)), Direction.WEST);
            helper.assertTrue(in != null && in.canReceive(), "The wooden FE pipe can't take FE");
            IEnergyStorage middle = helper.getLevel().getCapability(Capabilities.EnergyStorage.BLOCK,
                helper.absolutePos(new BlockPos(2, 1, 3)), Direction.WEST);
            helper.assertTrue(middle == null || !middle.canReceive(), "A cobblestone FE pipe took FE from outside");
            helper.succeed();
        });
    }

    // Plugs and wires

    @GameTest(template = "factory")
    public static void blockerPlugStopsConnections(GameTestHelper helper) {
        BlockPos a = new BlockPos(2, 1, 3);
        BlockPos b = new BlockPos(3, 1, 3);
        placePipe(helper, a, BCTransportPipes.cobbleItem, null);
        placePipe(helper, b, BCTransportPipes.cobbleItem, null);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(pipe(helper, a).isConnected(Direction.EAST), "Pipes didn't connect");
            FakePlayer player = player(helper);
            ItemStack plug = new ItemStack(BCTransportItems.PLUG_BLOCKER.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, plug);
            TilePipeHolder holder = helper.getBlockEntity(a);
            var placed = BCTransportItems.PLUG_BLOCKER.get().onPlace(plug, holder, Direction.EAST, player,
                InteractionHand.MAIN_HAND);
            helper.assertTrue(placed instanceof PluggableBlocker, "Blocker wasn't made");
            holder.replacePluggable(Direction.EAST, placed);
            helper.runAfterDelay(3, () -> {
                helper.assertFalse(pipe(helper, a).isConnected(Direction.EAST), "Plugged side still connected");
                helper.succeed();
            });
        });
    }

    @GameTest(template = "factory")
    public static void wiresJoinIntoSystems(GameTestHelper helper) {
        BlockPos a = new BlockPos(2, 1, 3);
        BlockPos b = new BlockPos(3, 1, 3);
        placePipe(helper, a, BCTransportPipes.cobbleItem, null);
        placePipe(helper, b, BCTransportPipes.cobbleItem, null);
        helper.runAfterDelay(2, () -> {
            TilePipeHolder ta = helper.getBlockEntity(a);
            TilePipeHolder tb = helper.getBlockEntity(b);
            // The corners touching between the two pipes (east side of a, west side of b)
            helper.assertTrue(ta.getWireManager().addPart(EnumWirePart.EAST_UP_SOUTH, DyeColor.RED),
                "Couldn't add wire to a");
            helper.assertTrue(tb.getWireManager().addPart(EnumWirePart.WEST_UP_SOUTH, DyeColor.RED),
                "Couldn't add wire to b");
            helper.runAfterDelay(2, () -> {
                var systems = buildcraftreborn.transport.wire.WorldSavedDataWireSystems.get(helper.getLevel());
                var element = new buildcraftreborn.transport.wire.WireSystem.WireElement(helper.absolutePos(a),
                    EnumWirePart.EAST_UP_SOUTH);
                var found = systems.getWireSystemsWithElement(element);
                helper.assertTrue(found.size() == 1, "Wire is in " + found.size() + " systems");
                var other = new buildcraftreborn.transport.wire.WireSystem.WireElement(helper.absolutePos(b),
                    EnumWirePart.WEST_UP_SOUTH);
                helper.assertTrue(found.get(0).elements.contains(other), "The two wires aren't joined");
                helper.succeed();
            });
        });
    }

    // Stripes

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void stripesPipePlacesBlocks(GameTestHelper helper) {
        BlockPos stripes = new BlockPos(3, 1, 3);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleItem, null);
        placePipe(helper, stripes, BCTransportPipes.stripesItem, null);
        helper.runAfterDelay(3, () -> {
            helper.assertTrue(((PipeBehaviourStripes) pipe(helper, stripes).getBehaviour()).direction
                == Direction.EAST, "Stripes pipe doesn't face east");
            PipeFlowItems flow = (PipeFlowItems) pipe(helper, new BlockPos(2, 1, 3)).getFlow();
            flow.insertItemsForce(new ItemStack(Items.OAK_PLANKS), Direction.WEST, null, 0.08);
        });
        helper.succeedWhen(() -> helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(4, 1, 3)));
    }

    @GameTest(template = "factory", timeoutTicks = 200)
    public static void stripesPipeExtendsWithPipes(GameTestHelper helper) {
        BlockPos stripes = new BlockPos(3, 1, 3);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.cobbleItem, null);
        placePipe(helper, stripes, BCTransportPipes.stripesItem, null);
        helper.runAfterDelay(3, () -> {
            PipeFlowItems flow = (PipeFlowItems) pipe(helper, new BlockPos(2, 1, 3)).getFlow();
            flow.insertItemsForce(new ItemStack(pipeItem(BCTransportPipes.cobbleItem)), Direction.WEST, null, 0.08);
        });
        helper.succeedWhen(() -> {
            IPipe moved = pipeOrNull(helper, new BlockPos(4, 1, 3));
            helper.assertTrue(moved != null && moved.getDefinition() == BCTransportPipes.stripesItem,
                "No stripes pipe at the new end");
            IPipe placed = pipeOrNull(helper, stripes);
            helper.assertTrue(placed != null && placed.getDefinition() == BCTransportPipes.cobbleItem,
                "No new pipe where the stripes pipe was");
        });
    }

    // Saving and loading

    @GameTest(template = "factory", timeoutTicks = 100)
    public static void pipeSavesAndLoadsWithoutLevel(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        placePipe(helper, pos, BCTransportPipes.stripesItem, DyeColor.LIME);
        placePipe(helper, new BlockPos(2, 1, 3), BCTransportPipes.woodItem, null);
        placePipe(helper, new BlockPos(4, 1, 3), BCTransportPipes.diamondItem, null);
        helper.runAfterDelay(3, () -> {
            for (BlockPos p : new BlockPos[] { new BlockPos(2, 1, 3), pos, new BlockPos(4, 1, 3) }) {
                TilePipeHolder tile = helper.getBlockEntity(p);
                CompoundTag nbt = tile.saveWithoutMetadata(helper.getLevel().registryAccess());
                // Block entities load before they know their level (as when a chunk loads)
                TilePipeHolder copy = new TilePipeHolder(tile.getBlockPos(), tile.getBlockState());
                copy.loadWithComponents(nbt, helper.getLevel().registryAccess());
                helper.assertTrue(copy.getPipe() != null
                    && copy.getPipe().getDefinition() == tile.getPipe().getDefinition(), "Pipe didn't load at " + p);
            }
            TilePipeHolder stripes = helper.getBlockEntity(pos);
            helper.assertTrue(stripes.getPipe().getColour() == DyeColor.LIME, "Colour lost");
            helper.succeed();
        });
    }

    @GameTest(template = "factory")
    public static void pipeItemPlacesPaintedPipe(GameTestHelper helper) {
        BlockPos below = new BlockPos(3, 1, 3);
        helper.setBlock(below, Blocks.STONE);
        FakePlayer player = player(helper);
        ItemStack stack = new ItemStack(pipeItem(BCTransportPipes.goldItem));
        stack.set(BCItemComponents.COLOUR.get(), DyeColor.BLUE);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        BlockPos abs = helper.absolutePos(below);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs).add(0, 0.5, 0), Direction.UP, abs, false);
        stack.useOn(new net.minecraft.world.item.context.UseOnContext(helper.getLevel(), player,
            InteractionHand.MAIN_HAND, stack, hit));
        IPipe pipe = pipeOrNull(helper, below.above());
        helper.assertTrue(pipe != null && pipe.getDefinition() == BCTransportPipes.goldItem, "No gold pipe placed");
        helper.assertTrue(pipe.getColour() == DyeColor.BLUE, "Pipe colour is " + pipe.getColour());
        helper.succeed();
    }

    @GameTest(template = "factory")
    public static void brokenPipeDropsItsItem(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        placePipe(helper, pos, BCTransportPipes.quartzItem, DyeColor.RED);
        helper.runAfterDelay(2, () -> {
            helper.getLevel().destroyBlock(helper.absolutePos(pos), true);
            helper.runAfterDelay(1, () -> {
                helper.assertItemEntityPresent(pipeItem(BCTransportPipes.quartzItem));
                helper.succeed();
            });
        });
    }

    // Recipes

    @GameTest(template = "empty")
    public static void pipeUpgradeKeepsPaint(GameTestHelper helper) {
        Item from = pipeItem(BCTransportPipes.ironItem);
        Item to = pipeItem(BCTransportPipes.ironFluid);
        PipeUpgradeRecipe upgrade = new PipeUpgradeRecipe("", from, Optional.of(Ingredient.of(
            BCTransportItems.WATERPROOF.get())), to);
        ItemStack painted = new ItemStack(from);
        painted.set(BCItemComponents.COLOUR.get(), DyeColor.ORANGE);
        CraftingInput input = CraftingInput.of(2, 1, java.util.List.of(painted, new ItemStack(
            BCTransportItems.WATERPROOF.get())));
        helper.assertTrue(upgrade.matches(input, helper.getLevel()), "Upgrade doesn't match");
        ItemStack out = upgrade.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(out.is(to) && out.get(BCItemComponents.COLOUR.get()) == DyeColor.ORANGE,
            "Upgrade made " + out);
        CraftingInput wrong = CraftingInput.of(2, 1, java.util.List.of(painted, new ItemStack(Items.DIRT)));
        helper.assertFalse(upgrade.matches(wrong, helper.getLevel()), "Upgrade matched dirt");

        PipeUpgradeRecipe undo = new PipeUpgradeRecipe("", to, Optional.empty(), from);
        ItemStack fluidPipe = new ItemStack(to);
        fluidPipe.set(BCItemComponents.COLOUR.get(), DyeColor.ORANGE);
        CraftingInput undoInput = CraftingInput.of(1, 1, java.util.List.of(fluidPipe));
        helper.assertTrue(undo.matches(undoInput, helper.getLevel()), "Undo doesn't match");
        helper.assertTrue(undo.assemble(undoInput, helper.getLevel().registryAccess())
            .get(BCItemComponents.COLOUR.get()) == DyeColor.ORANGE, "Undo lost the paint");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void everyPipeHasAnItem(GameTestHelper helper) {
        int count = 0;
        for (PipeDefinition def : PipeApi.pipeRegistry.getAllRegisteredPipes()) {
            if (def == BCTransportPipes.obsidianFluid) {
                continue; // 1.12 had no item for it either
            }
            helper.assertTrue(PipeApi.pipeRegistry.getItemForPipe(def) != null, "No item for " + def.identifier);
            count++;
        }
        helper.assertTrue(count == BCTransportItems.PIPES.size(), count + " pipes but " + BCTransportItems.PIPES.size()
            + " pipe items");
        helper.succeed();
    }

    @GameTest(template = "factory")
    public static void paintbrushPaintsPipes(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        placePipe(helper, pos, BCTransportPipes.stoneItem, null);
        FakePlayer player = player(helper);
        player.setGameMode(net.minecraft.world.level.GameType.SURVIVAL);
        ItemStack brush = new ItemStack(BCCoreItems.PAINTBRUSH.get());
        brush.set(BCItemComponents.COLOUR.get(), DyeColor.RED);
        player.setItemInHand(InteractionHand.MAIN_HAND, brush);
        BlockPos abs = helper.absolutePos(pos);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);
        var result = brush.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND,
            hit));
        helper.assertTrue(result.consumesAction(), "The brush didn't paint (" + result + ")");
        helper.assertTrue(pipe(helper, pos).getColour() == DyeColor.RED, "The pipe isn't red");
        ItemStack used = player.getItemInHand(InteractionHand.MAIN_HAND);
        helper.assertTrue(used.getOrDefault(BCItemComponents.PAINT_USED.get(), 0) == 1, "The brush wasn't used");
        // A clean brush takes the paint off
        ItemStack clean = new ItemStack(BCCoreItems.PAINTBRUSH.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, clean);
        clean.useOn(new net.minecraft.world.item.context.UseOnContext(player, InteractionHand.MAIN_HAND, hit));
        helper.assertTrue(pipe(helper, pos).getColour() == null, "The clean brush didn't clean the pipe");
        helper.succeed();
    }

    // Helpers

    static Item pipeItem(PipeDefinition def) {
        return BCTransportItems.PIPES.get(def).get();
    }

    /** Places a pipe as a player placing its item would. */
    static TilePipeHolder placePipe(GameTestHelper helper, BlockPos pos, PipeDefinition def, DyeColor colour) {
        helper.setBlock(pos, BCTransportBlocks.PIPE_HOLDER.get());
        TilePipeHolder tile = helper.getBlockEntity(pos);
        ItemStack stack = new ItemStack(pipeItem(def));
        if (colour != null) {
            stack.set(BCItemComponents.COLOUR.get(), colour);
        }
        tile.onPlacedBy(null, stack);
        return tile;
    }

    static IPipe pipe(GameTestHelper helper, BlockPos pos) {
        IPipe pipe = pipeOrNull(helper, pos);
        helper.assertTrue(pipe != null, "No pipe at " + pos);
        return pipe;
    }

    static IPipe pipeOrNull(GameTestHelper helper, BlockPos pos) {
        return helper.getLevel().getCapability(PipeApi.CAP_PIPE, helper.absolutePos(pos), null);
    }

    private static void chest(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
    }

    private static int countItems(ChestBlockEntity chest, Item item) {
        int count = 0;
        for (int i = 0; i < chest.getContainerSize(); i++) {
            ItemStack stack = chest.getItem(i);
            if (stack.is(item)) {
                count += stack.getCount();
            }
        }
        return count;
    }

    private static void powerEachTick(GameTestHelper helper, BlockPos relative, long microJoules) {
        BlockPos abs = helper.absolutePos(relative);
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, abs, Direction.UP);
            if (receiver != null && receiver.canReceive()) {
                receiver.receivePower(microJoules, false);
            }
        });
    }

    private static FakePlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(
            "bc_transport".getBytes()), "bc_transport"));
    }

    @SuppressWarnings("unused")
    private static IItemHandler items(GameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, helper.absolutePos(pos), side);
    }

    @SuppressWarnings("unused")
    private static IFluidHandler fluids(GameTestHelper helper, BlockPos pos, Direction side) {
        return helper.getLevel().getCapability(Capabilities.FluidHandler.BLOCK, helper.absolutePos(pos), side);
    }
}
