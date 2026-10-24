/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import java.util.ArrayList;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.inventory.filter.PassThroughStackFilter;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.robotics.ai.AIRobotDeliverRequested;
import buildcraftreborn.robotics.ai.AIRobotDisposeItems;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndLoad;
import buildcraftreborn.robotics.ai.AIRobotSearchStackRequest;
import buildcraftreborn.robotics.station.StackRequest;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** Fetches what stations (and requesters next to them) ask for, and delivers it. */
public class BoardRobotDelivery extends RedstoneBoardRobot {

    private final List<ItemStack> deliveryBlacklist = new ArrayList<>();

    @Nullable
    private StackRequest currentRequest = null;

    public BoardRobotDelivery(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("delivery");
    }

    @Override
    public void update() {
        if (robot.containsItems()) {
            startDelegateAI(new AIRobotDisposeItems(robot));
            return;
        }

        if (currentRequest == null) {
            IStackFilter filter = robot.getLinkedStation() == null ? new PassThroughStackFilter()
                : ActionRobotFilter.getGateFilter(robot.getLinkedStation());
            startDelegateAI(new AIRobotSearchStackRequest(robot, filter, deliveryBlacklist));
        } else {
            final StackRequest request = currentRequest;
            startDelegateAI(new AIRobotGotoStationAndLoad(robot,
                stack -> StackUtil.isMatchingItemOrList(stack, request.getStack()), request.getStack().getCount()));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStackRequest search) {
            if (!ai.success() || search.request == null) {
                deliveryBlacklist.clear();
                startDelegateAI(new AIRobotGotoSleep(robot));
            } else {
                currentRequest = search.request;

                DockingStation station = currentRequest.getStation(robot.level());
                if (station == null || !station.take(robot)) {
                    releaseCurrentRequest();
                }
            }
        } else if (ai instanceof AIRobotGotoStationAndLoad) {
            if (!ai.success()) {
                if (currentRequest != null) {
                    deliveryBlacklist.add(currentRequest.getStack());
                }
                releaseCurrentRequest();
            } else if (currentRequest != null) {
                startDelegateAI(new AIRobotDeliverRequested(robot, currentRequest));
            }
        } else if (ai instanceof AIRobotDeliverRequested) {
            releaseCurrentRequest();
        }
    }

    private void releaseCurrentRequest() {
        if (currentRequest != null) {
            robot.getRegistry().release(currentRequest.getResourceId(robot.level()));
            DockingStation station = currentRequest.getStation(robot.level());
            if (station != null) {
                station.release(robot);
            }
            currentRequest = null;
        }
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (currentRequest != null) {
            CompoundTag requestNBT = new CompoundTag();
            currentRequest.writeToNBT(requestNBT, robot.registryAccess());
            nbt.put("currentRequest", requestNBT);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);
        if (nbt.contains("currentRequest")) {
            currentRequest = StackRequest.loadFromNBT(nbt.getCompound("currentRequest"), robot.registryAccess());
        }
    }
}
