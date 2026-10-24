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

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import buildcraftreborn.api.boards.RedstoneBoardRobot;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.robots.ResourceIdBlock;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.ai.AIRobotGotoSleep;
import buildcraftreborn.robotics.ai.AIRobotSearchAndGotoBlock;
import buildcraftreborn.robotics.statements.ActionRobotFilter;

/** A board that looks for a kind of block (limited to the blocks in the station's filter action, if any).
 * <p>
 * 1.21: the filter compares blocks (1.12 compared block states made from the item's metadata). */
public abstract class BoardRobotGenericSearchBlock extends RedstoneBoardRobot {

    @Nullable
    private BlockPos blockFound;
    private final List<Block> blockFilter = new ArrayList<>();

    public BoardRobotGenericSearchBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public abstract boolean isExpectedBlock(Level world, BlockPos pos);

    @Override
    public void update() {
        updateFilter();

        startDelegateAI(new AIRobotSearchAndGotoBlock(robot, false, (world, pos) -> {
            if (isExpectedBlock(world, pos) && !robot.getRegistry().isTaken(new ResourceIdBlock(pos))) {
                return matchesGateFilter(world, pos);
            } else {
                return false;
            }
        }));
    }

    @Override
    public void delegateAIEnded(AIRobot ai) {
        if (ai instanceof AIRobotSearchAndGotoBlock search) {
            if (ai.success()) {
                blockFound = search.getBlockFound();
            } else {
                startDelegateAI(new AIRobotGotoSleep(robot));
            }
        }
    }

    @Override
    public void end() {
        releaseBlockFound(true);
    }

    @Nullable
    protected BlockPos blockFound() {
        return blockFound;
    }

    protected void releaseBlockFound(boolean success) {
        if (blockFound != null) {
            // TODO: if !ai.success() -> can't break block, blacklist it
            robot.getRegistry().release(new ResourceIdBlock(blockFound));
            blockFound = null;
        }
    }

    public final void updateFilter() {
        blockFilter.clear();

        if (robot.getLinkedStation() == null) {
            return;
        }
        for (StatementSlot slot : robot.getLinkedStation().getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter) {
                for (IStatementParameter p : slot.parameters) {
                    if (p instanceof StatementParameterItemStack param) {
                        ItemStack stack = param.getItemStack();

                        if (stack.getItem() instanceof BlockItem item) {
                            blockFilter.add(item.getBlock());
                        }
                    }
                }
            }
        }
    }

    protected boolean matchesGateFilter(Level world, BlockPos pos) {
        if (blockFilter.isEmpty()) {
            return true;
        }

        Block block = world.getBlockState(pos).getBlock();
        return blockFilter.contains(block);
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (blockFound != null) {
            nbt.put("indexStored", NBTUtilBC.writeBlockPos(blockFound));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("indexStored")) {
            blockFound = NBTUtilBC.readBlockPos(nbt.get("indexStored"));
        }
    }
}
