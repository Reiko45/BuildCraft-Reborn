/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.station.IStationFilter;
import buildcraftreborn.robotics.statements.ActionStationForbidRobot;

/** Finds the nearest free station that matches a filter (and that the station's gates don't forbid). */
public class AIRobotSearchStation extends AIRobot {

    @Nullable
    public DockingStation targetStation;
    private IStationFilter filter;
    @Nullable
    private IZone zone;

    public AIRobotSearchStation(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchStation(EntityRobotBase iRobot, IStationFilter iFilter, @Nullable IZone iZone) {
        this(iRobot);

        filter = iFilter;
        zone = iZone;
    }

    @Override
    public void start() {
        if (filter == null) {
            terminate();
            return;
        }
        if (robot.getDockingStation() != null && filter.matches(robot.getDockingStation())) {
            targetStation = robot.getDockingStation();
            terminate();
            return;
        }

        double potentialStationDistance = Float.MAX_VALUE;
        DockingStation potentialStation = null;

        for (DockingStation station : robot.getRegistry().getStations()) {
            if (!station.isInitialized()) {
                continue;
            }

            if (station.isTaken() && station.robotIdTaking() != robot.getRobotId()) {
                continue;
            }

            if (zone != null && !zone.contains(Vec3.atCenterOf(station.getPos()))) {
                continue;
            }

            if (filter.matches(station)) {
                if (ActionStationForbidRobot.isForbidden(station, robot)) {
                    continue;
                }

                double distance = robot.blockPosition().distSqr(station.getPos());

                if (potentialStation == null || distance < potentialStationDistance) {
                    potentialStation = station;
                    potentialStationDistance = distance;
                }
            }
        }

        if (potentialStation != null) {
            targetStation = potentialStation;
        }

        terminate();
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        terminate();
    }

    @Override
    public boolean success() {
        return targetStation != null;
    }
}
