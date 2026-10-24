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
import net.minecraft.util.Mth;

import buildcraftreborn.api.core.IZone;
import buildcraftreborn.api.mj.MjAPI;
import buildcraftreborn.api.robots.AIRobot;
import buildcraftreborn.api.robots.EntityRobotBase;
import buildcraftreborn.robotics.util.IBlockFilter;

/** Picks random columns (in the work area, or within a range) and looks down them for a block. */
public class AIRobotSearchRandomGroundBlock extends AIRobot {

    private static final int MAX_ATTEMPTS = 4096;

    @Nullable
    public BlockPos blockFound;

    private int range;
    @Nullable
    private IBlockFilter filter;
    @Nullable
    private IZone zone;
    private int attempts = 0;

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot) {
        super(iRobot);
    }

    public AIRobotSearchRandomGroundBlock(EntityRobotBase iRobot, int iRange, IBlockFilter iFilter,
        @Nullable IZone iZone) {
        this(iRobot);

        range = iRange;
        filter = iFilter;
        zone = iZone;
    }

    @Override
    public void update() {
        if (filter == null) {
            terminate();
            return;
        }

        attempts++;

        if (attempts > MAX_ATTEMPTS) {
            terminate();
            return;
        }

        int x, z;

        if (zone == null) {
            double r = robot.level().random.nextFloat() * range;
            float a = robot.level().random.nextFloat() * 2.0F * (float) Math.PI;

            x = (int) (Mth.cos(a) * r + Math.floor(robot.getX()));
            z = (int) (Mth.sin(a) * r + Math.floor(robot.getZ()));
        } else {
            BlockPos b = zone.getRandomBlockPos(robot.level().random);
            x = b.getX();
            z = b.getZ();
        }

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos(x, robot.level().getMaxBuildHeight() - 1, z);
        if (!robot.level().isLoaded(pos)) {
            return;
        }
        for (; pos.getY() >= robot.level().getMinBuildHeight(); pos.move(0, -1, 0)) {
            if (filter.matches(robot.level(), pos)) {
                blockFound = pos.immutable();
                terminate();
                return;
            } else if (!robot.level().isEmptyBlock(pos)) {
                return;
            }
        }
    }

    @Override
    public boolean success() {
        return blockFound != null;
    }

    @Override
    public long getPowerCost() {
        return MjAPI.MJ / 5;
    }
}
