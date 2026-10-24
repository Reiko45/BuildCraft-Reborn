/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Takes a station and docks at it. */
public class AIRobotGotoStation extends AIRobot {

    private BlockPos stationIndex;
    private Direction stationSide;

    public AIRobotGotoStation(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGotoStation(EntityRobotBase iRobot, DockingStation station) {
        this(iRobot);

        stationIndex = station.index();
        stationSide = station.side();
        setSuccess(false);
    }

    private DockingStation getStation() {
        return stationIndex == null ? null : robot.getRegistry().getStation(stationIndex, stationSide);
    }

    @Override
    public void start() {
        DockingStation station = getStation();

        if (station == null) {
            terminate();
        } else if (station == robot.getDockingStation()) {
            setSuccess(true);
            terminate();
        } else {
            if (station.take(robot)) {
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().relative(stationSide)));
            } else {
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        DockingStation station = getStation();

        if (station == null) {
            terminate();
        } else if (ai instanceof AIRobotGotoBlock) {
            if (ai.success()) {
                startDelegateAI(new AIRobotStraightMoveTo(robot, EntityRobot.dockedPosition(station)));
            } else {
                terminate();
            }
        } else {
            setSuccess(true);
            if (stationSide.getAxis() != Direction.Axis.Y) {
                robot.aimItemAt(stationIndex.relative(stationSide, 2));
            } else {
                robot.aimItemAt(Mth.floor(robot.getAimYaw() / 90f) * 90f + 180f, robot.getAimPitch());
            }
            robot.dock(station);
            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        if (stationIndex != null) {
            nbt.put("stationIndex", NBTUtilBC.writeBlockPos(stationIndex));
            nbt.putByte("stationSide", (byte) stationSide.ordinal());
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("stationIndex")) {
            stationIndex = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
            stationSide = Direction.from3DDataValue(nbt.getByte("stationSide"));
        }
    }
}
