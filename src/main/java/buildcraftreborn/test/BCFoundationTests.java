/*
 * BuildCraft Reborn, an unofficial port of BuildCraft to Minecraft 1.21.1.
 * BuildCraft is (c) SpaceToad and the BuildCraft Team.
 *
 * This Source Code Form is subject to the terms of the Mozilla Public License, v. 2.0. If a copy of the MPL was not
 * distributed with this file, You can obtain one at https://mozilla.org/MPL/2.0/.
 */
package buildcraftreborn.test;

import java.util.UUID;

import com.mojang.authlib.GameProfile;

import io.netty.buffer.Unpooled;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.common.util.FakePlayerFactory;
import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.capability.IFluidHandler;
import net.neoforged.neoforge.fluids.capability.IFluidHandler.FluidAction;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import net.neoforged.neoforge.items.IItemHandler;

import buildcraftreborn.BuildCraftReborn;
import buildcraftreborn.api.mj.IMjReadable;
import buildcraftreborn.api.mj.IMjReceiver;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.net.TileUpdatePayload;
import buildcraftreborn.lib.tile.TileBC_Neptune;

/** Phase 1 exit tests, run on a headless dedicated server by {@code gradlew runGameTestServer}. Together they cover
 * the capability bridge, saving, the tile netcode, the menu and widget path, and drops. */
@GameTestHolder(BuildCraftReborn.MODID)
@PrefixGameTestTemplate(false)
public final class BCFoundationTests {
    private static final BlockPos POS = new BlockPos(1, 1, 1);

    private BCFoundationTests() {}

    private static TileTestMachine place(GameTestHelper helper) {
        helper.setBlock(POS, BCTestContent.TEST_MACHINE_BLOCK.get());
        return helper.getBlockEntity(POS);
    }

    @GameTest(template = "empty")
    public static void capabilitiesReachTheTile(GameTestHelper helper) {
        TileTestMachine tile = place(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(POS);

        IFluidHandler fluid = level.getCapability(Capabilities.FluidHandler.BLOCK, abs, Direction.UP);
        helper.assertTrue(fluid != null, "No fluid handler on the test machine");
        int filled = fluid.fill(new FluidStack(Fluids.WATER, 1000), FluidAction.EXECUTE);
        helper.assertTrue(filled == 1000 && tile.tank.getFluidAmount() == 1000, "Tank accepted " + filled + " mB");

        IItemHandler items = level.getCapability(Capabilities.ItemHandler.BLOCK, abs, Direction.NORTH);
        helper.assertTrue(items != null, "No item handler on the test machine");
        ItemStack left = items.insertItem(0, new ItemStack(Items.COBBLESTONE, 16), false);
        helper.assertTrue(left.isEmpty() && tile.inventory.getStackInSlot(0).getCount() == 16,
            "Inventory holds " + tile.inventory.getStackInSlot(0));

        IMjReceiver receiver = level.getCapability(MjAPI.CAP_RECEIVER, abs, Direction.WEST);
        helper.assertTrue(receiver != null, "No MJ receiver on the test machine");
        long excess = receiver.receivePower(5 * MjAPI.MJ, false);
        helper.assertTrue(excess == 0 && tile.battery.getStored() == 5 * MjAPI.MJ,
            "Battery holds " + tile.battery.getDebugString());

        IMjReadable readable = level.getCapability(MjAPI.CAP_READABLE, abs, null);
        helper.assertTrue(readable != null && readable.getStored() == 5 * MjAPI.MJ, "MJ readable reports wrong power");

        // A capability the tile doesn't offer must come back as null, not crash
        helper.assertTrue(level.getCapability(MjAPI.CAP_PASSIVE_PROVIDER, abs, null) == null,
            "Test machine should not be an MJ provider");
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void saveAndLoadKeepsContents(GameTestHelper helper) {
        TileTestMachine tile = place(helper);
        tile.tank.fill(new FluidStack(Fluids.LAVA, 3000), FluidAction.EXECUTE);
        tile.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 5));
        tile.battery.addPower(42 * MjAPI.MJ, false);

        HolderLookup.Provider registries = helper.getLevel().registryAccess();
        CompoundTag tag = tile.saveWithFullMetadata(registries);
        BlockEntity loaded = BlockEntity.loadStatic(tile.getBlockPos(), tile.getBlockState(), tag, registries);

        helper.assertTrue(loaded instanceof TileTestMachine, "Loaded " + loaded);
        TileTestMachine copy = (TileTestMachine) loaded;
        helper.assertTrue(copy.tank.getFluidAmount() == 3000 && copy.tank.getFluid().is(Fluids.LAVA),
            "Tank after reload: " + copy.tank.getDebugString());
        helper.assertTrue(ItemStack.matches(copy.inventory.getStackInSlot(0), new ItemStack(Items.DIAMOND, 5)),
            "Inventory after reload: " + copy.inventory.getStackInSlot(0));
        helper.assertTrue(copy.battery.getStored() == 42 * MjAPI.MJ,
            "Battery after reload: " + copy.battery.getDebugString());
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void guiPayloadRoundTrip(GameTestHelper helper) {
        TileTestMachine server = place(helper);
        server.tank.fill(new FluidStack(Fluids.WATER, 2000), FluidAction.EXECUTE);
        server.battery.addPower(7 * MjAPI.MJ, false);

        PacketBufferBC buffer = new PacketBufferBC(Unpooled.buffer(), helper.getLevel().registryAccess());
        server.writePayload(TileBC_Neptune.NET_GUI_DATA, buffer, LogicalSide.SERVER);

        // A second instance stands in for the client's copy of the tile
        TileTestMachine client = new TileTestMachine(server.getBlockPos(), server.getBlockState());
        try {
            client.readPayload(TileBC_Neptune.NET_GUI_DATA, buffer, LogicalSide.CLIENT, null);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        helper.assertTrue(buffer.readableBytes() == 0, buffer.readableBytes() + " bytes left unread");
        FluidStack shown = client.tank.getFluidForRender();
        helper.assertTrue(shown.is(Fluids.WATER) && shown.getAmount() == 2000, "Client tank shows " + shown);
        helper.assertTrue(client.getClientStored() == 7 * MjAPI.MJ, "Client battery shows " + client.getClientStored());
        buffer.release();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void tileUpdatePayloadCodecRoundTrip(GameTestHelper helper) {
        TileUpdatePayload original = new TileUpdatePayload(new BlockPos(12, -40, 99), new byte[] { 0, 1, 2, -3, 127 });
        FriendlyByteBuf buf = new FriendlyByteBuf(Unpooled.buffer());
        TileUpdatePayload.STREAM_CODEC.encode(buf, original);
        TileUpdatePayload decoded = TileUpdatePayload.STREAM_CODEC.decode(buf);
        helper.assertTrue(decoded.pos().equals(original.pos())
            && java.util.Arrays.equals(decoded.data(), original.data()), "Payload changed in transit");
        buf.release();
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void menuTracksViewersAndAcceptsBuckets(GameTestHelper helper) {
        TileTestMachine tile = place(helper);
        FakePlayer player = FakePlayerFactory.getMinecraft(helper.getLevel());
        player.moveTo(Vec3.atCenterOf(tile.getBlockPos()));

        ContainerTestMachine menu = new ContainerTestMachine(1, player, tile);
        helper.assertTrue(tile.getUsingPlayers().contains(player), "Opening the menu didn't register the viewer");
        helper.assertTrue(menu.stillValid(player), "Menu should be valid next to the machine");

        // Simulate the client clicking the tank with a water bucket on the cursor
        menu.setCarried(new ItemStack(Items.WATER_BUCKET));
        PacketBufferBC click = new PacketBufferBC(Unpooled.buffer(), helper.getLevel().registryAccess());
        click.writeByte(0);
        try {
            menu.widgetTank.handleWidgetDataServer(null, click);
        } catch (java.io.IOException e) {
            throw new RuntimeException(e);
        }
        click.release();
        helper.assertTrue(tile.tank.getFluidAmount() == 1000, "Tank holds " + tile.tank.getDebugString());
        helper.assertTrue(menu.getCarried().is(Items.BUCKET), "Cursor holds " + menu.getCarried());

        menu.removed(player);
        helper.assertTrue(!tile.getUsingPlayers().contains(player), "Closing the menu didn't remove the viewer");
        helper.succeed();
    }

    /** Right-clicking the block with a bucket goes through the real block interaction path. A fluid container is always
     * "handled", so it never opens the GUI; creative players keep their bucket (both as in 1.12). */
    @GameTest(template = "empty")
    public static void rightClickWithBuckets(GameTestHelper helper) {
        TileTestMachine tile = place(helper);
        ServerLevel level = helper.getLevel();
        BlockPos abs = helper.absolutePos(POS);
        BlockState state = level.getBlockState(abs);
        BlockHitResult hit = new BlockHitResult(Vec3.atCenterOf(abs), Direction.UP, abs, false);

        FakePlayer survival = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "bc_survival"));
        survival.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        ItemInteractionResult result = state.useItemOn(survival.getMainHandItem(), level, survival,
            InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction(), "Water bucket click returned " + result);
        helper.assertTrue(tile.tank.getFluidAmount() == 1000, "Tank holds " + tile.tank.getDebugString());
        helper.assertTrue(survival.getMainHandItem().is(Items.BUCKET), "Hand holds " + survival.getMainHandItem());

        // The empty bucket fills back up from the tank
        result = state.useItemOn(survival.getMainHandItem(), level, survival, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction() && tile.tank.isEmpty(), "Tank holds " + tile.tank.getDebugString());
        helper.assertTrue(survival.getMainHandItem().is(Items.WATER_BUCKET),
            "Hand holds " + survival.getMainHandItem());

        // An empty bucket on an empty tank does nothing, but is still handled (so no GUI opens)
        survival.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.BUCKET));
        result = state.useItemOn(survival.getMainHandItem(), level, survival, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction() && survival.getMainHandItem().is(Items.BUCKET),
            "Empty bucket on empty tank returned " + result);

        FakePlayer creative = FakePlayerFactory.get(level, new GameProfile(UUID.randomUUID(), "bc_creative"));
        creative.getAbilities().instabuild = true;
        creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.WATER_BUCKET));
        result = state.useItemOn(creative.getMainHandItem(), level, creative, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction(), "Creative water bucket click returned " + result);
        helper.assertTrue(tile.tank.getFluidAmount() == 1000, "Tank holds " + tile.tank.getDebugString());
        helper.assertTrue(creative.getMainHandItem().is(Items.WATER_BUCKET),
            "Creative hand holds " + creative.getMainHandItem());

        // Anything that isn't a fluid container opens the GUI instead (the tile handles the click)
        creative.setItemInHand(InteractionHand.MAIN_HAND, new ItemStack(Items.STICK));
        result = state.useItemOn(creative.getMainHandItem(), level, creative, InteractionHand.MAIN_HAND, hit);
        helper.assertTrue(result.consumesAction(), "Stick click returned " + result);
        helper.succeed();
    }

    @GameTest(template = "empty")
    public static void breakingDropsInventory(GameTestHelper helper) {
        TileTestMachine tile = place(helper);
        tile.inventory.setStackInSlot(0, new ItemStack(Items.DIAMOND, 3));
        helper.destroyBlock(POS);
        helper.assertItemEntityPresent(Items.DIAMOND, POS, 2.0);
        helper.succeed();
    }
}
