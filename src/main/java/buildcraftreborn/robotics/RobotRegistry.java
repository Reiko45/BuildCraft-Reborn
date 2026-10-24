/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.SavedData;

import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRobotRegistry;
import buildcraftreborn.api.robots.ResourceId;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.robotics.entity.EntityRobot;
import buildcraftreborn.robotics.station.StationIndex;

/** The robots, stations and reserved resources of one level.
 * <p>
 * 1.21: the level's {@link SavedData} (file {@code data/buildcraftreborn_robots.dat}). Robots unloading with their
 * chunk tell the registry themselves (1.12 checked every robot when a chunk unloaded), and stations are told by their
 * pipe. */
public class RobotRegistry extends SavedData implements IRobotRegistry {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final String NAME = "buildcraftreborn_robots";

    protected final Level world;
    protected final Map<StationIndex, DockingStation> stations = new HashMap<>();

    private long nextRobotID = Long.MIN_VALUE;

    private final Long2ObjectMap<EntityRobot> robotsLoaded = new Long2ObjectOpenHashMap<>();
    private final Map<ResourceId, Long> resourcesTaken = new HashMap<>();
    private final Long2ObjectMap<Set<ResourceId>> resourcesTakenByRobot = new Long2ObjectOpenHashMap<>();
    private final Long2ObjectMap<Set<StationIndex>> stationsTakenByRobot = new Long2ObjectOpenHashMap<>();

    public RobotRegistry(Level world) {
        this.world = world;
    }

    public static SavedData.Factory<RobotRegistry> factory(ServerLevel level) {
        return new SavedData.Factory<>(() -> new RobotRegistry(level), (tag, registries) -> {
            RobotRegistry registry = new RobotRegistry(level);
            registry.readFromNBT(tag);
            return registry;
        }, null);
    }

    @Override
    public CompoundTag save(CompoundTag tag, HolderLookup.Provider registries) {
        writeToNBT(tag);
        return tag;
    }

    @Override
    public long getNextRobotId() {
        long result = nextRobotID;

        nextRobotID = nextRobotID + 1;
        setDirty();

        return result;
    }

    @Override
    public void registerRobot(EntityRobotBase robot) {
        setDirty();

        if (robot.getRobotId() == EntityRobotBase.NULL_ROBOT_ID) {
            ((EntityRobot) robot).setUniqueRobotId(getNextRobotId());
        }
        if (robotsLoaded.containsKey(robot.getRobotId())) {
            LOGGER.warn("Robot with id {} was not unregistered properly", robot.getRobotId());
        }

        robotsLoaded.put(robot.getRobotId(), (EntityRobot) robot);
    }

    @Nullable
    private Set<ResourceId> getResourcesTakenByRobot(long robotId) {
        return resourcesTakenByRobot.get(robotId);
    }

    @Nullable
    private Set<StationIndex> getStationsTakenByRobot(long robotId) {
        return stationsTakenByRobot.get(robotId);
    }

    private void removeRobotLoaded(EntityRobotBase robot) {
        if (robotsLoaded.get(robot.getRobotId()) == robot) {
            robotsLoaded.remove(robot.getRobotId());
        }
    }

    @Override
    public void killRobot(EntityRobotBase robot) {
        setDirty();

        releaseResources(robot, true, false);
        removeRobotLoaded(robot);
    }

    @Override
    public void unloadRobot(EntityRobotBase robot) {
        setDirty();

        releaseResources(robot, false, true);
        removeRobotLoaded(robot);
    }

    @Override
    @Nullable
    public EntityRobot getLoadedRobot(long id) {
        return robotsLoaded.get(id);
    }

    /** @return Every robot of this level that is loaded. */
    public Collection<EntityRobot> getLoadedRobots() {
        return robotsLoaded.values();
    }

    @Override
    public boolean isTaken(ResourceId resourceId) {
        return robotIdTaking(resourceId) != EntityRobotBase.NULL_ROBOT_ID;
    }

    @Override
    public long robotIdTaking(ResourceId resourceId) {
        Long robotId = resourcesTaken.get(resourceId);
        if (robotId == null) {
            return EntityRobotBase.NULL_ROBOT_ID;
        }

        EntityRobot robot = robotsLoaded.get(robotId.longValue());
        if (robot != null && !robot.isRemoved()) {
            return robotId;
        } else {
            // If the robot is either not loaded or dead, the resource is not
            // actively used anymore. Release it.
            release(resourceId);
            return EntityRobotBase.NULL_ROBOT_ID;
        }
    }

    @Override
    @Nullable
    public EntityRobot robotTaking(ResourceId resourceId) {
        long robotId = robotIdTaking(resourceId);

        if (robotId == EntityRobotBase.NULL_ROBOT_ID) {
            return null;
        } else {
            return robotsLoaded.get(robotId);
        }
    }

    @Override
    public boolean take(ResourceId resourceId, EntityRobotBase robot) {
        setDirty();

        return take(resourceId, robot.getRobotId());
    }

    @Override
    public boolean take(@Nullable ResourceId resourceId, long robotId) {
        if (resourceId == null) {
            return false;
        }

        setDirty();

        if (!resourcesTaken.containsKey(resourceId)) {
            resourcesTaken.put(resourceId, robotId);
            resourcesTakenByRobot.computeIfAbsent(robotId, k -> new HashSet<>()).add(resourceId);

            return true;
        } else {
            return false;
        }
    }

    @Override
    public void release(@Nullable ResourceId resourceId) {
        if (resourceId == null) {
            return;
        }

        setDirty();

        Long robotId = resourcesTaken.remove(resourceId);
        if (robotId != null) {
            Set<ResourceId> taken = getResourcesTakenByRobot(robotId);
            if (taken != null) {
                taken.remove(resourceId);
            }
        }
    }

    @Override
    public void releaseResources(EntityRobotBase robot) {
        releaseResources(robot, false, false);
    }

    private void releaseResources(EntityRobotBase robot, boolean forceAll, boolean resetEntities) {
        setDirty();

        Set<ResourceId> resources = getResourcesTakenByRobot(robot.getRobotId());
        if (resources != null) {
            for (ResourceId id : new ArrayList<>(resources)) {
                release(id);
            }

            resourcesTakenByRobot.remove(robot.getRobotId());
        }

        Set<StationIndex> stationsTaken = getStationsTakenByRobot(robot.getRobotId());
        if (stationsTaken != null) {
            for (StationIndex s : new ArrayList<>(stationsTaken)) {
                DockingStation d = stations.get(s);

                if (d != null) {
                    if (!d.canRelease()) {
                        if (forceAll) {
                            d.unsafeRelease(robot);
                        } else if (resetEntities && d.robotIdTaking() == robot.getRobotId()) {
                            d.invalidateRobotTakingEntity();
                        }
                    } else {
                        d.unsafeRelease(robot);
                    }
                }
            }

            if (forceAll) {
                stationsTakenByRobot.remove(robot.getRobotId());
            }
        }
    }

    @Override
    @Nullable
    public DockingStation getStation(BlockPos pos, Direction side) {
        return stations.get(new StationIndex(pos, side));
    }

    @Override
    public Collection<DockingStation> getStations() {
        return stations.values();
    }

    @Override
    public void registerStation(DockingStation station) {
        setDirty();

        StationIndex index = new StationIndex(station);

        if (stations.containsKey(index)) {
            throw new IllegalArgumentException("Station " + index + " already registered");
        } else {
            stations.put(index, station);
        }
    }

    @Override
    public void removeStation(DockingStation station) {
        setDirty();

        StationIndex index = new StationIndex(station);

        if (stations.containsKey(index)) {
            EntityRobotBase robot = station.robotTaking();
            if (robot != null) {
                if (!station.isMainStation()) {
                    robot.undock();
                } else {
                    robot.setMainStation(null);
                }
            } else if (station.robotIdTaking() != EntityRobotBase.NULL_ROBOT_ID) {
                Set<StationIndex> taken = getStationsTakenByRobot(station.robotIdTaking());
                if (taken != null) {
                    taken.remove(index);
                }
            }

            stations.remove(index);
        }
    }

    @Override
    public void take(DockingStation station, long robotId) {
        stationsTakenByRobot.computeIfAbsent(robotId, k -> new HashSet<>()).add(new StationIndex(station));
    }

    @Override
    public void release(DockingStation station, long robotId) {
        Set<StationIndex> taken = getStationsTakenByRobot(robotId);
        if (taken != null) {
            taken.remove(new StationIndex(station));
        }
    }

    @Override
    public void writeToNBT(CompoundTag nbt) {
        nbt.putLong("nextRobotID", nextRobotID);

        ListTag resourceList = new ListTag();

        for (Map.Entry<ResourceId, Long> e : resourcesTaken.entrySet()) {
            CompoundTag cpt = new CompoundTag();
            CompoundTag resourceId = new CompoundTag();
            e.getKey().writeToNBT(resourceId);
            cpt.put("resourceId", resourceId);
            cpt.putLong("robotId", e.getValue());

            resourceList.add(cpt);
        }

        nbt.put("resourceList", resourceList);

        ListTag stationList = new ListTag();

        for (DockingStation station : stations.values()) {
            String type = RobotManager.getDockingStationName(station.getClass());
            if (type == null) {
                continue;
            }
            CompoundTag cpt = new CompoundTag();
            station.writeToNBT(cpt);
            cpt.putString("stationType", type);
            stationList.add(cpt);
        }

        nbt.put("stationList", stationList);
    }

    @Override
    public void readFromNBT(CompoundTag nbt) {
        nextRobotID = nbt.getLong("nextRobotID");

        ListTag resourceList = nbt.getList("resourceList", Tag.TAG_COMPOUND);

        for (int i = 0; i < resourceList.size(); ++i) {
            CompoundTag cpt = resourceList.getCompound(i);
            ResourceId resourceId = ResourceId.load(cpt.getCompound("resourceId"));
            long robotId = cpt.getLong("robotId");

            take(resourceId, robotId);
        }

        ListTag stationList = nbt.getList("stationList", Tag.TAG_COMPOUND);

        for (int i = 0; i < stationList.size(); ++i) {
            CompoundTag cpt = stationList.getCompound(i);

            Class<? extends DockingStation> cls = RobotManager.getDockingStationByName(cpt.getString("stationType"));
            if (cls == null) {
                LOGGER.error("Could not load docking station of type {}", cpt.getString("stationType"));
                continue;
            }

            try {
                DockingStation station = cls.getConstructor().newInstance();
                station.readFromNBT(cpt);
                station.world = world;

                if (stations.containsKey(new StationIndex(station))) {
                    continue;
                }
                registerStation(station);

                if (station.linkedId() != EntityRobotBase.NULL_ROBOT_ID) {
                    take(station, station.linkedId());
                }
            } catch (Exception e) {
                LOGGER.error("Could not load docking station", e);
            }
        }
        setDirty(false);
    }

    @Override
    public void registryMarkDirty() {
        setDirty();
    }
}
