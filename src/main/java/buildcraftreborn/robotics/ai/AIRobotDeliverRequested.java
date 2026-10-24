/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.IRequestProvider;
import buildcraftreborn.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraftreborn.robotics.station.StackRequest;

/** Takes the requested items to the requesting station and hands them over. */
public class AIRobotDeliverRequested extends AIRobot {

    @Nullable
    private StackRequest requested;
    private boolean delivered = false;

    public AIRobotDeliverRequested(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotDeliverRequested(EntityRobotBase robot, StackRequest request) {
        this(robot);

        requested = request;
    }

    @Override
    public void start() {
        DockingStation station = requested == null ? null : requested.getStation(robot.level());
        if (station != null) {
            startDelegateAI(new AIRobotGotoStation(robot, station));
        } else {
            setSuccess(false);
            terminate();
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotGotoStation) {
            if (!ai.success() || requested == null) {
                setSuccess(false);
                terminate();
                return;
            }

            ArrayStackOrListFilter filter = new ArrayStackOrListFilter(requested.getStack());
            int slot = -1;
            for (int i = 0; i < robot.getSlots(); i++) {
                if (!robot.getStackInSlot(i).isEmpty() && filter.matches(robot.getStackInSlot(i))) {
                    slot = i;
                    break;
                }
            }

            if (slot < 0) {
                setSuccess(false);
                terminate();
                return;
            }

            IRequestProvider requester = requested.getRequester(robot.level());
            if (requester == null) {
                setSuccess(false);
                terminate();
                return;
            }
            ItemStack inSlot = robot.getStackInSlot(slot);
            ItemStack newStack = requester.offerItem(requested.getSlot(), inSlot.copy());

            if (newStack.getCount() != inSlot.getCount()) {
                delivered = true;
                robot.extractItem(slot, inSlot.getCount() - newStack.getCount(), false);
            }
            terminate();
        }
    }

    @Override
    public boolean success() {
        return delivered;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (requested != null) {
            CompoundTag requestNBT = new CompoundTag();
            requested.writeToNBT(requestNBT, robot.registryAccess());
            nbt.put("currentRequest", requestNBT);
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);
        if (nbt.contains("currentRequest")) {
            requested = StackRequest.loadFromNBT(nbt.getCompound("currentRequest"), robot.registryAccess());
        }
    }
}
