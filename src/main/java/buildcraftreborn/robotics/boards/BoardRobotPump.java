/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.boards;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.FluidState;

import net.neoforged.neoforge.fluids.FluidStack;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IWorldProperty;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.lib.inventory.filter.PassThroughFluidFilter;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotGotoStationAndUnloadFluids;
import buildcraftreborn.robotics.ai.AIRobotPumpBlock;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** Pumps fluid source blocks and unloads the fluid at a station. */
public class BoardRobotPump extends RedstoneBoardRobot {

    @Nullable
    private BlockPos blockFound;
    @Nullable
    private IFluidFilter fluidFilter = null;

    public BoardRobotPump(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("pump");
    }

    @Override
    public void update() {
        final IWorldProperty isFluidSource = BuildCraftAPI.getWorldProperty("fluidSource");

        if (!robot.getFluidInTank(0).isEmpty()) {
            startDelegateAI(new AIRobotGotoStationAndUnloadFluids(robot));
        } else {
            updateFilter();

            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, (world, pos) -> {
                if (isFluidSource.get(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
                    return matchesGateFilter(world, pos);
                } else {
                    return false;
                }
            }));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (ai.success() && search.getBlockFound() != null) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotPumpBlock(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPumpBlock) {
            releaseBlockFound();
        } else if (ai instanceof AIRobotGotoStationAndUnloadFluids) {
            if (!ai.success()) {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    private void releaseBlockFound() {
        if (blockFound != null) {
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    public void updateFilter() {
        fluidFilter = robot.getLinkedStation() == null ? null
            : ActionRobotFilter.getGateFluidFilter(robot.getLinkedStation());
        if (fluidFilter instanceof PassThroughFluidFilter) {
            fluidFilter = null;
        }
    }

    private boolean matchesGateFilter(Level world, BlockPos pos) {
        if (fluidFilter == null) {
            return true;
        }

        FluidState fluid = world.getFluidState(pos);

        return !fluid.isEmpty() && fluidFilter.matches(new FluidStack(fluid.getType(), 1000));
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);
        if (blockFound != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}
