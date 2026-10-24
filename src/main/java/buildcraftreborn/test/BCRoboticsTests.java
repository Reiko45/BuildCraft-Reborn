/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.MapColor;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.item.ItemMapLocation;
import buildcraftreborn.core.item.MapLocationData;
import buildcraftreborn.lib.item.BCItemComponents;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.container.ContainerZonePlanner;
import buildcraftreborn.robotics.tile.TileZonePlanner;
import buildcraftreborn.robotics.zone.ZoneMapRequestPayload;
import buildcraftreborn.robotics.zone.ZonePlan;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunk;
import buildcraftreborn.robotics.zone.ZonePlannerMapChunkKey;
import buildcraftreborn.robotics.zone.ZonePlannerMapDataServer;

/** Phase 6 tests for the robotics module: the zone planner and its map. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCRoboticsTests {
    private BCRoboticsTests() {}

    private static final BlockPos PLANNER = new BlockPos(1, 1, 1);

    private static TileZonePlanner placePlanner(GameTestHelper helper) {
        helper.setBlock(PLANNER, BCRoboticsBlocks.ZONE_PLANNER.get());
        return (TileZonePlanner) helper.getBlockEntity(PLANNER);
    }

    private static ItemStack brush(DyeColor colour) {
        ItemStack brush = new ItemStack(BCCoreItems.PAINTBRUSH.get());
        brush.set(BCItemComponents.COLOUR.get(), colour);
        return brush;
    }

    private static ZonePlan plan(int... xz) {
        ZonePlan plan = new ZonePlan();
        for (int i = 0; i < xz.length; i += 2) {
            plan.set(xz[i], xz[i + 1], true);
        }
        return plan;
    }

    private static Set<ZonePlan.Point> points(ZonePlan plan) {
        return plan.getAll().stream().collect(Collectors.toSet());
    }

    /** A zone map location and a red paintbrush give the planner's red zone the map's zone (moved to be relative to
     * the planner); a blue paintbrush and a clean map location give the map the blue zone (back in world
     * coordinates). Both work at the same time, so they finish together, after 200 ticks. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void zonePlannerCopiesZones(GameTestHelper helper) {
        TileZonePlanner tile = placePlanner(helper);
        BlockPos abs = helper.absolutePos(PLANNER);

        ItemStack zoneMap = new ItemStack(BCCoreItems.MAP_LOCATION.get());
        ItemMapLocation.setZone(zoneMap, plan(abs.getX() + 3, abs.getZ() + 2, abs.getX() - 1, abs.getZ()));
        tile.invInputPaintbrush.setStackInSlot(0, brush(DyeColor.RED));
        tile.invInputMapLocation.setStackInSlot(0, zoneMap);

        tile.layers[DyeColor.BLUE.getId()] = plan(0, 0, 5, -3);
        tile.invOutputPaintbrush.setStackInSlot(0, brush(DyeColor.BLUE));
        tile.invOutputMapLocation.setStackInSlot(0, new ItemStack(BCCoreItems.MAP_LOCATION.get()));

        helper.runAfterDelay(150, () -> helper.assertTrue(tile.invInputResult.getStackInSlot(0).isEmpty()
            && tile.invOutputResult.getStackInSlot(0).isEmpty(), "The planner finished too soon"));
        helper.succeedWhen(() -> {
            ItemStack inputResult = tile.invInputResult.getStackInSlot(0);
            ItemStack outputResult = tile.invOutputResult.getStackInSlot(0);
            helper.assertTrue(!inputResult.isEmpty() && !outputResult.isEmpty(), "Not done yet");
            helper.assertTrue(helper.getTick() < 230, "Input and output didn't work at the same time");

            helper.assertTrue(tile.invInputMapLocation.getStackInSlot(0).isEmpty()
                && MapLocationType.getFromStack(inputResult) == MapLocationType.CLEAN,
                "The zone map wasn't swapped for a clean one: " + inputResult);
            helper.assertTrue(points(tile.layers[DyeColor.RED.getId()]).equals(points(plan(3, 2, -1, 0))),
                "Red zone is " + tile.layers[DyeColor.RED.getId()].getAll());

            helper.assertTrue(tile.invOutputMapLocation.getStackInSlot(0).isEmpty()
                && MapLocationType.getFromStack(outputResult) == MapLocationType.ZONE,
                "The output map holds " + outputResult);
            ZonePlan written = (ZonePlan) BCCoreItems.MAP_LOCATION.get().getZone(outputResult);
            helper.assertTrue(points(written).equals(points(plan(abs.getX(), abs.getZ(), abs.getX() + 5,
                abs.getZ() - 3))), "Written zone is " + written.getAll());
            // the paintbrushes stay
            helper.assertTrue(!tile.invInputPaintbrush.getStackInSlot(0).isEmpty()
                && !tile.invOutputPaintbrush.getStackInSlot(0).isEmpty(), "A paintbrush was used up");
        });
    }

    /** Only zone map locations go in the input map slot, and only paintbrushes in the brush slots. */
    @GameTest(template = "empty", timeoutTicks = 300)
    public static void zonePlannerSlotsTakeTheRightItems(GameTestHelper helper) {
        TileZonePlanner tile = placePlanner(helper);
        ItemStack spot = new ItemStack(BCCoreItems.MAP_LOCATION.get());
        ItemMapLocation.setData(spot, MapLocationData.spot("", helper.absolutePos(PLANNER),
            Direction.UP));
        helper.assertTrue(!tile.invInputMapLocation.isItemValid(0, spot), "The input took a spot map location");
        ItemStack zone = new ItemStack(BCCoreItems.MAP_LOCATION.get());
        ItemMapLocation.setZone(zone, plan(0, 0));
        helper.assertTrue(tile.invInputMapLocation.isItemValid(0, zone), "The input refused a zone map location");
        helper.assertTrue(tile.invOutputMapLocation.isItemValid(0, spot), "The output refused a map location");
        // A stack of maps goes in one at a time
        ItemStack blankMaps = new ItemStack(BCCoreItems.MAP_LOCATION.get(), 4);
        ItemStack left = tile.invOutputMapLocation.insertItem(0, blankMaps, true);
        helper.assertTrue(left.getCount() == 3, "The output took " + (4 - left.getCount()) + " of 4 maps");
        // (menu slots go by the slot limit, so clicking a stack in puts one in too)
        helper.assertTrue(tile.invOutputMapLocation.getSlotLimit(0) == 1, "The map slot's limit isn't 1");
        helper.assertTrue(!tile.invPaintbrushes.isItemValid(0, new ItemStack(Blocks.STONE)),
            "The brush storage took stone");
        helper.assertTrue(tile.invPaintbrushes.isItemValid(0, brush(DyeColor.GREEN)),
            "The brush storage refused a paintbrush");
        // a clean brush has no zone to copy
        tile.invInputPaintbrush.setStackInSlot(0, new ItemStack(BCCoreItems.PAINTBRUSH.get()));
        tile.invInputMapLocation.setStackInSlot(0, zone);
        helper.runAfterDelay(TileZonePlanner.PROCESS_TICKS + 10, () -> {
            helper.assertTrue(tile.invInputResult.getStackInSlot(0).isEmpty(), "A clean brush copied a zone");
            helper.succeed();
        });
    }

    /** The zones are saved with the planner. */
    @GameTest(template = "empty")
    public static void zonePlannerSavesItsZones(GameTestHelper helper) {
        TileZonePlanner tile = placePlanner(helper);
        tile.layers[DyeColor.ORANGE.getId()] = plan(0, 0, 17, -40, -100, 3);
        tile.layers[DyeColor.BLACK.getId()] = plan(2, 2);
        CompoundTag nbt = tile.saveWithFullMetadata(helper.getLevel().registryAccess());
        TileZonePlanner copy = new TileZonePlanner(tile.getBlockPos(), tile.getBlockState());
        copy.loadWithComponents(nbt, helper.getLevel().registryAccess());
        for (int i = 0; i < 16; i++) {
            helper.assertTrue(points(copy.layers[i]).equals(points(tile.layers[i])), "Zone " + i + " changed");
        }
        helper.assertTrue(copy.layers[DyeColor.ORANGE.getId()].get(-100, 3), "Orange zone lost a column");
        helper.succeed();
    }

    /** A zone painted in the GUI is only taken from a player using the planner, for one of the 16 colours, and if it
     * isn't too big. */
    @GameTest(template = "empty")
    public static void zonePlannerOnlyTakesZonesFromItsUsers(GameTestHelper helper) {
        TileZonePlanner tile = placePlanner(helper);
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
            new GameProfile(UUID.nameUUIDFromBytes("bc_zone".getBytes()), "bc_zone"));
        helper.assertTrue(!tile.receivePlanChange(player, 3, plan(1, 1)), "Took a zone from a player not using it");
        ContainerZonePlanner menu = new ContainerZonePlanner(0, player, tile);
        try {
            helper.assertTrue(tile.receivePlanChange(player, 3, plan(1, 1)), "Refused a zone from its user");
            helper.assertTrue(tile.layers[3].get(1, 1), "The zone wasn't set");
            helper.assertTrue(!tile.receivePlanChange(player, 16, plan(1, 1)), "Took a zone for colour 16");
            ZonePlan huge = new ZonePlan();
            for (int i = 0; i <= TileZonePlanner.MAX_SENT_CHUNKS; i++) {
                huge.set(i * 16, 0, true);
            }
            helper.assertTrue(!tile.receivePlanChange(player, 4, huge), "Took a zone that was too big");
        } finally {
            menu.removed(player);
        }
        helper.succeed();
    }

    /** A map chunk holds each column's top block that has a map colour (glass has none), and the server only makes
     * them for loaded chunks near the player who asks. */
    @GameTest(template = "empty")
    public static void zonePlannerMapsTheSurface(GameTestHelper helper) {
        BlockPos stone = new BlockPos(2, 2, 2);
        helper.setBlock(stone, Blocks.STONE);
        helper.setBlock(stone.above(), Blocks.GLASS);
        BlockPos abs = helper.absolutePos(stone);
        Level level = helper.getLevel();
        int topLevel = Math.floorDiv(level.getMaxBuildHeight(), ZonePlannerMapChunkKey.LEVEL_HEIGHT);
        ZonePlannerMapChunkKey key = new ZonePlannerMapChunkKey(new ChunkPos(abs), level.dimension(), topLevel);

        ZonePlannerMapChunk chunk = new ZonePlannerMapChunk(level, key);
        ZonePlannerMapChunk.MapColourData data = chunk.getData(abs.getX(), abs.getZ());
        helper.assertTrue(data != null && data.posY() == abs.getY() && data.colour() == MapColor.STONE.col,
            "The map found " + data + " above " + abs);
        helper.assertTrue(ZonePlannerMapDataServer.INSTANCE.getChunk(level, key) != null, "No map of a loaded chunk");

        ZonePlannerMapChunkKey far = new ZonePlannerMapChunkKey(new ChunkPos(100_000, 100_000), level.dimension(),
            topLevel);
        helper.assertTrue(ZonePlannerMapDataServer.INSTANCE.getChunk(level, far) == null,
            "Made a map of an unloaded chunk");

        FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
            new GameProfile(UUID.nameUUIDFromBytes("bc_zone_map".getBytes()), "bc_zone_map"));
        player.setPos(abs.getX(), abs.getY(), abs.getZ());
        helper.assertTrue(ZoneMapRequestPayload.canSee(player, key), "The player can't see the chunk they are in");
        helper.assertTrue(!ZoneMapRequestPayload.canSee(player, far), "The player can see a chunk far away");
        helper.assertTrue(!ZoneMapRequestPayload.canSee(player, new ZonePlannerMapChunkKey(key.chunkPos(),
            Level.NETHER, topLevel)), "The player can see another dimension");
        helper.succeed();
    }
}
