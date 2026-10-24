/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.BitSet;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionContents;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.DoorBlock;
import net.minecraft.world.level.block.LayeredCauldronBlock;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumOptionalSnapshotType;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.api.filler.IFilledTemplate;
import buildcraftreborn.api.filler.IFillerPattern;
import buildcraftreborn.api.filler.IFillerPatternShape;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.schematics.ISchematicBlock;
import buildcraftreborn.api.schematics.SchematicBlockContext;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.BCBuildersItems;
import buildcraftreborn.builders.BCBuildersStatements;
import buildcraftreborn.builders.addon.AddonFillerPlanner;
import buildcraftreborn.builders.block.BlockBuilder;
import buildcraftreborn.builders.block.BlockFrame;
import buildcraftreborn.builders.block.BlockQuarry;
import buildcraftreborn.builders.item.ItemSchematicSingle;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.RulesLoader;
import buildcraftreborn.builders.snapshot.SchematicBlockManager;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.snapshot.Template;
import buildcraftreborn.builders.snapshot.pattern.parameter.PatternParameterFacing;
import buildcraftreborn.builders.snapshot.pattern.parameter.PatternParameterHollow;
import buildcraftreborn.builders.tile.TileArchitectTable;
import buildcraftreborn.builders.tile.TileBuilder;
import buildcraftreborn.builders.tile.TileFiller;
import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.builders.tile.TileReplacer;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.VolumeSubCache;
import buildcraftreborn.core.marker.volume.EnumAddonSlot;
import buildcraftreborn.core.marker.volume.VolumeBox;
import buildcraftreborn.core.marker.volume.WorldSavedDataVolumeBoxes;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;
import buildcraftreborn.lib.misc.VecUtil;

/** Phase 5 tests: the quarry, filler, architect table, builder, replacer, single schematic, the filler patterns and
 * the schematic rules.
 * Machines get their power straight into their MJ receiver every tick. Most use the 12x12x12 {@code builders}
 * template. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCBuildersTests {
    private BCBuildersTests() {}

    // Blocks

    /** Frames join up with each other and with a quarry. */
    @GameTest(template = "empty")
    public static void framesConnectToFramesAndQuarries(GameTestHelper helper) {
        BlockPos a = new BlockPos(0, 1, 1);
        BlockPos b = new BlockPos(1, 1, 1);
        place(helper, a, BCBuildersBlocks.FRAME.get().defaultBlockState());
        place(helper, b, BCBuildersBlocks.FRAME.get().defaultBlockState());
        helper.assertBlockProperty(a, BlockFrame.CONNECTED_MAP.get(Direction.EAST), true);
        helper.assertBlockProperty(b, BlockFrame.CONNECTED_MAP.get(Direction.WEST), true);
        helper.assertBlockProperty(b, BlockFrame.CONNECTED_MAP.get(Direction.UP), false);
        place(helper, b.above(), BCBuildersBlocks.QUARRY.get().defaultBlockState());
        helper.assertBlockProperty(b, BlockFrame.CONNECTED_MAP.get(Direction.UP), true);
        helper.setBlock(a, Blocks.AIR);
        helper.assertBlockProperty(b, BlockFrame.CONNECTED_MAP.get(Direction.WEST), false);
        helper.succeed();
    }

    /** The quarry shows an item hatch on the side that has an inventory, relative to its front. */
    @GameTest(template = "empty")
    public static void quarryShowsHatchTowardsInventories(GameTestHelper helper) {
        BlockPos quarry = new BlockPos(1, 1, 1);
        helper.setBlock(quarry.east(), Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.SOUTH));
        // Facing south: the front is south, so east is on the quarry's own west (its right, seen from the front)
        place(helper, quarry, BCBuildersBlocks.QUARRY.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.SOUTH));
        BlockState state = helper.getBlockState(quarry);
        int connected = 0;
        Direction found = null;
        for (Direction side : Direction.values()) {
            if (state.getValue(BlockQuarry.CONNECTED_MAP.get(side))) {
                connected++;
                found = side;
            }
        }
        helper.assertTrue(connected == 1 && found == Direction.WEST, "Hatches: " + state);
        helper.succeed();
    }

    // Quarry

    /** A quarry given a 5x5 area by volume markers builds its frame, then mines out the middle into a chest. */
    @GameTest(template = "builders", timeoutTicks = 1200)
    public static void quarryBuildsFrameAndMines(GameTestHelper helper) {
        BlockPos quarry = new BlockPos(1, 1, 3);
        BlockPos chest = new BlockPos(0, 1, 3);
        // Markers: the box is x 2..6, z 1..5 at the quarry's height (raised to 4 high by the quarry)
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(6, 1, 1), new BlockPos(2, 1, 5));
        // Something to mine in the middle
        BlockPos ore = new BlockPos(4, 1, 3);
        helper.setBlock(ore, Blocks.COBBLESTONE);
        placeMarkers(helper, markers);
        powerEachTick(helper, quarry, 200 * MjAPI.MJ);
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            helper.setBlock(chest, Blocks.CHEST.defaultBlockState().setValue(ChestBlock.FACING, Direction.WEST));
            // Facing west, so the area is behind it to the east, where the first marker is (next to it)
            placeQuarry(helper, quarry);
            TileQuarry tile = helper.getBlockEntity(quarry);
            helper.assertTrue(tile.frameBox.isInitialized(), "No frame box");
            helper.assertTrue(tile.frameBox.min().equals(helper.absolutePos(markers.get(0)))
                && tile.frameBox.max().equals(helper.absolutePos(new BlockPos(6, 5, 5))),
                "Frame box " + tile.frameBox);
            helper.assertBlockNotPresent(BCCoreBlocks.MARKER_VOLUME.get(), markers.get(0));
        }).thenWaitUntil(() -> {
            for (BlockPos corner : List.of(new BlockPos(2, 1, 1), new BlockPos(6, 5, 5), new BlockPos(6, 1, 5),
                new BlockPos(2, 5, 1))) {
                helper.assertBlockPresent(BCBuildersBlocks.FRAME.get(), corner);
            }
            helper.assertBlockNotPresent(Blocks.COBBLESTONE, ore);
            ChestBlockEntity inv = helper.getBlockEntity(chest);
            boolean gotCobble = false;
            for (int i = 0; i < inv.getContainerSize(); i++) {
                gotCobble |= inv.getItem(i).is(Items.COBBLESTONE);
            }
            helper.assertTrue(gotCobble, "The cobblestone didn't reach the chest");
            helper.assertTrue(helper.<TileQuarry>getBlockEntity(quarry).drillPos != null, "The drill isn't out");
        }).thenSucceed();
    }

    private static void placeQuarry(GameTestHelper helper, BlockPos quarry) {
        helper.setBlock(quarry, BCBuildersBlocks.QUARRY.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.WEST));
        helper.<TileQuarry>getBlockEntity(quarry).onPlacedBy(player(helper, "bc_quarry"),
            new ItemStack(BCBuildersBlocks.QUARRY.get()));
    }

    /** The quarry's area, drill and (unlike 1.12) current task survive saving. */
    @GameTest(template = "builders", timeoutTicks = 400)
    public static void quarrySavesItsState(GameTestHelper helper) {
        BlockPos quarry = new BlockPos(1, 1, 3);
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(6, 1, 1), new BlockPos(2, 1, 5));
        placeMarkers(helper, markers);
        powerEachTick(helper, quarry, 2 * MjAPI.MJ);
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            placeQuarry(helper, quarry);
        }).thenWaitUntil(() -> {
            TileQuarry tile = helper.getBlockEntity(quarry);
            helper.assertTrue(tile.currentTask != null, "No task yet");
            CompoundTag nbt = tile.saveWithFullMetadata(helper.getLevel().registryAccess());
            TileQuarry copy = new TileQuarry(tile.getBlockPos(), tile.getBlockState());
            copy.loadWithComponents(nbt, helper.getLevel().registryAccess());
            helper.assertTrue(copy.frameBox.equals(tile.frameBox), "Frame box " + copy.frameBox);
            helper.assertTrue(copy.currentTask != null && copy.currentTask.getClass() == tile.currentTask.getClass(),
                "Task " + copy.currentTask);
        }).thenSucceed();
    }

    /** Breaking the quarry takes its frame down too. */
    @GameTest(template = "builders", timeoutTicks = 600)
    public static void breakingTheQuarryRemovesTheFrame(GameTestHelper helper) {
        BlockPos quarry = new BlockPos(1, 1, 3);
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(6, 1, 1), new BlockPos(2, 1, 5));
        placeMarkers(helper, markers);
        powerEachTick(helper, quarry, 200 * MjAPI.MJ);
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            placeQuarry(helper, quarry);
        }).thenWaitUntil(() -> {
            helper.assertBlockPresent(BCBuildersBlocks.FRAME.get(), new BlockPos(6, 5, 5));
            helper.assertBlockPresent(BCBuildersBlocks.FRAME.get(), new BlockPos(2, 1, 1));
        }).thenExecute(() -> helper.destroyBlock(quarry)).thenExecute(() -> {
            helper.assertBlockNotPresent(BCBuildersBlocks.FRAME.get(), new BlockPos(6, 5, 5));
            helper.assertBlockNotPresent(BCBuildersBlocks.FRAME.get(), new BlockPos(2, 1, 1));
        }).thenSucceed();
    }

    // Filler

    /** A filler set to "fill" fills the area of its markers with the blocks in its inventory. */
    @GameTest(template = "builders", timeoutTicks = 1200)
    public static void fillerFillsItsArea(GameTestHelper helper) {
        BlockPos filler = new BlockPos(1, 1, 1);
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(4, 1, 1), new BlockPos(2, 1, 3));
        placeMarkers(helper, markers);
        powerEachTick(helper, filler, 100 * MjAPI.MJ);
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            helper.setBlock(filler, BCBuildersBlocks.FILLER.get().defaultBlockState()
                .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.WEST));
            TileFiller tile = helper.getBlockEntity(filler);
            tile.onPlacedBy(player(helper, "bc_filler"), new ItemStack(BCBuildersBlocks.FILLER.get()));
            helper.assertTrue(tile.hasBox(), "The filler has no box");
            tile.setPattern(BCBuildersStatements.PATTERN_FILL, new IStatementParameter[0]);
            tile.invResources.setStackInSlot(0, new ItemStack(Blocks.STONE, 64));
        }).thenWaitUntil(() -> {
            for (int x = 2; x <= 4; x++) {
                for (int z = 1; z <= 3; z++) {
                    helper.assertBlockPresent(Blocks.STONE, new BlockPos(x, 1, z));
                }
            }
        }).thenSucceed();
    }

    /** A filler next to a volume box with a filler planner builds the planner's pattern (here, a box frame). */
    @GameTest(template = "builders", timeoutTicks = 1200)
    public static void fillerUsesFillerPlanner(GameTestHelper helper) {
        WorldSavedDataVolumeBoxes boxes = WorldSavedDataVolumeBoxes.get(helper.getLevel());
        VolumeBox volumeBox = boxes.addVolumeBox(helper.absolutePos(new BlockPos(2, 1, 1)));
        volumeBox.box.setMax(helper.absolutePos(new BlockPos(4, 3, 3)));
        AddonFillerPlanner addon = new AddonFillerPlanner();
        addon.volumeBox = volumeBox;
        volumeBox.addons.put(EnumAddonSlot.VALUES[0], addon);
        addon.patternStatement.set(BCBuildersStatements.PATTERN_FRAME);
        addon.onAdded();
        boxes.setDirty();
        BlockPos filler = new BlockPos(1, 1, 1);
        helper.setBlock(filler, BCBuildersBlocks.FILLER.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.WEST));
        TileFiller tile = helper.getBlockEntity(filler);
        tile.onPlacedBy(player(helper, "bc_filler"), new ItemStack(BCBuildersBlocks.FILLER.get()));
        helper.assertTrue(tile.addon == addon, "The filler didn't pick up the addon");
        tile.invResources.setStackInSlot(0, new ItemStack(Blocks.OAK_PLANKS, 64));
        powerEachTick(helper, filler, 100 * MjAPI.MJ);
        helper.succeedWhen(() -> {
            // A frame: the edges are filled, the middle of each face isn't
            helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(2, 1, 1));
            helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(4, 3, 3));
            helper.assertBlockPresent(Blocks.OAK_PLANKS, new BlockPos(3, 1, 1));
            helper.assertBlockNotPresent(Blocks.OAK_PLANKS, new BlockPos(3, 2, 2));
            helper.assertBlockNotPresent(Blocks.OAK_PLANKS, new BlockPos(3, 1, 2));
            boxes.volumeBoxes.remove(volumeBox);
            boxes.setDirty();
        });
    }

    // Architect table and builder

    /** The architect table scans its area into a blueprint, which a builder then builds elsewhere. */
    @GameTest(template = "builders", timeoutTicks = 1600)
    public static void architectScansAndBuilderBuilds(GameTestHelper helper) {
        // Scan: an area of 3x2x3 next to the architect table at (1, 1, 1), with a few blocks in it
        BlockPos architect = new BlockPos(1, 1, 1);
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(4, 1, 1), new BlockPos(2, 1, 3),
            new BlockPos(2, 2, 1));
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.STONE);
        helper.setBlock(new BlockPos(4, 1, 3), Blocks.OAK_LOG);
        placeMarkers(helper, markers);
        // A builder at (1, 1, 7), facing the same way, builds at the same offset from itself
        BlockPos builder = new BlockPos(1, 1, 7);
        powerEachTick(helper, builder, 100 * MjAPI.MJ);
        ItemStack[] blueprint = new ItemStack[1];
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            TileArchitectTable table = placeArchitect(helper, architect);
            table.name = "test house";
            table.invSnapshotIn.setStackInSlot(0, ItemSnapshot.getClean(EnumSnapshotType.BLUEPRINT));
        }).thenWaitUntil(() -> {
            TileArchitectTable table = helper.getBlockEntity(architect);
            ItemStack out = table.invSnapshotOut.getStackInSlot(0);
            helper.assertTrue(ItemSnapshot.isUsed(out), "No blueprint came out: " + out);
            blueprint[0] = out.copy();
        }).thenExecute(() -> {
            Snapshot.Header header = ItemSnapshot.getHeader(blueprint[0]);
            helper.assertTrue(header != null && header.name.equals("test house"), "Header " + header);
            Snapshot snapshot = GlobalSavedDataSnapshots.get(helper.getLevel()).getSnapshot(header.key);
            helper.assertTrue(snapshot instanceof Blueprint, "Snapshot " + snapshot);
            helper.assertTrue(snapshot.size.equals(new BlockPos(3, 2, 3)), "Size " + snapshot.size);
            TileBuilder tile = placeBuilder(helper, builder);
            tile.invResources.setStackInSlot(0, new ItemStack(Blocks.STONE, 4));
            tile.invResources.setStackInSlot(1, new ItemStack(Blocks.OAK_LOG, 4));
            tile.invSnapshot.setStackInSlot(0, blueprint[0]);
            helper.assertBlockProperty(builder, BlockBuilder.SNAPSHOT_TYPE, EnumOptionalSnapshotType.BLUEPRINT);
        }).thenWaitUntil(() -> {
            helper.assertBlockPresent(Blocks.STONE, new BlockPos(3, 1, 8));
            helper.assertBlockPresent(Blocks.OAK_LOG, new BlockPos(4, 1, 9));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(2, 1, 7));
        }).thenSucceed();
    }

    /** A template scanned by the architect table is built by a builder with any blocks. */
    @GameTest(template = "builders", timeoutTicks = 1600)
    public static void builderBuildsTemplate(GameTestHelper helper) {
        BlockPos architect = new BlockPos(1, 1, 1);
        List<BlockPos> markers = List.of(new BlockPos(2, 1, 1), new BlockPos(4, 1, 1), new BlockPos(2, 1, 3));
        helper.setBlock(new BlockPos(3, 1, 2), Blocks.GOLD_BLOCK);
        helper.setBlock(new BlockPos(4, 1, 3), Blocks.DIAMOND_BLOCK);
        placeMarkers(helper, markers);
        BlockPos builder = new BlockPos(1, 1, 7);
        powerEachTick(helper, builder, 100 * MjAPI.MJ);
        ItemStack[] template = new ItemStack[1];
        helper.startSequence().thenExecuteAfter(2, () -> {
            connectMarkers(helper, markers);
            placeArchitect(helper, architect).invSnapshotIn.setStackInSlot(0,
                ItemSnapshot.getClean(EnumSnapshotType.TEMPLATE));
        }).thenWaitUntil(() -> {
            ItemStack out = helper.<TileArchitectTable>getBlockEntity(architect).invSnapshotOut.getStackInSlot(0);
            helper.assertTrue(ItemSnapshot.isUsed(out) && out.getItem() == BCBuildersItems.TEMPLATE.get(),
                "No template came out: " + out);
            template[0] = out.copy();
        }).thenExecute(() -> {
            Snapshot snapshot = GlobalSavedDataSnapshots.get(helper.getLevel())
                .getSnapshot(ItemSnapshot.getHeader(template[0]).key);
            helper.assertTrue(snapshot instanceof Template, "Snapshot " + snapshot);
            TileBuilder tile = placeBuilder(helper, builder);
            tile.invResources.setStackInSlot(0, new ItemStack(Blocks.COBBLESTONE, 8));
            tile.invSnapshot.setStackInSlot(0, template[0]);
        }).thenWaitUntil(() -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(3, 1, 8));
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(4, 1, 9));
            helper.assertBlockPresent(Blocks.AIR, new BlockPos(3, 1, 9));
        }).thenSucceed();
    }

    private static TileArchitectTable placeArchitect(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BCBuildersBlocks.ARCHITECT.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.WEST));
        TileArchitectTable table = helper.getBlockEntity(pos);
        table.onPlacedBy(player(helper, "bc_architect"), new ItemStack(BCBuildersBlocks.ARCHITECT.get()));
        return table;
    }

    private static TileBuilder placeBuilder(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos, BCBuildersBlocks.BUILDER.get().defaultBlockState()
            .setValue(BlockBCBase_Neptune.PROP_FACING, Direction.WEST));
        TileBuilder tile = helper.getBlockEntity(pos);
        tile.onPlacedBy(player(helper, "bc_builder"), new ItemStack(BCBuildersBlocks.BUILDER.get()));
        return tile;
    }

    /** An architect table with nothing behind it shows that it has no area. */
    @GameTest(template = "empty")
    public static void architectWithoutAreaIsInvalid(GameTestHelper helper) {
        BlockPos architect = new BlockPos(1, 1, 1);
        helper.setBlock(architect, BCBuildersBlocks.ARCHITECT.get().defaultBlockState());
        helper.<TileArchitectTable>getBlockEntity(architect).onPlacedBy(player(helper, "bc_architect"),
            new ItemStack(BCBuildersBlocks.ARCHITECT.get()));
        helper.assertBlockProperty(architect, buildcraftreborn.builders.block.BlockArchitectTable.PROP_VALID, false);
        helper.succeed();
    }

    // Replacer and single schematics

    /** The replacer swaps one block for another in a blueprint, and the blueprint that comes out is the new one. */
    @GameTest(template = "empty")
    public static void replacerSwapsBlocks(GameTestHelper helper) {
        ISchematicBlock stone = schematic(helper, Blocks.STONE.defaultBlockState());
        ISchematicBlock glass = schematic(helper, Blocks.GLASS.defaultBlockState());
        Blueprint blueprint = new Blueprint();
        blueprint.size = new BlockPos(1, 1, 1);
        blueprint.offset = BlockPos.ZERO;
        blueprint.facing = Direction.NORTH;
        blueprint.data = new int[] { 0 };
        blueprint.palette.add(stone);
        blueprint.computeKey();
        GlobalSavedDataSnapshots snapshots = GlobalSavedDataSnapshots.get(helper.getLevel());
        snapshots.addSnapshot(blueprint);
        ItemStack used = ItemSnapshot.getUsed(EnumSnapshotType.BLUEPRINT,
            new Snapshot.Header(blueprint.key, UUID.randomUUID(), new java.util.Date(), "replace me"));

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCBuildersBlocks.REPLACER.get());
        TileReplacer replacer = helper.getBlockEntity(pos);
        replacer.invSnapshot.setStackInSlot(0, used);
        replacer.invSchematicFrom.setStackInSlot(0, schematicItem(stone));
        replacer.invSchematicTo.setStackInSlot(0, schematicItem(glass));
        helper.runAfterDelay(2, () -> {
            Snapshot.Header header = ItemSnapshot.getHeader(replacer.invSnapshot.getStackInSlot(0));
            helper.assertTrue(header != null && !header.key.equals(blueprint.key), "The blueprint wasn't changed");
            Snapshot changed = snapshots.getSnapshot(header.key);
            helper.assertTrue(changed instanceof Blueprint b && b.palette.get(0).equals(glass),
                "The new blueprint holds " + (changed instanceof Blueprint b ? b.palette : changed));
            helper.assertTrue(replacer.invSchematicFrom.getStackInSlot(0).isEmpty(), "The schematics weren't used");
            helper.succeed();
        });
    }

    /** A single schematic copies a block, then places copies of it using the player's items. */
    @GameTest(template = "empty")
    public static void singleSchematicCopiesAndPlaces(GameTestHelper helper) {
        BlockPos source = new BlockPos(0, 1, 0);
        helper.setBlock(source, Blocks.BRICKS);
        FakePlayer player = player(helper, "bc_schematic");
        ItemStack schematic = new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, schematic);
        InteractionResult copied = schematic.onItemUseFirst(useOn(helper, player, source, Direction.UP));
        helper.assertTrue(copied == InteractionResult.SUCCESS && ItemSchematicSingle.isUsed(schematic),
            "Didn't copy: " + copied);
        helper.assertTrue(schematic.getMaxStackSize() == 1, "A used schematic stacks");

        BlockPos target = new BlockPos(2, 0, 2);
        helper.setBlock(target, Blocks.STONE);
        // Without bricks it can't place
        InteractionResult failed = schematic.onItemUseFirst(useOn(helper, player, target, Direction.UP));
        helper.assertTrue(failed == InteractionResult.FAIL, "Placed without items");
        player.getInventory().add(new ItemStack(Blocks.BRICKS, 2));
        InteractionResult placed = schematic.onItemUseFirst(useOn(helper, player, target, Direction.UP));
        helper.assertTrue(placed == InteractionResult.SUCCESS, "Didn't place: " + placed);
        helper.assertBlockPresent(Blocks.BRICKS, target.above());
        helper.assertTrue(player.getInventory().countItem(Items.BRICKS) == 1, "Didn't use a brick");
        // Sneak-using it in the air clears it, and it stacks again (with new ones too)
        player.setShiftKeyDown(true);
        schematic.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.assertTrue(!ItemSchematicSingle.isUsed(schematic) && schematic.getMaxStackSize() == 16
            && ItemStack.isSameItemSameComponents(schematic, new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE.get())),
            "A cleared schematic is " + schematic + " stacking to " + schematic.getMaxStackSize());
        helper.succeed();
    }

    // Filler patterns (1.12's ShapePatternsTester, which needed vanilla's bootstrap, so it runs in game here)

    private static final BlockPos[] PATTERN_SIZES = {
        new BlockPos(1, 1, 1), new BlockPos(2, 1, 1), new BlockPos(3, 1, 1),
        new BlockPos(2, 2, 2), new BlockPos(3, 2, 2), new BlockPos(4, 2, 2),
        new BlockPos(2, 3, 2), new BlockPos(2, 2, 3), new BlockPos(2, 8, 2),
        new BlockPos(3, 3, 3), new BlockPos(4, 4, 4), new BlockPos(5, 5, 5),
        new BlockPos(6, 6, 6), new BlockPos(7, 7, 7), new BlockPos(11, 13, 12)
    };

    /** Every shape pattern fills templates of a few sizes with its default parameters ("none" refuses to). */
    @GameTest(template = "empty")
    public static void shapePatternsFillEverySize(GameTestHelper helper) {
        for (IFillerPattern pattern : BCBuildersStatements.PATTERNS) {
            if (!(pattern instanceof IFillerPatternShape shape)) {
                continue;
            }
            IStatementParameter[] params = new IStatementParameter[shape.maxParameters()];
            for (int i = 0; i < params.length; i++) {
                params[i] = shape.createParameter(i);
            }
            for (BlockPos size : PATTERN_SIZES) {
                boolean filled = shape.fillTemplate(emptyTemplate(size), params);
                String what = shape.getUniqueTag() + " in " + size.toShortString();
                if (shape == BCBuildersStatements.PATTERN_NONE) {
                    helper.assertFalse(filled, what + " filled");
                } else {
                    helper.assertTrue(filled, what + " didn't fill");
                }
            }
        }
        helper.succeed();
    }

    /** A hemisphere facing any way is exactly half of the full sphere of twice its size. */
    @GameTest(template = "empty")
    public static void halfSpheresMatchTheFullSphere(GameTestHelper helper) {
        for (BlockPos size : PATTERN_SIZES) {
            BlockPos fullSize = new BlockPos(size.getX() * 2, size.getY() * 2, size.getZ() * 2);
            IFilledTemplate full = emptyTemplate(fullSize);
            helper.assertTrue(BCBuildersStatements.PATTERN_SPHERE.fillTemplate(full,
                new IStatementParameter[] { PatternParameterHollow.HOLLOW }), "Sphere didn't fill");
            for (Direction face : Direction.values()) {
                BlockPos halfSize = VecUtil.replaceValue(fullSize, face.getAxis(),
                    VecUtil.getValue(size, face.getAxis()));
                IFilledTemplate half = emptyTemplate(halfSize);
                helper.assertTrue(BCBuildersStatements.PATTERN_HEMI_SPHERE.fillTemplate(half,
                    new IStatementParameter[] { PatternParameterHollow.HOLLOW, PatternParameterFacing.get(face) }),
                    "Half sphere didn't fill");
                int dx = face == Direction.WEST ? half.getSize().getX() : 0;
                int dy = face == Direction.DOWN ? half.getSize().getY() : 0;
                int dz = face == Direction.NORTH ? half.getSize().getZ() : 0;
                for (int z = 0; z <= half.getMax().getZ(); z++) {
                    for (int y = 0; y <= half.getMax().getY(); y++) {
                        for (int x = 0; x <= half.getMax().getX(); x++) {
                            if (full.get(x + dx, y + dy, z + dz) != half.get(x, y, z)) {
                                helper.fail("Half sphere [" + face + "] in " + fullSize.toShortString()
                                    + " didn't match the full sphere at " + x + ", " + y + ", " + z);
                            }
                        }
                    }
                }
            }
        }
        helper.succeed();
    }

    private static IFilledTemplate emptyTemplate(BlockPos size) {
        Template template = new Template();
        template.size = size;
        template.offset = BlockPos.ZERO;
        template.data = new BitSet(Snapshot.getDataSize(size));
        return template.getFilledTemplate();
    }

    // Schematic rules (1.21's block ids, block tags and NBT)

    @GameTest(template = "empty")
    public static void schematicRulesNeedTheRightItems(GameTestHelper helper) {
        helper.assertTrue(RulesLoader.READ_DOMAINS.contains("minecraft"), "Rules not loaded");
        assertItems(helper, Blocks.OAK_SLAB.defaultBlockState().setValue(SlabBlock.TYPE, SlabType.DOUBLE),
            List.of(new ItemStack(Items.OAK_SLAB, 2)));
        assertItems(helper, Blocks.SNOW.defaultBlockState().setValue(net.minecraft.world.level.block.SnowLayerBlock
            .LAYERS, 3), List.of(new ItemStack(Items.SNOW, 3)));
        assertItems(helper, Blocks.OAK_DOOR.defaultBlockState().setValue(DoorBlock.HALF, DoubleBlockHalf.UPPER),
            List.of());
        assertItems(helper, Blocks.OAK_DOOR.defaultBlockState(), List.of(new ItemStack(Items.OAK_DOOR)));
        assertItems(helper, Blocks.WALL_TORCH.defaultBlockState(), List.of(new ItemStack(Items.TORCH)));
        assertItems(helper, Blocks.POTTED_DANDELION.defaultBlockState(),
            List.of(new ItemStack(Items.FLOWER_POT), new ItemStack(Items.DANDELION)));
        ItemStack water = PotionContents.createItemStack(Items.POTION, Potions.WATER);
        water.setCount(2);
        assertItems(helper, Blocks.WATER_CAULDRON.defaultBlockState().setValue(LayeredCauldronBlock.LEVEL, 2),
            List.of(new ItemStack(Items.CAULDRON), water));
        assertItems(helper, Blocks.CARROTS.defaultBlockState(), List.of(new ItemStack(Items.CARROT)));
        // Fire isn't copied at all
        BlockPos firePos = new BlockPos(1, 1, 1);
        helper.setBlock(firePos.below(), Blocks.NETHERRACK);
        helper.setBlock(firePos, Blocks.FIRE);
        BlockPos abs = helper.absolutePos(firePos);
        ISchematicBlock fire = SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(helper.getLevel(),
            abs, abs, helper.getLevel().getBlockState(abs), Blocks.FIRE));
        helper.assertTrue(fire.isAir(), "Fire was copied as " + fire);
        // Rails don't care about their shape, crops about their age
        helper.succeed();
    }

    private static void assertItems(GameTestHelper helper, BlockState state, List<ItemStack> expected) {
        BlockPos pos = new BlockPos(1, 1, 1);
        BlockPos abs = helper.absolutePos(pos);
        ISchematicBlock schematic = SchematicBlockManager.getSchematicBlock(
            new SchematicBlockContext(helper.getLevel(), abs, abs, state, state.getBlock()));
        List<ItemStack> items = schematic.computeRequiredItems();
        boolean same = items.size() == expected.size();
        for (int i = 0; same && i < items.size(); i++) {
            same = ItemStack.isSameItemSameComponents(items.get(i), expected.get(i))
                && items.get(i).getCount() == expected.get(i).getCount();
        }
        helper.assertTrue(same, state + " needs " + items + ", not " + expected);
    }

    // Helpers

    static ISchematicBlock schematic(GameTestHelper helper, BlockState state) {
        BlockPos abs = helper.absolutePos(BlockPos.ZERO);
        return SchematicBlockManager.getSchematicBlock(new SchematicBlockContext(helper.getLevel(), abs, abs, state,
            state.getBlock()));
    }

    private static ItemStack schematicItem(ISchematicBlock schematic) {
        ItemStack stack = new ItemStack(BCBuildersItems.SCHEMATIC_SINGLE.get());
        stack.set(BCBuildersItems.SCHEMATIC.get(), SchematicBlockManager.writeToNBT(schematic));
        stack.set(DataComponents.MAX_STACK_SIZE, 1);
        return stack;
    }

    private static UseOnContext useOn(GameTestHelper helper, FakePlayer player, BlockPos relative, Direction side) {
        BlockPos abs = helper.absolutePos(relative);
        return new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(abs).relative(side, 0.5), side, abs, false));
    }

    private static void placeMarkers(GameTestHelper helper, List<BlockPos> markers) {
        for (BlockPos pos : markers) {
            helper.setBlock(pos, BCCoreBlocks.MARKER_VOLUME.get().defaultBlockState()
                .setValue(BlockBCBase_Neptune.BLOCK_FACING_6, Direction.UP));
        }
    }

    /** Joins placed volume markers into one box (they join the marker cache a tick after they load, so call this a
     * couple of ticks after {@link #placeMarkers}). */
    private static void connectMarkers(GameTestHelper helper, List<BlockPos> markers) {
        VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(helper.getLevel());
        BlockPos first = helper.absolutePos(markers.get(0));
        for (int i = 1; i < markers.size(); i++) {
            helper.assertTrue(cache.tryConnect(first, helper.absolutePos(markers.get(i))),
                "Markers " + markers.get(0) + " and " + markers.get(i) + " didn't connect");
        }
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

    /** Sets a block with the state it would get from its neighbours (like a placed block). */
    private static void place(GameTestHelper helper, BlockPos relative, BlockState state) {
        BlockPos abs = helper.absolutePos(relative);
        helper.setBlock(relative, Block.updateFromNeighbourShapes(state, helper.getLevel(), abs));
    }

    private static FakePlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }
}
