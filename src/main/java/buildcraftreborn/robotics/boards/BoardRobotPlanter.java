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
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.core.BuildCraftAPI;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.crops.CropManager;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.lib.inventory.filter.AggregateFilter;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.ai.AIRobotFetchAndEquipItemStack;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotPlant;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.util.IBlockFilter;

/** Plants seeds (and saplings, and the like) fetched from a station. */
public class BoardRobotPlanter extends RedstoneBoardRobot {

    @Nullable
    private BlockPos blockFound;
    private final IStackFilter filter = CropManager::isSeed;

    public BoardRobotPlanter(EntityRobotBase iRobot) {
        super(iRobot);
    }

    @Override
    public RedstoneBoardRobotNBT getNBTHandler() {
        return BCBoardNBT.REGISTRY.get("planter");
    }

    @Override
    public void update() {
        if (robot.getMainHandItem().isEmpty()) {
            IStackFilter combined = robot.getLinkedStation() == null ? filter
                : new AggregateFilter(filter, ActionRobotFilter.getGateFilter(robot.getLinkedStation()));
            startDelegateAI(new AIRobotFetchAndEquipItemStack(robot, combined));
        } else {
            final ItemStack itemStack = robot.getMainHandItem();
            IBlockFilter blockFilter = (world, pos) -> !BuildCraftAPI.getWorldProperty("replaceable").get(world, pos)
                && CropManager.canSustainPlant(world, itemStack, pos)
                && !robot.getRegistry().isTaken(new ResourceIdBlock(pos));
            startDelegateAI(new AIRobotSearchAndGotoBlock(robot, true, blockFilter, 1));
        }
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (ai.success() && search.getBlockFound() != null) {
                blockFound = search.getBlockFound();
                startDelegateAI(new AIRobotPlant(robot, blockFound));
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        } else if (ai instanceof AIRobotPlant) {
            releaseBlockFound();
        } else if (ai instanceof AIRobotFetchAndEquipItemStack) {
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
