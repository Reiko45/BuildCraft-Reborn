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

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.BlockState;

import net.neoforged.fml.LogicalSide;
import net.neoforged.neoforge.capabilities.BlockCapability;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.network.handling.IPayloadContext;

import buildcraftreborn.api.core.EnumPipePart;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.mj.MjBattery;
import buildcraftreborn.api.mj.MjCapabilityHelper;
import buildcraftreborn.api.tiles.IDebuggable;
import buildcraftreborn.lib.fluid.Tank;
import buildcraftreborn.lib.mj.MjBatteryReceiver;
import buildcraftreborn.lib.net.PacketBufferBC;
import buildcraftreborn.lib.tile.ITickable;
import buildcraftreborn.lib.tile.TileBC_Neptune;
import buildcraftreborn.lib.tile.item.ItemHandlerManager.EnumAccess;
import buildcraftreborn.lib.tile.item.ItemHandlerSimple;

/** Development test tile: a 16-bucket tank, a 1,000 MJ battery and one item slot, all exposed on every side, with a
 * GUI that shows them. Exercises every part of the phase 1 foundation. */
public class TileTestMachine extends TileBC_Neptune implements ITickable, MenuProvider, IDebuggable {
    public static final int TANK_CAPACITY = 16 * 1000;
    public static final long BATTERY_CAPACITY = 1000 * MjAPI.MJ;

    public final Tank tank = new Tank("tank", TANK_CAPACITY, this);
    public final MjBattery battery = new MjBattery(BATTERY_CAPACITY);
    public final ItemHandlerSimple inventory;
    private final MjCapabilityHelper mjCaps = new MjCapabilityHelper(new MjBatteryReceiver(battery));

    /** The battery level as last synced to the client. */
    private long clientStored;

    public TileTestMachine(BlockPos pos, BlockState state) {
        super(BCTestContent.TEST_MACHINE_TILE.get(), pos, state);
        tankManager.add(tank);
        inventory = itemManager.addInvHandler("inv", 1, EnumAccess.BOTH, EnumPipePart.VALUES);
        caps.addCapabilityInstance(Capabilities.FluidHandler.BLOCK, tankManager, EnumPipePart.VALUES);
        addMjCap(MjAPI.CAP_CONNECTOR);
        addMjCap(MjAPI.CAP_RECEIVER);
        addMjCap(MjAPI.CAP_READABLE);
    }

    private <T> void addMjCap(BlockCapability<T, @Nullable Direction> cap) {
        caps.addCapability(cap, () -> mjCaps.getCapability(cap), EnumPipePart.VALUES);
    }

    @Override
    public void update() {
        if (level != null && !level.isClientSide) {
            battery.tick(level, worldPosition);
        }
    }

    public long getClientStored() {
        return clientStored;
    }

    // Network

    @Override
    public void writePayload(int id, PacketBufferBC buffer, LogicalSide side) {
        super.writePayload(id, buffer, side);
        if (side == LogicalSide.SERVER && (id == NET_GUI_DATA || id == NET_GUI_TICK)) {
            tankManager.writeData(buffer);
            buffer.writeLong(battery.getStored());
        }
    }

    @Override
    public void readPayload(int id, PacketBufferBC buffer, LogicalSide side, @Nullable IPayloadContext ctx)
        throws IOException {
        super.readPayload(id, buffer, side, ctx);
        if (side == LogicalSide.CLIENT && (id == NET_GUI_DATA || id == NET_GUI_TICK)) {
            tankManager.readData(buffer);
            clientStored = buffer.readLong();
        }
    }

    // Saving

    @Override
    protected void saveAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.saveAdditional(nbt, registries);
        nbt.put("battery", battery.serializeNBT());
    }

    @Override
    protected void loadAdditional(CompoundTag nbt, HolderLookup.Provider registries) {
        super.loadAdditional(nbt, registries);
        battery.deserializeNBT(nbt.getCompound("battery"));
    }

    // GUI

    @Override
    public boolean onActivated(Player player, InteractionHand hand, ItemStack held) {
        // Buckets go into the tank; anything else opens the GUI (like 1.12 machines do)
        if (super.onActivated(player, hand, held)) {
            return true;
        }
        openGui(player);
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.translatable(getBlockState().getBlock().getDescriptionId());
    }

    @Override
    public AbstractContainerMenu createMenu(int windowId, Inventory inventory, Player player) {
        return new ContainerTestMachine(windowId, player, this);
    }

    @Override
    public void getDebugInfo(List<String> left, List<String> right, @Nullable Direction side) {
        left.add("battery = " + battery.getDebugString());
        left.add("tank = " + tank.getDebugString());
        left.add("inventory = " + inventory.getStackInSlot(0));
    }
}
