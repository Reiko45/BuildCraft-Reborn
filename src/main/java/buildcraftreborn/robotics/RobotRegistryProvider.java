/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics;

import java.util.Map;
import java.util.WeakHashMap;

import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;

import buildcraftreborn.api.robots.IRobotRegistryProvider;

/** 1.21: server levels keep their registry as saved data; client levels get an empty one that is never saved (robots
 * and stations only work on the server). */
public enum RobotRegistryProvider implements IRobotRegistryProvider {
    INSTANCE;

    private final Map<Level, RobotRegistry> clientRegistries = new WeakHashMap<>();

    @Override
    public RobotRegistry getRegistry(Level world) {
        if (world instanceof ServerLevel server) {
            return server.getDataStorage().computeIfAbsent(RobotRegistry.factory(server), RobotRegistry.NAME);
        }
        return clientRegistries.computeIfAbsent(world, RobotRegistry::new);
    }
}
