/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.station;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.lib.misc.NBTUtilBC;

/** Where a docking station is: a block and one of its sides. 1.21: a record. */
public record StationIndex(BlockPos index, Direction side) {

    public StationIndex(DockingStation station) {
        this(station.index(), station.side());
    }

    public void writeToNBT(CompoundTag nbt) {
        nbt.put("index", NBTUtilBC.writeBlockPos(index));
        nbt.putByte("side", (byte) side.ordinal());
    }

    public static StationIndex readFromNBT(CompoundTag nbt) {
        return new StationIndex(NBTUtilBC.readBlockPos(nbt.get("index")), Direction.from3DDataValue(nbt.getByte("side")));
    }
}
