/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.nbt.Tag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.enums.EnumDecoratedBlock;
import buildcraftreborn.api.tiles.ITileAreaProvider;
import buildcraftreborn.api.tiles.TilesAPI;
import buildcraftreborn.core.BCCoreBlocks;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.marker.PathCache;
import buildcraftreborn.core.marker.PathSubCache;
import buildcraftreborn.core.marker.VolumeCache;
import buildcraftreborn.core.marker.VolumeSavedData;
import buildcraftreborn.core.marker.VolumeSubCache;
import buildcraftreborn.core.tile.TileMarkerPath;
import buildcraftreborn.core.tile.TileMarkerVolume;
import buildcraftreborn.lib.block.BlockBCBase_Neptune;

/** Phase 2 tests for volume and path markers, the marker connector and decorations. Markers are connected in pairs
 * (not with a right click, which would also find the markers of other tests running in the same world). */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCMarkerTests {
    private BCMarkerTests() {}

    private static void placeMarker(GameTestHelper helper, BlockPos pos, Block marker) {
        BlockState state = marker.defaultBlockState().setValue(BlockBCBase_Neptune.BLOCK_FACING_6, Direction.UP);
        helper.setBlock(pos, state);
    }

    /** Three volume markers in an L make a flat box; the area capability reports it, and breaking one shrinks it. */
    @GameTest(template = "empty")
    public static void volumeMarkersMakeABox(GameTestHelper helper) {
        BlockPos a = new BlockPos(0, 0, 0);
        BlockPos b = new BlockPos(2, 0, 0);
        BlockPos c = new BlockPos(0, 0, 2);
        for (BlockPos pos : List.of(a, b, c)) {
            placeMarker(helper, pos, BCCoreBlocks.MARKER_VOLUME.get());
        }
        // Tiles join the marker cache when they load, on the next tick
        helper.runAfterDelay(2, () -> {
            VolumeSubCache cache = VolumeCache.INSTANCE.getSubCache(helper.getLevel());
            BlockPos absA = helper.absolutePos(a);
            helper.assertTrue(cache.tryConnect(absA, helper.absolutePos(b)), "A and B didn't connect");
            helper.assertTrue(cache.tryConnect(absA, helper.absolutePos(c)), "C didn't join the box");
            helper.assertFalse(cache.canConnect(absA, helper.absolutePos(new BlockPos(1, 0, 1))),
                "The middle of the box can join it");

            ITileAreaProvider area = helper.getLevel().getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER,
                helper.absolutePos(b), null);
            helper.assertTrue(area != null, "No area capability");
            helper.assertTrue(area.min().equals(absA) && area.max().equals(helper.absolutePos(new BlockPos(2, 0, 2))),
                "Wrong area: " + area.min() + " to " + area.max());

            // The box is saved with the level
            CompoundTag saved = helper.getLevel().getDataStorage()
                .computeIfAbsent(VolumeSavedData.FACTORY, VolumeSavedData.NAME)
                .save(new CompoundTag(), helper.getLevel().registryAccess());
            boolean found = false;
            for (Tag connection : saved.getList("connections", Tag.TAG_LIST)) {
                ListTag list = (ListTag) connection;
                if (list.size() == 3) {
                    CompoundTag first = new CompoundTag();
                    first.put("p", list.get(0));
                    BlockPos pos = NbtUtils.readBlockPos(first, "p").orElse(BlockPos.ZERO);
                    found |= pos.equals(absA) || pos.equals(helper.absolutePos(b)) || pos.equals(helper.absolutePos(c));
                }
            }
            helper.assertTrue(found, "The box wasn't saved");

            helper.getLevel().destroyBlock(helper.absolutePos(b), true);
            area = helper.getLevel().getCapability(TilesAPI.CAP_TILE_AREA_PROVIDER, helper.absolutePos(a), null);
            helper.assertTrue(area.max().equals(helper.absolutePos(c)), "Box didn't shrink: " + area.max());
            helper.assertItemEntityPresent(BCCoreBlocks.MARKER_VOLUME.get().asItem(), b, 2.0);
            helper.succeed();
        });
    }

    /** A powered volume marker shows its signal lasers; markers break when the block under them goes. */
    @GameTest(template = "empty")
    public static void volumeMarkerSignalsAndSupport(GameTestHelper helper) {
        BlockPos marker = new BlockPos(1, 1, 1);
        helper.setBlock(marker.below(), Blocks.STONE);
        placeMarker(helper, marker, BCCoreBlocks.MARKER_VOLUME.get());
        helper.setBlock(marker.east(), Blocks.REDSTONE_BLOCK);
        helper.runAfterDelay(2, () -> {
            TileMarkerVolume tile = helper.getBlockEntity(marker);
            helper.assertTrue(tile.isShowingSignals(), "Powered marker isn't showing signals");
            helper.setBlock(marker.east(), Blocks.AIR);
            helper.assertFalse(tile.isShowingSignals(), "Unpowered marker still shows signals");
            helper.setBlock(marker.below(), Blocks.AIR);
            helper.assertBlockNotPresent(BCCoreBlocks.MARKER_VOLUME.get(), marker);
            helper.succeed();
        });
    }

    /** Path markers join end to end, can be turned around by right clicking, and can close into a loop. */
    @GameTest(template = "empty")
    public static void pathMarkersMakeAPath(GameTestHelper helper) {
        BlockPos p1 = new BlockPos(0, 0, 0);
        BlockPos p2 = new BlockPos(2, 0, 0);
        BlockPos p3 = new BlockPos(2, 0, 2);
        for (BlockPos pos : List.of(p1, p2, p3)) {
            placeMarker(helper, pos, BCCoreBlocks.MARKER_PATH.get());
        }
        helper.runAfterDelay(2, () -> {
            PathSubCache cache = PathCache.INSTANCE.getSubCache(helper.getLevel());
            BlockPos a1 = helper.absolutePos(p1), a2 = helper.absolutePos(p2), a3 = helper.absolutePos(p3);
            helper.assertTrue(cache.tryConnect(a1, a2), "P1 and P2 didn't connect");
            helper.assertTrue(cache.tryConnect(a2, a3), "P3 didn't join the path");
            TileMarkerPath tile = helper.getBlockEntity(p2);
            helper.assertTrue(tile.getPath().equals(List.of(a1, a2, a3)), "Wrong path: " + tile.getPath());

            FakePlayer player = FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.randomUUID(), "bc_path"));
            tile.onActivated(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            helper.assertTrue(tile.getPath().equals(List.of(a3, a2, a1)), "Path didn't turn round: " + tile.getPath());

            helper.assertTrue(cache.tryConnect(a1, a3), "Path didn't close into a loop");
            helper.assertTrue(tile.getPath().equals(List.of(a3, a2, a1, a3)), "Wrong loop: " + tile.getPath());
            helper.succeed();
        });
    }

    /** Right clicking with the marker connector while looking along the gap between two markers connects them. */
    @GameTest(template = "empty")
    public static void markerConnectorConnectsWhatYouLookAt(GameTestHelper helper) {
        BlockPos a = new BlockPos(0, 1, 2);
        BlockPos b = new BlockPos(2, 1, 2);
        helper.setBlock(a.below(), Blocks.STONE);
        helper.setBlock(b.below(), Blocks.STONE);
        placeMarker(helper, a, BCCoreBlocks.MARKER_VOLUME.get());
        placeMarker(helper, b, BCCoreBlocks.MARKER_VOLUME.get());
        helper.runAfterDelay(2, () -> {
            FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
                new GameProfile(UUID.randomUUID(), "bc_connector"));
            // Eyes 2 blocks north of the middle of the gap, looking south at it
            BlockPos gap = helper.absolutePos(new BlockPos(1, 1, 2));
            player.moveTo(gap.getX() + 0.5, gap.getY() + 0.5 - player.getEyeHeight(), gap.getZ() + 0.5 - 2, 0, 0);
            ItemStack connector = new ItemStack(BCCoreItems.MARKER_CONNECTOR.get());
            player.setItemInHand(InteractionHand.MAIN_HAND, connector);
            connector.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
            TileMarkerVolume tile = helper.getBlockEntity(a);
            helper.assertTrue(tile.getCurrentConnection() != null, "Connector didn't connect the markers");
            helper.assertTrue(tile.max().equals(helper.absolutePos(b)), "Connected to the wrong marker");
            helper.succeed();
        });
    }

    /** Decorations give off light like 1.12's (10 for the paper-like ones, none for the others). */
    @GameTest(template = "empty")
    public static void decorationsGlow(GameTestHelper helper) {
        helper.assertTrue(BCCoreBlocks.DECORATED.get(EnumDecoratedBlock.BLUEPRINT).get().defaultBlockState()
            .getLightEmission() == 10, "Blueprint decoration should glow");
        helper.assertTrue(BCCoreBlocks.DECORATED.get(EnumDecoratedBlock.DESTROY).get().defaultBlockState()
            .getLightEmission() == 0, "Destroy decoration shouldn't glow");
        helper.succeed();
    }
}
