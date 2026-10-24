/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraftreborn.lib.inventory.filter.PassThroughStackFilter;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Limits which tools the robots of the station pick up. */
public class ActionRobotFilterTool extends BCStatement implements IActionInternal {

    public ActionRobotFilterTool() {
        super("buildcraft:robot.work_filter_tool");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.robot.filter_tool");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_FILTER_TOOL;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    public static Collection<ItemStack> getGateFilterStacks(DockingStation station) {
        List<ItemStack> result = new ArrayList<>();

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilterTool) {
                for (IStatementParameter p : slot.parameters) {
                    if (p instanceof StatementParameterItemStack param) {
                        ItemStack stack = param.getItemStack();

                        if (!stack.isEmpty()) {
                            result.add(stack);
                        }
                    }
                }
            }
        }

        return result;
    }

    public static IStackFilter getGateFilter(DockingStation station) {
        Collection<ItemStack> stacks = getGateFilterStacks(station);

        if (stacks.isEmpty()) {
            return new PassThroughStackFilter();
        } else {
            return new ArrayStackOrListFilter(stacks.toArray(new ItemStack[0]));
        }
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {}
}
