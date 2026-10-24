/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.boards.RedstoneBoardRobotNBT;
import buildcraftreborn.api.items.IList;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.api.statements.IStatement;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementMouseClick;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.lib.misc.StackUtil;
import buildcraftreborn.robotics.RobotUtils;
import buildcraftreborn.robotics.entity.EntityRobot;
import buildcraftreborn.robotics.item.ItemRobot;

/** A robot parameter: a robot (clicking with nothing held goes through the boards; right click goes back), a list,
 * or something a robot wears. */
public class StatementParameterRobot extends StatementParameterItemStack {

    public StatementParameterRobot() {}

    public StatementParameterRobot(ItemStack stack) {
        super(stack);
    }

    public StatementParameterRobot(CompoundTag nbt, HolderLookup.Provider registries) {
        super(nbt, registries);
    }

    @Override
    public StatementParameterRobot onClick(IStatementContainer source, IStatement stmt, ItemStack stack,
        StatementMouseClick mouse) {
        if (stack.isEmpty() && (this.stack.isEmpty() || this.stack.getItem() instanceof ItemRobot)) {
            RedstoneBoardRobotNBT nextBoard = RobotUtils.getNextBoard(this.stack, mouse.getButton() > 0);
            if (nextBoard != null) {
                return new StatementParameterRobot(ItemRobot.createRobotStack(nextBoard, 0));
            } else {
                return new StatementParameterRobot();
            }
        } else if (stack.isEmpty()) {
            return new StatementParameterRobot();
        }
        return new StatementParameterRobot(stack.copyWithCount(1));
    }

    @Override
    public String getUniqueTag() {
        return "buildcraft:robot";
    }

    public static boolean matches(IStatementParameter param, EntityRobotBase robot) {
        ItemStack stack = param.getItemStack();
        if (!stack.isEmpty()) {
            EntityRobot bcRobot = robot instanceof EntityRobot r ? r : null;
            if (stack.getItem() instanceof IList list) {
                if (bcRobot != null && list.matches(stack, bcRobot.createRobotStack())) {
                    return true;
                }
                if (bcRobot != null) {
                    for (ItemStack target : bcRobot.getWearables()) {
                        if (!target.isEmpty() && list.matches(stack, target)) {
                            return true;
                        }
                    }
                }
            } else if (stack.getItem() instanceof ItemRobot) {
                return ItemRobot.getRobotNBT(stack) == robot.getBoard().getNBTHandler();
            } else if (bcRobot != null) {
                for (ItemStack target : bcRobot.getWearables()) {
                    if (!target.isEmpty() && StackUtil.isMatchingItem(stack, target, true)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }
}
