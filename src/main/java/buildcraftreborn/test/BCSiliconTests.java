/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.Map;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;

import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.recipes.AssemblyRecipe;
import buildcraftreborn.core.BCCoreStatements;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.recipe.AssemblyRecipeRegistry;
import buildcraftreborn.lib.statement.ActionWrapper;
import buildcraftreborn.lib.statement.TriggerWrapper;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.BCSiliconPlugs;
import buildcraftreborn.silicon.EnumAssemblyRecipeState;
import buildcraftreborn.silicon.gate.EnumGateLogic;
import buildcraftreborn.silicon.gate.EnumGateMaterial;
import buildcraftreborn.silicon.gate.EnumGateModifier;
import buildcraftreborn.silicon.gate.GateVariant;
import buildcraftreborn.silicon.item.ItemPluggableGate;
import buildcraftreborn.silicon.item.ItemPluggableFacade;
import buildcraftreborn.silicon.plug.FacadeBlockStateInfo;
import buildcraftreborn.silicon.plug.FacadeInstance;
import buildcraftreborn.silicon.plug.FacadeStateManager;
import buildcraftreborn.silicon.plug.PluggableFacade;
import buildcraftreborn.silicon.plug.PluggableGate;
import buildcraftreborn.silicon.recipe.FacadeSwapRecipe;
import buildcraftreborn.transport.BCTransportItems;
import buildcraftreborn.silicon.tile.TileAdvancedCraftingTable;
import buildcraftreborn.silicon.tile.TileAssemblyTable;
import buildcraftreborn.transport.BCTransportPipes;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Phase 4 silicon tests: the assembly recipes load, the assembly table makes a saved recipe, a laser powers a laser
 * table, and gates work out their triggers and actions (and keep them when saved). */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCSiliconTests {
    private BCSiliconTests() {}

    @GameTest(template = "empty")
    public static void assemblyRecipesLoad(GameTestHelper helper) {
        Map<ResourceLocation, AssemblyRecipe> recipes = AssemblyRecipeRegistry.getAll(helper.getLevel());
        // 5 chipsets, 6 gates, 18 gate modifiers, 34 lenses, 16 wires, the pulsar, timer, light sensor, copier,
        // facades and the redstone crystal
        helper.assertTrue(recipes.size() == 85, recipes.size() + " assembly recipes");
        helper.assertTrue(recipes.containsKey(BuildCraftReborn.id("assembly/redstone_chipset")),
            "No redstone chipset recipe");
        helper.succeed();
    }

    @GameTest(template = "factory", timeoutTicks = 100)
    public static void assemblyTableMakesSavedRecipe(GameTestHelper helper) {
        BlockPos table = new BlockPos(3, 1, 3);
        BlockPos chest = new BlockPos(4, 1, 3);
        helper.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.NORTH));
        helper.setBlock(table, BCSiliconBlocks.ASSEMBLY_TABLE.get());
        TileAssemblyTable tile = helper.getBlockEntity(table);
        tile.inv.setStackInSlot(0, new ItemStack(Items.REDSTONE, 2));
        ItemStack chipset = new ItemStack(BCSiliconItems.CHIPSETS.get(EnumRedstoneChipset.RED).get());
        boolean[] saved = { false };
        helper.onEachTick(() -> {
            if (!saved[0]) {
                // What the GUI does when the player clicks the recipe
                for (var entry : tile.recipesStates.entrySet()) {
                    if (ItemStack.isSameItemSameComponents(entry.getKey().output, chipset)) {
                        entry.setValue(EnumAssemblyRecipeState.SAVED);
                        saved[0] = true;
                    }
                }
            } else {
                tile.receiveLaserPower(1000 * MjAPI.MJ);
            }
        });
        helper.succeedWhen(() -> {
            helper.assertTrue(saved[0], "The table never offered the chipset recipe");
            ChestBlockEntity out = helper.getBlockEntity(chest);
            helper.assertTrue(out.getItem(0).is(chipset.getItem()), "No chipset in the chest");
            helper.assertTrue(tile.inv.getStackInSlot(0).getCount() == 1, "The redstone wasn't used up");
        });
    }

    @GameTest(template = "factory", timeoutTicks = 600)
    public static void laserPowersAdvancedCraftingTable(GameTestHelper helper) {
        BlockPos laser = new BlockPos(3, 1, 3);
        BlockPos table = new BlockPos(3, 3, 3);
        helper.setBlock(laser, BCSiliconBlocks.LASER.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.BLOCK_FACING_6, Direction.UP));
        helper.setBlock(table, BCSiliconBlocks.ADVANCED_CRAFTING_TABLE.get());
        TileAdvancedCraftingTable tile = helper.getBlockEntity(table);
        // Two planks on top of each other make 4 sticks
        tile.invBlueprint.setStackInSlot(1, new ItemStack(Items.OAK_PLANKS));
        tile.invBlueprint.setStackInSlot(4, new ItemStack(Items.OAK_PLANKS));
        tile.invMaterials.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 4));
        BlockPos laserAbs = helper.absolutePos(laser);
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, laserAbs, Direction.DOWN);
            if (receiver != null && receiver.canReceive()) {
                receiver.receivePower(20 * MjAPI.MJ, false);
            }
        });
        helper.succeedWhen(() -> {
            int sticks = 0;
            for (int i = 0; i < tile.invResults.getSlots(); i++) {
                if (tile.invResults.getStackInSlot(i).is(Items.STICK)) {
                    sticks += tile.invResults.getStackInSlot(i).getCount();
                }
            }
            helper.assertTrue(sticks >= 4, "The table made " + sticks + " sticks");
        });
    }

    @GameTest(template = "factory", timeoutTicks = 100)
    public static void gateEmitsRedstone(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        TilePipeHolder holder = BCTransportTests.placePipe(helper, pos, BCTransportPipes.stoneItem, null);
        PluggableGate gate = new PluggableGate(BCSiliconPlugs.gate, holder, Direction.EAST,
            ItemPluggableGate.BASIC);
        holder.replacePluggable(Direction.EAST, gate);
        // "Always" -> "Redstone Signal": the gate lights up and the pipe gives out a signal
        gate.logic.statements[0].trigger.set(TriggerWrapper.wrap(BCCoreStatements.TRIGGER_TRUE, null));
        gate.logic.statements[0].action.set(ActionWrapper.wrap(BCCoreStatements.ACTION_REDSTONE, null));
        helper.succeedWhen(() -> {
            helper.assertTrue(gate.logic.isOn, "The gate isn't on");
            int output = 0;
            for (Direction side : Direction.values()) {
                output = Math.max(output, holder.getRedstoneOutput(side));
            }
            helper.assertTrue(output == 15, "The pipe's redstone output is " + output);
        });
    }

    @GameTest(template = "factory")
    public static void gateKeepsItsStatements(GameTestHelper helper) {
        BlockPos pos = new BlockPos(3, 1, 3);
        TilePipeHolder holder = BCTransportTests.placePipe(helper, pos, BCTransportPipes.stoneItem, null);
        GateVariant variant = new GateVariant(EnumGateLogic.OR, EnumGateMaterial.IRON, EnumGateModifier.LAPIS);
        PluggableGate gate = new PluggableGate(BCSiliconPlugs.gate, holder, Direction.UP, variant);
        holder.replacePluggable(Direction.UP, gate);
        gate.logic.statements[1].trigger.set(TriggerWrapper.wrap(BCCoreStatements.TRIGGER_TRUE, null));
        gate.logic.statements[1].action.set(ActionWrapper.wrap(BCCoreStatements.ACTION_REDSTONE, null));
        gate.logic.connections[0] = true;

        CompoundTag nbt = gate.writeToNbt(helper.getLevel().registryAccess());
        PluggableGate loaded = new PluggableGate(BCSiliconPlugs.gate, holder, Direction.UP, nbt,
            helper.getLevel().registryAccess());
        helper.assertTrue(loaded.logic.variant.equals(variant), "Variant changed to " + loaded.logic.variant);
        helper.assertTrue(loaded.logic.statements[1].trigger.get() != null
            && loaded.logic.statements[1].trigger.get().getUniqueTag().equals(
                BCCoreStatements.TRIGGER_TRUE.getUniqueTag()), "Trigger wasn't kept");
        helper.assertTrue(loaded.logic.statements[1].action.get() != null, "Action wasn't kept");
        helper.assertTrue(loaded.logic.connections[0], "Connection wasn't kept");

        // And the item for the gate is the same variant
        ItemStack item = BCSiliconItems.PLUG_GATE.get().getStack(variant);
        helper.assertTrue(ItemPluggableGate.getVariant(item).equals(variant), "Gate item has the wrong variant");
        helper.assertTrue(!BCSiliconItems.PLUG_GATE.get().getStack(ItemPluggableGate.BASIC)
            .has(BCSiliconItems.GATE_VARIANT.get()), "Basic gate items have a variant component");
        helper.succeed();
    }

    // Facades

    @GameTest(template = "empty")
    public static void facadeStatesAreFound(GameTestHelper helper) {
        helper.assertTrue(FacadeStateManager.validFacadeStates.containsKey(Blocks.STONE.defaultBlockState()),
            "Stone can't be a facade");
        helper.assertTrue(FacadeStateManager.validFacadeStates.containsKey(Blocks.GLASS.defaultBlockState()),
            "Glass can't be a facade");
        helper.assertTrue(FacadeStateManager.getInfoForBlock(Blocks.OAK_LOG) != null, "Logs can't be facades");
        helper.assertFalse(FacadeStateManager.validFacadeStates.containsKey(Blocks.CHEST.defaultBlockState()),
            "Chests (block entities) can be facades");
        helper.assertFalse(FacadeStateManager.validFacadeStates.containsKey(Blocks.OAK_STAIRS.defaultBlockState()),
            "Stairs (not full cubes) can be facades");
        helper.assertFalse(FacadeStateManager.validFacadeStates.containsKey(Blocks.WATER.defaultBlockState()),
            "Water can be a facade");
        // A log's axis varies between the states its item makes
        FacadeBlockStateInfo log = FacadeStateManager.getInfoForBlock(Blocks.OAK_LOG);
        helper.assertTrue(log.varyingProperties.contains(net.minecraft.world.level.block.RotatedPillarBlock.AXIS),
            "Log axis isn't a varying property");
        helper.succeed();
    }

    @GameTest(template = "factory")
    public static void facadesBlockPipesUnlessHollow(GameTestHelper helper) {
        BlockPos a = new BlockPos(2, 1, 3);
        BlockPos b = new BlockPos(3, 1, 3);
        BlockPos c = new BlockPos(4, 1, 3);
        TilePipeHolder ta = BCTransportTests.placePipe(helper, a, BCTransportPipes.cobbleItem, null);
        BCTransportTests.placePipe(helper, b, BCTransportPipes.cobbleItem, null);
        TilePipeHolder tc = BCTransportTests.placePipe(helper, c, BCTransportPipes.cobbleItem, null);
        FacadeBlockStateInfo stone = FacadeStateManager.getInfoForBlock(Blocks.STONE);
        ta.replacePluggable(Direction.EAST, new PluggableFacade(BCSiliconPlugs.facade, ta, Direction.EAST,
            FacadeInstance.createSingle(stone, false)));
        tc.replacePluggable(Direction.WEST, new PluggableFacade(BCSiliconPlugs.facade, tc, Direction.WEST,
            FacadeInstance.createSingle(stone, true)));
        helper.runAfterDelay(3, () -> {
            helper.assertFalse(BCTransportTests.pipe(helper, a).isConnected(Direction.EAST),
                "A solid facade didn't block the pipe");
            helper.assertTrue(BCTransportTests.pipe(helper, c).isConnected(Direction.WEST),
                "A hollow facade blocked the pipe");
            // Saving keeps the facade
            CompoundTag nbt = ta.getPluggable(Direction.EAST).writeToNbt(helper.getLevel().registryAccess());
            PluggableFacade loaded = new PluggableFacade(BCSiliconPlugs.facade, ta, Direction.EAST, nbt,
                helper.getLevel().registryAccess());
            helper.assertTrue(loaded.states.equals(FacadeInstance.createSingle(stone, false)),
                "Loaded facade is " + loaded.states);
            helper.succeed();
        });
    }

    @GameTest(template = "factory", timeoutTicks = 100)
    public static void assemblyTableMakesFacades(GameTestHelper helper) {
        BlockPos table = new BlockPos(3, 1, 3);
        helper.setBlock(table, BCSiliconBlocks.ASSEMBLY_TABLE.get());
        TileAssemblyTable tile = helper.getBlockEntity(table);
        tile.inv.setStackInSlot(0, new ItemStack(BCTransportItems.PIPES.get(BCTransportPipes.structure).get(), 3));
        tile.inv.setStackInSlot(1, new ItemStack(Items.STONE));
        helper.succeedWhen(() -> {
            boolean found = false;
            for (var instruction : tile.recipesStates.keySet()) {
                ItemStack out = instruction.output;
                if (out.is(BCSiliconItems.PLUG_FACADE.get()) && out.getCount() == 6) {
                    FacadeInstance facade = ItemPluggableFacade.getStates(out);
                    if (facade.phasedStates[0].stateInfo.state == Blocks.STONE.defaultBlockState()) {
                        found = true;
                    }
                }
            }
            helper.assertTrue(found, "The table doesn't offer stone facades (offers " + tile.recipesStates.size()
                + " recipes)");
        });
    }

    @GameTest(template = "empty")
    public static void facadeItemsSwapToHollow(GameTestHelper helper) {
        FacadeInstance solid = FacadeInstance.createSingle(FacadeStateManager.getInfoForBlock(Blocks.BRICKS), false);
        ItemStack stack = BCSiliconItems.PLUG_FACADE.get().createItemStack(solid);
        // The component survives saving
        var encoded = ItemStack.CODEC.encodeStart(helper.getLevel().registryAccess().createSerializationContext(
            net.minecraft.nbt.NbtOps.INSTANCE), stack).getOrThrow();
        ItemStack decoded = ItemStack.CODEC.parse(helper.getLevel().registryAccess().createSerializationContext(
            net.minecraft.nbt.NbtOps.INSTANCE), encoded).getOrThrow();
        helper.assertTrue(ItemStack.isSameItemSameComponents(stack, decoded), "Facade item changed when saved");

        FacadeSwapRecipe swap = new FacadeSwapRecipe(net.minecraft.world.item.crafting.CraftingBookCategory.MISC);
        var input = net.minecraft.world.item.crafting.CraftingInput.of(1, 1, java.util.List.of(stack));
        helper.assertTrue(swap.matches(input, helper.getLevel()), "Swap recipe doesn't match a facade");
        ItemStack hollow = swap.assemble(input, helper.getLevel().registryAccess());
        helper.assertTrue(ItemPluggableFacade.getStates(hollow).isHollow, "Swapped facade isn't hollow");
        helper.succeed();
    }
}
