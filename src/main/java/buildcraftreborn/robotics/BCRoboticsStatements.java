/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import buildcraftreborn.api.statements.StatementManager;
import buildcraftreborn.robotics.statements.ActionRobotFilter;
import buildcraftreborn.robotics.statements.ActionRobotFilterTool;
import buildcraftreborn.robotics.statements.ActionRobotGotoStation;
import buildcraftreborn.robotics.statements.ActionRobotWakeUp;
import buildcraftreborn.robotics.statements.ActionRobotWorkInArea;
import buildcraftreborn.robotics.statements.ActionStationAcceptFluids;
import buildcraftreborn.robotics.statements.ActionStationAcceptItems;
import buildcraftreborn.robotics.statements.ActionStationForbidRobot;
import buildcraftreborn.robotics.statements.ActionStationProvideFluids;
import buildcraftreborn.robotics.statements.ActionStationProvideItems;
import buildcraftreborn.robotics.statements.ActionStationRequestItems;
import buildcraftreborn.robotics.statements.ActionStationRequestItemsMachine;
import buildcraftreborn.robotics.statements.RobotsActionProvider;
import buildcraftreborn.robotics.statements.RobotsTriggerProvider;
import buildcraftreborn.robotics.statements.StatementParameterMapLocation;
import buildcraftreborn.robotics.statements.StatementParameterRobot;
import buildcraftreborn.robotics.statements.TriggerRobotInStation;
import buildcraftreborn.robotics.statements.TriggerRobotLinked;
import buildcraftreborn.robotics.statements.TriggerRobotSleep;

/** The robot triggers and actions (7.1's {@code BuildCraftRobotics} statement fields). */
public final class BCRoboticsStatements {
    public static final TriggerRobotSleep TRIGGER_ROBOT_SLEEP = new TriggerRobotSleep();
    public static final TriggerRobotInStation TRIGGER_ROBOT_IN_STATION = new TriggerRobotInStation();
    public static final TriggerRobotLinked TRIGGER_ROBOT_LINKED = new TriggerRobotLinked(false);
    public static final TriggerRobotLinked TRIGGER_ROBOT_RESERVED = new TriggerRobotLinked(true);

    public static final ActionRobotGotoStation ACTION_ROBOT_GOTO_STATION = new ActionRobotGotoStation();
    public static final ActionRobotWakeUp ACTION_ROBOT_WAKE_UP = new ActionRobotWakeUp();
    public static final ActionRobotWorkInArea ACTION_ROBOT_WORK_IN_AREA =
        new ActionRobotWorkInArea(ActionRobotWorkInArea.AreaType.WORK);
    public static final ActionRobotWorkInArea ACTION_ROBOT_LOAD_UNLOAD_AREA =
        new ActionRobotWorkInArea(ActionRobotWorkInArea.AreaType.LOAD_UNLOAD);
    public static final ActionRobotFilter ACTION_ROBOT_FILTER = new ActionRobotFilter();
    public static final ActionRobotFilterTool ACTION_ROBOT_FILTER_TOOL = new ActionRobotFilterTool();
    public static final ActionStationProvideItems ACTION_STATION_PROVIDE_ITEMS = new ActionStationProvideItems();
    public static final ActionStationProvideFluids ACTION_STATION_PROVIDE_FLUIDS = new ActionStationProvideFluids();
    public static final ActionStationForbidRobot ACTION_STATION_FORBID_ROBOT = new ActionStationForbidRobot(false);
    public static final ActionStationForbidRobot ACTION_STATION_FORCE_ROBOT = new ActionStationForbidRobot(true);
    public static final ActionStationAcceptItems ACTION_STATION_ACCEPT_ITEMS = new ActionStationAcceptItems();
    public static final ActionStationAcceptFluids ACTION_STATION_ACCEPT_FLUIDS = new ActionStationAcceptFluids();
    public static final ActionStationRequestItems ACTION_STATION_REQUEST_ITEMS = new ActionStationRequestItems();
    public static final ActionStationRequestItemsMachine ACTION_STATION_MACHINE_REQUEST =
        new ActionStationRequestItemsMachine();

    private BCRoboticsStatements() {}

    static void init() {
        StatementManager.registerParameter(StatementParameterRobot::new);
        StatementManager.registerParameter(StatementParameterMapLocation::new);
        StatementManager.registerActionProvider(RobotsActionProvider.INSTANCE);
        StatementManager.registerTriggerProvider(RobotsTriggerProvider.INSTANCE);
    }
}
