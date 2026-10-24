/*
 * Copyright (c) 2011-2017 SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * The BuildCraft API is distributed under the terms of the MIT License. Please check the contents of the license,
 * which is located as "LICENSE-API" in the BuildCraft Reborn source code distribution.
 */
package buildcraftreborn.api.robots;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

/** Names for robot AIs, resources and docking station types, so they can be saved. 1.21: 7.1's migration from 6.4's
 * class names is gone. */
public abstract class RobotManager {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static IRobotRegistryProvider registryProvider;
    public static final List<Class<? extends AIRobot>> aiRobots = new ArrayList<>();

    private static final Map<Class<? extends AIRobot>, String> aiRobotsNames = new HashMap<>();
    private static final Map<String, Class<? extends AIRobot>> aiRobotsByNames = new HashMap<>();
    private static final Map<Class<? extends ResourceId>, String> resourceIdNames = new HashMap<>();
    private static final Map<String, Class<? extends ResourceId>> resourceIdByNames = new HashMap<>();
    private static final Map<Class<? extends DockingStation>, String> dockingStationNames = new HashMap<>();
    private static final Map<String, Class<? extends DockingStation>> dockingStationByNames = new HashMap<>();

    static {
        registerResourceId(ResourceIdBlock.class, "resourceIdBlock");
        registerResourceId(ResourceIdRequest.class, "resourceIdRequest");
    }

    public static void registerAIRobot(Class<? extends AIRobot> aiRobot, String name) {
        if (aiRobotsByNames.containsKey(name)) {
            LOGGER.info("Overriding {} with {}", aiRobotsByNames.get(name).getName(), aiRobot.getName());
        }

        // Check if NBT-load constructor is present
        try {
            aiRobot.getConstructor(EntityRobotBase.class);
        } catch (NoSuchMethodException e) {
            throw new RuntimeException("AI class " + aiRobot.getName() + " lacks NBT load constructor! This is a bug!");
        }

        aiRobots.add(aiRobot);
        aiRobotsByNames.put(name, aiRobot);
        aiRobotsNames.put(aiRobot, name);
    }

    @Nullable
    public static Class<? extends AIRobot> getAIRobotByName(String aiRobotName) {
        return aiRobotsByNames.get(aiRobotName);
    }

    public static String getAIRobotName(Class<? extends AIRobot> aiRobotClass) {
        String name = aiRobotsNames.get(aiRobotClass);
        if (name == null) {
            throw new IllegalStateException("The robot AI " + aiRobotClass.getName() + " isn't registered!");
        }
        return name;
    }

    public static void registerResourceId(Class<? extends ResourceId> resourceId, String name) {
        resourceIdByNames.put(name, resourceId);
        resourceIdNames.put(resourceId, name);
    }

    @Nullable
    public static Class<? extends ResourceId> getResourceIdByName(String resourceIdName) {
        return resourceIdByNames.get(resourceIdName);
    }

    public static String getResourceIdName(Class<? extends ResourceId> resourceIdClass) {
        return resourceIdNames.get(resourceIdClass);
    }

    public static void registerDockingStation(Class<? extends DockingStation> dockingStation, String name) {
        dockingStationByNames.put(name, dockingStation);
        dockingStationNames.put(dockingStation, name);
    }

    @Nullable
    public static Class<? extends DockingStation> getDockingStationByName(String dockingStationTypeName) {
        return dockingStationByNames.get(dockingStationTypeName);
    }

    public static String getDockingStationName(Class<? extends DockingStation> dockingStation) {
        return dockingStationNames.get(dockingStation);
    }
}
