/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.builders.tile.TileConstructionMarker;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.robotics.ai.AIRobotDisposeItems;
import buildcraftreborn.robotics.ai.AIRobotGotoBlock;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoad;

/** Builds the blueprints of construction markers: fetches what the nearest marker needs from stations that provide
 * items, flies it over, and gives the marker the power to place it.
 * <p>
 * 1.21: 7.1's builder robot built each block itself, with 7.1's builder; here the marker runs 8.0's builder, and the
 * robot keeps it supplied with items and power. */
public class BoardRobotBuilder extends RedstoneBoardRobot {
    private static final double MAX_RANGE_SQ = 3 * 64 * 64;
    /** The robot keeps this much power for itself (on top of what it needs to stay safe). */
    private static final long POWER_RESERVE = EntityRobotBase.SAFETY_POWER + 500 * MjAPI.MJ;
    /** How close to the marker the robot gets to hand things over. */
    private static final double DELIVERY_DISTANCE = 3;

    @Nullable
    private TileConstructionMarker markerToBuild;
    private int launchingDelay = 0;

    public BoardRobotBuilder(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("builder");
    }

    @Override
    public void update() {
        if (launchingDelay > 0) {
            launchingDelay--;
            return;
        }

        if (markerToBuild == null || !markerToBuild.needsToBuild()) {
            markerToBuild = findClosestMarker();
            if (markerToBuild == null) {
                if (robot.containsItems()) {
                    startDelegateAI(new AIRobotDisposeItems(robot));
                } else {
                    startDelegateAI(new AIRobotGotoSleep(robot));
                }
                return;
            }
        }

        List<ItemStack> needed = markerToBuild.getNeededItems();
        boolean carrying = false;
        for (int i = 0; i < robot.getSlots() && !carrying; i++) {
            ItemStack stack = robot.getStackInSlot(i);
            carrying = !stack.isEmpty() && needed.stream().anyMatch(n -> StackUtil.canMerge(n, stack));
        }
        boolean canGivePower = markerToBuild.getPowerWanted() > markerToBuild.getBattery().getCapacity() / 2
            && robot.getBattery().getStored() > POWER_RESERVE;

        if (carrying || canGivePower) {
            startDelegateAI(new AIRobotGotoBlock(robot, markerToBuild.getBlockPos(), DELIVERY_DISTANCE));
        } else if (robot.containsItems()) {
            // Things the marker doesn't need (any more) go back to a station
            startDelegateAI(new AIRobotDisposeItems(robot));
        } else if (!needed.isEmpty()) {
            ItemStack wanted = needed.get(0);
            startDelegateAI(new AIRobotGotoStationAndLoad(robot, stack -> StackUtil.canMerge(wanted, stack),
                Math.min(wanted.getCount(), wanted.getMaxStackSize())));
        } else {
            // The marker has all it needs for now
            launchingDelay = 40;
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotGotoBlock && markerToBuild != null && markerToBuild.needsToBuild()) {
            Vec3 marker = Vec3.atCenterOf(markerToBuild.getBlockPos());
            if (robot.position().distanceToSqr(marker) <= (DELIVERY_DISTANCE + 1) * (DELIVERY_DISTANCE + 1)) {
                deliver(markerToBuild);
            }
            launchingDelay = 20;
        }
    }

    private void deliver(TileConstructionMarker marker) {
        List<ItemStack> needed = marker.getNeededItems();
        for (int i = 0; i < robot.getSlots(); i++) {
            ItemStack stack = robot.getStackInSlot(i);
            if (!stack.isEmpty() && needed.stream().anyMatch(n -> StackUtil.canMerge(n, stack))) {
                ItemStack left = marker.addResources(robot.extractItem(i, stack.getCount(), false));
                if (!left.isEmpty()) {
                    left = robot.insertItem(i, left, false);
                    if (!left.isEmpty()) {
                        // (an emptied bucket the robot has no room for)
                        left = robot.receiveItem(marker, left);
                        if (!left.isEmpty()) {
                            robot.spawnAtLocation(left);
                        }
                    }
                }
            }
        }
        long spare = robot.getBattery().getStored() - POWER_RESERVE;
        long given = Math.min(spare, marker.getPowerWanted());
        if (given > 0) {
            robot.getBattery().extractPower(given);
            marker.addPower(given);
        }
    }

    @Nullable
    private TileConstructionMarker findClosestMarker() {
        double minDistance = Double.MAX_VALUE;
        TileConstructionMarker minMarker = null;
        IZone zone = robot.getZoneToWork();
        for (TileConstructionMarker marker : TileConstructionMarker.getMarkers()) {
            if (marker.getLevel() != robot.level() || !marker.needsToBuild()) {
                continue;
            }
            BlockPos pos = marker.getBlockPos();
            if (zone != null && !zone.contains(Vec3.atCenterOf(pos))) {
                continue;
            }
            double distance = robot.position().distanceToSqr(Vec3.atCenterOf(pos));
            if (distance < minDistance) {
                minMarker = marker;
                minDistance = distance;
            }
        }
        return minDistance < MAX_RANGE_SQ ? minMarker : null;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);
        nbt.putInt("launchingDelay", launchingDelay);
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);
        launchingDelay = nbt.getInt("launchingDelay");
    }
}
