/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Makes a station the robot's home, and docks there. */
public class AIRobotGoAndLinkToDock extends AIRobot {

    @Nullable
    private DockingStation station;

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotGoAndLinkToDock(EntityRobotBase iRobot, DockingStation iStation) {
        this(iRobot);

        station = iStation;
    }

    @Override
    public void start() {
        if (station == robot.getLinkedStation() && station == robot.getDockingStation()) {
            terminate();
        } else {
            if (station != null && station.takeAsMain(robot)) {
                startDelegateAI(new AIRobotGotoBlock(robot, station.getPos().relative(station.side(), 2)));
            } else {
                setSuccess(false);
                terminate();
            }
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoBlock) {
            if (ai.success() && station != null) {
                startDelegateAI(new AIRobotStraightMoveTo(robot, EntityRobot.dockedPosition(station)));
            } else {
                terminate();
            }
        } else if (ai instanceof AIRobotStraightMoveTo) {
            if (ai.success() && station != null) {
                robot.dock(station);
            }
            terminate();
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (station != null && station.index() != null) {
            nbt.put("stationIndex", NBTUtilBC.writeBlockPos(station.index()));
            nbt.putByte("stationSide", (byte) station.side().ordinal());
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        if (nbt.contains("stationIndex")) {
            BlockPos index = NBTUtilBC.readBlockPos(nbt.get("stationIndex"));
            Direction side = Direction.from3DDataValue(nbt.getByte("stationSide"));

            station = robot.getRegistry().getStation(index, side);
        } else {
            station = robot.getLinkedStation();
        }
    }
}
