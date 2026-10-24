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
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.lib.misc.NBTUtilBC;
import buildcraftreborn.robotics.RobotUtils;

/** Uses the held tool on the top of a block (a hoe on dirt).
 * <p>
 * 1.21: the tool takes its own damage when used (7.1 damaged it a second time). */
public class AIRobotUseToolOnBlock extends AIRobot {

    @Nullable
    private BlockPos useToBlock;
    private int useCycles = 0;

    public AIRobotUseToolOnBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotUseToolOnBlock(EntityRobotBase iRobot, BlockPos index) {
        this(iRobot);

        useToBlock = index;
    }

    @Override
    public void start() {
        if (useToBlock != null) {
            robot.aimItemAt(useToBlock);
        }
        robot.setItemActive(true);
    }

    @Override
    public void update() {
        if (useToBlock == null) {
            setSuccess(false);
            terminate();
            return;
        }
        useCycles++;

        if (useCycles > 40) {
            ItemStack stack = robot.getMainHandItem();

            if (RobotUtils.useItemOnBlock(robot, stack, useToBlock, Direction.UP)) {
                if (stack.isDamageableItem()) {
                    robot.setItemInUse(stack.isEmpty() ? ItemStack.EMPTY : stack);
                } else {
                    robot.setItemInUse(ItemStack.EMPTY);
                }
            } else {
                setSuccess(false);
                if (!stack.isEmpty() && !stack.isDamageableItem()) {
                    RobotUtils.dropItem(robot.level(), robot.position(), 6000, stack);
                    robot.setItemInUse(ItemStack.EMPTY);
                }
            }

            terminate();
        }
    }

    @Override
    public void end() {
        robot.setItemActive(false);
    }

    @Override
    public long getPowerCost() {
        return 8 * MjAPI.MJ / 10;
    }

    @Override
    public boolean canLoadFromNBT() {
        return true;
    }

    @Override
    public void writeSelfToNBT(CompoundTag nbt) {
        super.writeSelfToNBT(nbt);

        if (useToBlock != null) {
            nbt.put("blockFound", NBTUtilBC.writeBlockPos(useToBlock));
        }
    }

    @Override
    public void loadSelfFromNBT(CompoundTag nbt) {
        super.loadSelfFromNBT(nbt);

        if (nbt.contains("blockFound")) {
            useToBlock = NBTUtilBC.readBlockPos(nbt.get("blockFound"));
        }
    }
}
