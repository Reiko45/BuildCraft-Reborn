/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import buildcraftreborn.api.core.render.ISprite;
import buildcraftreborn.core.BCCoreSprites;

/** The robot and station statement icons ({@code textures/gui/sprites/triggers/}). */
public final class BCRoboticsSprites {
    public static final ISprite ACTION_ROBOT_FILTER = BCCoreSprites.get("action_robot_filter");
    public static final ISprite ACTION_ROBOT_FILTER_TOOL = BCCoreSprites.get("action_robot_filter_tool");
    public static final ISprite ACTION_ROBOT_GOTO_STATION = BCCoreSprites.get("action_robot_goto_station");
    public static final ISprite ACTION_ROBOT_LOAD_UNLOAD_AREA = BCCoreSprites.get("action_robot_load_unload_area");
    public static final ISprite ACTION_ROBOT_WAKEUP = BCCoreSprites.get("action_robot_wakeup");
    public static final ISprite ACTION_ROBOT_WORK_IN_AREA = BCCoreSprites.get("action_robot_work_in_area");
    public static final ISprite ACTION_STATION_ACCEPT_FLUIDS = BCCoreSprites.get("action_station_accept_fluids");
    public static final ISprite ACTION_STATION_ACCEPT_ITEMS = BCCoreSprites.get("action_station_accept_items");
    public static final ISprite ACTION_STATION_MACHINE_REQUEST = BCCoreSprites.get("action_station_machine_request");
    public static final ISprite ACTION_STATION_PROVIDE_FLUIDS = BCCoreSprites.get("action_station_provide_fluids");
    public static final ISprite ACTION_STATION_PROVIDE_ITEMS = BCCoreSprites.get("action_station_provide_items");
    public static final ISprite ACTION_STATION_REQUEST_ITEMS = BCCoreSprites.get("action_station_request_items");
    public static final ISprite ACTION_STATION_ROBOT_FORBIDDEN = BCCoreSprites.get("action_station_robot_forbidden");
    public static final ISprite ACTION_STATION_ROBOT_MANDATORY = BCCoreSprites.get("action_station_robot_mandatory");
    public static final ISprite TRIGGER_ROBOT_IN_STATION = BCCoreSprites.get("trigger_robot_in_station");
    public static final ISprite TRIGGER_ROBOT_LINKED = BCCoreSprites.get("trigger_robot_linked");
    public static final ISprite TRIGGER_ROBOT_RESERVED = BCCoreSprites.get("trigger_robot_reserved");
    public static final ISprite TRIGGER_ROBOT_SLEEP = BCCoreSprites.get("trigger_robot_sleep");

    private BCRoboticsSprites() {}
}
