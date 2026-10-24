/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRequestProvider;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.robotics.station.StackRequest;

/** Finds a station whose requester wants something the robot isn't told to ignore, and takes the request. */
public class AIRobotSearchStackRequest extends AIRobot {

    @Nullable
    public StackRequest request = null;
    @Nullable
    public DockingStation station = null;

    private Collection<ItemStack> blackList = List.of();

    private IStackFilter filter;

    public AIRobotSearchStackRequest(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchStackRequest(EntityRobotBase iRobot, IStackFilter iFilter, Collection<ItemStack> iBlackList) {
        this(iRobot);

        blackList = iBlackList;
        filter = iFilter;
    }

    @Override
    public void start() {
        if (filter == null) {
            terminate();
            return;
        }
        startDelegateAI(new AIRobotSearchStation(robot, s -> getOrderFromRequestingStation(s, false) != null,
            robot.getZoneToWork()));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchStation search) {
            if (ai.success() && search.targetStation != null) {
                request = getOrderFromRequestingStation(search.targetStation, true);
            }

            terminate();
        }
    }

    @Override
    public boolean success() {
        return request != null;
    }

    private boolean isBlacklisted(ItemStack stack) {
        for (ItemStack black : blackList) {
            if (StackUtil.isMatchingItem(stack, black)) {
                return true;
            }
        }

        return false;
    }

    @Nullable
    private StackRequest getOrderFromRequestingStation(DockingStation station, boolean take) {
        for (StackRequest req : getAvailableRequests(station)) {
            if (!isBlacklisted(req.getStack()) && filter.matches(req.getStack())) {
                req.setStation(station);
                if (take) {
                    if (robot.getRegistry().take(req.getResourceId(robot.level()), robot)) {
                        return req;
                    }
                } else {
                    return req;
                }
            }
        }

        return null;
    }

    private Collection<StackRequest> getAvailableRequests(DockingStation station) {
        List<StackRequest> result = new ArrayList<>();

        IRequestProvider provider = station.getRequestProvider();
        if (provider == null) {
            return result;
        }

        for (int i = 0; i < provider.getRequestsCount(); i++) {
            ItemStack wanted = provider.getRequest(i);
            if (wanted.isEmpty()) {
                continue;
            }
            StackRequest req = new StackRequest(provider, i, wanted);
            req.setStation(station);
            if (!robot.getRegistry().isTaken(req.getResourceId(robot.level()))) {
                result.add(req);
            }
        }
        return result;
    }
}
