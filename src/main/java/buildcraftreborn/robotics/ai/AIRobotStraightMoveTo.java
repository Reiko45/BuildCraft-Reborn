/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.robots.EntityRobotBase;

/** Flies straight to a point (used to dock and undock). */
public class AIRobotStraightMoveTo extends AIRobotGoto {

    private double prevDistance = Double.MAX_VALUE;

    private Vec3 pos;

    public AIRobotStraightMoveTo(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotStraightMoveTo(EntityRobotBase iRobot, Vec3 pos) {
        this(iRobot);
        this.pos = pos;
        robot.aimItemAt(BlockPos.containing(pos));
    }

    @Override
    public void start() {
        robot.undock();
        if (pos == null) {
            setSuccess(false);
            terminate();
            return;
        }
        setDestination(robot, pos);
    }

    @Override
    public void update() {
        if (next == null) {
            if (pos == null) {
                setSuccess(false);
                terminate();
                return;
            }
            setDestination(robot, pos);
        }
        double distance = robot.position().distanceTo(next);

        if (distance < prevDistance) {
            prevDistance = distance;
        } else {
            robot.setDeltaMovement(Vec3.ZERO);
            robot.setPos(pos.x, pos.y, pos.z);

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

        if (pos != null) {
            nbt.putDouble("x", pos.x);
            nbt.putDouble("y", pos.y);
            nbt.putDouble("z", pos.z);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("x")) {
            pos = new Vec3(nbt.getDouble("x"), nbt.getDouble("y"), nbt.getDouble("z"));
        }
    }
}
