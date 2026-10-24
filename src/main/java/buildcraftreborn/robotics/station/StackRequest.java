/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.station;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.IRequestProvider;
import buildcraftreborn.api.robots.ResourceId;
import buildcraftreborn.api.robots.ResourceIdRequest;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.lib.misc.NBTUtilBC;

/** A request a delivery robot is fulfilling: which station (and request slot) wants which stack. */
public class StackRequest {
    @Nullable
    private IRequestProvider requester;

    private final int slot;

    private final ItemStack stack;

    @Nullable
    private DockingStation station;
    @Nullable
    private BlockPos stationIndex;
    @Nullable
    private Direction stationSide;

    public StackRequest(IRequestProvider requester, int slot, ItemStack stack) {
        this.requester = requester;
        this.slot = slot;
        this.stack = stack;
        this.station = null;
    }

    private StackRequest(int slot, ItemStack stack, BlockPos stationIndex, Direction stationSide) {
        requester = null;
        this.slot = slot;
        this.stack = stack;
        station = null;
        this.stationIndex = stationIndex;
        this.stationSide = stationSide;
    }

    @Nullable
    public IRequestProvider getRequester(Level world) {
        if (requester == null) {
            DockingStation dockingStation = getStation(world);
            if (dockingStation != null) {
                requester = dockingStation.getRequestProvider();
            }
        }
        return requester;
    }

    public int getSlot() {
        return slot;
    }

    public ItemStack getStack() {
        return stack;
    }

    @Nullable
    public DockingStation getStation(Level world) {
        if (station == null && stationIndex != null) {
            station = RobotManager.registryProvider.getRegistry(world).getStation(stationIndex, stationSide);
        }
        return station;
    }

    public void setStation(DockingStation station) {
        this.station = station;
        this.stationIndex = station.index();
        this.stationSide = station.side();
    }

    public void writeToNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        nbt.putInt("slot", slot);
        if (!stack.isEmpty()) {
            nbt.put("stack", stack.save(registries));
        }

        if (stationIndex != null) {
            nbt.put("stationIndex", NBTUtilBC.writeBlockPos(stationIndex));
            nbt.putByte("stationSide", (byte) stationSide.ordinal());
        }
    }

    @Nullable
    public static StackRequest loadFromNBT(CompoundTag nbt, HolderLookup.Provider registries) {
        if (nbt.contains("stationIndex")) {
            int slot = nbt.getInt("slot");

            ItemStack stack = ItemStack.parseOptional(registries, nbt.getCompound("stack"));

            BlockPos stationIndex = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
            Direction stationSide = Direction.from3DDataValue(nbt.getByte("stationSide"));

            return new StackRequest(slot, stack, stationIndex, stationSide);
        } else {
            return null;
        }
    }

    @Nullable
    public ResourceId getResourceId(Level world) {
        DockingStation s = getStation(world);
        return s != null ? new ResourceIdRequest(s, slot) : null;
    }
}
