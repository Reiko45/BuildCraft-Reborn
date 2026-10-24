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

import net.neoforged.neoforge.fluids.FluidStack;
import net.neoforged.neoforge.fluids.FluidUtil;

import buildcraftreborn.api.core.IFluidFilter;
import buildcraftreborn.api.core.IStackFilter;
import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.api.statements.StatementSlot;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.inventory.filter.ArrayFluidFilter;
import buildcraftreborn.lib.inventory.filter.ArrayStackOrListFilter;
import buildcraftreborn.lib.inventory.filter.PassThroughFluidFilter;
import buildcraftreborn.lib.inventory.filter.PassThroughStackFilter;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;

/** Limits what the robots of the station work with (the items, blocks or fluids in the parameters). */
public class ActionRobotFilter extends BCStatement implements IActionInternal {

    public ActionRobotFilter() {
        super("buildcraft:robot.work_filter");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.robot.filter");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_FILTER;
    }

    @Override
    public int minParameters() {
        return 1;
    }

    @Override
    public int maxParameters() {
        return 3;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }

    public static Collection<ItemStack> getGateFilterStacks(DockingStation station) {
        List<ItemStack> result = new ArrayList<>();

        for (StatementSlot slot : station.getActiveActions()) {
            if (slot.statement instanceof ActionRobotFilter) {
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

    public static IFluidFilter getGateFluidFilter(DockingStation station) {
        Collection<ItemStack> stacks = getGateFilterStacks(station);

        if (stacks.isEmpty()) {
            return new PassThroughFluidFilter();
        } else {
            return new ArrayFluidFilter(stacks.toArray(new ItemStack[0]));
        }
    }

    public static boolean canInteractWithItem(DockingStation station, IStackFilter filter, Class<?> actionClass) {
        for (StatementSlot s : station.getActiveActions()) {
            if (actionClass.isAssignableFrom(s.statement.getClass())) {
                StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

                if (!param.hasFilter() || param.matches(filter)) {
                    return true;
                }
            }
        }

        return false;
    }

    public static boolean canInteractWithFluid(DockingStation station, IFluidFilter filter, Class<?> actionClass) {
        for (StatementSlot s : station.getActiveActions()) {
            if (actionClass.isAssignableFrom(s.statement.getClass())) {
                StatementParameterStackFilter param = new StatementParameterStackFilter(s.parameters);

                if (!param.hasFilter()) {
                    return true;
                } else {
                    for (ItemStack stack : param.getStacks()) {
                        if (!stack.isEmpty()) {
                            FluidStack fluid = FluidUtil.getFluidContained(stack).orElse(FluidStack.EMPTY);

                            if (!fluid.isEmpty() && filter.matches(fluid)) {
                                return true;
                            }
                        }
                    }
                }
            }
        }

        return false;
    }

    @Override
    public void actionActivate(IStatementContainer source, IStatementParameter[] parameters) {

    }
}
