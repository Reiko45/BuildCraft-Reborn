/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CraftingInput;
import net.minecraft.world.item.crafting.CraftingRecipe;
import net.minecraft.world.item.crafting.RecipeHolder;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.energy.IEnergyStorage;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.boards.RedstoneBoardRegistry;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.enums.EnumRedstoneChipset;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.api.transport.pipe.PipeDefinition;
import buildcraftreborn.api.enums.EnumSnapshotType;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.item.ItemSnapshot;
import buildcraftreborn.builders.snapshot.Blueprint;
import buildcraftreborn.builders.snapshot.GlobalSavedDataSnapshots;
import buildcraftreborn.builders.snapshot.Snapshot;
import buildcraftreborn.builders.tile.TileConstructionMarker;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.robotics.BCRoboticsBlocks;
import buildcraftreborn.robotics.BCRoboticsEntities;
import buildcraftreborn.robotics.BCRoboticsItems;
import buildcraftreborn.robotics.BCRoboticsPlugs;
import buildcraftreborn.robotics.boards.BCBoardNBT;
import buildcraftreborn.robotics.entity.EntityRobot;
import buildcraftreborn.robotics.item.ItemRedstoneBoard;
import buildcraftreborn.robotics.item.ItemRobot;
import buildcraftreborn.robotics.item.ItemRobotGoggles;
import buildcraftreborn.robotics.station.PluggableRobotStation;
import buildcraftreborn.robotics.tile.TileChargingTable;
import buildcraftreborn.robotics.tile.TileProgrammingTable;
import buildcraftreborn.robotics.tile.TileRequester;
import buildcraftreborn.silicon.BCSiliconBlocks;
import buildcraftreborn.silicon.BCSiliconItems;
import buildcraftreborn.silicon.plug.PluggablePulsar;
import buildcraftreborn.silicon.tile.TileIntegrationTable;
import buildcraftreborn.transport.BCTransportPipes;
import buildcraftreborn.transport.stripes.StripesHandlerPlant;
import buildcraftreborn.transport.tile.TilePipeHolder;

/** Phase 7 tests: 7.1's robots, their stations and tables, and the requester. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCRobotTests {
    private BCRobotTests() {}

    private static FakePlayer player(GameTestHelper helper) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(
            "bc_robots".getBytes()), "bc_robots"));
    }

    private static RedstoneBoardRobotNBT board(String name) {
        RedstoneBoardRobotNBT board = BCBoardNBT.REGISTRY.get(name);
        if (board == null) {
            throw new IllegalStateException("No board " + name);
        }
        return board;
    }

    /** Puts a docking station on the top of a pipe and a robot on it, as a player would. */
    private static EntityRobot placeRobot(GameTestHelper helper, BlockPos pipePos,
        PipeDefinition pipe, String boardName, long power) {
        TilePipeHolder holder = BCTransportTests.placePipe(helper, pipePos, pipe, null);
        PluggableRobotStation plug = new PluggableRobotStation(BCRoboticsPlugs.robotStation, holder, Direction.UP);
        holder.replacePluggable(Direction.UP, plug);
        FakePlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemRobot.createRobotStack(board(boardName), power));
        helper.assertTrue(plug.onPluggableActivate(player, null, 0.5F, 1, 0.5F), "The station ignored the robot");
        helper.assertTrue(player.getMainHandItem().isEmpty(), "The robot item wasn't used");
        List<EntityRobot> robots = robots(helper);
        helper.assertTrue(robots.size() == 1, robots.size() + " robots were placed");
        return robots.get(0);
    }

    private static List<EntityRobot> robots(GameTestHelper helper) {
        return helper.getLevel().getEntitiesOfClass(EntityRobot.class, helper.getBounds());
    }

    // Items and recipes

    /** Every 7.1 board is registered and programmable, with 7.1's costs. */
    @GameTest(template = "empty")
    public static void boardsAreRegistered(GameTestHelper helper) {
        String[] names = { "picker", "carrier", "fluid_carrier", "delivery", "lumberjack", "harvester", "miner",
            "planter", "farmer", "leave_cutter", "butcher", "shovelman", "pump", "knight", "bomber", "stripes",
            "builder" };
        for (String name : names) {
            RedstoneBoardRobotNBT board = board(name);
            helper.assertTrue(RedstoneBoardRegistry.instance.getAllBoardNBTs().contains(board), name + " is hidden");
        }
        helper.assertTrue(RedstoneBoardRegistry.instance.getPowerCost(board("miner")) == 3200 * MjAPI.MJ,
            "The miner costs " + RedstoneBoardRegistry.instance.getPowerCost(board("miner")));
        helper.assertTrue(RedstoneBoardRegistry.instance.getPowerCost(board("picker")) == 800 * MjAPI.MJ,
            "The picker costs " + RedstoneBoardRegistry.instance.getPowerCost(board("picker")));
        // Unknown ids read as the clean board
        helper.assertTrue(RedstoneBoardRegistry.instance.getRedstoneBoard("nonsense:board")
            == RedstoneBoardRegistry.instance.getEmptyRobotBoard(), "An unknown board isn't clean");
        helper.succeed();
    }

    /** Robot items keep their board and power, stack only when blank, and take Forge Energy. */
    @GameTest(template = "empty")
    public static void robotItemsHoldBoardsAndPower(GameTestHelper helper) {
        ItemStack blank = new ItemStack(BCRoboticsItems.ROBOT.get());
        helper.assertTrue(!ItemRobot.hasBoard(blank) && blank.getMaxStackSize() == 16, "Blank robots stack to "
            + blank.getMaxStackSize());
        ItemStack miner = ItemRobot.createRobotStack(board("miner"), 0);
        helper.assertTrue(ItemRobot.getRobotNBT(miner) == board("miner") && miner.getMaxStackSize() == 1,
            "The miner robot is " + ItemRobot.getRobotNBT(miner).getID() + " stacking to " + miner.getMaxStackSize());

        IEnergyStorage fe = miner.getCapability(Capabilities.EnergyStorage.ITEM);
        helper.assertTrue(fe != null && fe.canReceive(), "The robot has no Forge Energy capability");
        int taken = fe.receiveEnergy(1000, false);
        helper.assertTrue(taken == 1000 && ItemRobot.getPower(miner) == 1000 * MjAPI.getRfConversion().mjPerRf,
            "1000 RF gave the robot " + ItemRobot.getPower(miner));
        helper.assertTrue(fe.getMaxEnergyStored() == 100_000, "The robot holds " + fe.getMaxEnergyStored() + " RF");
        IEnergyStorage blankFe = blank.getCapability(Capabilities.EnergyStorage.ITEM);
        helper.assertTrue(blankFe == null || blankFe.receiveEnergy(100, false) == 0, "A blank robot took energy");
        helper.succeed();
    }

    /** The programming table offers every board for a blank board, and makes the one picked once lasers have given it
     * the board's cost. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void programmingTableProgramsBoards(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCRoboticsBlocks.PROGRAMMING_TABLE.get());
        TileProgrammingTable table = helper.getBlockEntity(pos);
        helper.assertTrue(!table.invInput.isItemValid(0, new ItemStack(Blocks.STONE)), "The table took stone");
        table.invInput.setStackInSlot(0, new ItemStack(BCRoboticsItems.REDSTONE_BOARD.get(), 2));
        helper.assertTrue(table.recipe != null && table.options.size() == 17, "The table offers "
            + table.options.size() + " options");
        helper.assertTrue(table.getTarget() == 0, "The table works with nothing picked");
        int miner = -1;
        for (int i = 0; i < table.options.size(); i++) {
            if (ItemRedstoneBoard.getBoardNBT(table.options.get(i)) == board("miner")) {
                miner = i;
            }
        }
        helper.assertTrue(miner >= 0, "The miner isn't offered");
        // (the GUI sends this)
        table.optionId = miner;
        helper.assertTrue(table.getTarget() == 3200 * MjAPI.MJ, "The miner board needs " + table.getTarget());
        table.receiveLaserPower(table.getRequiredLaserPower());
        helper.runAfterDelay(2, () -> {
            ItemStack out = table.invOutput.getStackInSlot(0);
            helper.assertTrue(ItemRedstoneBoard.getBoardNBT(out) == board("miner"), "The table made " + out);
            helper.assertTrue(table.invInput.getStackInSlot(0).getCount() == 1, "The input wasn't used once");
            helper.assertTrue(table.getTarget() == 0, "The table works with its output full");
            // it keeps the choice: taking the output starts the next board
            table.invOutput.setStackInSlot(0, ItemStack.EMPTY);
            helper.assertTrue(table.getTarget() == 3200 * MjAPI.MJ, "The table forgot the miner");
            helper.succeed();
        });
    }

    /** The charging table fills a robot with laser power. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void chargingTableChargesRobots(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCRoboticsBlocks.CHARGING_TABLE.get());
        TileChargingTable table = helper.getBlockEntity(pos);
        helper.assertTrue(!table.inv.isItemValid(0, new ItemStack(BCRoboticsItems.ROBOT.get())),
            "The table took a blank robot");
        table.inv.setStackInSlot(0, ItemRobot.createRobotStack(board("picker"), 0));
        helper.assertTrue(table.getTarget() == EntityRobotBase.MAX_POWER, "The robot needs " + table.getTarget());
        table.receiveLaserPower(1000 * MjAPI.MJ);
        helper.runAfterDelay(2, () -> {
            long power = ItemRobot.getPower(table.inv.getStackInSlot(0));
            helper.assertTrue(power == 1000 * MjAPI.MJ, "The robot has " + power);
            helper.assertTrue(table.getTarget() == EntityRobotBase.MAX_POWER - power, "The table wants "
                + table.getTarget());
            helper.succeed();
        });
    }

    /** The integration table puts a board in a robot; a robot without power gets a fifth of a charge. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void integrationTableProgramsRobots(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCSiliconBlocks.INTEGRATION_TABLE.get());
        TileIntegrationTable table = helper.getBlockEntity(pos);
        table.invTarget.setStackInSlot(0, new ItemStack(BCRoboticsItems.ROBOT.get()));
        table.invToIntegrate.setStackInSlot(0, ItemRedstoneBoard.createStack(board("lumberjack")));
        helper.runAfterDelay(2, () -> {
            helper.assertTrue(table.getTarget() == 5000 * MjAPI.MJ, "The table needs " + table.getTarget());
            table.receiveLaserPower(table.getRequiredLaserPower());
            helper.runAfterDelay(2, () -> {
                ItemStack out = table.invResult.getStackInSlot(0);
                helper.assertTrue(ItemRobot.getRobotNBT(out) == board("lumberjack"), "The table made " + out);
                helper.assertTrue(ItemRobot.getPower(out) == EntityRobotBase.SAFETY_POWER, "The robot has "
                    + ItemRobot.getPower(out));
                helper.assertTrue(table.invTarget.getStackInSlot(0).isEmpty()
                    && table.invToIntegrate.getStackInSlot(0).isEmpty(), "The ingredients weren't used");
                // (8.0 wouldn't let pipes take the result out)
                var handler = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK,
                    helper.absolutePos(pos), Direction.DOWN);
                boolean extracted = false;
                for (int i = 0; handler != null && i < handler.getSlots(); i++) {
                    if (!handler.extractItem(i, 1, true).isEmpty()) {
                        extracted = ItemRobot.hasBoard(handler.extractItem(i, 1, true));
                    }
                }
                helper.assertTrue(extracted, "Pipes can't take the robot out");
                helper.succeed();
            });
        });
    }

    /** The requester asks for what its templates show, takes only that, and tells a comparator how full it is. */
    @GameTest(template = "empty")
    public static void requesterAsksForItsTemplates(GameTestHelper helper) {
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCRoboticsBlocks.REQUESTER.get());
        TileRequester requester = helper.getBlockEntity(pos);
        requester.requests.setStackInSlot(3, new ItemStack(Items.COBBLESTONE, 10));
        helper.assertTrue(requester.getRequest(0).isEmpty(), "An empty template asks for something");
        ItemStack wanted = requester.getRequest(3);
        helper.assertTrue(wanted.is(Items.COBBLESTONE) && wanted.getCount() == 10, "It asks for " + wanted);
        helper.assertTrue(!requester.inv.isItemValid(3, new ItemStack(Items.DIRT)), "It takes dirt for cobblestone");
        helper.assertTrue(requester.offerItem(3, new ItemStack(Items.DIRT, 4)).getCount() == 4, "It took dirt");
        ItemStack left = requester.offerItem(3, new ItemStack(Items.COBBLESTONE, 6));
        helper.assertTrue(left.isEmpty() && requester.getRequest(3).getCount() == 4, "After 6 it asks for "
            + requester.getRequest(3));
        left = requester.offerItem(3, new ItemStack(Items.COBBLESTONE, 6));
        helper.assertTrue(left.getCount() == 2 && requester.isFulfilled(3) && requester.getRequest(3).isEmpty(),
            "It took more than it asked for: " + left);
        int signal = helper.getBlockState(pos).getAnalogOutputSignal(helper.getLevel(), helper.absolutePos(pos));
        helper.assertTrue(signal == 1 + 14 * 10 / 64, "The comparator reads " + signal);
        helper.succeed();
    }

    /** 8.0's pulsar item placed nothing (its definition had no creator). */
    @GameTest(template = "factory")
    public static void pulsarsCanBePlaced(GameTestHelper helper) {
        // (only wooden pipes take redstone power)
        TilePipeHolder holder = BCTransportTests.placePipe(helper, new BlockPos(3, 1, 3), BCTransportPipes.woodItem,
            null);
        ItemStack stack = new ItemStack(BCSiliconItems.PLUG_PULSAR.get());
        FakePlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        var placed = BCSiliconItems.PLUG_PULSAR.get().onPlace(stack, holder, Direction.EAST, player,
            InteractionHand.MAIN_HAND);
        helper.assertTrue(placed instanceof PluggablePulsar, "The pulsar item made " + placed);
        helper.succeed();
    }

    // Robots

    /** A robot placed on a docking station is linked to it and registered; with nothing to do (a knight without a
     * sword, as no station offers one), it goes to sleep on its station. (Robots without a work area look a long way
     * for work, so a picker would find other tests' items.) */
    @GameTest(template = "builders", timeoutTicks = 400)
    public static void robotsLinkToTheirStationAndSleep(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(5, 1, 5), BCTransportPipes.stoneItem, "knight",
            EntityRobotBase.MAX_POWER);
        DockingStation station = robot.getLinkedStation();
        helper.assertTrue(station != null && station.robotTaking() == robot && station.isMainStation(),
            "The robot isn't linked to its station: " + station);
        helper.succeedWhen(() -> {
            helper.assertTrue(RobotManager.registryProvider.getRegistry(helper.getLevel()).getLoadedRobot(
                robot.getRobotId()) == robot, "The robot isn't registered");
            helper.assertTrue(robot.isAsleep(), "The robot is awake: " + robot.mainAI.getActiveAI());
            helper.assertTrue(robot.getDockingStation() == station, "The robot isn't docked");
        });
    }

    /** A robot saves its board, power and inventory. */
    @GameTest(template = "builders", timeoutTicks = 100)
    public static void robotsSaveThemselves(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(5, 1, 5), BCTransportPipes.stoneItem, "carrier",
            3000 * MjAPI.MJ);
        robot.setStackInSlot(2, new ItemStack(Items.DIAMOND, 5));
        CompoundTag nbt = new CompoundTag();
        robot.saveWithoutId(nbt);
        EntityRobot copy = BCRoboticsEntities.ROBOT.get().create(helper.getLevel());
        helper.assertTrue(copy != null, "No robot");
        copy.load(nbt);
        helper.assertTrue(copy.getBoard().getNBTHandler() == board("carrier"), "The board is "
            + copy.getBoard().getNBTHandler().getID());
        helper.assertTrue(copy.getBattery().getStored() == robot.getBattery().getStored(), "The power changed");
        helper.assertTrue(copy.getStackInSlot(2).is(Items.DIAMOND) && copy.getStackInSlot(2).getCount() == 5,
            "The inventory holds " + copy.getStackInSlot(2));
        helper.assertTrue(copy.getRobotId() == robot.getRobotId(), "The id changed");
        copy.discard();
        helper.succeed();
    }

    /** A wrench (while sneaking) turns the robot back into its item, with its board and power. */
    @GameTest(template = "builders", timeoutTicks = 100)
    public static void wrenchesDismantleRobots(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(5, 1, 5), BCTransportPipes.stoneItem, "miner",
            4000 * MjAPI.MJ);
        FakePlayer player = player(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(BCCoreItems.WRENCH.get()));
        player.setShiftKeyDown(true);
        robot.interact(player, InteractionHand.MAIN_HAND);
        player.setShiftKeyDown(false);
        helper.succeedWhen(() -> {
            helper.assertTrue(robot.isRemoved(), "The robot is still there");
            List<ItemEntity> drops = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                new AABB(robot.blockPosition()).inflate(3));
            ItemStack dropped = drops.stream().map(ItemEntity::getItem).filter(s -> s.getItem() instanceof ItemRobot)
                .findFirst().orElse(ItemStack.EMPTY);
            helper.assertTrue(ItemRobot.getRobotNBT(dropped) == board("miner"), "The robot dropped " + dropped);
            helper.assertTrue(ItemRobot.getPower(dropped) > 3900 * MjAPI.MJ, "The robot kept "
                + ItemRobot.getPower(dropped));
        });
    }

    /** A knight with a sword flies to a spider and kills it. */
    @GameTest(template = "builders", timeoutTicks = 800)
    public static void knightsKillMonsters(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(2, 1, 2), BCTransportPipes.stoneItem, "knight",
            EntityRobotBase.MAX_POWER);
        robot.setItemInUse(new ItemStack(Items.DIAMOND_SWORD));
        Spider spider = helper.spawn(EntityType.SPIDER, new BlockPos(8, 1, 8));
        spider.setNoAi(true);
        long start = robot.getBattery().getStored();
        helper.succeedWhen(() -> {
            helper.assertTrue(!spider.isAlive(), "The spider is alive (" + spider.getHealth() + "), the robot is "
                + robot.mainAI.getActiveAI() + " at " + robot.blockPosition());
            helper.assertTrue(robot.isAlive(), "The robot died");
            helper.assertTrue(robot.getBattery().getStored() < start, "The robot used no power");
        });
    }

    /** A miner with a pickaxe finds ore nearby and mines it (the drops fall, as in 7.1). */
    @GameTest(template = "builders", timeoutTicks = 1200)
    public static void minersMineOre(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(2, 1, 2), BCTransportPipes.stoneItem, "miner",
            EntityRobotBase.MAX_POWER);
        robot.setItemInUse(new ItemStack(Items.IRON_PICKAXE));
        BlockPos ore = new BlockPos(7, 2, 6);
        helper.setBlock(ore, Blocks.IRON_ORE);
        helper.succeedWhen(() -> {
            helper.assertBlockNotPresent(Blocks.IRON_ORE, ore);
            helper.assertItemEntityPresent(Items.RAW_IRON);
            helper.assertTrue(robot.getMainHandItem().getDamageValue() > 0, "The pickaxe wasn't used");
        });
    }

    /** A picker flies to an item and takes it. */
    @GameTest(template = "builders", timeoutTicks = 800)
    public static void pickersPickUpItems(GameTestHelper helper) {
        EntityRobot robot = placeRobot(helper, new BlockPos(2, 1, 2), BCTransportPipes.stoneItem, "picker",
            EntityRobotBase.MAX_POWER);
        helper.spawnItem(Items.EMERALD, 8, 1, 7);
        helper.succeedWhen(() -> {
            boolean held = false;
            for (int i = 0; i < robot.getSlots(); i++) {
                held |= robot.getStackInSlot(i).is(Items.EMERALD);
            }
            helper.assertTrue(held, "The robot hasn't got the emerald (" + robot.mainAI.getActiveAI() + ")");
            helper.assertItemEntityNotPresent(Items.EMERALD);
        });
    }

    /** A robot low on power goes to a station on a kinesis pipe and charges from it. */
    @GameTest(template = "builders", timeoutTicks = 600)
    public static void robotsChargeAtKinesisStations(GameTestHelper helper) {
        BlockPos pipe = new BlockPos(5, 1, 5);
        EntityRobot robot = placeRobot(helper, pipe, BCTransportPipes.woodPower, "knight",
            EntityRobotBase.SAFETY_POWER / 2);
        long start = robot.getBattery().getStored();
        BlockPos input = helper.absolutePos(pipe);
        helper.onEachTick(() -> {
            IMjReceiver receiver = helper.getLevel().getCapability(MjAPI.CAP_RECEIVER, input, Direction.WEST);
            if (receiver != null && receiver.canReceive()) {
                receiver.receivePower(40 * MjAPI.MJ, false);
            }
        });
        helper.succeedWhen(() -> {
            long now = robot.getBattery().getStored();
            helper.assertTrue(now > start + 500 * MjAPI.MJ, "The robot only has " + now / MjAPI.MJ + " MJ ("
                + robot.mainAI.getActiveAI() + ")");
        });
    }

    // Construction marks

    private static ItemStack twoCobblestoneBlueprint(GameTestHelper helper) {
        Blueprint blueprint = new Blueprint();
        blueprint.size = new BlockPos(2, 1, 1);
        blueprint.offset = BlockPos.ZERO;
        blueprint.facing = Direction.NORTH;
        blueprint.data = new int[] { 0, 0 };
        blueprint.palette.add(BCBuildersTests.schematic(helper, Blocks.COBBLESTONE.defaultBlockState()));
        blueprint.computeKey();
        GlobalSavedDataSnapshots.get(helper.getLevel()).addSnapshot(blueprint);
        return ItemSnapshot.getUsed(EnumSnapshotType.BLUEPRINT,
            new Snapshot.Header(blueprint.key, UUID.randomUUID(), new java.util.Date(), "two cobblestone"));
    }

    private static TileConstructionMarker placeMark(GameTestHelper helper, BlockPos pos) {
        helper.setBlock(pos.below(), Blocks.STONE);
        helper.setBlock(pos, BCBuildersBlocks.CONSTRUCTION_MARKER.get());
        return helper.getBlockEntity(pos);
    }

    /** A blueprint goes into a construction mark from a player's hand; the mark asks for what it needs, and builds
     * once it has the items and power (behind it: the mark faces north, as a builder would). Sneaking with an empty
     * hand takes the blueprint back. */
    @GameTest(template = "builders", timeoutTicks = 400)
    public static void constructionMarksBuildBlueprints(GameTestHelper helper) {
        BlockPos pos = new BlockPos(5, 1, 5);
        TileConstructionMarker mark = placeMark(helper, pos);
        FakePlayer player = player(helper);
        ItemStack blueprint = twoCobblestoneBlueprint(helper);
        player.setItemInHand(InteractionHand.MAIN_HAND, blueprint);
        helper.assertTrue(mark.onActivated(player, InteractionHand.MAIN_HAND, blueprint), "The mark took no blueprint");
        helper.assertTrue(mark.needsToBuild(), "The mark has nothing to build");
        helper.startSequence().thenWaitUntil(() -> {
            List<ItemStack> needed = mark.getNeededItems();
            helper.assertTrue(needed.size() == 1 && needed.get(0).is(Items.COBBLESTONE)
                && needed.get(0).getCount() == 2, "The mark needs " + needed);
        }).thenExecute(() -> {
            helper.assertTrue(mark.addResources(new ItemStack(Items.COBBLESTONE, 5)).isEmpty(), "Items didn't fit");
            mark.addPower(mark.getPowerWanted());
        }).thenWaitUntil(() -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(5, 1, 6));
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(6, 1, 6));
        }).thenWaitUntil(() -> helper.assertTrue(!mark.needsToBuild(), "The mark isn't done")).thenExecute(() -> {
            player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
            player.setShiftKeyDown(true);
            helper.assertTrue(mark.onActivated(player, InteractionHand.MAIN_HAND, ItemStack.EMPTY),
                "The blueprint didn't come out");
            player.setShiftKeyDown(false);
            helper.assertTrue(mark.invSnapshot.getStackInSlot(0).isEmpty(), "The mark kept the blueprint");
        }).thenSucceed();
    }

    /** A builder robot carrying cobblestone flies it to a construction mark, with power, and the mark builds. */
    @GameTest(template = "builders", timeoutTicks = 1200)
    public static void builderRobotsSupplyConstructionMarks(GameTestHelper helper) {
        BlockPos pos = new BlockPos(7, 1, 7);
        TileConstructionMarker mark = placeMark(helper, pos);
        mark.invSnapshot.setStackInSlot(0, twoCobblestoneBlueprint(helper));
        EntityRobot robot = placeRobot(helper, new BlockPos(2, 1, 2), BCTransportPipes.stoneItem, "builder",
            EntityRobotBase.MAX_POWER);
        robot.setStackInSlot(0, new ItemStack(Items.COBBLESTONE, 2));
        helper.succeedWhen(() -> {
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(7, 1, 8));
            helper.assertBlockPresent(Blocks.COBBLESTONE, new BlockPos(8, 1, 8));
            helper.assertTrue(robot.getBattery().getStored() < EntityRobotBase.MAX_POWER, "The robot gave no power");
        });
    }

    // Crops

    /** The stripes pipe plants through the crop API again: seeds on farmland in front of it, and a sapling on the
     * block in front when the one below is taken. */
    @GameTest(template = "empty")
    public static void stripesPipesPlantCrops(GameTestHelper helper) {
        helper.setBlock(new BlockPos(1, 0, 1), Blocks.FARMLAND);
        FakePlayer player = player(helper);
        ItemStack seeds = new ItemStack(Items.WHEAT_SEEDS, 3);
        helper.assertTrue(StripesHandlerPlant.INSTANCE.handle(helper.getLevel(), helper.absolutePos(new BlockPos(0,
            1, 1)), Direction.EAST, seeds, player, null), "The seeds weren't planted");
        helper.assertBlockPresent(Blocks.WHEAT, new BlockPos(1, 1, 1));
        helper.assertTrue(seeds.getCount() == 2, seeds.getCount() + " seeds are left");

        helper.setBlock(new BlockPos(1, 1, 0), Blocks.DIRT);
        ItemStack sapling = new ItemStack(Items.OAK_SAPLING);
        helper.assertTrue(StripesHandlerPlant.INSTANCE.handle(helper.getLevel(), helper.absolutePos(new BlockPos(0,
            1, 0)), Direction.EAST, sapling, player, null), "The sapling wasn't planted");
        helper.assertBlockPresent(Blocks.OAK_SAPLING, new BlockPos(1, 2, 0));
        helper.assertTrue(!StripesHandlerPlant.INSTANCE.handle(helper.getLevel(), helper.absolutePos(new BlockPos(0,
            1, 2)), Direction.EAST, new ItemStack(Items.STONE), player, null), "Stone was planted");
        helper.succeed();
    }

    // Robot goggles

    /** The goggles are a helmet with no armour that never wears out, robots can wear them too, and they are made from
     * leather, glass panes and a redstone chipset. */
    @GameTest(template = "empty")
    public static void robotGogglesAreHeadwear(GameTestHelper helper) {
        ItemStack goggles = new ItemStack(BCRoboticsItems.ROBOT_GOGGLES.get());
        helper.assertTrue(goggles.getItem() instanceof ArmorItem armor
            && armor.getEquipmentSlot() == EquipmentSlot.HEAD, "The goggles aren't a helmet");
        helper.assertFalse(goggles.isDamageableItem(), "The goggles wear out");
        helper.assertTrue(goggles.getMaxStackSize() == 1, "The goggles stack");
        helper.assertTrue(goggles.getAttributeModifiers().modifiers().isEmpty(),
            "The goggles have " + goggles.getAttributeModifiers().modifiers());
        helper.assertTrue(EntityRobot.isWearable(goggles), "Robots can't wear the goggles");
        ArmorStand stand = helper.spawn(EntityType.ARMOR_STAND, new BlockPos(1, 1, 1));
        helper.assertFalse(ItemRobotGoggles.isWornBy(stand), "A bare armour stand wears goggles");
        stand.setItemSlot(EquipmentSlot.HEAD, goggles.copy());
        helper.assertTrue(ItemRobotGoggles.isWornBy(stand), "The armour stand's goggles don't count");

        ItemStack leather = new ItemStack(Items.LEATHER);
        ItemStack pane = new ItemStack(Items.GLASS_PANE);
        ItemStack chipset = new ItemStack(BCSiliconItems.CHIPSETS.get(EnumRedstoneChipset.RED).get());
        CraftingInput input = CraftingInput.of(3, 2, List.of(leather, chipset, leather, pane, ItemStack.EMPTY, pane));
        Optional<RecipeHolder<CraftingRecipe>> recipe = helper.getLevel().getRecipeManager()
            .getRecipeFor(RecipeType.CRAFTING, input, helper.getLevel());
        helper.assertTrue(recipe.isPresent() && recipe.get().id().equals(BuildCraftReborn.id("robot_goggles")),
            "Leather, glass panes and a chipset make " + recipe.map(RecipeHolder::id));
        helper.assertTrue(recipe.get().value().assemble(input, helper.getLevel().registryAccess())
            .is(BCRoboticsItems.ROBOT_GOGGLES.get()), "The goggles recipe makes something else");
        helper.succeed();
    }

    /** Robot goggles show robots red with an empty battery, yellow half full and green full. Only the client sees
     * them glow: on the server a robot glows only with the glowing effect, as other mobs do. */
    @GameTest(template = "empty")
    public static void robotGogglesShowBatteries(GameTestHelper helper) {
        int[][] colours = { { 0, 0xFF0000 }, { 25, 0xFF8000 }, { 50, 0xFFFF00 }, { 75, 0x80FF00 }, { 100, 0x00FF00 },
            { 150, 0x00FF00 } };
        for (int[] c : colours) {
            int got = EntityRobot.getBatteryColour(c[0] / 100D);
            helper.assertTrue(got == c[1], c[0] + "% charged is " + Integer.toHexString(got));
        }
        EntityRobot robot = helper.spawn(BCRoboticsEntities.ROBOT.get(), new BlockPos(2, 2, 2));
        helper.assertFalse(robot.isCurrentlyGlowing(), "The robot glows on the server");
        helper.assertTrue(robot.getTeamColor() == 0xFFFFFF, "The robot's outline is " + robot.getTeamColor());
        robot.discard();
        helper.succeed();
    }
}
