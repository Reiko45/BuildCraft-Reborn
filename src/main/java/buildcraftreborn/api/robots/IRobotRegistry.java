/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import java.util.Collection;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

/** Every robot and docking station in a level, and which robot has taken which station and resource. 1.21: kept as
 * the level's saved data. */
public interface IRobotRegistry {

    long getNextRobotId();

    void registerRobot(EntityRobotBase robot);

    void killRobot(EntityRobotBase robot);

    void unloadRobot(EntityRobotBase robot);

    @Nullable
    EntityRobotBase getLoadedRobot(long id);

    boolean isTaken(ResourceId resourceId);

    long robotIdTaking(ResourceId resourceId);

    @Nullable
    EntityRobotBase robotTaking(ResourceId resourceId);

    boolean take(ResourceId resourceId, EntityRobotBase robot);

    boolean take(ResourceId resourceId, long robotId);

    void release(ResourceId resourceId);

    void releaseResources(EntityRobotBase robot);

    @Nullable
    DockingStation getStation(BlockPos pos, Direction side);

    Collection<DockingStation> getStations();

    void registerStation(DockingStation station);

    void removeStation(DockingStation station);

    void take(DockingStation station, long robotId);

    void release(DockingStation station, long robotId);

    void writeToNBT(CompoundTag nbt);

    void readFromNBT(CompoundTag nbt);

    void registryMarkDirty();
}
