/*
 * Copyright (c) 2011-2017, SpaceToad and the BuildCraft Team http://www.mod-buildcraft.com
 * Ported to Minecraft 1.21.1 by BuildCraft Reborn.
 *
 * BuildCraft is distributed under the terms of the Minecraft Mod Public License 1.0, or MMPL. Please check the contents
 * of the license located in http://www.mod-buildcraft.com/MMPL-1.0.txt (a copy is in LICENSE-ASSETS).
 */
package buildcraftreborn.robotics.ai;

import org.jetbrains.annotations.Nullable;

import net.minecraft.world.entity.Entity;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.util.IEntityFilter;

/** Finds the nearest entity matching a filter. */
public class AIRobotSearchEntity extends AIRobot {

    @Nullable
    public Entity target;

    private float maxRange;
    @Nullable
    private IZone zone;
    private IEntityFilter filter;

    public AIRobotSearchEntity(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchEntity(EntityRobotBase iRobot, IEntityFilter iFilter, float iMaxRange, @Nullable IZone iZone) {
        this(iRobot);

        maxRange = iMaxRange;
        zone = iZone;
        filter = iFilter;
    }

    @Override
    public void start() {
        if (filter == null) {
            terminate();
            return;
        }
        double previousDistance = Double.MAX_VALUE;
        double maxDistance = maxRange * maxRange;

        for (Entity e : robot.level().getEntities(robot, robot.getBoundingBox().inflate(maxRange))) {
            if (e.isAlive() && filter.matches(e) && (zone == null || zone.contains(e.position()))
                && !robot.isKnownUnreachable(e)) {
                double sqrDistance = e.distanceToSqr(robot);

                if (sqrDistance < maxDistance && (target == null || sqrDistance < previousDistance)) {
                    previousDistance = sqrDistance;
                    target = e;
                }
            }
        }

        terminate();
    }

    @Override
    public boolean success() {
        return target != null;
    }

    @Override
    public long getPowerCost() {
        return MjAPI.MJ / 5;
    }
}
