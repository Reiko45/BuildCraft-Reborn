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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.items.IMapLocation.MapLocationType;
import buildcraftreborn.builders.BCBuildersBlocks;
import buildcraftreborn.builders.tile.TileQuarry;
import buildcraftreborn.core.BCCoreItems;
import buildcraftreborn.core.item.ItemMapLocation;
import buildcraftreborn.core.list.ContainerList;
import buildcraftreborn.lib.debug.BCAdvDebugging;
import buildcraftreborn.lib.item.ItemDebugger;
import buildcraftreborn.lib.list.ListHandler;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.robotics.zone.ZonePlan;

/** Phase 6 tests for the core items that 1.12 had and earlier phases left out: the list, the map location and the
 * debugger, and the zone plans that map locations hold. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCCoreItemTests {
    private BCCoreItemTests() {}

    private static FakePlayer player(GameTestHelper helper, String name) {
        return FakePlayerFactory.get(helper.getLevel(), new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name));
    }

    private static ItemStack list(ListHandler.Line... lines) {
        ItemStack list = new ItemStack(BCCoreItems.LIST.get());
        ListHandler.Line[] all = ListHandler.getLines(list);
        System.arraycopy(lines, 0, all, 0, lines.length);
        ListHandler.saveLines(list, all);
        return list;
    }

    private static ListHandler.Line line(boolean precise, boolean byType, boolean byMaterial, ItemStack... stacks) {
        ListHandler.Line line = new ListHandler.Line();
        line.precise = precise;
        line.byType = byType;
        line.byMaterial = byMaterial;
        for (int i = 0; i < stacks.length; i++) {
            line.setStack(i, stacks[i]);
        }
        return line;
    }

    /** A line of items matches those items; a "same material" line matches the material's tags (iron ingots, nuggets
     * and blocks), a "same type" line the kind of item (any ingot, any pickaxe). */
    @GameTest(template = "empty")
    public static void listMatchesItsLines(GameTestHelper helper) {
        ItemStack plain = list(line(false, false, false, new ItemStack(Items.STONE), new ItemStack(Items.DIRT)));
        helper.assertTrue(ListHandler.matches(plain, new ItemStack(Items.DIRT)), "Plain line doesn't match dirt");
        helper.assertFalse(ListHandler.matches(plain, new ItemStack(Items.COBBLESTONE)), "Plain line matches cobble");

        ItemStack material = list(line(false, false, true, new ItemStack(Items.IRON_INGOT)));
        helper.assertTrue(ListHandler.matches(material, new ItemStack(Items.IRON_NUGGET)), "Iron doesn't match nugget");
        helper.assertTrue(ListHandler.matches(material, new ItemStack(Items.IRON_BLOCK)), "Iron doesn't match block");
        helper.assertFalse(ListHandler.matches(material, new ItemStack(Items.GOLD_INGOT)), "Iron matches gold");

        ItemStack type = list(line(false, true, false, new ItemStack(Items.IRON_INGOT)));
        helper.assertTrue(ListHandler.matches(type, new ItemStack(Items.GOLD_INGOT)), "Ingot doesn't match ingot");
        helper.assertFalse(ListHandler.matches(type, new ItemStack(Items.IRON_NUGGET)), "Ingot matches nugget");

        ItemStack tools = list(line(false, true, false, new ItemStack(Items.DIAMOND_PICKAXE)));
        helper.assertTrue(ListHandler.matches(tools, new ItemStack(Items.WOODEN_PICKAXE)), "Pickaxes don't match");
        helper.assertFalse(ListHandler.matches(tools, new ItemStack(Items.WOODEN_AXE)), "Pickaxe matches axe");

        ItemStack food = list(line(false, true, false, new ItemStack(Items.BREAD)));
        helper.assertTrue(ListHandler.matches(food, new ItemStack(Items.APPLE)), "Food doesn't match food");

        ItemStack armour = list(line(false, true, false, new ItemStack(Items.IRON_HELMET)));
        helper.assertTrue(ListHandler.matches(armour, new ItemStack(Items.LEATHER_HELMET)), "Helmets don't match");
        helper.assertFalse(ListHandler.matches(armour, new ItemStack(Items.IRON_BOOTS)), "Helmet matches boots");

        ItemStack fluid = list(line(false, false, true, new ItemStack(Items.WATER_BUCKET)));
        helper.assertFalse(ListHandler.matches(fluid, new ItemStack(Items.LAVA_BUCKET)), "Water matches lava");

        // Filters take lists, either way round
        helper.assertTrue(StackUtil.isMatchingItemOrList(material, new ItemStack(Items.IRON_BLOCK)),
            "Filter doesn't use the list");
        helper.assertTrue(StackUtil.matchesStackOrList(material, new ItemStack(Items.RAW_IRON)),
            "Filter doesn't use the list for raw iron");
        helper.assertTrue(ListHandler.hasItems(plain) && !ListHandler.hasItems(new ItemStack(BCCoreItems.LIST.get())),
            "hasItems is wrong");
        helper.succeed();
    }

    /** The list's menu edits the held list: phantom slots copy what is clicked onto them, a "same type" button clears
     * the rest of the line, and the label is stored. The list data survives a save. */
    @GameTest(template = "empty")
    public static void listMenuEditsTheHeldList(GameTestHelper helper) {
        FakePlayer player = player(helper, "bc_list");
        ItemStack list = new ItemStack(BCCoreItems.LIST.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, list);
        ContainerList menu = new ContainerList(0, player);
        menu.setCarried(new ItemStack(Items.COAL, 5));
        menu.clicked(0, 0, ClickType.PICKUP, player);
        menu.clicked(1, 0, ClickType.PICKUP, player);
        helper.assertTrue(menu.getCarried().getCount() == 5, "Phantom slots took the carried items");
        helper.assertTrue(ListHandler.matches(player.getMainHandItem(), new ItemStack(Items.COAL)),
            "List doesn't match coal after clicking it in");

        menu.switchButton(0, 1);
        helper.assertTrue(menu.lines[0].byType && menu.slots.get(1).getItem().isEmpty(),
            "Same type mode kept the second slot");
        menu.setLabel("Fuel");
        helper.assertTrue("Fuel".equals(BCCoreItems.LIST.get().getLabel(player.getMainHandItem())), "Label not set");

        CompoundTag saved = (CompoundTag) player.getMainHandItem().save(helper.getLevel().registryAccess());
        ItemStack loaded = ItemStack.parseOptional(helper.getLevel().registryAccess(), saved);
        helper.assertTrue(ListHandler.getLines(loaded)[0].byType
            && "Fuel".equals(BCCoreItems.LIST.get().getLabel(loaded)), "List data didn't survive a save");
        helper.succeed();
    }

    /** A map location used on a block remembers that spot (one from a stack of several comes back used); sneak-using
     * it in the air cleans it. */
    @GameTest(template = "empty")
    public static void mapLocationRemembersASpot(GameTestHelper helper) {
        FakePlayer player = player(helper, "bc_map");
        player.getInventory().clearContent();
        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, Blocks.STONE);
        BlockPos abs = helper.absolutePos(pos);
        ItemStack stack = new ItemStack(BCCoreItems.MAP_LOCATION.get(), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);
        UseOnContext ctx = new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(abs), Direction.NORTH, abs, false));
        stack.onItemUseFirst(ctx);
        helper.assertTrue(stack.getCount() == 2 && MapLocationType.getFromStack(stack) == MapLocationType.CLEAN,
            "The stack held " + stack);
        ItemStack used = ItemStack.EMPTY;
        for (ItemStack s : player.getInventory().items) {
            if (MapLocationType.getFromStack(s) == MapLocationType.SPOT) {
                used = s;
            }
        }
        helper.assertTrue(!used.isEmpty(), "No used map location came back");
        ItemMapLocation item = BCCoreItems.MAP_LOCATION.get();
        helper.assertTrue(abs.equals(item.getPoint(used)) && item.getPointSide(used) == Direction.NORTH,
            "Remembered " + item.getPoint(used) + " " + item.getPointSide(used));
        helper.assertTrue(used.getMaxStackSize() == 1, "A used map location stacks to " + used.getMaxStackSize());

        player.setShiftKeyDown(true);
        player.setItemInHand(InteractionHand.MAIN_HAND, used);
        used.use(helper.getLevel(), player, InteractionHand.MAIN_HAND);
        helper.assertTrue(MapLocationType.getFromStack(used) == MapLocationType.CLEAN && used.getMaxStackSize() == 16
            && ItemStack.isSameItemSameComponents(used, new ItemStack(BCCoreItems.MAP_LOCATION.get())),
            "Sneak use left " + used + " stacking to " + used.getMaxStackSize());
        player.setShiftKeyDown(false);
        helper.succeed();
    }

    /** Zone plans keep their columns through NBT and a map location, and measure distance to their nearest chunk. */
    @GameTest(template = "empty")
    public static void zonePlansRoundTrip(GameTestHelper helper) {
        ZonePlan plan = new ZonePlan();
        plan.set(5, 5, true);
        plan.set(40, -3, true);
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                plan.set(64 + x, 64 + z, true);
            }
        }
        plan.set(70, 70, false);
        helper.assertTrue(plan.get(40, -3) && !plan.get(41, -3) && plan.get(64, 79) && !plan.get(70, 70)
            && plan.get(79, 79), "Plan contents are wrong");
        helper.assertTrue(plan.getAll().size() == 2 + 255, "Plan has " + plan.getAll().size() + " columns");
        ZonePlan copy = new ZonePlan().readFromNBT(plan.toNBT());
        helper.assertTrue(copy.equals(plan), "Plan changed through NBT");
        helper.assertTrue(Math.abs(plan.distanceTo(new BlockPos(8, 70, 8))) < 0.001,
            "Distance to its own chunk centre is " + plan.distanceTo(new BlockPos(8, 70, 8)));

        ItemStack map = new ItemStack(BCCoreItems.MAP_LOCATION.get());
        ItemMapLocation.setZone(map, plan);
        helper.assertTrue(MapLocationType.getFromStack(map) == MapLocationType.ZONE
            && plan.equals(BCCoreItems.MAP_LOCATION.get().getZone(map)), "Zone didn't survive the map location");
        helper.succeed();
    }

    /** The debugger shows debug info in creative or while held, and using it on a tile makes that the advanced debug
     * target. */
    @GameTest(template = "empty")
    public static void debuggerPicksTheDebugTarget(GameTestHelper helper) {
        FakePlayer player = player(helper, "bc_debugger");
        player.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        helper.assertFalse(ItemDebugger.isShowDebugInfo(player), "Debug info shown without a debugger");
        ItemStack debugger = new ItemStack(BCCoreItems.DEBUGGER.get());
        player.setItemInHand(InteractionHand.MAIN_HAND, debugger);
        helper.assertTrue(ItemDebugger.isShowDebugInfo(player), "Debug info not shown with a debugger");

        BlockPos pos = new BlockPos(1, 1, 1);
        helper.setBlock(pos, BCBuildersBlocks.QUARRY.get());
        BlockPos abs = helper.absolutePos(pos);
        TileQuarry quarry = helper.getBlockEntity(pos);
        debugger.onItemUseFirst(new UseOnContext(player, InteractionHand.MAIN_HAND,
            new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false)));
        helper.assertTrue(BCAdvDebugging.isBeingDebugged(quarry), "The quarry isn't the debug target");
        BCAdvDebugging.setCurrentDebugTarget(null);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void listRecipeExists(GameTestHelper helper) {
        helper.assertTrue(helper.getLevel().getRecipeManager().byKey(BuildCraftReborn.id("list")).isPresent(),
            "No list recipe");
        helper.assertTrue(List.of(BCCoreItems.LIST.get(), BCCoreItems.MAP_LOCATION.get(), BCCoreItems.DEBUGGER.get())
            .stream().allMatch(item -> new ItemStack(item).getMaxStackSize() >= 1), "Items are broken");
        helper.succeed();
    }
}
