/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.statements;

import java.util.List;

import org.jetbrains.annotations.Nullable;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.api.items.IMapLocation;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.DockingStation;
import buildcraftreborn.api.robots.IRobotRegistry;
import buildcraftreborn.api.robots.RobotManager;
import buildcraftreborn.api.statements.IActionInternal;
import buildcraftreborn.api.statements.IStatementContainer;
import buildcraftreborn.api.statements.IStatementParameter;
import buildcraftreborn.api.statements.StatementParameterItemStack;
import buildcraftreborn.core.statements.BCStatement;
import buildcraftreborn.lib.misc.LocaleUtil;
import buildcraftreborn.robotics.BCRoboticsSprites;
import buildcraftreborn.robotics.RobotUtils;
import buildcraftreborn.robotics.ai.AIRobotGoAndLinkToDock;
import buildcraftreborn.robotics.entity.EntityRobot;

/** Sends the station's robots home, or to the station in the map location parameter (linking them to it). */
public class ActionRobotGotoStation extends BCStatement implements IActionInternal {

    public ActionRobotGotoStation() {
        super("buildcraft:robot.goto_station");
    }

    @Override
    public String getDescription() {
        return LocaleUtil.localize("gate.action.robot.goto_station");
    }

    @Override
    public ISprite getSprite() {
        return BCRoboticsSprites.ACTION_ROBOT_GOTO_STATION;
    }

    @Override
    public void actionActivate(IStatementContainer container, IStatementParameter[] parameters) {
        if (container.getTile().getLevel() == null || container.getTile().getLevel().isClientSide) {
            return;
        }
        IRobotRegistry registry = RobotManager.registryProvider.getRegistry(container.getTile().getLevel());

        List<DockingStation> stations = RobotUtils.getStations(container.getTile());

        for (DockingStation station : stations) {
            if (station.robotTaking() instanceof EntityRobot robot) {
                AIRobot ai = robot.getOverridingAI();

                if (ai != null) {
                    continue;
                }

                DockingStation newStation = station;

                if (parameters.length > 0 && parameters[0] instanceof StatementParameterItemStack param) {
                    newStation = getStation(param, registry);
                }

                if (newStation != null) {
                    robot.overrideAI(new AIRobotGoAndLinkToDock(robot, newStation));
                }
            }
        }
    }

    @Nullable
    private DockingStation getStation(StatementParameterItemStack stackParam, IRobotRegistry registry) {
        ItemStack item = stackParam.getItemStack();

        if (item.getItem() instanceof IMapLocation map) {
            BlockPos index = map.getPoint(item);

            if (index != null) {
                Direction side = map.getPointSide(item);
                if (side != null) {
                    return registry.getStation(index, side);
                }
            }
        }
        return null;
    }

    @Override
    public int maxParameters() {
        return 1;
    }

    @Override
    public IStatementParameter createParameter(int index) {
        return new StatementParameterItemStack();
    }
}
