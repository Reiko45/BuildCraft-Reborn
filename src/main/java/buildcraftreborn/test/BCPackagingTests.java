/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.io.IOException;
import java.util.List;
import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.DispenserBlock;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.entity.DispenserBlockEntity;

import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.lib.gui.ContainerBC_Neptune;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.silicon.BCSiliconPackaging;
import buildcraftreborn.silicon.container.ContainerPackager;
import buildcraftreborn.silicon.entity.EntityPackage;
import buildcraftreborn.silicon.item.ItemPackage;
import buildcraftreborn.silicon.tile.TilePackager;
import buildcraftreborn.silicon.tile.TileStampingTable;

/** Phase 8 tests: BuildCraft 7.1's recipe packager, stamping table and packages. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCPackagingTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private BCPackagingTests() {}

    private static TilePackager packager(GameTestHelper helper) {
        helper.setBlock(POS, BCSiliconPackaging.PACKAGER.get());
        return helper.getBlockEntity(POS);
    }

    /** Sets a pattern slot: to the item, or to "anything" if it's empty. */
    private static void setPattern(TilePackager packager, int slot, ItemStack filter) {
        packager.invPattern.setStackInSlot(slot, filter);
        packager.setPatternSlot(slot, true);
    }

    private static ItemStack makePackage(ItemStack... grid) {
        ItemStack pkg = new ItemStack(BCSiliconPackaging.PACKAGE.get());
        NonNullList<ItemStack> contents = NonNullList.withSize(ItemPackage.SLOTS, ItemStack.EMPTY);
        for (int i = 0; i < grid.length; i++) {
            contents.set(i, grid[i]);
        }
        ItemPackage.setContents(pkg, contents);
        return pkg;
    }

    /** The 2x2 crafting table recipe, packed from the packager's own storage. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void packagerPacksItsPattern(GameTestHelper helper) {
        TilePackager packager = packager(helper);
        for (int slot : new int[] { 0, 1, 3, 4 }) {
            setPattern(packager, slot, new ItemStack(Items.OAK_PLANKS));
        }
        packager.invStorage.setStackInSlot(0, new ItemStack(Items.OAK_PLANKS, 5));
        packager.invPaper.setStackInSlot(0, new ItemStack(Items.PAPER, 2));
        helper.succeedWhen(() -> {
            ItemStack pkg = packager.invOutput.getStackInSlot(0);
            helper.assertTrue(pkg.getItem() instanceof ItemPackage, "No package yet: " + pkg);
            NonNullList<ItemStack> contents = ItemPackage.getContents(pkg);
            for (int i = 0; i < ItemPackage.SLOTS; i++) {
                boolean wanted = i == 0 || i == 1 || i == 3 || i == 4;
                helper.assertTrue(wanted ? contents.get(i).is(Items.OAK_PLANKS) && contents.get(i).getCount() == 1
                    : contents.get(i).isEmpty(), "Slot " + i + " holds " + contents.get(i));
            }
            helper.assertTrue(packager.invStorage.getStackInSlot(0).getCount() == 1, "It took "
                + (5 - packager.invStorage.getStackInSlot(0).getCount()) + " planks");
            helper.assertTrue(packager.invPaper.getStackInSlot(0).getCount() == 1, "It used the wrong amount of paper");
        });
    }

    /** "Anything" slots all take the same item, one that no slot names, and the packager takes from a chest beside
     * it. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void packagerFillsAnythingSlotsFromNeighbours(GameTestHelper helper) {
        TilePackager packager = packager(helper);
        BlockPos chestPos = POS.north();
        helper.setBlock(chestPos, Blocks.CHEST);
        ChestBlockEntity chest = helper.getBlockEntity(chestPos);
        chest.setItem(0, new ItemStack(Items.COBBLESTONE, 3));
        chest.setItem(1, new ItemStack(Items.DIRT, 1));
        chest.setItem(2, new ItemStack(Items.DIRT, 4));
        setPattern(packager, 0, new ItemStack(Items.COBBLESTONE));
        setPattern(packager, 1, ItemStack.EMPTY);
        setPattern(packager, 2, ItemStack.EMPTY);
        packager.invPaper.setStackInSlot(0, new ItemStack(Items.PAPER));
        helper.succeedWhen(() -> {
            NonNullList<ItemStack> contents = ItemPackage.getContents(packager.invOutput.getStackInSlot(0));
            helper.assertTrue(contents.get(0).is(Items.COBBLESTONE), "Slot 0 holds " + contents.get(0));
            // Not cobblestone (slot 0 names it), and not the single dirt (two slots need the same stack)
            helper.assertTrue(contents.get(1).is(Items.DIRT) && contents.get(2).is(Items.DIRT),
                "The anything slots hold " + contents.get(1) + " and " + contents.get(2));
            helper.assertTrue(chest.getItem(1).getCount() == 1 && chest.getItem(2).getCount() == 2,
                "It took dirt from the wrong stack");
            helper.assertTrue(chest.getItem(0).getCount() == 2, "It took " + (3 - chest.getItem(0).getCount())
                + " cobblestone");
        });
    }

    /** A package goes round again to have its empty slots filled, but not slots it already fills. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void packagerFillsPackagesOnlyWhereTheyAreEmpty(GameTestHelper helper) {
        TilePackager packager = packager(helper);
        setPattern(packager, 0, new ItemStack(Items.STICK));
        packager.invStorage.setStackInSlot(0, new ItemStack(Items.STICK, 4));
        packager.invPaper.setStackInSlot(0, makePackage(new ItemStack(Items.COAL)));
        helper.runAfterDelay(12, () -> {
            helper.assertTrue(packager.invOutput.getStackInSlot(0).isEmpty(), "It packed over the coal");
            packager.setPatternSlot(0, false);
            setPattern(packager, 3, new ItemStack(Items.STICK));
            helper.succeedWhen(() -> {
                NonNullList<ItemStack> contents = ItemPackage.getContents(packager.invOutput.getStackInSlot(0));
                helper.assertTrue(contents.get(0).is(Items.COAL) && contents.get(3).is(Items.STICK),
                    "The package holds " + contents);
                helper.assertTrue(packager.invPaper.getStackInSlot(0).isEmpty(), "The old package is still in");
            });
        });
    }

    /** Pipes put ingredients in from the sides (only what the pattern can use), paper from the top and bottom, and
     * take packages out of any side. */
    @GameTest(template = "empty")
    public static void packagerSidesForPipes(GameTestHelper helper) {
        TilePackager packager = packager(helper);
        setPattern(packager, 0, new ItemStack(Items.STICK));
        BlockPos abs = helper.absolutePos(POS);
        IItemHandler side = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, abs, Direction.EAST);
        IItemHandler top = helper.getLevel().getCapability(Capabilities.ItemHandler.BLOCK, abs, Direction.UP);
        helper.assertTrue(side != null && top != null, "No item handlers");
        helper.assertTrue(insert(side, new ItemStack(Items.DIRT)) == 1, "The side took dirt");
        helper.assertTrue(insert(side, new ItemStack(Items.STICK, 3)) == 0, "The side didn't take sticks");
        helper.assertTrue(insert(side, new ItemStack(Items.PAPER)) == 1, "The side took paper");
        helper.assertTrue(insert(top, new ItemStack(Items.PAPER)) == 0, "The top didn't take paper");
        helper.assertTrue(insert(top, new ItemStack(Items.STICK)) == 1, "The top took a stick");
        packager.invOutput.setStackInSlot(0, makePackage(new ItemStack(Items.STICK)));
        boolean taken = false;
        for (int i = 0; i < side.getSlots(); i++) {
            if (side.extractItem(i, 1, true).getItem() instanceof ItemPackage) {
                taken = true;
            }
        }
        helper.assertTrue(taken, "The package can't be taken out of the side");
        helper.succeed();
    }

    private static int insert(IItemHandler handler, ItemStack stack) {
        for (int i = 0; i < handler.getSlots() && !stack.isEmpty(); i++) {
            stack = handler.insertItem(i, stack, false);
        }
        return stack.getCount();
    }

    /** The pattern slots in the GUI: an item sets the slot to it, an empty hand clears it, and an empty hand on an
     * empty slot switches "anything" on and off. */
    @GameTest(template = "empty")
    public static void packagerPatternClicks(GameTestHelper helper) {
        TilePackager packager = packager(helper);
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
            new GameProfile(UUID.nameUUIDFromBytes("bc_packager".getBytes()), "bc_packager"));
        ContainerPackager menu = new ContainerPackager(1, player, packager);
        int slotId = -1;
        for (Slot slot : menu.slots) {
            if (slot instanceof ContainerPackager.SlotPattern pattern && pattern.handlerIndex == 4) {
                slotId = slot.index;
            }
        }
        helper.assertTrue(slotId >= 0, "No pattern slot 4");
        menu.setCarried(new ItemStack(Items.STONE, 12));
        menu.clicked(slotId, 0, ClickType.PICKUP, player);
        helper.assertTrue(packager.isPatternSlotSet(4) && packager.invPattern.getStackInSlot(4).is(Items.STONE)
            && packager.invPattern.getStackInSlot(4).getCount() == 1, "Clicking with stone didn't set it");
        helper.assertTrue(menu.getCarried().getCount() == 12, "The click took the stone");
        menu.setCarried(ItemStack.EMPTY);
        menu.clicked(slotId, 0, ClickType.PICKUP, player);
        helper.assertTrue(!packager.isPatternSlotSet(4) && packager.invPattern.getStackInSlot(4).isEmpty(),
            "An empty hand didn't clear it");
        menu.clicked(slotId, 0, ClickType.PICKUP, player);
        helper.assertTrue(packager.isPatternSlotSet(4) && packager.invPattern.getStackInSlot(4).isEmpty(),
            "An empty hand didn't set it to anything");
        menu.clicked(slotId, 0, ClickType.PICKUP, player);
        helper.assertTrue(!packager.isPatternSlotSet(4), "An empty hand didn't switch anything off");
        helper.succeed();
    }

    /** What JEI's and EMI's recipe transfer and item dragging send: the pattern slots they fill are set to those
     * items, and the ones a recipe leaves empty are switched off. */
    @GameTest(template = "empty")
    public static void packagerPatternFromRecipeViewers(GameTestHelper helper) throws IOException {
        TilePackager packager = packager(helper);
        packager.setPatternSlot(8, true);
        FakePlayer player = FakePlayerFactory.get(helper.getLevel(),
            new GameProfile(UUID.nameUUIDFromBytes("bc_packager".getBytes()), "bc_packager"));
        ContainerPackager menu = new ContainerPackager(1, player, packager);
        int[] slotIds = new int[9];
        for (Slot slot : menu.slots) {
            if (slot instanceof ContainerPackager.SlotPattern pattern) {
                slotIds[pattern.handlerIndex] = slot.index;
            }
        }

        // A crafting table's recipe: planks in the top left 2x2
        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), helper.getLevel().registryAccess());
        buffer.writeByte(9);
        for (int i = 0; i < 9; i++) {
            buffer.writeVarInt(slotIds[i]);
            boolean planks = i == 0 || i == 1 || i == 3 || i == 4;
            ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, planks ? new ItemStack(Items.OAK_PLANKS) : ItemStack.EMPTY);
        }
        menu.readMessage(ContainerBC_Neptune.NET_SET_PHANTOM_MULTI, buffer, LogicalSide.SERVER, null);
        buffer.release();
        for (int i = 0; i < 9; i++) {
            boolean planks = i == 0 || i == 1 || i == 3 || i == 4;
            helper.assertTrue(packager.isPatternSlotSet(i) == planks, "Pattern slot " + i + " is "
                + (planks ? "off" : "on") + " after a recipe transfer");
            helper.assertTrue(packager.invPattern.getStackInSlot(i).is(Items.OAK_PLANKS) == planks,
                "Pattern slot " + i + " has " + packager.invPattern.getStackInSlot(i));
        }

        // Dragging an item onto one slot
        buffer = new PacketBufferBC(Unpooled.buffer(), helper.getLevel().registryAccess());
        buffer.writeVarInt(slotIds[7]);
        ItemStack.OPTIONAL_STREAM_CODEC.encode(buffer, new ItemStack(Items.STONE));
        menu.readMessage(ContainerBC_Neptune.NET_SET_PHANTOM, buffer, LogicalSide.SERVER, null);
        buffer.release();
        helper.assertTrue(packager.isPatternSlotSet(7) && packager.invPattern.getStackInSlot(7).is(Items.STONE),
            "Dragging stone onto slot 7 didn't set it");
        helper.succeed();
    }

    private static TileStampingTable stampingTable(GameTestHelper helper) {
        helper.setBlock(POS, BCSiliconPackaging.STAMPING_TABLE.get());
        return helper.getBlockEntity(POS);
    }

    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stampingTableCraftsPackages(GameTestHelper helper) {
        TileStampingTable table = stampingTable(helper);
        ItemStack planks = new ItemStack(Items.OAK_PLANKS);
        table.invInput.setStackInSlot(0, makePackage(planks.copy(), planks.copy(), ItemStack.EMPTY, planks.copy(),
            planks.copy()));
        helper.assertTrue(table.getTarget() == 4 * TileStampingTable.POWER_PER_ITEM, "The table wants "
            + table.getTarget());
        table.receiveLaserPower(table.getRequiredLaserPower());
        helper.succeedWhen(() -> {
            helper.assertTrue(table.invOutput.getStackInSlot(0).is(Items.CRAFTING_TABLE), "The table made "
                + table.invOutput.getStackInSlot(0));
            helper.assertTrue(table.invInput.getStackInSlot(0).isEmpty(), "The package is still there");
            helper.assertTrue(table.getTarget() == 0, "The table still wants power");
        });
    }

    /** A plain item is crafted on its own; what can't be crafted passes through. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stampingTableCraftsSingleItems(GameTestHelper helper) {
        TileStampingTable table = stampingTable(helper);
        table.invInput.setStackInSlot(0, new ItemStack(Items.OAK_LOG, 2));
        helper.assertTrue(table.getTarget() == TileStampingTable.POWER_PER_ITEM, "A log needs " + table.getTarget());
        table.receiveLaserPower(table.getRequiredLaserPower());
        helper.runAfterDelay(3, () -> {
            ItemStack out = table.invOutput.getStackInSlot(0);
            helper.assertTrue(out.is(Items.OAK_PLANKS) && out.getCount() == 4, "A log made " + out);
            helper.assertTrue(table.invInput.getStackInSlot(0).getCount() == 1, "It used the wrong number of logs");
            table.invOutput.setStackInSlot(0, ItemStack.EMPTY);
            table.invInput.setStackInSlot(0, new ItemStack(Items.DIRT));
            table.receiveLaserPower(table.getRequiredLaserPower());
            helper.succeedWhen(() -> {
                helper.assertTrue(table.invOutput.getStackInSlot(0).is(Items.DIRT), "Dirt didn't pass through");
                helper.assertTrue(table.invInput.getStackInSlot(0).isEmpty(), "The dirt is still in the input");
            });
        });
    }

    /** A cake leaves three empty buckets, which go in the leftover slots. */
    @GameTest(template = "empty", timeoutTicks = 40)
    public static void stampingTableKeepsLeftovers(GameTestHelper helper) {
        TileStampingTable table = stampingTable(helper);
        ItemStack milk = new ItemStack(Items.MILK_BUCKET);
        ItemStack sugar = new ItemStack(Items.SUGAR);
        ItemStack wheat = new ItemStack(Items.WHEAT);
        table.invInput.setStackInSlot(0, makePackage(milk.copy(), milk.copy(), milk.copy(), sugar.copy(),
            new ItemStack(Items.EGG), sugar.copy(), wheat.copy(), wheat.copy(), wheat.copy()));
        table.receiveLaserPower(table.getRequiredLaserPower());
        helper.succeedWhen(() -> {
            helper.assertTrue(table.invOutput.getStackInSlot(0).is(Items.CAKE), "The table made "
                + table.invOutput.getStackInSlot(0));
            ItemStack buckets = table.invOutput.getStackInSlot(1);
            helper.assertTrue(buckets.is(Items.BUCKET) && buckets.getCount() == 3, "The leftovers are " + buckets);
        });
    }

    /** A thrown package bursts where it lands. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void packagesBurstWhereTheyLand(GameTestHelper helper) {
        BlockPos above = helper.absolutePos(new BlockPos(1, 2, 1));
        EntityPackage thrown = new EntityPackage(helper.getLevel(), above.getX() + 0.5, above.getY() + 0.5,
            above.getZ() + 0.5, makePackage(new ItemStack(Items.DIAMOND), new ItemStack(Items.EMERALD, 2)));
        helper.getLevel().addFreshEntity(thrown);
        helper.succeedWhen(() -> {
            helper.assertTrue(thrown.isRemoved(), "The package hasn't landed");
            List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                helper.getBounds().inflate(2));
            helper.assertTrue(items.stream().anyMatch(e -> e.getItem().is(Items.DIAMOND))
                && items.stream().anyMatch(e -> e.getItem().is(Items.EMERALD) && e.getItem().getCount() == 2),
                "It dropped " + items.stream().map(ItemEntity::getItem).toList());
        });
    }

    /** Dispensers (and so stripes pipes) throw packages. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void dispensersThrowPackages(GameTestHelper helper) {
        BlockPos dispenserPos = new BlockPos(1, 0, 1);
        helper.setBlock(dispenserPos, Blocks.DISPENSER.defaultBlockState().setValue(DispenserBlock.FACING,
            Direction.UP));
        DispenserBlockEntity dispenser = helper.getBlockEntity(dispenserPos);
        dispenser.setItem(0, makePackage(new ItemStack(Items.GOLD_INGOT)));
        helper.setBlock(new BlockPos(0, 0, 1), Blocks.REDSTONE_BLOCK);
        helper.succeedWhen(() -> {
            helper.assertTrue(dispenser.getItem(0).isEmpty(), "The dispenser still has the package");
            List<ItemEntity> items = helper.getLevel().getEntitiesOfClass(ItemEntity.class,
                helper.getBounds().inflate(4));
            helper.assertTrue(items.stream().anyMatch(e -> e.getItem().is(Items.GOLD_INGOT)),
                "The gold isn't out: " + items.stream().map(ItemEntity::getItem).toList());
        });
    }
}
